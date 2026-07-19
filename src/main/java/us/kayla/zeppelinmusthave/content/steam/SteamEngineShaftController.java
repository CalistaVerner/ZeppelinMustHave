package us.kayla.zeppelinmusthave.content.steam;

import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Owns shaft lookup, lifecycle and direction transfer for graded steam engines. */
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
        return engine.getShaft();
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
}
