package us.kayla.zeppelinmusthave.content.redstone.conduit;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Waterproof inline repeater that resets conduit distance without changing
 * analog strength. Delay is configurable exactly like a vanilla repeater:
 * one through four redstone ticks, represented as two through eight game ticks.
 */
public final class PipedRedstoneRepeaterBlock extends Block
        implements SimpleWaterloggedBlock, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE = Block.box(3.0D, 3.0D, 3.0D, 13.0D, 13.0D, 13.0D);

    private final MapCodec<PipedRedstoneRepeaterBlock> codec = MapCodec.unit(this);

    public PipedRedstoneRepeaterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(POWER, 0)
                        .setValue(DELAY, 1)
                        .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return this.codec;
    }

    public static int getLightPower(BlockState state) {
        return state.getValue(POWER) > 0 ? 7 : 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(WATERLOGGED, fluidState.is(Fluids.WATER));
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }

        BlockState nextState = state.cycle(DELAY);
        int redstoneTicks = nextState.getValue(DELAY);
        int gameTicks = redstoneTicks * 2;

        if (!level.isClientSide) {
            level.setBlock(pos, nextState, Block.UPDATE_ALL);
            level.playSound(
                    null,
                    pos,
                    SoundEvents.COMPARATOR_CLICK,
                    SoundSource.BLOCKS,
                    0.35F,
                    0.55F + redstoneTicks * 0.1F
            );
            player.displayClientMessage(
                    Component.translatable(
                            "message.zeppelin_must_have.piped_redstone.repeater_delay",
                            redstoneTicks,
                            gameTicks
                    ),
                    true
            );
            this.scheduleUpdate(level, pos, nextState);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
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
        this.scheduleUpdate(level, pos, state);
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
        this.scheduleUpdate(level, pos, state);
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    protected void tick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        Direction outputDirection = state.getValue(FACING);
        BlockPos inputPos = pos.relative(outputDirection.getOpposite());
        int inputPower = level.getSignal(inputPos, outputDirection.getOpposite());

        if (state.getValue(POWER) == inputPower) {
            return;
        }

        level.setBlock(
                pos,
                state.setValue(POWER, inputPower),
                Block.UPDATE_CLIENTS
        );
        level.neighborChanged(pos.relative(outputDirection), this, pos);
    }

    private void scheduleUpdate(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide || level.getBlockTicks().hasScheduledTick(pos, this)) {
            return;
        }
        level.scheduleTick(pos, this, state.getValue(DELAY) * 2);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction side
    ) {
        return state.getValue(FACING).getOpposite() == side ? state.getValue(POWER) : 0;
    }

    @Override
    public int getDirectSignal(
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
        if (side == null) {
            return false;
        }
        Direction facing = state.getValue(FACING);
        return side == facing || side == facing.getOpposite();
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
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
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, POWER, DELAY, WATERLOGGED);
    }
}
