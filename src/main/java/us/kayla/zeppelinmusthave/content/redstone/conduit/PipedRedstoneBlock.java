package us.kayla.zeppelinmusthave.content.redstone.conduit;

import java.util.List;
import java.util.function.Predicate;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public final class PipedRedstoneBlock extends PipeBlock
        implements SimpleWaterloggedBlock, IWrenchable {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final int PLACEMENT_HELPER_ID = PlacementHelpers.register(new PlacementHelper());

    private final PipedRedstoneTier tier;
    private final MapCodec<PipedRedstoneBlock> codec = MapCodec.unit(this);

    public PipedRedstoneBlock(Properties properties, PipedRedstoneTier tier) {
        super(3.0F / 16.0F, properties);
        this.tier = tier;

        BlockState state = this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(POWER, 0)
                .setValue(WATERLOGGED, false);
        this.registerDefaultState(state);
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        Direction.Axis axis = context.getClickedFace().getAxis();
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == axis) {
                state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), true);
            }
        }

        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return state.setValue(WATERLOGGED, fluidState.is(Fluids.WATER));
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
        if (!oldState.is(state.getBlock())) {
            connectReciprocalPorts(level, pos, state);
            PipedRedstoneNetworkManager.requestRebuild(level, pos);
        }
    }

    private static void connectReciprocalPorts(
            Level level,
            BlockPos pos,
            BlockState placedState
    ) {
        for (Direction direction : Direction.values()) {
            if (!hasPort(placedState, direction)) {
                continue;
            }

            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (!(neighborState.getBlock() instanceof PipedRedstoneBlock)) {
                continue;
            }

            Direction reciprocal = direction.getOpposite();
            BlockState reshapedNeighbor = reshapeTerminalForTurn(
                    level,
                    neighborPos,
                    neighborState,
                    reciprocal
            ).setValue(PROPERTY_BY_DIRECTION.get(reciprocal), true);

            if (reshapedNeighbor != neighborState) {
                level.setBlock(neighborPos, reshapedNeighbor, Block.UPDATE_ALL);
            }
            PipedRedstoneNetworkManager.requestRebuild(level, neighborPos);
        }
    }

    /**
     * Turns a straight terminal into an elbow when a conduit is placed against
     * its side. Middle segments keep both axial connections and become a tee.
     */
    private static BlockState reshapeTerminalForTurn(
            Level level,
            BlockPos pos,
            BlockState state,
            Direction newConnection
    ) {
        Direction connectedDirection = null;
        int connectedConduits = 0;

        for (Direction direction : Direction.values()) {
            if (!hasPort(state, direction)) {
                continue;
            }
            BlockState adjacent = level.getBlockState(pos.relative(direction));
            if (adjacent.getBlock() instanceof PipedRedstoneBlock
                    && hasPort(adjacent, direction.getOpposite())) {
                connectedDirection = direction;
                connectedConduits++;
            }
        }

        if (connectedConduits != 1
                || connectedDirection == null
                || connectedDirection.getAxis() == newConnection.getAxis()) {
            return state;
        }

        Direction dangling = connectedDirection.getOpposite();
        if (!hasPort(state, dangling)) {
            return state;
        }

        BlockState danglingNeighbor = level.getBlockState(pos.relative(dangling));
        if (!danglingNeighbor.canBeReplaced()) {
            return state;
        }

        return state.setValue(PROPERTY_BY_DIRECTION.get(dangling), false);
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
            if (!level.isClientSide && state.getValue(POWER) > 0) {
                for (Direction direction : Direction.values()) {
                    if (hasPort(state, direction)) {
                        level.neighborChanged(pos.relative(direction), this, pos);
                    }
                }
            }
            PipedRedstoneNetworkManager.requestAdjacentComponents(level, pos);
        }
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
        PipedRedstoneNetworkManager.requestRebuild(level, pos);
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    protected void tick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        PipedRedstoneNetworkManager.rebuild(level, pos);
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

        IPlacementHelper helper = PlacementHelpers.get(PLACEMENT_HELPER_ID);
        if (helper.matchesItem(stack) && stack.getItem() instanceof BlockItem blockItem) {
            return helper.getOffset(player, level, state, pos, hitResult, stack)
                    .placeInWorld(level, blockItem, player, hand, hitResult);
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        boolean nextValue = !hasPort(state, direction);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockState nextState = state.setValue(
                PROPERTY_BY_DIRECTION.get(direction),
                nextValue
        );
        level.setBlock(pos, nextState, Block.UPDATE_ALL);

        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof PipedRedstoneBlock) {
            level.setBlock(
                    neighborPos,
                    neighborState.setValue(
                            PROPERTY_BY_DIRECTION.get(direction.getOpposite()),
                            nextValue
                    ),
                    Block.UPDATE_ALL
            );
            PipedRedstoneNetworkManager.requestRebuild(level, neighborPos);
        }

        PipedRedstoneNetworkManager.requestRebuild(level, pos);
        IWrenchable.playRotateSound(level, pos);
        return InteractionResult.SUCCESS;
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
        return hasPort(state, side.getOpposite()) ? state.getValue(POWER) : 0;
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
        return side != null && hasPort(state, side.getOpposite());
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
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, POWER, WATERLOGGED);
    }

    private static final class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof PipedRedstoneBlock;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.getBlock() instanceof PipedRedstoneBlock;
        }

        @Override
        public PlacementOffset getOffset(
                Player player,
                Level level,
                BlockState state,
                BlockPos pos,
                BlockHitResult hitResult
        ) {
            List<Direction> directions = IPlacementHelper.orderedByDistance(
                    pos,
                    hitResult.getLocation(),
                    direction -> hasPort(state, direction)
            );

            for (Direction direction : directions) {
                int range = AllConfigs.server().equipment.placementAssistRange.get();
                if (player != null) {
                    AttributeInstance reach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
                    if (reach != null
                            && reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier.id())) {
                        range += 4;
                    }
                }

                BlockPos cursor = pos;
                BlockState cursorState = state;
                int attached = 0;

                while (attached < range && cursorState.getBlock() instanceof PipedRedstoneBlock) {
                    if (!hasPort(cursorState, direction)) {
                        break;
                    }

                    BlockPos nextPos = cursor.relative(direction);
                    BlockState nextState = level.getBlockState(nextPos);
                    if (nextState.getBlock() instanceof PipedRedstoneBlock
                            && hasPort(nextState, direction.getOpposite())) {
                        cursor = nextPos;
                        cursorState = nextState;
                        attached++;
                        continue;
                    }

                    if (!nextState.canBeReplaced()) {
                        break;
                    }

                    Direction extensionDirection = direction;
                    return PlacementOffset.success(nextPos, placedState -> {
                        BlockState result = placedState;
                        for (Direction candidate : Direction.values()) {
                            result = result.setValue(PROPERTY_BY_DIRECTION.get(candidate), false);
                        }
                        return result
                                .setValue(PROPERTY_BY_DIRECTION.get(extensionDirection), true)
                                .setValue(
                                        PROPERTY_BY_DIRECTION.get(extensionDirection.getOpposite()),
                                        true
                                );
                    });
                }
            }

            return PlacementOffset.fail();
        }
    }
}
