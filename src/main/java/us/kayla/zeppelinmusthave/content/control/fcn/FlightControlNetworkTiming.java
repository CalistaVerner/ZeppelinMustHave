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

    /**
     * Treat timestamps from a future clock (or an uninitialized state) as
     * expired. This prevents stale control frames surviving a time reset.
     */
    static boolean isExpired(long now, long lastSeen, long timeoutTicks) {
        return lastSeen == Long.MIN_VALUE
                || now < lastSeen
                || now - lastSeen > timeoutTicks;
    }

    /** Maintenance must run again after a clock rollback, not be postponed. */
    static boolean isPruneDue(long now, long lastPruned, long intervalTicks) {
        return lastPruned == Long.MIN_VALUE
                || now < lastPruned
                || now - lastPruned >= intervalTicks;
    }
}
