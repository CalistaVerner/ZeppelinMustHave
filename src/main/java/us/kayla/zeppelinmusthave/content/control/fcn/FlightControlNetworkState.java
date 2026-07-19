package us.kayla.zeppelinmusthave.content.control.fcn;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Mutable state for one addressed FCN network inside one vessel. */
final class FlightControlNetworkState {
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
        if (sources == null) {
            return;
        }
        sources.remove(source);
        if (sources.isEmpty()) {
            this.inputs.remove(channel);
        }
    }

    FlightControlFrame composeInputs(long gameTime) {
        this.pruneInputs(gameTime);
        EnumMap<FlightControlChannel, Integer> values = new EnumMap<>(FlightControlChannel.class);
        for (FlightControlChannel channel : FlightControlChannel.values()) {
            if (channel == FlightControlChannel.EMERGENCY_STOP) {
                continue;
            }
            SignalSelection selected = selectSignal(this.inputs.get(channel));
            if (selected != null) {
                values.put(channel, selected.signal().value());
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
            this.clearOutput(gameTime);
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
                && gameTime - this.outputGameTime <= FlightControlNetworkTiming.OUTPUT_TIMEOUT_TICKS;
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

    boolean isIdle(long gameTime) {
        this.pruneInputs(gameTime);
        this.pruneComputers(gameTime);
        boolean outputExpired = this.outputComputer == Long.MIN_VALUE
                || gameTime - this.outputGameTime > FlightControlNetworkTiming.OUTPUT_TIMEOUT_TICKS;
        return this.inputs.isEmpty() && this.computers.isEmpty() && outputExpired;
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
        Iterator<Map.Entry<FlightControlChannel, Map<Long, SignalState>>> channels =
                this.inputs.entrySet().iterator();
        while (channels.hasNext()) {
            Map<Long, SignalState> sources = channels.next().getValue();
            sources.values().removeIf(
                    signal -> gameTime - signal.gameTime() > FlightControlNetworkTiming.INPUT_TIMEOUT_TICKS
            );
            if (sources.isEmpty()) {
                channels.remove();
            }
        }
    }

    private void pruneComputers(long gameTime) {
        this.computers.values().removeIf(
                heartbeat -> gameTime - heartbeat > FlightControlNetworkTiming.COMPUTER_TIMEOUT_TICKS
        );
    }

    private static SignalSelection selectSignal(Map<Long, SignalState> sources) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        SignalSelection best = null;
        for (Map.Entry<Long, SignalState> entry : sources.entrySet()) {
            SignalSelection candidate = new SignalSelection(entry.getKey(), entry.getValue());
            if (best == null || candidate.precedes(best)) {
                best = candidate;
            }
        }
        return best;
    }

    private record SignalState(int value, FlightControlAuthority authority, long gameTime) {
    }

    private record SignalSelection(long source, SignalState signal) {
        boolean precedes(SignalSelection other) {
            int authority = Integer.compare(
                    this.signal.authority().priority(),
                    other.signal.authority().priority()
            );
            if (authority != 0) {
                return authority > 0;
            }
            int freshness = Long.compare(this.signal.gameTime(), other.signal.gameTime());
            if (freshness != 0) {
                return freshness > 0;
            }
            return Long.compareUnsigned(this.source, other.source) < 0;
        }
    }
}
