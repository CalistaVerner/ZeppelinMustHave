package us.kayla.zeppelinmusthave.content.steam;

import net.minecraft.util.StringRepresentable;

public enum LeviathanSteamEnginePart implements StringRepresentable {
    CONTROLLER("controller"),
    LEFT_CYLINDER("left_cylinder"),
    RIGHT_CYLINDER("right_cylinder"),
    SHAFT_NOSE("shaft_nose");

    private final String serializedName;

    LeviathanSteamEnginePart(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
