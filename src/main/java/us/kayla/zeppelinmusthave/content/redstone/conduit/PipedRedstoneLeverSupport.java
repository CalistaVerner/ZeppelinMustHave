package us.kayla.zeppelinmusthave.content.redstone.conduit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock.FACE;
import static net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock.FACING;

final class PipedRedstoneLeverSupport {
    private static final VoxelShape NORTH = Block.box(5.0D, 4.0D, 10.0D, 11.0D, 12.0D, 16.0D);
    private static final VoxelShape SOUTH = Block.box(5.0D, 4.0D, 0.0D, 11.0D, 12.0D, 6.0D);
    private static final VoxelShape WEST = Block.box(10.0D, 4.0D, 5.0D, 16.0D, 12.0D, 11.0D);
    private static final VoxelShape EAST = Block.box(0.0D, 4.0D, 5.0D, 6.0D, 12.0D, 11.0D);
    private static final VoxelShape FLOOR_Z = Block.box(5.0D, 0.0D, 4.0D, 11.0D, 8.0D, 12.0D);
    private static final VoxelShape FLOOR_X = Block.box(4.0D, 0.0D, 5.0D, 12.0D, 8.0D, 11.0D);
    private static final VoxelShape CEILING_Z = Block.box(5.0D, 8.0D, 4.0D, 11.0D, 16.0D, 12.0D);
    private static final VoxelShape CEILING_X = Block.box(4.0D, 8.0D, 5.0D, 12.0D, 16.0D, 11.0D);

    private PipedRedstoneLeverSupport() {
    }

    static boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction pipeDirection = connectedDirection(state);
        BlockPos pipePos = pos.relative(pipeDirection.getOpposite());
        return level.getBlockState(pipePos).getBlock() instanceof PipedRedstoneBlock;
    }

    static void placed(BlockState state, Level level, BlockPos pos, BlockState oldState) {
        if (oldState.is(state.getBlock())) {
            return;
        }
        Direction pipeDirection = connectedDirection(state);
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

    static InteractionResult toggle(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockState nextState = state.cycle(PipedRedstoneNativeLeverBlock.POWERED);
        level.setBlock(pos, nextState, Block.UPDATE_ALL);
        level.playSound(
                null,
                pos,
                SoundEvents.LEVER_CLICK,
                SoundSource.BLOCKS,
                0.35F,
                nextState.getValue(PipedRedstoneNativeLeverBlock.POWERED) ? 0.65F : 0.5F
        );
        updatePipe(nextState, level, pos);
        return InteractionResult.CONSUME;
    }

    static void updatePipe(BlockState state, Level level, BlockPos pos) {
        Direction pipeDirection = connectedDirection(state);
        BlockPos pipePos = pos.relative(pipeDirection.getOpposite());
        if (level.getBlockState(pipePos).getBlock() instanceof PipedRedstoneBlock) {
            level.neighborChanged(pipePos, state.getBlock(), pos);
            PipedRedstoneNetworkManager.requestRebuild(level, pipePos);
        }
    }

    static int signal(BlockState state, Direction side) {
        return state.getValue(PipedRedstoneNativeLeverBlock.POWERED)
                && connectedDirection(state) == side
                ? 15
                : 0;
    }

    static boolean canConnect(BlockState state, Direction side) {
        return side != null && connectedDirection(state) == side;
    }

    static VoxelShape shape(BlockState state) {
        AttachFace face = state.getValue(FACE);
        Direction facing = state.getValue(FACING);
        return switch (face) {
            case FLOOR -> facing.getAxis() == Direction.Axis.X ? FLOOR_X : FLOOR_Z;
            case WALL -> switch (facing) {
                case EAST -> EAST;
                case WEST -> WEST;
                case SOUTH -> SOUTH;
                default -> NORTH;
            };
            case CEILING -> facing.getAxis() == Direction.Axis.X ? CEILING_X : CEILING_Z;
        };
    }
    private static Direction connectedDirection(BlockState state) {
        return switch (state.getValue(FACE)) {
            case CEILING -> Direction.DOWN;
            case FLOOR -> Direction.UP;
            case WALL -> state.getValue(FACING);
        };
    }

}
