package us.kayla.zeppelinmusthave.content.helm;

/** Owns the sampling cadence independently of the block entity lifecycle. */
final class AirshipHelmTelemetrySampler {
    private static final int SAMPLE_INTERVAL_TICKS = 5;
    private static final int FORCED_SYNC_INTERVAL_TICKS = 20;

    private int ticksUntilSample;

    void reset() {
        this.ticksUntilSample = 0;
    }

    boolean shouldSample() {
        if (this.ticksUntilSample > 0) {
            this.ticksUntilSample--;
            return false;
        }
        this.ticksUntilSample = SAMPLE_INTERVAL_TICKS - 1;
        return true;
    }

    boolean shouldForceSync(long gameTime) {
        return gameTime % FORCED_SYNC_INTERVAL_TICKS == 0L;
    }
}
