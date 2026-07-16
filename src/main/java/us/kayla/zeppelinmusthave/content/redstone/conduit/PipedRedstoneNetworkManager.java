package us.kayla.zeppelinmusthave.content.redstone.conduit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Event-driven solver for isolated Piped Redstone networks.
 *
 * <p>The network is discovered only through reciprocal, explicitly enabled
 * pipe faces. Adjacent conduits therefore never couple merely because their
 * blocks touch. Mixed-tier networks operate at the weakest-link profile: the
 * greatest delay and shortest range found in the connected component.</p>
 */
public final class PipedRedstoneNetworkManager {
    private static final int MAX_NETWORK_NODES = 8192;
    private static final Set<ServerLevel> APPLYING = Collections.newSetFromMap(new WeakHashMap<>());

    private PipedRedstoneNetworkManager() {
    }

    public static void requestRebuild(Level level, BlockPos start) {
        if (!(level instanceof ServerLevel serverLevel) || APPLYING.contains(serverLevel)) {
            return;
        }

        Network network = discover(serverLevel, start);
        if (network.positions().isEmpty()) {
            return;
        }

        BlockPos anchor = network.positions().stream()
                .min(Comparator.comparingLong(BlockPos::asLong))
                .orElse(start);
        Block anchorBlock = serverLevel.getBlockState(anchor).getBlock();
        if (!(anchorBlock instanceof PipedRedstoneBlock)) {
            return;
        }

        if (!serverLevel.getBlockTicks().hasScheduledTick(anchor, anchorBlock)) {
            serverLevel.scheduleTick(anchor, anchorBlock, network.delayTicks());
        }
    }

    public static void requestAdjacentComponents(Level level, BlockPos removedPos) {
        if (!(level instanceof ServerLevel serverLevel) || APPLYING.contains(serverLevel)) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = removedPos.relative(direction);
            if (serverLevel.getBlockState(neighborPos).getBlock() instanceof PipedRedstoneBlock) {
                requestRebuild(serverLevel, neighborPos);
            }
        }
    }

    public static void rebuild(ServerLevel level, BlockPos start) {
        if (APPLYING.contains(level)) {
            return;
        }

        Network network = discover(level, start);
        if (network.positions().isEmpty()) {
            return;
        }

        APPLYING.add(level);
        try {
            clearNetworkPower(level, network);
            notifyExternalNeighbors(level, network.positions());

            Map<BlockPos, SignalPath> resolved = network.truncated()
                    ? Map.of()
                    : solveSignal(level, network);

            List<BlockPos> changed = applyResolvedPower(level, network, resolved);
            notifyExternalNeighbors(level, changed);

            if (network.truncated()) {
                ZeppelinMustHave.LOGGER.warn(
                        "Piped Redstone network at {} exceeded {} blocks and was disabled",
                        start,
                        MAX_NETWORK_NODES
                );
            }
        } finally {
            APPLYING.remove(level);
        }
    }

    private static Network discover(ServerLevel level, BlockPos start) {
        BlockState startState = level.getBlockState(start);
        if (!(startState.getBlock() instanceof PipedRedstoneBlock)) {
            return Network.EMPTY;
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
            if (positions.size() > MAX_NETWORK_NODES) {
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
        return new Network(Set.copyOf(positions), delay, maxDistance, truncated);
    }

    private static void clearNetworkPower(ServerLevel level, Network network) {
        for (BlockPos pos : network.positions()) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof PipedRedstoneBlock)
                    || state.getValue(PipedRedstoneBlock.POWER) == 0) {
                continue;
            }
            level.setBlock(
                    pos,
                    state.setValue(PipedRedstoneBlock.POWER, 0),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    private static Map<BlockPos, SignalPath> solveSignal(ServerLevel level, Network network) {
        PriorityQueue<SignalCandidate> queue = new PriorityQueue<>(
                Comparator.comparingInt(SignalCandidate::power).reversed()
                        .thenComparingInt(SignalCandidate::distance)
        );
        Map<BlockPos, SignalPath> resolved = new HashMap<>();

        for (BlockPos pos : network.positions()) {
            BlockState state = level.getBlockState(pos);
            int sourcePower = readExternalSource(level, pos, state);
            if (sourcePower > 0) {
                queue.add(new SignalCandidate(pos, sourcePower, 0));
            }
        }

        while (!queue.isEmpty()) {
            SignalCandidate candidate = queue.poll();
            SignalPath previous = resolved.get(candidate.pos());
            if (previous != null && !candidate.isBetterThan(previous)) {
                continue;
            }

            resolved.put(
                    candidate.pos(),
                    new SignalPath(candidate.power(), candidate.distance())
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
                SignalPath known = resolved.get(neighborPos);
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
                // Pipes are only connected through reciprocal ports, never
                // through vanilla weak-power adjacency.
                continue;
            }

            strongest = Math.max(
                    strongest,
                    level.getSignal(neighborPos, direction)
            );
        }
        return strongest;
    }

    private static List<BlockPos> applyResolvedPower(
            ServerLevel level,
            Network network,
            Map<BlockPos, SignalPath> resolved
    ) {
        List<BlockPos> changed = new ArrayList<>();
        for (BlockPos pos : network.positions()) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof PipedRedstoneBlock)) {
                continue;
            }

            int power = resolved.getOrDefault(pos, SignalPath.OFF).power();
            if (state.getValue(PipedRedstoneBlock.POWER) == power) {
                continue;
            }

            level.setBlock(
                    pos,
                    state.setValue(PipedRedstoneBlock.POWER, power),
                    Block.UPDATE_CLIENTS
            );
            changed.add(pos);
        }
        return changed;
    }

    private static void notifyExternalNeighbors(
            ServerLevel level,
            Iterable<BlockPos> positions
    ) {
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof PipedRedstoneBlock conduit)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                if (!PipedRedstoneBlock.hasPort(state, direction)) {
                    continue;
                }
                BlockPos neighborPos = pos.relative(direction);
                if (level.getBlockState(neighborPos).getBlock() instanceof PipedRedstoneBlock) {
                    continue;
                }
                level.neighborChanged(neighborPos, conduit, pos);
            }
        }
    }

    private record Network(
            Set<BlockPos> positions,
            int delayTicks,
            int maxSignalDistance,
            boolean truncated
    ) {
        private static final Network EMPTY = new Network(Set.of(), 1, 1, false);
    }

    private record SignalPath(int power, int distance) {
        private static final SignalPath OFF = new SignalPath(0, Integer.MAX_VALUE);
    }

    private record SignalCandidate(BlockPos pos, int power, int distance) {
        private boolean isBetterThan(SignalPath previous) {
            return this.power > previous.power()
                    || (this.power == previous.power() && this.distance < previous.distance());
        }
    }
}
