package us.kayla.zeppelinmusthave.content.control.fcn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** All transient FCN routing and telemetry state associated with one vessel UUID. */
final class FlightControlVesselState {
    private String name;
    private final Map<AddressKey, FlightControlNetworkState> networks = new HashMap<>();
    private final FlightControlSystemsRegistry systems = new FlightControlSystemsRegistry();
    private boolean emergencyLatched;
    private long lastNetworkPruneTime = Long.MIN_VALUE;

    FlightControlVesselState(String name, boolean emergencyLatched) {
        this.name = normalizeName(name);
        this.emergencyLatched = emergencyLatched;
    }

    String name() {
        return this.name;
    }

    void updateName(String name) {
        this.name = normalizeName(name);
    }

    boolean emergencyLatched() {
        return this.emergencyLatched;
    }

    FlightControlNetworkState network(FlightControlAddress address, long gameTime) {
        this.pruneNetworksIfDue(gameTime);
        return this.networks.computeIfAbsent(new AddressKey(address), ignored -> new FlightControlNetworkState());
    }

    FlightControlNetworkState networkIfPresent(FlightControlAddress address, long gameTime) {
        this.pruneNetworksIfDue(gameTime);
        return this.networks.get(new AddressKey(address));
    }

    void latchEmergency(long gameTime) {
        this.emergencyLatched = true;
        this.clearOutputs(gameTime);
    }

    void resetEmergency(long gameTime) {
        this.emergencyLatched = false;
        this.clearOutputs(gameTime);
    }

    void reportSystem(long position, FlightSystemStatus status, long gameTime) {
        this.systems.report(position, status, gameTime);
    }

    FlightSystemsSnapshot sampleSystems(long gameTime) {
        return this.systems.sample(gameTime);
    }

    boolean isIdle(long gameTime) {
        this.pruneNetworks(gameTime);
        return this.networks.isEmpty() && this.systems.isIdle(gameTime);
    }

    private void clearOutputs(long gameTime) {
        for (FlightControlNetworkState network : this.networks.values()) {
            network.clearOutput(gameTime);
        }
    }

    private void pruneNetworksIfDue(long gameTime) {
        if (this.lastNetworkPruneTime != Long.MIN_VALUE
                && gameTime - this.lastNetworkPruneTime < FlightControlNetworkTiming.NETWORK_PRUNE_INTERVAL_TICKS) {
            return;
        }
        this.pruneNetworks(gameTime);
    }

    private void pruneNetworks(long gameTime) {
        Iterator<FlightControlNetworkState> iterator = this.networks.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isIdle(gameTime)) {
                iterator.remove();
            }
        }
        this.lastNetworkPruneTime = gameTime;
    }

    private static String normalizeName(String name) {
        return name == null ? "" : name;
    }

    private record AddressKey(String networkName, int frequency) {
        private AddressKey(FlightControlAddress address) {
            this(address.networkName(), address.frequency());
        }
    }
}
