package us.kayla.zeppelinmusthave.content.control;

import us.kayla.zeppelinmusthave.content.helm.AirshipFlightSnapshot;

final class AltitudeGaugeRuntimeState {
    private AltitudeGaugeMode mode = AltitudeGaugeMode.ALTITUDE;
    private boolean altitudeHoldEnabled;
    private double targetAltitude;
    private int trimInput;
    private int outputSignal;
    private AirshipFlightSnapshot snapshot = AirshipFlightSnapshot.detached(0L);

    AltitudeGaugeMode mode() {
        return this.mode;
    }

    void cycleMode() {
        this.mode = this.mode.next();
        if (this.mode != AltitudeGaugeMode.ALTITUDE_HOLD) {
            this.altitudeHoldEnabled = false;
        }
    }

    boolean toggleHoldAtCurrentAltitude() {
        if (!this.snapshot.attached()) {
            return false;
        }
        if (this.mode != AltitudeGaugeMode.ALTITUDE_HOLD) {
            this.mode = AltitudeGaugeMode.ALTITUDE_HOLD;
            this.altitudeHoldEnabled = true;
            this.targetAltitude = this.snapshot.worldY();
        } else if (this.altitudeHoldEnabled) {
            this.altitudeHoldEnabled = false;
        } else {
            this.altitudeHoldEnabled = true;
            this.targetAltitude = this.snapshot.worldY();
        }
        return true;
    }

    boolean altitudeHoldEnabled() {
        return this.altitudeHoldEnabled;
    }

    double targetAltitude() {
        return this.targetAltitude;
    }

    int trimInput() {
        return this.trimInput;
    }

    void setTrimInput(int trimInput) {
        this.trimInput = Math.clamp(trimInput, 0, 15);
    }

    int outputSignal() {
        return this.outputSignal;
    }

    void setOutputSignal(int outputSignal) {
        this.outputSignal = Math.clamp(outputSignal, 0, 15);
    }

    AirshipFlightSnapshot snapshot() {
        return this.snapshot;
    }

    void setSnapshot(AirshipFlightSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    PersistentState persistentState() {
        return new PersistentState(
                this.mode,
                this.altitudeHoldEnabled,
                this.targetAltitude,
                this.trimInput,
                this.outputSignal,
                this.snapshot
        );
    }

    void restore(PersistentState state) {
        this.mode = state.mode();
        this.altitudeHoldEnabled = state.altitudeHoldEnabled();
        this.targetAltitude = state.targetAltitude();
        this.trimInput = Math.clamp(state.trimInput(), 0, 15);
        this.outputSignal = Math.clamp(state.outputSignal(), 0, 15);
        this.snapshot = state.snapshot();
    }

    record PersistentState(
            AltitudeGaugeMode mode,
            boolean altitudeHoldEnabled,
            double targetAltitude,
            int trimInput,
            int outputSignal,
            AirshipFlightSnapshot snapshot
    ) {
    }
}
