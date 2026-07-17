package us.kayla.zeppelinmusthave.content.redstone.conduit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

        PipedRedstoneNetwork network = PipedRedstoneNetworkDiscovery.discover(
                serverLevel,
                start,
                MAX_NETWORK_NODES
        );
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

        PipedRedstoneNetwork network = PipedRedstoneNetworkDiscovery.discover(
                level,
                start,
                MAX_NETWORK_NODES
        );
        if (network.positions().isEmpty()) {
            return;
        }

        APPLYING.add(level);
        try {
            clearNetworkPower(level, network);
            notifyExternalNeighbors(level, network.positions());

            Map<BlockPos, PipedRedstoneSignalPath> resolved = network.truncated()
                    ? Map.of()
                    : PipedRedstoneSignalSolver.solve(level, network);

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

    private static void clearNetworkPower(ServerLevel level, PipedRedstoneNetwork network) {
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

    private static List<BlockPos> applyResolvedPower(
            ServerLevel level,
            PipedRedstoneNetwork network,
            Map<BlockPos, PipedRedstoneSignalPath> resolved
    ) {
        List<BlockPos> changed = new ArrayList<>();
        for (BlockPos pos : network.positions()) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof PipedRedstoneBlock)) {
                continue;
            }

            int power = resolved.getOrDefault(pos, PipedRedstoneSignalPath.OFF).power();
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

}
