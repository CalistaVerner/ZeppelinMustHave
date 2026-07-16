package us.kayla.zeppelinmusthave.content.redstone.conduit;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

/**
 * Lever mounted directly to a Piped Redstone conduit face.
 *
 * <p>It emits only into the supporting conduit, automatically opens the
 * corresponding conduit port on placement, and keeps all unrelated redstone
 * lines electrically isolated.</p>
 */
public final class PipedRedstoneNativeLeverBlock
        extends FaceAttachedHorizontalDirectionalBlock
        implements SimpleWaterloggedBlock, IBE<PipedRedstoneNativeLeverBlockEntity> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape NORTH_AABB = Block.box(4.0D, 3.0D, 10.0D, 12.0D, 13.0D, 16.0D);
    private static final VoxelShape SOUTH_AABB = Block.box(4.0D, 3.0D, 0.0D, 12.0D, 13.0D, 6.0D);
    private static final VoxelShape WEST_AABB = Block.box(10.0D, 3.0D, 4.0D, 16.0D, 13.0D, 12.0D);
    private static final VoxelShape EAST_AABB = Block.box(0.0D, 3.0D, 4.0D, 6.0D, 13.0D, 12.0D);
    private static final VoxelShape FLOOR_Z_AABB = Block.box(4.0D, 0.0D, 3.0D, 12.0D, 8.0D, 13.0D);
    private static final VoxelShape FLOOR_X_AABB = Block.box(3.0D, 0.0D, 4.0D, 13.0D, 8.0D, 12.0D);
    private static final VoxelShape CEILING_Z_AABB = Block.box(4.0D, 8.0D, 3.0D, 12.0D, 16.0D, 13.0D);
    private static final VoxelShape CEILING_X_AABB = Block.box(3.0D, 8.0D, 4.0D, 13.0D, 16.0D, 12.0D);

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
        if (state == null) {
            return null;
        }
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return state.setValue(WATERLOGGED, fluidState.is(Fluids.WATER));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction pipeDirection = getConnectedDirection(state);
        BlockPos pipePos = pos.relative(pipeDirection.getOpposite());
        return level.getBlockState(pipePos).getBlock() instanceof PipedRedstoneBlock;
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
        if (oldState.is(state.getBlock())) {
            return;
        }

        Direction pipeDirection = getConnectedDirection(state);
        BlockPos pipePos = pos.relative(pipeDirection.getOpposite());
        BlockState pipeState = level.getBlockState(pipePos);
        if (!(pipeState.getBlock() instanceof PipedRedstoneBlock)) {
            return;
        }

        if (!PipedRedstoneBlock.hasPort(pipeState, pipeDirection)) {
            level.setBlock(
                    pipePos,
                    pipeState.setValue(
                            PipedRedstoneBlock.PROPERTY_BY_DIRECTION.get(pipeDirection),
                            true
                    ),
                    Block.UPDATE_ALL
            );
        }
        PipedRedstoneNetworkManager.requestRebuild(level, pipePos);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockState nextState = state.cycle(POWERED);
        level.setBlock(pos, nextState, Block.UPDATE_ALL);
        level.playSound(
                null,
                pos,
                SoundEvents.LEVER_CLICK,
                SoundSource.BLOCKS,
                0.35F,
                nextState.getValue(POWERED) ? 0.65F : 0.5F
        );
        updatePipe(nextState, level, pos);
        return InteractionResult.CONSUME;
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
            updatePipe(state, level, pos);
            IBE.onRemove(state, level, pos, newState);
            return;
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void updatePipe(BlockState state, Level level, BlockPos pos) {
        Direction pipeDirection = getConnectedDirection(state);
        BlockPos pipePos = pos.relative(pipeDirection.getOpposite());
        if (level.getBlockState(pipePos).getBlock() instanceof PipedRedstoneBlock) {
            level.neighborChanged(pipePos, state.getBlock(), pos);
            PipedRedstoneNetworkManager.requestRebuild(level, pipePos);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction side
    ) {
        return state.getValue(POWERED) && getConnectedDirection(state) == side ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction side
    ) {
        return this.getSignal(state, level, pos, side);
    }

    @Override
    public boolean canConnectRedstone(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction side
    ) {
        return side != null && getConnectedDirection(state) == side;
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
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return switch (state.getValue(FACE)) {
            case FLOOR -> state.getValue(FACING).getAxis() == Direction.Axis.X
                    ? FLOOR_X_AABB
                    : FLOOR_Z_AABB;
            case WALL -> switch (state.getValue(FACING)) {
                case EAST -> EAST_AABB;
                case WEST -> WEST_AABB;
                case SOUTH -> SOUTH_AABB;
                default -> NORTH_AABB;
            };
            case CEILING -> state.getValue(FACING).getAxis() == Direction.Axis.X
                    ? CEILING_X_AABB
                    : CEILING_Z_AABB;
        };
    }

    @Override
    protected boolean isPathfindable(
            BlockState state,
            PathComputationType pathComputationType
    ) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
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
