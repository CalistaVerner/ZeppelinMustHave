package us.kayla.zeppelinmusthave.content.steam;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlock;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeTier;

import java.util.List;

/**
 * Three-bank MK VII engine with nine internal crankshafts and one central
 * Create-compatible output shaft.
 *
 * <p>Placement is transactional. The complete three-block body, the full
 * front service row, the central output position, and all three Industrial
 * Boiler MK III backing cells are validated before auxiliary blocks are
 * written to the level.</p>
 */
public final class MkViiSteamEngineBlock extends SteamEngineGradeBlock {
    public static final EnumProperty<MkViiSteamEnginePart> PART =
            EnumProperty.create("part", MkViiSteamEnginePart.class);

    public static final int BODY_WIDTH = 3;
    public static final int INTERNAL_SHAFTS_PER_BANK = 3;
    public static final int INTERNAL_SHAFT_COUNT = BODY_WIDTH * INTERNAL_SHAFTS_PER_BANK;
    public static final int SERVICE_CLEARANCE_DEPTH = 1;
    public static final int OUTPUT_SHAFT_DEPTH = 2;

    private static final ThreadLocal<Boolean> DISMANTLING = ThreadLocal.withInitial(() -> false);

    public MkViiSteamEngineBlock(Properties properties) {
        super(properties, SteamEngineGradeTier.MK_VII);
        this.registerDefaultState(this.defaultBlockState().setValue(PART, MkViiSteamEnginePart.CONTROLLER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PART);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        state = state.setValue(PART, MkViiSteamEnginePart.CONTROLLER);

        if (!hasIndustrialBoilerBacking(context.getLevel(), context.getClickedPos(), state)) {
            displayPlacementError(
                    context,
                    "zeppelin_must_have.placement.mk_vii.requires_three_wide_industrial_boiler"
            );
            return null;
        }
        if (!this.hasClearAssemblySpace(context.getLevel(), context.getClickedPos(), state)) {
            displayPlacementError(context, "zeppelin_must_have.placement.mk_vii.requires_clear_drive_space");
            return null;
        }
        return state;
    }

    private static void displayPlacementError(BlockPlaceContext context, String translationKey) {
        if (context.getPlayer() != null) {
            context.getPlayer().displayClientMessage(Component.translatable(translationKey), true);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (state.getValue(PART) != MkViiSteamEnginePart.CONTROLLER) {
            return;
        }
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            this.placeAssembly(level, pos, state);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos controllerPos = controllerPos(state, pos);
        BlockState controller = level.getBlockState(controllerPos);
        if (state.getValue(PART) != MkViiSteamEnginePart.CONTROLLER
                && !isMatchingController(controller, state)) {
            return false;
        }
        BlockState reference = state.getValue(PART) == MkViiSteamEnginePart.CONTROLLER ? state : controller;
        return hasIndustrialBoilerBacking(level, controllerPos, reference);
    }

    public boolean hasClearAssemblySpace(LevelReader level, BlockPos controllerPos, BlockState controllerState) {
        if (!hasIndustrialBoilerBacking(level, controllerPos, controllerState)) {
            return false;
        }

        AssemblyPositions positions = assemblyPositions(controllerState, controllerPos);
        for (BankPositions bank : positions.banks()) {
            if (!bank.body().equals(controllerPos) && !level.getBlockState(bank.body()).canBeReplaced()) {
                return false;
            }
            if (!level.getBlockState(bank.serviceClearance()).canBeReplaced()) {
                return false;
            }
        }
        return level.getBlockState(positions.outputShaft()).canBeReplaced();
    }

    public boolean isAssemblyComplete(LevelReader level, BlockPos anyBodyPos, BlockState anyBodyState) {
        BlockPos controllerPos = controllerPos(anyBodyState, anyBodyPos);
        BlockState controller = level.getBlockState(controllerPos);
        if (!isMatchingController(controller, anyBodyState)
                || !hasIndustrialBoilerBacking(level, controllerPos, controller)) {
            return false;
        }

        AssemblyPositions positions = assemblyPositions(controller, controllerPos);
        for (BankPositions bank : positions.banks()) {
            if (!matchesPart(level.getBlockState(bank.body()), controller, bank.part())) {
                return false;
            }
            if (!level.getBlockState(bank.serviceClearance()).isAir()) {
                return false;
            }
        }

        BlockState shaftState = level.getBlockState(positions.outputShaft());
        return AllBlocks.POWERED_SHAFT.has(shaftState)
                && shaftState.getValue(ShaftBlock.AXIS) == outputShaftAxis(controller);
    }

    public void placeAssembly(Level level, BlockPos controllerPos, BlockState controllerState) {
        AssemblyPositions positions = assemblyPositions(controllerState, controllerPos);
        for (BankPositions bank : positions.banks()) {
            if (!bank.body().equals(controllerPos)) {
                level.setBlock(
                        bank.body(),
                        stateForPart(level, bank.body(), controllerState, bank.part()),
                        Block.UPDATE_ALL
                );
            }
        }

        BlockState outputShaft = AllBlocks.POWERED_SHAFT.getDefaultState()
                .setValue(ShaftBlock.AXIS, outputShaftAxis(controllerState));
        level.setBlock(positions.outputShaft(), outputShaft, Block.UPDATE_ALL);
    }

    public static BlockState stateForPart(
            LevelReader level,
            BlockPos pos,
            BlockState controllerState,
            MkViiSteamEnginePart part
    ) {
        return controllerState
                .setValue(PART, part)
                .setValue(
                        BlockStateProperties.WATERLOGGED,
                        level.getFluidState(pos).getType() == Fluids.WATER
                );
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (state.is(newState.getBlock())) {
            return;
        }

        BlockPos controllerPos = controllerPos(state, pos);
        BlockState controllerState = state.getValue(PART) == MkViiSteamEnginePart.CONTROLLER
                ? state
                : level.getBlockState(controllerPos);

        super.onRemove(state, level, pos, newState, moving);
        if (level.isClientSide || DISMANTLING.get()) {
            return;
        }

        if (state.getValue(PART) == MkViiSteamEnginePart.CONTROLLER) {
            removeAuxiliaryAssembly(level, controllerPos, controllerState);
        } else if (isMatchingController(controllerState, state)) {
            level.destroyBlock(controllerPos, true);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide
                && player.isCreative()
                && state.getValue(PART) != MkViiSteamEnginePart.CONTROLLER) {
            BlockPos controllerPos = controllerPos(state, pos);
            BlockState controller = level.getBlockState(controllerPos);
            if (isMatchingController(controller, state)) {
                removeWholeAssembly(level, controllerPos, controller);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private static void removeAuxiliaryAssembly(Level level, BlockPos controllerPos, BlockState controllerState) {
        DISMANTLING.set(true);
        try {
            AssemblyPositions positions = assemblyPositions(controllerState, controllerPos);
            for (BankPositions bank : positions.banks()) {
                if (!bank.body().equals(controllerPos)) {
                    removeBodyPart(level, bank.body());
                }
            }
            removeOutputShaft(level, positions.outputShaft());
        } finally {
            DISMANTLING.set(false);
        }
    }

    private static void removeWholeAssembly(Level level, BlockPos controllerPos, BlockState controllerState) {
        DISMANTLING.set(true);
        try {
            AssemblyPositions positions = assemblyPositions(controllerState, controllerPos);
            removeOutputShaft(level, positions.outputShaft());
            for (BankPositions bank : positions.banks()) {
                removeBodyPart(level, bank.body());
            }
        } finally {
            DISMANTLING.set(false);
        }
    }

    private static void removeBodyPart(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof MkViiSteamEngineBlock) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static void removeOutputShaft(Level level, BlockPos outputShaftPos) {
        if (AllBlocks.POWERED_SHAFT.has(level.getBlockState(outputShaftPos))) {
            level.setBlock(outputShaftPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = super.mirror(state, mirror);
        if (mirror == Mirror.NONE) {
            return mirrored;
        }
        return switch (mirrored.getValue(PART)) {
            case LEFT -> mirrored.setValue(PART, MkViiSteamEnginePart.RIGHT);
            case RIGHT -> mirrored.setValue(PART, MkViiSteamEnginePart.LEFT);
            case CONTROLLER -> mirrored;
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return super.rotate(state, rotation);
    }

    public static boolean isController(BlockState state) {
        return state.getBlock() instanceof MkViiSteamEngineBlock
                && state.getValue(PART) == MkViiSteamEnginePart.CONTROLLER;
    }

    public static boolean isAuxiliary(BlockState state) {
        return state.getBlock() instanceof MkViiSteamEngineBlock && !isController(state);
    }

    public static BlockPos outputShaftPos(BlockState state, BlockPos anyBodyPos) {
        BlockPos controllerPos = controllerPos(state, anyBodyPos);
        return controllerPos.relative(SteamEngineBlock.getConnectedDirection(state), OUTPUT_SHAFT_DEPTH);
    }

    public static AssemblyPositions assemblyPositions(BlockState controllerState, BlockPos controllerPos) {
        Direction left = leftDirection(controllerState);
        List<BankPositions> banks = List.of(
                bank(controllerState, MkViiSteamEnginePart.LEFT, controllerPos.relative(left)),
                bank(controllerState, MkViiSteamEnginePart.CONTROLLER, controllerPos),
                bank(controllerState, MkViiSteamEnginePart.RIGHT, controllerPos.relative(left.getOpposite()))
        );
        return new AssemblyPositions(
                banks,
                controllerPos.relative(
                        SteamEngineBlock.getConnectedDirection(controllerState),
                        OUTPUT_SHAFT_DEPTH
                )
        );
    }

    private static BankPositions bank(
            BlockState controllerState,
            MkViiSteamEnginePart part,
            BlockPos bodyPos
    ) {
        Direction forward = SteamEngineBlock.getConnectedDirection(controllerState);
        return new BankPositions(
                part,
                bodyPos,
                bodyPos.relative(forward, SERVICE_CLEARANCE_DEPTH)
        );
    }

    public static Direction.Axis outputShaftAxis(BlockState state) {
        return leftDirection(state).getAxis();
    }

    private static boolean hasIndustrialBoilerBacking(
            LevelReader level,
            BlockPos controllerPos,
            BlockState controllerState
    ) {
        Direction boilerDirection = SteamEngineBlock.getConnectedDirection(controllerState).getOpposite();
        for (BankPositions bank : assemblyPositions(controllerState, controllerPos).banks()) {
            BlockState boilerState = level.getBlockState(bank.body().relative(boilerDirection));
            if (!(boilerState.getBlock() instanceof BoilerGradeBlock boiler)
                    || boiler.tier() != BoilerGradeTier.INDUSTRIAL) {
                return false;
            }
        }
        return true;
    }

    public static BlockPos controllerPos(BlockState state, BlockPos partPos) {
        Direction left = leftDirection(state);
        return switch (state.getValue(PART)) {
            case LEFT -> partPos.relative(left.getOpposite());
            case CONTROLLER -> partPos;
            case RIGHT -> partPos.relative(left);
        };
    }

    private static Direction leftDirection(BlockState state) {
        Direction forward = SteamEngineBlock.getConnectedDirection(state);
        Direction reference = forward.getAxis().isVertical()
                ? state.getValue(FACING)
                : forward;
        return reference.getCounterClockWise();
    }

    private static boolean isMatchingController(BlockState controller, BlockState reference) {
        return controller.getBlock() instanceof MkViiSteamEngineBlock
                && controller.getValue(PART) == MkViiSteamEnginePart.CONTROLLER
                && controller.getValue(FACE) == reference.getValue(FACE)
                && controller.getValue(FACING) == reference.getValue(FACING);
    }

    private static boolean matchesPart(
            BlockState candidate,
            BlockState controller,
            MkViiSteamEnginePart expectedPart
    ) {
        return candidate.getBlock() instanceof MkViiSteamEngineBlock
                && candidate.getValue(PART) == expectedPart
                && candidate.getValue(FACE) == controller.getValue(FACE)
                && candidate.getValue(FACING) == controller.getValue(FACING);
    }

    public record BankPositions(
            MkViiSteamEnginePart part,
            BlockPos body,
            BlockPos serviceClearance
    ) {
    }

    public record AssemblyPositions(
            List<BankPositions> banks,
            BlockPos outputShaft
    ) {
    }
}
