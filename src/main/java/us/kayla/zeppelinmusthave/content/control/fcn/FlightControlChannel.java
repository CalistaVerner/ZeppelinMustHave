package us.kayla.zeppelinmusthave.content.control.fcn;

import java.util.Locale;

/** Independent command lanes carried by a Flight Control Network. */
public enum FlightControlChannel {
    LIFT(false, 0, 15),
    VERTICAL_THRUST(true, -15, 15),
    PITCH_TRIM(true, -15, 15),
    ROLL_TRIM(true, -15, 15),
    YAW_TRIM(true, -15, 15),
    ENGINE_THROTTLE(true, -15, 15),
    EMERGENCY_STOP(false, 0, 1);

    private static final int[] SIGNED_ANALOG_COMMANDS = {
            -15, -13, -11, -9, -7, -5, -3, -1,
            0, 2, 4, 6, 9, 11, 13, 15
    };

    private final boolean signed;
    private final int minimum;
    private final int maximum;

    FlightControlChannel(boolean signed, int minimum, int maximum) {
        this.signed = signed;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public boolean signed() {
        return this.signed;
    }

    public int clamp(int value) {
        return Math.clamp(value, this.minimum, this.maximum);
    }

    /** Signed channels use bipolar redstone: 0=-15, 8=neutral, 15=+15. */
    public int toAnalogSignal(int value) {
        int clamped = this.clamp(value);
        if (!this.signed) {
            return Math.clamp(clamped, 0, 15);
        }
        int bestSignal = 8;
        int bestError = Integer.MAX_VALUE;
        for (int signal = 0; signal < SIGNED_ANALOG_COMMANDS.length; signal++) {
            int error = Math.abs(SIGNED_ANALOG_COMMANDS[signal] - clamped);
            if (error < bestError
                    || error == bestError && Math.abs(signal - 8) < Math.abs(bestSignal - 8)) {
                bestSignal = signal;
                bestError = error;
            }
        }
        return bestSignal;
    }

    public int fromAnalogSignal(int signal) {
        int clamped = Math.clamp(signal, 0, 15);
        if (!this.signed) {
            return this.clamp(clamped);
        }
        return SIGNED_ANALOG_COMMANDS[clamped];
    }

    public String translationKey() {
        return "zeppelin_must_have.fcn.channel." + this.name().toLowerCase(Locale.ROOT);
    }

    public FlightControlChannel next() {
        FlightControlChannel[] values = values();
        return values[(this.ordinal() + 1) % (values.length - 1)];
    }
}
