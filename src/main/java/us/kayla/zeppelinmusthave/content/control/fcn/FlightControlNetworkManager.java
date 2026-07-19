package us.kayla.zeppelinmusthave.content.control.fcn;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

import static us.kayla.zeppelinmusthave.content.control.fcn.FlightControlVesselResolver.ResolvedVessel;

/**
 * Server-authoritative facade for vessel-local Flight Control Network traffic.
 *
 * <p>Resolution, addressed-network state, system telemetry and cache lifecycle
 * are delegated to focused package components. Public callers retain one stable
 * API while routing never crosses a Sable sub-level boundary.</p>
 */
public final class FlightControlNetworkManager {
    private static final Map<MinecraftServer, FlightControlNetworkManager> INSTANCES = new WeakHashMap<>();

    private final Map<UUID, FlightControlVesselState> vessels = new HashMap<>();
    private long lastVesselPruneTime = Long.MIN_VALUE;

    private FlightControlNetworkManager() {
    }

    public static boolean publishInput(
            Level level,
            BlockPos sourcePos,
            FlightControlAddress address,
            FlightControlChannel channel,
            int value,
            FlightControlAuthority authority
    ) {
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, sourcePos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            long gameTime = level.getGameTime();
            FlightControlVesselState vessel = manager.vessel(resolved, gameTime);
            if (channel == FlightControlChannel.EMERGENCY_STOP && value > 0) {
                vessel.latchEmergency(gameTime);
                setEmergencyPersisted(resolved, true);
                return true;
            }
            vessel.network(address, gameTime).publishInput(
                    sourcePos.asLong(),
                    channel,
                    channel.clamp(value),
                    authority,
                    gameTime
            );
            return true;
        }
    }

    public static void clearInput(
            Level level,
            BlockPos sourcePos,
            FlightControlAddress address,
            FlightControlChannel channel
    ) {
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, sourcePos);
        if (resolved == null) {
            return;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            long gameTime = level.getGameTime();
            FlightControlVesselState vessel = manager.vessels.get(resolved.vesselId());
            if (vessel == null) {
                return;
            }
            FlightControlNetworkState network = vessel.networkIfPresent(address, gameTime);
            if (network != null) {
                network.clearInput(sourcePos.asLong(), channel);
            }
        }
    }

    public static boolean heartbeatComputer(
            Level level,
            BlockPos computerPos,
            FlightControlAddress address
    ) {
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, computerPos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            long gameTime = level.getGameTime();
            FlightControlNetworkState network = manager.vessel(resolved, gameTime).network(address, gameTime);
            long source = computerPos.asLong();
            network.heartbeatComputer(source, gameTime);
            return network.isPrimaryComputer(source, gameTime);
        }
    }

    public static void withdrawComputer(
            Level level,
            BlockPos computerPos,
            FlightControlAddress address
    ) {
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, computerPos);
        if (resolved == null) {
            return;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            long gameTime = level.getGameTime();
            FlightControlVesselState vessel = manager.vessels.get(resolved.vesselId());
            if (vessel == null) {
                return;
            }
            FlightControlNetworkState network = vessel.networkIfPresent(address, gameTime);
            if (network != null) {
                network.withdrawComputer(computerPos.asLong(), gameTime);
            }
        }
    }

    public static FlightControlFrame readInputs(
            Level level,
            BlockPos computerPos,
            FlightControlAddress address
    ) {
        long gameTime = level.getGameTime();
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, computerPos);
        if (resolved == null) {
            return FlightControlFrame.zero(gameTime);
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            FlightControlVesselState vessel = manager.vessel(resolved, gameTime);
            if (vessel.emergencyLatched()) {
                return FlightControlFrame.zero(gameTime)
                        .with(FlightControlChannel.EMERGENCY_STOP, 1);
            }
            return vessel.network(address, gameTime).composeInputs(gameTime);
        }
    }

    public static boolean publishComputerOutput(
            Level level,
            BlockPos computerPos,
            FlightControlAddress address,
            FlightControlFrame output
    ) {
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, computerPos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            long gameTime = level.getGameTime();
            FlightControlVesselState vessel = manager.vessel(resolved, gameTime);
            FlightControlNetworkState network = vessel.network(address, gameTime);
            long source = computerPos.asLong();
            if (vessel.emergencyLatched() || !network.isPrimaryComputer(source, gameTime)) {
                return false;
            }
            network.publishOutput(source, output, gameTime);
            return true;
        }
    }

    public static NetworkOutput readOutput(
            Level level,
            BlockPos receiverPos,
            FlightControlAddress address
    ) {
        long gameTime = level.getGameTime();
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, receiverPos);
        if (resolved == null) {
            return NetworkOutput.detached(gameTime);
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            FlightControlVesselState vessel = manager.vessel(resolved, gameTime);
            if (vessel.emergencyLatched()) {
                return new NetworkOutput(
                        true,
                        false,
                        true,
                        FlightControlFrame.zero(gameTime).with(FlightControlChannel.EMERGENCY_STOP, 1),
                        vessel.name()
                );
            }
            FlightControlNetworkState network = vessel.network(address, gameTime);
            boolean online = network.computerOnline(gameTime);
            return new NetworkOutput(
                    true,
                    online,
                    false,
                    online ? network.output(gameTime) : FlightControlFrame.zero(gameTime),
                    vessel.name()
            );
        }
    }

    public static boolean latchEmergency(Level level, BlockPos sourcePos) {
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, sourcePos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            manager.vessel(resolved, level.getGameTime()).latchEmergency(level.getGameTime());
            setEmergencyPersisted(resolved, true);
            return true;
        }
    }

    public static boolean resetEmergency(Level level, BlockPos sourcePos) {
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, sourcePos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            manager.vessel(resolved, level.getGameTime()).resetEmergency(level.getGameTime());
            setEmergencyPersisted(resolved, false);
            return true;
        }
    }

    public static boolean isEmergencyLatched(Level level, BlockPos pos) {
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, pos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            return manager.vessel(resolved, level.getGameTime()).emergencyLatched();
        }
    }

    public static void reportSystem(Level level, BlockPos pos, FlightSystemStatus status) {
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, pos);
        if (resolved == null) {
            return;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            long gameTime = level.getGameTime();
            manager.vessel(resolved, gameTime).reportSystem(pos.asLong(), status, gameTime);
        }
    }

    public static FlightSystemsSnapshot sampleSystems(Level level, BlockPos pos) {
        long gameTime = level.getGameTime();
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, pos);
        if (resolved == null) {
            return FlightSystemsSnapshot.empty(gameTime);
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            return manager.vessel(resolved, gameTime).sampleSystems(gameTime);
        }
    }

    public static Optional<VesselIdentity> identify(Level level, BlockPos pos) {
        ResolvedVessel resolved = FlightControlVesselResolver.resolve(level, pos);
        return resolved == null
                ? Optional.empty()
                : Optional.of(new VesselIdentity(resolved.vesselId(), resolved.vesselName()));
    }

    private FlightControlVesselState vessel(ResolvedVessel resolved, long gameTime) {
        this.pruneVesselsIfDue(gameTime, resolved.vesselId());
        FlightControlVesselState vessel = this.vessels.computeIfAbsent(
                resolved.vesselId(),
                ignored -> new FlightControlVesselState(
                        resolved.vesselName(),
                        FlightControlEmergencySavedData.get(resolved.server()).isLatched(resolved.vesselId())
                )
        );
        vessel.updateName(resolved.vesselName());
        return vessel;
    }

    private void pruneVesselsIfDue(long gameTime, UUID activeVessel) {
        if (this.lastVesselPruneTime != Long.MIN_VALUE
                && gameTime - this.lastVesselPruneTime < FlightControlNetworkTiming.VESSEL_PRUNE_INTERVAL_TICKS) {
            return;
        }
        Iterator<Map.Entry<UUID, FlightControlVesselState>> iterator = this.vessels.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, FlightControlVesselState> entry = iterator.next();
            if (!entry.getKey().equals(activeVessel) && entry.getValue().isIdle(gameTime)) {
                iterator.remove();
            }
        }
        this.lastVesselPruneTime = gameTime;
    }

    private static void setEmergencyPersisted(ResolvedVessel resolved, boolean latched) {
        FlightControlEmergencySavedData.get(resolved.server()).setLatched(resolved.vesselId(), latched);
    }

    private static FlightControlNetworkManager instance(MinecraftServer server) {
        synchronized (INSTANCES) {
            return INSTANCES.computeIfAbsent(server, ignored -> new FlightControlNetworkManager());
        }
    }

    public record NetworkOutput(
            boolean attached,
            boolean computerOnline,
            boolean emergencyLatched,
            FlightControlFrame frame,
            String vesselName
    ) {
        private static NetworkOutput detached(long gameTime) {
            return new NetworkOutput(false, false, false, FlightControlFrame.zero(gameTime), "");
        }
    }

    public record VesselIdentity(UUID id, String name) {
    }
}
