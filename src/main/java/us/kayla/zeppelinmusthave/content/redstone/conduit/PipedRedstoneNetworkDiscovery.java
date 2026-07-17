package us.kayla.zeppelinmusthave.content.redstone.conduit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** Discovers one reciprocal-port connected component and its weakest-link profile. */
final class PipedRedstoneNetworkDiscovery {
    private PipedRedstoneNetworkDiscovery() {
    }

    static PipedRedstoneNetwork discover(
            ServerLevel level,
            BlockPos start,
            int maximumNodes
    ) {
        BlockState startState = level.getBlockState(start);
        if (!(startState.getBlock() instanceof PipedRedstoneBlock)) {
            return PipedRedstoneNetwork.EMPTY;
        }

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> positions = new HashSet<>();
        queue.add(start.immutable());

        int delay = 1;
        int maxDistance = Integer.MAX_VALUE;
        boolean truncated = false;

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!positions.add(pos)) {
                continue;
            }
            if (positions.size() > maximumNodes) {
                truncated = true;
                break;
            }

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof PipedRedstoneBlock conduit)) {
                positions.remove(pos);
                continue;
            }

            PipedRedstoneProfile profile = PipedRedstoneProfiles.INSTANCE.resolve(conduit.tier());
            delay = Math.max(delay, profile.propagationDelayTicks());
            maxDistance = Math.min(maxDistance, profile.maxSignalDistance());

            for (Direction direction : Direction.values()) {
                if (!PipedRedstoneBlock.hasPort(state, direction)) {
                    continue;
                }
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof PipedRedstoneBlock
                        && PipedRedstoneBlock.hasPort(neighborState, direction.getOpposite())
                        && !positions.contains(neighborPos)) {
                    queue.addLast(neighborPos.immutable());
                }
            }
        }

        if (maxDistance == Integer.MAX_VALUE) {
            maxDistance = 1;
        }
        return new PipedRedstoneNetwork(Set.copyOf(positions), delay, maxDistance, truncated);
    }
}
