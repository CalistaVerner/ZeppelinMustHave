package us.kayla.zeppelinmusthave.content.control.fcn;

import java.util.Locale;

public enum EngineTelegraphOrder {
    FULL_ASTERN(-15),
    HALF_ASTERN(-10),
    SLOW_ASTERN(-5),
    STOP(0),
    SLOW_AHEAD(5),
    HALF_AHEAD(10),
    FULL_AHEAD(15);

    private final int command;

    EngineTelegraphOrder(int command) {
        this.command = command;
    }

    public int command() {
        return this.command;
    }

    public String translationKey() {
        return "zeppelin_must_have.fcn.telegraph." + this.name().toLowerCase(Locale.ROOT);
    }

    public EngineTelegraphOrder next() {
        EngineTelegraphOrder[] values = values();
        return values[Math.min(this.ordinal() + 1, values.length - 1)];
    }

    public EngineTelegraphOrder previous() {
        EngineTelegraphOrder[] values = values();
        return values[Math.max(this.ordinal() - 1, 0)];
    }

    public static EngineTelegraphOrder byIndex(int index) {
        EngineTelegraphOrder[] values = values();
        return values[Math.clamp(index, 0, values.length - 1)];
    }
}
