package us.kayla.zeppelinmusthave.content.control;

final class AltitudeGaugeConfiguration {
    private AltitudeControlProfile profile = AltitudeControlProfile.unresolved(
            AltitudeControlProfiles.DEFAULT_ID
    );
    private long observedRevision = Long.MIN_VALUE;
    private int ticksUntilSample;

    AltitudeControlProfile profile() {
        return this.profile;
    }

    void resetSampling() {
        this.ticksUntilSample = 0;
    }

    boolean shouldSample() {
        if (this.ticksUntilSample > 0) {
            this.ticksUntilSample--;
            return false;
        }
        this.ticksUntilSample = Math.max(0, this.profile.sampleIntervalTicks() - 1);
        return true;
    }

    boolean refresh(boolean force) {
        long revision = AltitudeControlProfiles.INSTANCE.revision();
        if (!force && revision == this.observedRevision) {
            return false;
        }
        this.profile = AltitudeControlProfiles.INSTANCE.resolveDefault();
        this.observedRevision = revision;
        this.resetSampling();
        return true;
    }
}
