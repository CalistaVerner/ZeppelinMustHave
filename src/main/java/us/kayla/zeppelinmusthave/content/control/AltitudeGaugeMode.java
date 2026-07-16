package us.kayla.zeppelinmusthave.content.control;

public enum AltitudeGaugeMode {
    ALTITUDE("goggles.altitude_gauge.mode.altitude"),
    VERTICAL_SPEED("goggles.altitude_gauge.mode.vertical_speed"),
    BALLOON_FILL("goggles.altitude_gauge.mode.balloon_fill"),
    ALTITUDE_HOLD("goggles.altitude_gauge.mode.altitude_hold");

    private final String translationKey;

    AltitudeGaugeMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return this.translationKey;
    }

    public AltitudeGaugeMode next() {
        AltitudeGaugeMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public static AltitudeGaugeMode parse(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return ALTITUDE;
        }
    }
}
