package us.kayla.zeppelinmusthave.content.redstone.conduit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/** Propagates the strongest external source through a discovered conduit graph. */
final class PipedRedstoneSignalSolver {
    private PipedRedstoneSignalSolver() {
    }

    static Map<BlockPos, PipedRedstoneSignalPath> solve(
            ServerLevel level,
            PipedRedstoneNetwork network
    ) {
        PriorityQueue<SignalCandidate> queue = new PriorityQueue<>(
                Comparator.comparingInt(SignalCandidate::power).reversed()
                        .thenComparingInt(SignalCandidate::distance)
        );
        Map<BlockPos, PipedRedstoneSignalPath> resolved = new HashMap<>();

        for (BlockPos pos : network.positions()) {
            BlockState state = level.getBlockState(pos);
            int sourcePower = readExternalSource(level, pos, state);
            if (sourcePower > 0) {
                queue.add(new SignalCandidate(pos, sourcePower, 0));
            }
        }

        while (!queue.isEmpty()) {
            SignalCandidate candidate = queue.poll();
            PipedRedstoneSignalPath previous = resolved.get(candidate.pos());
            if (previous != null && !candidate.isBetterThan(previous)) {
                continue;
            }

            resolved.put(
                    candidate.pos(),
                    new PipedRedstoneSignalPath(candidate.power(), candidate.distance())
            );
            if (candidate.distance() >= network.maxSignalDistance()) {
                continue;
            }

            BlockState state = level.getBlockState(candidate.pos());
            for (Direction direction : Direction.values()) {
                if (!PipedRedstoneBlock.hasPort(state, direction)) {
                    continue;
                }

                BlockPos neighborPos = candidate.pos().relative(direction);
                if (!network.positions().contains(neighborPos)) {
                    continue;
                }
                BlockState neighborState = level.getBlockState(neighborPos);
                if (!PipedRedstoneBlock.hasPort(neighborState, direction.getOpposite())) {
                    continue;
                }

                SignalCandidate next = new SignalCandidate(
                        neighborPos,
                        candidate.power(),
                        candidate.distance() + 1
                );
                PipedRedstoneSignalPath known = resolved.get(neighborPos);
                if (known == null || next.isBetterThan(known)) {
                    queue.add(next);
                }
            }
        }
        return Map.copyOf(resolved);
    }

    private static int readExternalSource(
            ServerLevel level,
            BlockPos pos,
            BlockState state
    ) {
        int strongest = 0;
        for (Direction direction : Direction.values()) {
            if (!PipedRedstoneBlock.hasPort(state, direction)) {
                continue;
            }

            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof PipedRedstoneBlock) {
                continue;
            }
            strongest = Math.max(strongest, level.getSignal(neighborPos, direction));
        }
        return strongest;
    }

    private record SignalCandidate(BlockPos pos, int power, int distance) {
        private boolean isBetterThan(PipedRedstoneSignalPath previous) {
            return this.power > previous.power()
                    || (this.power == previous.power() && this.distance < previous.distance());
        }
    }
}
