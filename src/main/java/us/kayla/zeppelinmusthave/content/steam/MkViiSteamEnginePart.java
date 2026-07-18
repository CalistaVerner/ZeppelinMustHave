package us.kayla.zeppelinmusthave.content.steam;

import net.minecraft.util.StringRepresentable;

public enum MkViiSteamEnginePart implements StringRepresentable {
    LEFT("left", 0),
    CONTROLLER("controller", 1),
    RIGHT("right", 2);

    private final String serializedName;
    private final int bankIndex;

    MkViiSteamEnginePart(String serializedName, int bankIndex) {
        this.serializedName = serializedName;
        this.bankIndex = bankIndex;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public int bankIndex() {
        return this.bankIndex;
    }
}
