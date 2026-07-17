package us.kayla.zeppelinmusthave.client.renderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeTier;

public final class SteamEngineGradePartialModels {
    private static final PartialModel COPPER_PISTON = model(SteamEngineGradeTier.COPPER, "piston");
    private static final PartialModel COPPER_LINKAGE = model(SteamEngineGradeTier.COPPER, "linkage");
    private static final PartialModel COPPER_CONNECTOR = model(SteamEngineGradeTier.COPPER, "shaft_connector");

    private static final PartialModel BRASS_PISTON = model(SteamEngineGradeTier.BRASS, "piston");
    private static final PartialModel BRASS_LINKAGE = model(SteamEngineGradeTier.BRASS, "linkage");
    private static final PartialModel BRASS_CONNECTOR = model(SteamEngineGradeTier.BRASS, "shaft_connector");

    private static final PartialModel INDUSTRIAL_PISTON = model(SteamEngineGradeTier.INDUSTRIAL, "piston");
    private static final PartialModel INDUSTRIAL_LINKAGE = model(SteamEngineGradeTier.INDUSTRIAL, "linkage");
    private static final PartialModel INDUSTRIAL_CONNECTOR = model(SteamEngineGradeTier.INDUSTRIAL, "shaft_connector");

    private SteamEngineGradePartialModels() {
    }

    /** Forces class initialization before model baking. */
    public static void init() {
    }

    public static PartialModel piston(SteamEngineGradeTier tier) {
        return switch (tier) {
            case COPPER -> COPPER_PISTON;
            case BRASS -> BRASS_PISTON;
            case INDUSTRIAL -> INDUSTRIAL_PISTON;
        };
    }

    public static PartialModel linkage(SteamEngineGradeTier tier) {
        return switch (tier) {
            case COPPER -> COPPER_LINKAGE;
            case BRASS -> BRASS_LINKAGE;
            case INDUSTRIAL -> INDUSTRIAL_LINKAGE;
        };
    }

    public static PartialModel connector(SteamEngineGradeTier tier) {
        return switch (tier) {
            case COPPER -> COPPER_CONNECTOR;
            case BRASS -> BRASS_CONNECTOR;
            case INDUSTRIAL -> INDUSTRIAL_CONNECTOR;
        };
    }

    private static PartialModel model(SteamEngineGradeTier tier, String part) {
        return PartialModel.of(tier.partialModel(part));
    }
}
