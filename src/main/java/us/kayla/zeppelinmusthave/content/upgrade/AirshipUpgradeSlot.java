package us.kayla.zeppelinmusthave.content.upgrade;

import com.google.gson.JsonParseException;

public enum AirshipUpgradeSlot {
    THERMAL("thermal", "upgrade_slot.thermal"),
    AIRFLOW("airflow", "upgrade_slot.airflow"),
    CONTROL("control", "upgrade_slot.control");

    private final String serializedName;
    private final String translationKey;

    AirshipUpgradeSlot(String serializedName, String translationKey) {
        this.serializedName = serializedName;
        this.translationKey = translationKey;
    }

    public String serializedName() {
        return this.serializedName;
    }

    public String translationKey() {
        return this.translationKey;
    }

    public static AirshipUpgradeSlot parse(String value) {
        for (AirshipUpgradeSlot slot : values()) {
            if (slot.serializedName.equals(value)) {
                return slot;
            }
        }
        throw new JsonParseException("Unknown airship upgrade slot: " + value);
    }
}
