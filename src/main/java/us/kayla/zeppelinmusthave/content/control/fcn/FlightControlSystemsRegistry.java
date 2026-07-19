package us.kayla.zeppelinmusthave.content.control.fcn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Aggregates short-lived vessel system telemetry independently from control routing. */
final class FlightControlSystemsRegistry {
    private final Map<Long, TimedSystemStatus> systems = new HashMap<>();

    void report(long position, FlightSystemStatus status, long gameTime) {
        this.systems.put(position, new TimedSystemStatus(status, gameTime));
    }

    FlightSystemsSnapshot sample(long gameTime) {
        this.prune(gameTime);
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

    boolean isIdle(long gameTime) {
        this.prune(gameTime);
        return this.systems.isEmpty();
    }

    private void prune(long gameTime) {
        Iterator<TimedSystemStatus> iterator = this.systems.values().iterator();
        while (iterator.hasNext()) {
            if (gameTime - iterator.next().gameTime() > FlightControlNetworkTiming.SYSTEM_TIMEOUT_TICKS) {
                iterator.remove();
            }
        }
    }

    private record TimedSystemStatus(FlightSystemStatus status, long gameTime) {
    }
}
