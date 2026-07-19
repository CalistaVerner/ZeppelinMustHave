package us.kayla.zeppelinmusthave.content.steam;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Owns shaft lookup, lifecycle, load recovery and direction transfer for graded steam engines. */
final class SteamEngineShaftController {
    private SteamEngineShaftController() {
    }

    static @Nullable PoweredShaftBlockEntity primaryShaft(SteamEngineGradeBlockEntity engine) {
        Level level = engine.getLevel();
        if (level == null) {
            return null;
        }
        BlockState state = engine.getBlockState();
        if (state.getBlock() instanceof MkViiSteamEngineBlock) {
            BlockPos controllerPos = MkViiSteamEngineBlock.controllerPos(state, engine.getBlockPos());
            if (!controllerPos.equals(engine.getBlockPos())) {
                if (level.getBlockEntity(controllerPos) instanceof SteamEngineGradeBlockEntity controller) {
                    return controller.getShaft();
                }
                return null;
            }
        }

        PoweredShaftBlockEntity shaft = engine.getShaft();
        if (shaft == null && recoverAfterLoad(engine) != RecoveryResult.UNAVAILABLE) {
            shaft = engine.getShaft();
        }
        return shaft;
    }

    static @Nullable PoweredShaftBlockEntity ownedShaft(SteamEngineGradeBlockEntity engine) {
        if (MkViiSteamEngineBlock.isAuxiliary(engine.getBlockState())) {
            return null;
        }
        PoweredShaftBlockEntity shaft = primaryShaft(engine);
        return shaft != null && shaft.isPoweredBy(engine.getBlockPos()) ? shaft : null;
    }

    static void deactivateOwnedShaft(SteamEngineGradeBlockEntity engine) {
        PoweredShaftBlockEntity shaft = ownedShaft(engine);
        if (shaft != null && (shaft.engineEfficiency != 0.0F || shaft.movementDirection != 0)) {
            shaft.update(engine.getBlockPos(), 0, 0.0F);
        }
    }

    static void removeOwnedShaft(SteamEngineGradeBlockEntity engine) {
        PoweredShaftBlockEntity shaft = ownedShaft(engine);
        if (shaft != null) {
            shaft.remove(engine.getBlockPos());
        }
    }

    static void refreshCapacity(SteamEngineGradeBlockEntity engine) {
        PoweredShaftBlockEntity shaft = ownedShaft(engine);
        if (shaft == null) {
            return;
        }
        int direction = shaft.movementDirection;
        float efficiency = shaft.engineEfficiency;
        shaft.remove(engine.getBlockPos());
        shaft.update(engine.getBlockPos(), direction, efficiency);
    }

    static boolean update(
            SteamEngineGradeBlockEntity engine,
            PoweredShaftBlockEntity shaft,
            float efficiency,
            RotationDirection movementDirection,
            boolean commandReversed
    ) {
        BlockState shaftState = shaft.getBlockState();
        Axis targetAxis = shaftState.getBlock() instanceof IRotate rotate
                ? rotate.getRotationAxis(shaftState)
                : Axis.X;
        boolean verticalTarget = targetAxis == Axis.Y;

        BlockState engineState = engine.getBlockState();
        Direction facing = SteamEngineBlock.getFacing(engineState);
        if (facing.getAxis() == Axis.Y) {
            facing = engineState.getValue(SteamEngineBlock.FACING);
        }

        int conveyedDirection = efficiency == 0.0F
                ? 1
                : verticalTarget
                        ? 1
                        : (int) GeneratingKineticBlockEntity.convertToDirection(1, facing);
        if (targetAxis == Axis.Z) {
            conveyedDirection *= -1;
        }
        if (movementDirection == RotationDirection.COUNTER_CLOCKWISE) {
            conveyedDirection *= -1;
        }
        if (commandReversed) {
            conveyedDirection *= -1;
        }

        boolean flipMovementDirection = shaft.hasSource()
                && shaft.getTheoreticalSpeed() != 0.0F
                && conveyedDirection != 0
                && (shaft.getTheoreticalSpeed() > 0.0F) != (conveyedDirection > 0);
        if (flipMovementDirection) {
            conveyedDirection *= -1;
        }
        shaft.update(engine.getBlockPos(), conveyedDirection, efficiency);
        return flipMovementDirection;
    }

