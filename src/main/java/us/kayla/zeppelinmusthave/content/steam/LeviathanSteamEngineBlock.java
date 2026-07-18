package us.kayla.zeppelinmusthave.content.steam;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlock;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeTier;

/**
 * Four-cell T-frame engine. The controller attaches to an Industrial boiler,
 * two lateral cells carry the cylinder banks, and the forward nose terminates
 * immediately before the Create powered shaft.
 */
public final class LeviathanSteamEngineBlock extends SteamEngineGradeBlock {
    public static final EnumProperty<LeviathanSteamEnginePart> PART =
            EnumProperty.create("part", LeviathanSteamEnginePart.class);

    private static final ThreadLocal<Boolean> DISMANTLING = ThreadLocal.withInitial(() -> false);

    public LeviathanSteamEngineBlock(Properties properties) {
        super(properties, SteamEngineGradeTier.LEVIATHAN);
        this.registerDefaultState(this.defaultBlockState().setValue(PART, LeviathanSteamEnginePart.CONTROLLER));
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
        state = state.setValue(PART, LeviathanSteamEnginePart.CONTROLLER);
        if (!this.canSurvive(state, context.getLevel(), context.getClickedPos())) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable("zeppelin_must_have.placement.leviathan.requires_industrial_boiler"),
                        true
                );
            }
            return null;
        }
        if (!this.hasClearAssemblySpace(context.getLevel(), context.getClickedPos(), state)) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.translatable("zeppelin_must_have.placement.leviathan.requires_clear_t_frame"),
                        true
                );
            }
            return null;
        }
        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (state.getValue(PART) != LeviathanSteamEnginePart.CONTROLLER) {
            return;
        }
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            this.placeAuxiliaryParts(level, pos, state);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == LeviathanSteamEnginePart.CONTROLLER
                ? super.newBlockEntity(pos, state)
                : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        LeviathanSteamEnginePart part = state.getValue(PART);
        if (part == LeviathanSteamEnginePart.CONTROLLER) {
            BlockPos boilerPos = pos.relative(getConnectedDirection(state).getOpposite());
            BlockState boilerState = level.getBlockState(boilerPos);
            return boilerState.getBlock() instanceof BoilerGradeBlock boiler
                    && boiler.tier() == BoilerGradeTier.INDUSTRIAL;
        }
        BlockPos controllerPos = controllerPos(state, pos);
        BlockState controller = level.getBlockState(controllerPos);
        return isMatchingController(controller, state);
    }

    public boolean hasClearAssemblySpace(LevelReader level, BlockPos controllerPos, BlockState controllerState) {
        if (!this.canSurvive(controllerState, level, controllerPos)) {
            return false;
        }
        AssemblyPositions positions = assemblyPositions(controllerState, controllerPos);
        return level.getBlockState(positions.leftCylinder()).canBeReplaced()
                && level.getBlockState(positions.rightCylinder()).canBeReplaced()
                && level.getBlockState(positions.shaftNose()).canBeReplaced();
    }

    public boolean isAssemblyComplete(LevelReader level, BlockPos controllerPos, BlockState controllerState) {
        if (controllerState.getBlock() != this
                || controllerState.getValue(PART) != LeviathanSteamEnginePart.CONTROLLER
                || !this.canSurvive(controllerState, level, controllerPos)) {
            return false;
        }
        AssemblyPositions positions = assemblyPositions(controllerState, controllerPos);
        return matchesPart(level.getBlockState(positions.leftCylinder()), controllerState,
                        LeviathanSteamEnginePart.LEFT_CYLINDER)
                && matchesPart(level.getBlockState(positions.rightCylinder()), controllerState,
                        LeviathanSteamEnginePart.RIGHT_CYLINDER)
                && matchesPart(level.getBlockState(positions.shaftNose()), controllerState,
                        LeviathanSteamEnginePart.SHAFT_NOSE);
    }

    public void placeAuxiliaryParts(Level level, BlockPos controllerPos, BlockState controllerState) {
        AssemblyPositions positions = assemblyPositions(controllerState, controllerPos);
        level.setBlock(positions.leftCylinder(), partState(level, positions.leftCylinder(), controllerState,
                LeviathanSteamEnginePart.LEFT_CYLINDER), Block.UPDATE_ALL);
        level.setBlock(positions.rightCylinder(), partState(level, positions.rightCylinder(), controllerState,
                LeviathanSteamEnginePart.RIGHT_CYLINDER), Block.UPDATE_ALL);
        level.setBlock(positions.shaftNose(), partState(level, positions.shaftNose(), controllerState,
                LeviathanSteamEnginePart.SHAFT_NOSE), Block.UPDATE_ALL);
    }

    public static BlockState stateForPart(
            BlockState controllerState,
            LeviathanSteamEnginePart part,
            boolean waterlogged
    ) {
        return controllerState.setValue(PART, part).setValue(BlockStateProperties.WATERLOGGED, waterlogged);
    }

    private BlockState partState(
            Level level,
            BlockPos pos,
            BlockState controllerState,
            LeviathanSteamEnginePart part
    ) {
        return stateForPart(
                controllerState,
                part,
                level.getFluidState(pos).getType() == Fluids.WATER
        );
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        if (state.getValue(PART) == LeviathanSteamEnginePart.CONTROLLER) {
            super.onPlace(state, level, pos, oldState, moving);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (state.is(newState.getBlock())) {
            return;
        }

        LeviathanSteamEnginePart part = state.getValue(PART);
        if (part == LeviathanSteamEnginePart.CONTROLLER) {
            super.onRemove(state, level, pos, newState, moving);
            if (!level.isClientSide && !DISMANTLING.get()) {
                removeAuxiliaryParts(level, pos, state);
            }
            return;
        }

        if (!level.isClientSide && !DISMANTLING.get()) {
            BlockPos controllerPos = controllerPos(state, pos);
            BlockState controller = level.getBlockState(controllerPos);
            if (isMatchingController(controller, state)) {
                level.destroyBlock(controllerPos, true);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide
                && player.isCreative()
                && state.getValue(PART) != LeviathanSteamEnginePart.CONTROLLER) {
            BlockPos controllerPos = controllerPos(state, pos);
            BlockState controller = level.getBlockState(controllerPos);
            if (isMatchingController(controller, state)) {
                removeWholeAssembly(level, controllerPos, controller);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private static void removeAuxiliaryParts(Level level, BlockPos controllerPos, BlockState controllerState) {
        AssemblyPositions positions = assemblyPositions(controllerState, controllerPos);
        DISMANTLING.set(true);
        try {
            removePart(level, positions.leftCylinder());
            removePart(level, positions.rightCylinder());
            removePart(level, positions.shaftNose());
        } finally {
            DISMANTLING.set(false);
        }
    }

    private static void removeWholeAssembly(Level level, BlockPos controllerPos, BlockState controllerState) {
        AssemblyPositions positions = assemblyPositions(controllerState, controllerPos);
        DISMANTLING.set(true);
        try {
            removePart(level, positions.leftCylinder());
            removePart(level, positions.rightCylinder());
            removePart(level, positions.shaftNose());
            if (level.getBlockState(controllerPos).getBlock() instanceof LeviathanSteamEngineBlock) {
                level.setBlock(controllerPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        } finally {
            DISMANTLING.set(false);
        }
    }

    private static void removePart(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof LeviathanSteamEngineBlock) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            net.minecraft.world.InteractionHand hand,
            BlockHitResult hitResult
    ) {
        LeviathanSteamEnginePart part = state.getValue(PART);
        if (part == LeviathanSteamEnginePart.CONTROLLER) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        if (part != LeviathanSteamEnginePart.SHAFT_NOSE || !AllBlocks.SHAFT.isIn(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Direction forward = getConnectedDirection(state);
        BlockPos shaftPos = pos.relative(forward);
        if (!level.getBlockState(shaftPos).canBeReplaced()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Direction.Axis axis = Direction.Axis.Y;
        for (Direction direction : Direction.orderedByNearest(player)) {
            if (direction.getAxis() != getFacing(state).getAxis()) {
                axis = direction.getAxis();
                break;
            }
        }
        BlockState shaftState = (level.isClientSide ? AllBlocks.SHAFT : AllBlocks.POWERED_SHAFT)
                .getDefaultState()
                .setValue(ShaftBlock.AXIS, axis);
        if (!level.isClientSide) {
            level.setBlock(shaftPos, shaftState, Block.UPDATE_ALL);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PART) == LeviathanSteamEnginePart.CONTROLLER
                ? super.getShape(state, level, pos, context)
                : Shapes.block();
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = super.mirror(state, mirror);
        if (mirror == Mirror.NONE) {
            return mirrored;
        }
        LeviathanSteamEnginePart part = mirrored.getValue(PART);
        if (part == LeviathanSteamEnginePart.LEFT_CYLINDER) {
            return mirrored.setValue(PART, LeviathanSteamEnginePart.RIGHT_CYLINDER);
        }
        if (part == LeviathanSteamEnginePart.RIGHT_CYLINDER) {
            return mirrored.setValue(PART, LeviathanSteamEnginePart.LEFT_CYLINDER);
        }
        return mirrored;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return super.rotate(state, rotation);
    }

    public static AssemblyPositions assemblyPositions(BlockState controllerState, BlockPos controllerPos) {
        Direction forward = getConnectedDirection(controllerState);
        Direction left = leftDirection(controllerState);
        return new AssemblyPositions(
                controllerPos.relative(left),
                controllerPos.relative(left.getOpposite()),
                controllerPos.relative(forward),
                controllerPos.relative(forward, 2)
        );
    }

    private static Direction leftDirection(BlockState state) {
        Direction forward = getConnectedDirection(state);
        Direction reference = forward.getAxis().isVertical()
                ? state.getValue(FACING)
                : forward;
        return reference.getCounterClockWise();
    }

    private static BlockPos controllerPos(BlockState state, BlockPos partPos) {
        Direction left = leftDirection(state);
        return switch (state.getValue(PART)) {
            case CONTROLLER -> partPos;
            case LEFT_CYLINDER -> partPos.relative(left.getOpposite());
            case RIGHT_CYLINDER -> partPos.relative(left);
            case SHAFT_NOSE -> partPos.relative(getConnectedDirection(state).getOpposite());
        };
    }

    private static boolean isMatchingController(BlockState controller, BlockState reference) {
        return controller.getBlock() instanceof LeviathanSteamEngineBlock
                && controller.getValue(PART) == LeviathanSteamEnginePart.CONTROLLER
                && controller.getValue(FACE) == reference.getValue(FACE)
                && controller.getValue(FACING) == reference.getValue(FACING);
    }

    private static boolean matchesPart(
            BlockState candidate,
            BlockState controller,
            LeviathanSteamEnginePart expectedPart
    ) {
        return candidate.getBlock() instanceof LeviathanSteamEngineBlock
                && candidate.getValue(PART) == expectedPart
                && candidate.getValue(FACE) == controller.getValue(FACE)
                && candidate.getValue(FACING) == controller.getValue(FACING);
    }

    public record AssemblyPositions(
            BlockPos leftCylinder,
            BlockPos rightCylinder,
            BlockPos shaftNose,
            BlockPos shaft
    ) {
    }
}
