package us.kayla.zeppelinmusthave.content.redstone.conduit;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.createmod.catnip.placement.IPlacementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

final class PipedRedstoneBlockSupport {
    private PipedRedstoneBlockSupport() {
    }

    static BlockState placementState(BlockState defaultState, BlockPlaceContext context) {
        BlockState state = defaultState;
        Direction.Axis axis = context.getClickedFace().getAxis();
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == axis) {
                state = state.setValue(
                        PipedRedstoneBlock.PROPERTY_BY_DIRECTION.get(direction),
                        true
                );
            }
        }
        return PipedRedstoneWaterlogging.applyPlacement(
                state,
                PipedRedstoneBlock.WATERLOGGED,
                context
        );
    }

    static boolean skipConnectedFace(
            BlockState state,
            BlockState adjacentState,
            Direction side
    ) {
        return adjacentState.getBlock() instanceof PipedRedstoneBlock
                && PipedRedstoneBlock.hasPort(state, side)
                && PipedRedstoneBlock.hasPort(adjacentState, side.getOpposite());
    }

    static void placed(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState
    ) {
        if (oldState.is(state.getBlock())) {
            return;
        }
        PipedRedstoneTopology.connectReciprocalPorts(level, pos, state);
        PipedRedstoneNetworkManager.requestRebuild(level, pos);
    }

    static void removed(
            PipedRedstoneBlock block,
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState
    ) {
        if (state.is(newState.getBlock())) {
            return;
        }
        if (!level.isClientSide && state.getValue(PipedRedstoneBlock.POWER) > 0) {
            for (Direction direction : Direction.values()) {
                if (PipedRedstoneBlock.hasPort(state, direction)) {
                    level.neighborChanged(pos.relative(direction), block, pos);
                }
            }
        }
        PipedRedstoneNetworkManager.requestAdjacentComponents(level, pos);
    }

    static void neighborChanged(Level level, BlockPos pos) {
        PipedRedstoneNetworkManager.requestRebuild(level, pos);
    }

    static void scheduledTick(ServerLevel level, BlockPos pos) {
        PipedRedstoneNetworkManager.rebuild(level, pos);
    }

    static ItemInteractionResult tryPlacementAssist(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        IPlacementHelper helper = PipedRedstonePlacement.helper();
        if (helper.matchesItem(stack) && stack.getItem() instanceof BlockItem blockItem) {
            return helper.getOffset(player, level, state, pos, hitResult, stack)
                    .placeInWorld(level, blockItem, player, hand, hitResult);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    static InteractionResult wrench(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        boolean nextValue = !PipedRedstoneBlock.hasPort(state, direction);
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        level.setBlock(
                pos,
                state.setValue(
                        PipedRedstoneBlock.PROPERTY_BY_DIRECTION.get(direction),
                        nextValue
                ),
                Block.UPDATE_ALL
        );
        toggleNeighborPort(level, pos, direction, nextValue);
        PipedRedstoneNetworkManager.requestRebuild(level, pos);
        IWrenchable.playRotateSound(level, pos);
        return InteractionResult.SUCCESS;
    }

    private static void toggleNeighborPort(
            Level level,
            BlockPos pos,
            Direction direction,
            boolean value
    ) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (!(neighborState.getBlock() instanceof PipedRedstoneBlock)) {
            return;
        }
        level.setBlock(
                neighborPos,
                neighborState.setValue(
                        PipedRedstoneBlock.PROPERTY_BY_DIRECTION.get(direction.getOpposite()),
                        value
                ),
                Block.UPDATE_ALL
        );
        PipedRedstoneNetworkManager.requestRebuild(level, neighborPos);
    }

    static int signal(BlockState state, Direction side) {
        return PipedRedstoneBlock.hasPort(state, side.getOpposite())
                ? state.getValue(PipedRedstoneBlock.POWER)
                : 0;
    }

    static boolean canConnect(BlockState state, Direction side) {
        return side != null && PipedRedstoneBlock.hasPort(state, side.getOpposite());
    }
}
