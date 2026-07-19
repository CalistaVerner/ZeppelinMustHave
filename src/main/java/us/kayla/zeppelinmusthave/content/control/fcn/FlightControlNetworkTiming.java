package us.kayla.zeppelinmusthave.content.control.fcn;

/** Central timeout and cache-maintenance policy for the Flight Control Network. */
final class FlightControlNetworkTiming {
    static final long INPUT_TIMEOUT_TICKS = 40L;
    static final long COMPUTER_TIMEOUT_TICKS = 20L;
    static final long OUTPUT_TIMEOUT_TICKS = 20L;
    static final long SYSTEM_TIMEOUT_TICKS = 40L;
    static final long NETWORK_PRUNE_INTERVAL_TICKS = 100L;
    static final long VESSEL_PRUNE_INTERVAL_TICKS = 200L;

    private FlightControlNetworkTiming() {
    }
}
