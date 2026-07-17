package us.kayla.zeppelinmusthave.content.redstone.conduit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Mutates reciprocal ports and terminal topology when conduit blocks join. */
final class PipedRedstoneTopology {
    private PipedRedstoneTopology() {
    }

    static void connectReciprocalPorts(Level level, BlockPos pos, BlockState placedState) {
        for (Direction direction : Direction.values()) {
            if (!PipedRedstoneBlock.hasPort(placedState, direction)) {
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
            ).setValue(PipedRedstoneBlock.PROPERTY_BY_DIRECTION.get(reciprocal), true);

            if (reshapedNeighbor != neighborState) {
                level.setBlock(neighborPos, reshapedNeighbor, Block.UPDATE_ALL);
            }
            PipedRedstoneNetworkManager.requestRebuild(level, neighborPos);
        }
    }

    /** Turns a straight terminal into an elbow when attached from its side. */
    private static BlockState reshapeTerminalForTurn(
            Level level,
            BlockPos pos,
            BlockState state,
            Direction newConnection
    ) {
        Direction connectedDirection = null;
        int connectedConduits = 0;

        for (Direction direction : Direction.values()) {
            if (!PipedRedstoneBlock.hasPort(state, direction)) {
                continue;
            }
            BlockState adjacent = level.getBlockState(pos.relative(direction));
            if (adjacent.getBlock() instanceof PipedRedstoneBlock
                    && PipedRedstoneBlock.hasPort(adjacent, direction.getOpposite())) {
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
        if (!PipedRedstoneBlock.hasPort(state, dangling)) {
            return state;
        }

        BlockState danglingNeighbor = level.getBlockState(pos.relative(dangling));
        if (!danglingNeighbor.canBeReplaced()) {
            return state;
        }
        return state.setValue(PipedRedstoneBlock.PROPERTY_BY_DIRECTION.get(dangling), false);
    }
}
