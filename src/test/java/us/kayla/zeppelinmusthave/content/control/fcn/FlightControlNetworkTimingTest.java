package us.kayla.zeppelinmusthave.content.control.fcn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlightControlNetworkTimingTest {

    @Test
    void expiresOnlyAfterTimeout() {
        assertFalse(FlightControlNetworkTiming.isExpired(120L, 100L, 20L));
        assertTrue(FlightControlNetworkTiming.isExpired(121L, 100L, 20L));
    }

    @Test
    void ignoresFutureAndUninitializedTimestamps() {
        assertTrue(FlightControlNetworkTiming.isExpired(99L, 100L, 20L));
        assertTrue(FlightControlNetworkTiming.isExpired(100L, Long.MIN_VALUE, 20L));
    }

    @Test
    void cacheMaintenanceRunsImmediatelyWhenClockResets() {
        assertTrue(FlightControlNetworkTiming.isPruneDue(1L, Long.MIN_VALUE, 100L));
        assertTrue(FlightControlNetworkTiming.isPruneDue(99L, 100L, 100L));
        assertFalse(FlightControlNetworkTiming.isPruneDue(199L, 100L, 100L));
        assertTrue(FlightControlNetworkTiming.isPruneDue(200L, 100L, 100L));
    }
}
