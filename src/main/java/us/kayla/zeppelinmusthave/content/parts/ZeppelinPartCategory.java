package us.kayla.zeppelinmusthave.content.parts;

public enum ZeppelinPartCategory {
    FLIGHT_CONTROL("flight_control", true),
    LIFT("lift", true),
    STEAM_POWER("steam_power", true),
    FLUID_SYSTEMS("fluid_systems", true),
    REDSTONE_CONTROL("redstone_control", true),
    BALLAST("ballast", true),
    MOORING("mooring", true),
    PROPULSION("propulsion", true),
    UPGRADE("upgrade", false);

    private final String path;
    private final boolean blockCategory;

    ZeppelinPartCategory(String path, boolean blockCategory) {
        this.path = path;
        this.blockCategory = blockCategory;
    }

    public String path() {
        return this.path;
    }

    public boolean supportsBlocks() {
        return this.blockCategory;
    }

    public String translationKey() {
        return "zeppelin_must_have.zeppelin_part.category." + this.path;
    }
}