    /**
     * Repairs load-order damage for graded engines. Existing regular shafts are promoted back to
     * Create's Powered Shaft. Missing shaft cells are rebuilt only for MK I/MK II engines that are
     * visibly part of an existing crankshaft row, preventing isolated engines from spawning shafts.
     */
    static RecoveryResult recoverAfterLoad(SteamEngineGradeBlockEntity engine) {
        Level level = engine.getLevel();
        if (level == null
                || level.isClientSide
                || engine.isRemoved()
                || MkViiSteamEngineBlock.isAuxiliary(engine.getBlockState())) {
            return RecoveryResult.UNAVAILABLE;
        }

        BlockState engineState = engine.getBlockState();
        if (!(engineState.getBlock() instanceof SteamEngineGradeBlock)) {
            return RecoveryResult.UNAVAILABLE;
        }

        BlockPos shaftPos = SteamEngineBlock.getShaftPos(engineState, engine.getBlockPos());
        if (!level.isLoaded(shaftPos)) {
            return RecoveryResult.UNAVAILABLE;
        }

        BlockState shaftState = level.getBlockState(shaftPos);
        if (AllBlocks.POWERED_SHAFT.has(shaftState)) {
            return RecoveryResult.PRESENT;
        }
        if (SteamEngineBlock.isShaftValid(engineState, shaftState)) {
            level.setBlock(shaftPos, PoweredShaftBlock.getEquivalent(shaftState), Block.UPDATE_ALL);
            return RecoveryResult.REPAIRED;
        }
        if (!shaftState.isAir() || !isBasicRecoveryTier(engine.tier())) {
            return RecoveryResult.UNAVAILABLE;
        }

        Axis recoveredAxis = inferMissingShaftAxis(level, engine.getBlockPos(), shaftPos, engineState);
        if (recoveredAxis == null) {
            return RecoveryResult.UNAVAILABLE;
        }

        BlockState poweredShaft = AllBlocks.POWERED_SHAFT.getDefaultState()
                .setValue(ShaftBlock.AXIS, recoveredAxis);
        level.setBlock(shaftPos, poweredShaft, Block.UPDATE_ALL);
        return RecoveryResult.REPAIRED;
    }

    private static boolean isBasicRecoveryTier(SteamEngineGradeTier tier) {
        return tier == SteamEngineGradeTier.COPPER || tier == SteamEngineGradeTier.BRASS;
    }

    private static @Nullable Axis inferMissingShaftAxis(
            Level level,
            BlockPos enginePos,
            BlockPos shaftPos,
            BlockState engineState
    ) {
        Axis facingAxis = SteamEngineBlock.getFacing(engineState).getAxis();
        for (Axis axis : Axis.values()) {
            if (axis == facingAxis) {
                continue;
            }
            if (hasNeighbouringShaft(level, shaftPos, axis)) {
                return axis;
            }
        }

        Axis naturalHorizontalAxis = switch (facingAxis) {
            case X -> Axis.Z;
            case Z -> Axis.X;
            case Y -> null;
        };
        if (naturalHorizontalAxis != null
                && hasAdjacentBasicEngine(level, enginePos, engineState, naturalHorizontalAxis)) {
            return naturalHorizontalAxis;
        }
        return null;
    }

    private static boolean hasNeighbouringShaft(Level level, BlockPos shaftPos, Axis axis) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != axis) {
                continue;
            }
            BlockState neighbour = level.getBlockState(shaftPos.relative(direction));
            if ((AllBlocks.SHAFT.has(neighbour) || AllBlocks.POWERED_SHAFT.has(neighbour))
                    && neighbour.getValue(ShaftBlock.AXIS) == axis) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAdjacentBasicEngine(
            Level level,
            BlockPos enginePos,
            BlockState engineState,
            Axis axis
    ) {
        Direction expectedFacing = SteamEngineBlock.getFacing(engineState);
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != axis) {
                continue;
            }
            BlockState neighbour = level.getBlockState(enginePos.relative(direction));
            if (!(neighbour.getBlock() instanceof SteamEngineGradeBlock adjacent)
                    || !isBasicRecoveryTier(adjacent.tier())) {
                continue;
            }
            if (SteamEngineBlock.getFacing(neighbour) == expectedFacing) {
                return true;
            }
        }
        return false;
    }

    enum RecoveryResult {
        UNAVAILABLE,
        PRESENT,
        REPAIRED
    }
}
