package us.kayla.zeppelinmusthave.content.upgrade;

import com.google.gson.JsonParseException;

public enum AirshipUpgradeTarget {
    AIRSHIP_BURNER("airship_burner");

    private final String serializedName;

    AirshipUpgradeTarget(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return this.serializedName;
    }

    public static AirshipUpgradeTarget parse(String value) {
        for (AirshipUpgradeTarget target : values()) {
            if (target.serializedName.equals(value)) {
                return target;
            }
        }
        throw new JsonParseException("Unknown airship upgrade target: " + value);
    }
}
