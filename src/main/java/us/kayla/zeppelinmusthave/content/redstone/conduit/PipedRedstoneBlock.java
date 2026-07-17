package us.kayla.zeppelinmusthave.content.redstone.conduit;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

public final class PipedRedstoneBlock extends PipeBlock
        implements SimpleWaterloggedBlock, IWrenchable {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final PipedRedstoneTier tier;
    private final MapCodec<PipedRedstoneBlock> codec = MapCodec.unit(this);

    public PipedRedstoneBlock(Properties properties, PipedRedstoneTier tier) {
        super(3.0F / 16.0F, properties);
        this.tier = tier;
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(NORTH, false)
                        .setValue(EAST, false)
                        .setValue(SOUTH, false)
                        .setValue(WEST, false)
                        .setValue(UP, false)
                        .setValue(DOWN, false)
                        .setValue(POWER, 0)
                        .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected MapCodec<? extends PipeBlock> codec() {
        return this.codec;
    }

    public PipedRedstoneTier tier() {
        return this.tier;
    }

    public static boolean hasPort(BlockState state, Direction direction) {
        return state.hasProperty(PROPERTY_BY_DIRECTION.get(direction))
                && state.getValue(PROPERTY_BY_DIRECTION.get(direction));
    }

    public static int getLightPower(BlockState state) {
        return state.getValue(POWER) > 0 ? 5 : 0;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return PipedRedstoneBlockSupport.skipConnectedFace(state, adjacentState, side)
                || super.skipRendering(state, adjacentState, side);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return PipedRedstoneBlockSupport.placementState(this.defaultBlockState(), context);
    }

    @Override
    protected void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston
    ) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        PipedRedstoneBlockSupport.placed(state, level, pos, oldState);
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        PipedRedstoneBlockSupport.removed(this, state, level, pos, newState);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston
    ) {
        PipedRedstoneBlockSupport.neighborChanged(level, pos);
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        PipedRedstoneBlockSupport.scheduledTick(level, pos);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (player.isShiftKeyDown() || !player.mayBuild()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ItemInteractionResult result = PipedRedstoneBlockSupport.tryPlacementAssist(
                stack,
                state,
                level,
                pos,
                player,
                hand,
                hitResult
        );
        return result == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                ? super.useItemOn(stack, state, level, pos, player, hand, hitResult)
                : result;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return PipedRedstoneBlockSupport.wrench(state, context);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return PipedRedstoneBlockSupport.signal(state, side);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return this.getSignal(state, level, pos, side);
    }

    @Override
    public boolean canConnectRedstone(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction side
    ) {
        return PipedRedstoneBlockSupport.canConnect(state, side);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        PipedRedstoneWaterlogging.scheduleWaterTick(state, WATERLOGGED, level, pos);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return PipedRedstoneWaterlogging.fluidState(
                state,
                WATERLOGGED,
                super.getFluidState(state)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, POWER, WATERLOGGED);
    }
}
