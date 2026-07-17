package us.kayla.zeppelinmusthave.content.redstone.conduit;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

public final class PipedRedstoneNativeLeverBlock
        extends FaceAttachedHorizontalDirectionalBlock
        implements SimpleWaterloggedBlock, IBE<PipedRedstoneNativeLeverBlockEntity> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final MapCodec<PipedRedstoneNativeLeverBlock> codec = MapCodec.unit(this);

    public PipedRedstoneNativeLeverBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACE, AttachFace.WALL)
                        .setValue(FACING, Direction.NORTH)
                        .setValue(POWERED, false)
                        .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
        return this.codec;
    }

    public static int getLightPower(BlockState state) {
        return state.getValue(POWERED) ? 5 : 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null
                ? null
                : PipedRedstoneWaterlogging.applyPlacement(state, WATERLOGGED, context);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return PipedRedstoneLeverSupport.canSurvive(state, level, pos);
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
        PipedRedstoneLeverSupport.placed(state, level, pos, oldState);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        return PipedRedstoneLeverSupport.toggle(state, level, pos);
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())) {
            PipedRedstoneLeverSupport.updatePipe(state, level, pos);
            IBE.onRemove(state, level, pos, newState);
            return;
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return PipedRedstoneLeverSupport.signal(state, side);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return this.getSignal(state, level, pos, side);
    }

    @Override
    public boolean canConnectRedstone(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction side
    ) {
        return PipedRedstoneLeverSupport.canConnect(state, side);
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
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return PipedRedstoneLeverSupport.shape(state);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, POWERED, WATERLOGGED);
    }

    @Override
    public Class<PipedRedstoneNativeLeverBlockEntity> getBlockEntityClass() {
        return PipedRedstoneNativeLeverBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PipedRedstoneNativeLeverBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.PIPED_REDSTONE_NATIVE_LEVER.get();
    }
}
