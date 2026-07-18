package us.kayla.zeppelinmusthave.content.control.fcn;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Server-authoritative router for vessel-local control traffic.
 *
 * <p>Routing never crosses a Sable sub-level boundary. Inputs expire after two
 * seconds, computer heartbeats after one second, and only the deterministic
 * primary computer may publish the receiver frame.</p>
 */
public final class FlightControlNetworkManager {
    private static final long INPUT_TIMEOUT_TICKS = 40L;
    private static final long COMPUTER_TIMEOUT_TICKS = 20L;
    private static final long OUTPUT_TIMEOUT_TICKS = 20L;
    private static final long SYSTEM_TIMEOUT_TICKS = 40L;
    private static final Map<MinecraftServer, FlightControlNetworkManager> INSTANCES = new WeakHashMap<>();

    private final Map<UUID, VesselState> vessels = new HashMap<>();

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
        ResolvedVessel resolved = resolve(level, sourcePos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            VesselState vessel = manager.vessel(resolved);
            long gameTime = level.getGameTime();
            if (channel == FlightControlChannel.EMERGENCY_STOP && value > 0) {
                vessel.latchEmergency(gameTime);
                manager.setEmergencyPersisted(resolved, true);
                return true;
            }
            NetworkState network = vessel.network(address);
            network.publishInput(sourcePos.asLong(), channel, channel.clamp(value), authority, gameTime);
            return true;
        }
    }

    public static void clearInput(
            Level level,
            BlockPos sourcePos,
            FlightControlAddress address,
            FlightControlChannel channel
    ) {
        ResolvedVessel resolved = resolve(level, sourcePos);
        if (resolved == null) {
            return;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            VesselState vessel = manager.vessels.get(resolved.vesselId());
            if (vessel != null) {
                vessel.network(address).clearInput(sourcePos.asLong(), channel);
            }
        }
    }

    public static boolean heartbeatComputer(
            Level level,
            BlockPos computerPos,
            FlightControlAddress address
    ) {
        ResolvedVessel resolved = resolve(level, computerPos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            VesselState vessel = manager.vessel(resolved);
            NetworkState network = vessel.network(address);
            network.heartbeatComputer(computerPos.asLong(), level.getGameTime());
            return network.isPrimaryComputer(computerPos.asLong(), level.getGameTime());
        }
    }

    public static void withdrawComputer(
            Level level,
            BlockPos computerPos,
            FlightControlAddress address
    ) {
        ResolvedVessel resolved = resolve(level, computerPos);
        if (resolved == null) {
            return;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            VesselState vessel = manager.vessels.get(resolved.vesselId());
            if (vessel != null) {
                vessel.network(address).withdrawComputer(computerPos.asLong(), level.getGameTime());
            }
        }
    }

    public static FlightControlFrame readInputs(
            Level level,
            BlockPos computerPos,
            FlightControlAddress address
    ) {
        ResolvedVessel resolved = resolve(level, computerPos);
        if (resolved == null) {
            return FlightControlFrame.zero(level.getGameTime());
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            VesselState vessel = manager.vessel(resolved);
            if (vessel.emergencyLatched) {
                return FlightControlFrame.zero(level.getGameTime())
                        .with(FlightControlChannel.EMERGENCY_STOP, 1);
            }
            return vessel.network(address).composeInputs(level.getGameTime());
        }
    }

    public static boolean publishComputerOutput(
            Level level,
            BlockPos computerPos,
            FlightControlAddress address,
            FlightControlFrame output
    ) {
        ResolvedVessel resolved = resolve(level, computerPos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            VesselState vessel = manager.vessel(resolved);
            NetworkState network = vessel.network(address);
            long gameTime = level.getGameTime();
            if (vessel.emergencyLatched || !network.isPrimaryComputer(computerPos.asLong(), gameTime)) {
                return false;
            }
            network.publishOutput(computerPos.asLong(), output, gameTime);
            return true;
        }
    }

    public static NetworkOutput readOutput(
            Level level,
            BlockPos receiverPos,
            FlightControlAddress address
    ) {
        ResolvedVessel resolved = resolve(level, receiverPos);
        if (resolved == null) {
            return NetworkOutput.detached(level.getGameTime());
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            VesselState vessel = manager.vessel(resolved);
            long gameTime = level.getGameTime();
            if (vessel.emergencyLatched) {
                return new NetworkOutput(
                        true,
                        false,
                        true,
                        FlightControlFrame.zero(gameTime).with(FlightControlChannel.EMERGENCY_STOP, 1),
                        vessel.name
                );
            }
            NetworkState network = vessel.network(address);
            boolean online = network.computerOnline(gameTime);
            return new NetworkOutput(
                    true,
                    online,
                    false,
                    online ? network.output(gameTime) : FlightControlFrame.zero(gameTime),
                    vessel.name
            );
        }
    }

    public static boolean latchEmergency(Level level, BlockPos sourcePos) {
        ResolvedVessel resolved = resolve(level, sourcePos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            VesselState vessel = manager.vessel(resolved);
            vessel.latchEmergency(level.getGameTime());
            manager.setEmergencyPersisted(resolved, true);
            return true;
        }
    }

    public static boolean resetEmergency(Level level, BlockPos sourcePos) {
        ResolvedVessel resolved = resolve(level, sourcePos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            VesselState vessel = manager.vessel(resolved);
            vessel.resetEmergency(level.getGameTime());
            manager.setEmergencyPersisted(resolved, false);
            return true;
        }
    }

    public static boolean isEmergencyLatched(Level level, BlockPos pos) {
        ResolvedVessel resolved = resolve(level, pos);
        if (resolved == null) {
            return false;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            return manager.vessel(resolved).emergencyLatched;
        }
    }

    public static void reportSystem(Level level, BlockPos pos, FlightSystemStatus status) {
        ResolvedVessel resolved = resolve(level, pos);
        if (resolved == null) {
            return;
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            VesselState vessel = manager.vessel(resolved);
            vessel.systems.put(pos.asLong(), new TimedSystemStatus(status, level.getGameTime()));
        }
    }

    public static FlightSystemsSnapshot sampleSystems(Level level, BlockPos pos) {
        ResolvedVessel resolved = resolve(level, pos);
        if (resolved == null) {
            return FlightSystemsSnapshot.empty(level.getGameTime());
        }
        FlightControlNetworkManager manager = instance(resolved.server());
        synchronized (manager) {
            VesselState vessel = manager.vessel(resolved);
            return vessel.sampleSystems(level.getGameTime());
        }
    }

    public static Optional<VesselIdentity> identify(Level level, BlockPos pos) {
        ResolvedVessel resolved = resolve(level, pos);
        return resolved == null
                ? Optional.empty()
                : Optional.of(new VesselIdentity(resolved.vesselId(), resolved.vesselName()));
    }

    private VesselState vessel(ResolvedVessel resolved) {
        return this.vessels.computeIfAbsent(
                resolved.vesselId(),
                ignored -> new VesselState(
                        resolved.vesselName(),
                        FlightControlEmergencySavedData.get(resolved.server())
                                .isLatched(resolved.vesselId())
                )
        );
    }

    private void setEmergencyPersisted(ResolvedVessel resolved, boolean latched) {
        FlightControlEmergencySavedData.get(resolved.server())
                .setLatched(resolved.vesselId(), latched);
    }

    private static FlightControlNetworkManager instance(MinecraftServer server) {
        synchronized (INSTANCES) {
            return INSTANCES.computeIfAbsent(server, ignored -> new FlightControlNetworkManager());
        }
    }

    @Nullable
    private static ResolvedVessel resolve(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        SubLevel containing = Sable.HELPER.getContaining(level, pos);
        if (!(containing instanceof ServerSubLevel serverSubLevel)) {
            return null;
        }
        return new ResolvedVessel(
                serverLevel.getServer(),
                serverSubLevel.getUniqueId(),
                serverSubLevel.getName()
        );
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

    private record ResolvedVessel(MinecraftServer server, UUID vesselId, String vesselName) {
    }

    private record AddressKey(String networkName, int frequency) {
        private AddressKey(FlightControlAddress address) {
            this(address.networkName(), address.frequency());
        }
    }

    private record SignalState(int value, FlightControlAuthority authority, long gameTime) {
    }

    private record TimedSystemStatus(FlightSystemStatus status, long gameTime) {
    }

    private static final class NetworkState {
        private final EnumMap<FlightControlChannel, Map<Long, SignalState>> inputs =
                new EnumMap<>(FlightControlChannel.class);
        private final Map<Long, Long> computers = new HashMap<>();
        private FlightControlFrame output = FlightControlFrame.zero(0L);
        private long outputGameTime = Long.MIN_VALUE;
        private long outputComputer = Long.MIN_VALUE;

        void publishInput(
                long source,
                FlightControlChannel channel,
                int value,
                FlightControlAuthority authority,
                long gameTime
        ) {
            this.inputs.computeIfAbsent(channel, ignored -> new HashMap<>())
                    .put(source, new SignalState(value, authority, gameTime));
        }

        void clearInput(long source, FlightControlChannel channel) {
            Map<Long, SignalState> sources = this.inputs.get(channel);
            if (sources != null) {
                sources.remove(source);
            }
        }

        FlightControlFrame composeInputs(long gameTime) {
            this.pruneInputs(gameTime);
            EnumMap<FlightControlChannel, Integer> values = new EnumMap<>(FlightControlChannel.class);
            for (FlightControlChannel channel : FlightControlChannel.values()) {
                if (channel == FlightControlChannel.EMERGENCY_STOP) {
                    continue;
                }
                SignalState best = null;
                long bestSource = 0L;
                Map<Long, SignalState> sources = this.inputs.get(channel);
                if (sources == null) {
                    continue;
                }
                for (Map.Entry<Long, SignalState> entry : sources.entrySet()) {
                    SignalState candidate = entry.getValue();
                    if (best == null
                            || candidate.authority().priority() > best.authority().priority()
                            || candidate.authority() == best.authority()
                            && candidate.gameTime() > best.gameTime()
                            || candidate.authority() == best.authority()
                            && candidate.gameTime() == best.gameTime()
                            && Long.compareUnsigned(entry.getKey(), bestSource) < 0) {
                        best = candidate;
                        bestSource = entry.getKey();
                    }
                }
                if (best != null) {
                    values.put(channel, best.value());
                }
            }
            return FlightControlFrame.of(values, gameTime);
        }

        void heartbeatComputer(long source, long gameTime) {
            this.computers.put(source, gameTime);
            this.pruneComputers(gameTime);
        }

        void withdrawComputer(long source, long gameTime) {
            this.computers.remove(source);
            if (this.outputComputer == source) {
                this.output = FlightControlFrame.zero(gameTime);
                this.outputGameTime = gameTime;
                this.outputComputer = Long.MIN_VALUE;
            }
        }

        boolean isPrimaryComputer(long source, long gameTime) {
            this.pruneComputers(gameTime);
            long primary = this.primaryComputer();
            return primary != Long.MIN_VALUE && primary == source;
        }

        boolean computerOnline(long gameTime) {
            this.pruneComputers(gameTime);
            long primary = this.primaryComputer();
            return primary != Long.MIN_VALUE
                    && this.outputComputer == primary
                    && gameTime - this.outputGameTime <= OUTPUT_TIMEOUT_TICKS;
        }

        void publishOutput(long source, FlightControlFrame frame, long gameTime) {
            this.output = frame;
            this.outputGameTime = gameTime;
            this.outputComputer = source;
        }

        FlightControlFrame output(long gameTime) {
            return this.computerOnline(gameTime) ? this.output : FlightControlFrame.zero(gameTime);
        }

        void clearOutput(long gameTime) {
            this.output = FlightControlFrame.zero(gameTime);
            this.outputGameTime = gameTime;
            this.outputComputer = Long.MIN_VALUE;
        }

        private long primaryComputer() {
            long primary = Long.MIN_VALUE;
            for (long source : this.computers.keySet()) {
                if (primary == Long.MIN_VALUE || Long.compareUnsigned(source, primary) < 0) {
                    primary = source;
                }
            }
            return primary;
        }

        private void pruneInputs(long gameTime) {
            for (Map<Long, SignalState> sources : this.inputs.values()) {
                sources.values().removeIf(signal -> gameTime - signal.gameTime() > INPUT_TIMEOUT_TICKS);
            }
        }

        private void pruneComputers(long gameTime) {
            this.computers.values().removeIf(heartbeat -> gameTime - heartbeat > COMPUTER_TIMEOUT_TICKS);
        }
    }

    private static final class VesselState {
        private final String name;
        private final Map<AddressKey, NetworkState> networks = new HashMap<>();
        private final Map<Long, TimedSystemStatus> systems = new HashMap<>();
        private boolean emergencyLatched;

        private VesselState(String name, boolean emergencyLatched) {
            this.name = name == null ? "" : name;
            this.emergencyLatched = emergencyLatched;
        }

        NetworkState network(FlightControlAddress address) {
            return this.networks.computeIfAbsent(new AddressKey(address), ignored -> new NetworkState());
        }

        void latchEmergency(long gameTime) {
            this.emergencyLatched = true;
            for (NetworkState network : this.networks.values()) {
                network.clearOutput(gameTime);
            }
        }

        void resetEmergency(long gameTime) {
            this.emergencyLatched = false;
            for (NetworkState network : this.networks.values()) {
                network.clearOutput(gameTime);
            }
        }

        FlightSystemsSnapshot sampleSystems(long gameTime) {
            Iterator<Map.Entry<Long, TimedSystemStatus>> iterator = this.systems.entrySet().iterator();
            while (iterator.hasNext()) {
                if (gameTime - iterator.next().getValue().gameTime() > SYSTEM_TIMEOUT_TICKS) {
                    iterator.remove();
                }
            }

            int engines = 0;
            int activeEngines = 0;
            int burners = 0;
            int activeBurners = 0;
            int thrusters = 0;
            int activeThrusters = 0;
            int ballast = 0;
            double ballastFill = 0.0D;

            for (TimedSystemStatus timed : this.systems.values()) {
                FlightSystemStatus status = timed.status();
                switch (status.type()) {
                    case ENGINE -> {
                        engines++;
                        if (status.active()) activeEngines++;
                    }
                    case BURNER -> {
                        burners++;
                        if (status.active()) activeBurners++;
                    }
                    case THRUSTER -> {
                        thrusters++;
                        if (status.active()) activeThrusters++;
                    }
                    case BALLAST -> {
                        ballast++;
                        ballastFill += status.resourceRatio();
                    }
                }
            }

            return new FlightSystemsSnapshot(
                    engines,
                    activeEngines,
                    burners,
                    activeBurners,
                    thrusters,
                    activeThrusters,
                    ballast,
                    ballast == 0 ? 0.0D : ballastFill / ballast,
                    gameTime
            );
        }
    }
}
