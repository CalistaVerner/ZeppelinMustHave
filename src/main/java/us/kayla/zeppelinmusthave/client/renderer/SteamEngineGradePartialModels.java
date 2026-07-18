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

    private static final PartialModel GRAND_PISTON = model(SteamEngineGradeTier.GRAND, "piston");
    private static final PartialModel GRAND_LINKAGE = model(SteamEngineGradeTier.GRAND, "linkage");
    private static final PartialModel GRAND_CONNECTOR = model(SteamEngineGradeTier.GRAND, "shaft_connector");
    private static final PartialModel GRAND_FLYWHEEL = model(SteamEngineGradeTier.GRAND, "flywheel");
    private static final PartialModel GRAND_COUNTER_FLYWHEEL = model(SteamEngineGradeTier.GRAND, "counter_flywheel");
    private static final PartialModel GRAND_GOVERNOR = model(SteamEngineGradeTier.GRAND, "governor");
    private static final PartialModel GRAND_VALVE_GEAR = model(SteamEngineGradeTier.GRAND, "valve_gear");

    private static final PartialModel SOVEREIGN_PISTON = model(SteamEngineGradeTier.SOVEREIGN, "piston");
    private static final PartialModel SOVEREIGN_LINKAGE = model(SteamEngineGradeTier.SOVEREIGN, "linkage");
    private static final PartialModel SOVEREIGN_CONNECTOR = model(SteamEngineGradeTier.SOVEREIGN, "shaft_connector");
    private static final PartialModel SOVEREIGN_FLYWHEEL = model(SteamEngineGradeTier.SOVEREIGN, "flywheel");
    private static final PartialModel SOVEREIGN_COUNTER_FLYWHEEL = model(SteamEngineGradeTier.SOVEREIGN, "counter_flywheel");
    private static final PartialModel SOVEREIGN_GOVERNOR = model(SteamEngineGradeTier.SOVEREIGN, "governor");
    private static final PartialModel SOVEREIGN_VALVE_GEAR = model(SteamEngineGradeTier.SOVEREIGN, "valve_gear");
    private static final PartialModel SOVEREIGN_POWER_CORE = model(SteamEngineGradeTier.SOVEREIGN, "power_core");
    private static final PartialModel SOVEREIGN_CROWN_ROTOR = model(SteamEngineGradeTier.SOVEREIGN, "crown_rotor");

    private static final PartialModel LEVIATHAN_PISTON = model(SteamEngineGradeTier.LEVIATHAN, "piston");
    private static final PartialModel LEVIATHAN_LINKAGE = model(SteamEngineGradeTier.LEVIATHAN, "linkage");
    private static final PartialModel LEVIATHAN_CONNECTOR = model(SteamEngineGradeTier.LEVIATHAN, "shaft_connector");
    private static final PartialModel LEVIATHAN_FLYWHEEL = model(SteamEngineGradeTier.LEVIATHAN, "flywheel");
    private static final PartialModel LEVIATHAN_COUNTER_FLYWHEEL = model(SteamEngineGradeTier.LEVIATHAN, "counter_flywheel");
    private static final PartialModel LEVIATHAN_GOVERNOR = model(SteamEngineGradeTier.LEVIATHAN, "governor");
    private static final PartialModel LEVIATHAN_VALVE_GEAR = model(SteamEngineGradeTier.LEVIATHAN, "valve_gear");
    private static final PartialModel LEVIATHAN_POWER_CORE = model(SteamEngineGradeTier.LEVIATHAN, "power_core");
    private static final PartialModel LEVIATHAN_CROWN_ROTOR = model(SteamEngineGradeTier.LEVIATHAN, "crown_rotor");

    private static final PartialModel MK_VII_PISTON = model(SteamEngineGradeTier.MK_VII, "piston");
    private static final PartialModel MK_VII_LINKAGE = model(SteamEngineGradeTier.MK_VII, "linkage");
    private static final PartialModel MK_VII_CONNECTOR = model(SteamEngineGradeTier.MK_VII, "shaft_connector");
    private static final PartialModel MK_VII_FLYWHEEL = model(SteamEngineGradeTier.MK_VII, "flywheel");
    private static final PartialModel MK_VII_COUNTER_FLYWHEEL = model(SteamEngineGradeTier.MK_VII, "counter_flywheel");
    private static final PartialModel MK_VII_GOVERNOR = model(SteamEngineGradeTier.MK_VII, "governor");
    private static final PartialModel MK_VII_VALVE_GEAR = model(SteamEngineGradeTier.MK_VII, "valve_gear");
    private static final PartialModel MK_VII_POWER_CORE = model(SteamEngineGradeTier.MK_VII, "power_core");
    private static final PartialModel MK_VII_CROWN_ROTOR = model(SteamEngineGradeTier.MK_VII, "crown_rotor");

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
            case GRAND -> GRAND_PISTON;
            case SOVEREIGN -> SOVEREIGN_PISTON;
            case LEVIATHAN -> LEVIATHAN_PISTON;
            case MK_VII -> MK_VII_PISTON;
        };
    }

    public static PartialModel linkage(SteamEngineGradeTier tier) {
        return switch (tier) {
            case COPPER -> COPPER_LINKAGE;
            case BRASS -> BRASS_LINKAGE;
            case INDUSTRIAL -> INDUSTRIAL_LINKAGE;
            case GRAND -> GRAND_LINKAGE;
            case SOVEREIGN -> SOVEREIGN_LINKAGE;
            case LEVIATHAN -> LEVIATHAN_LINKAGE;
            case MK_VII -> MK_VII_LINKAGE;
        };
    }

    public static PartialModel connector(SteamEngineGradeTier tier) {
        return switch (tier) {
            case COPPER -> COPPER_CONNECTOR;
            case BRASS -> BRASS_CONNECTOR;
            case INDUSTRIAL -> INDUSTRIAL_CONNECTOR;
            case GRAND -> GRAND_CONNECTOR;
            case SOVEREIGN -> SOVEREIGN_CONNECTOR;
            case LEVIATHAN -> LEVIATHAN_CONNECTOR;
            case MK_VII -> MK_VII_CONNECTOR;
        };
    }

    public static PartialModel flagshipFlywheel(SteamEngineGradeTier tier) {
        return switch (tier) {
            case MK_VII -> MK_VII_FLYWHEEL;
            case LEVIATHAN -> LEVIATHAN_FLYWHEEL;
            case SOVEREIGN -> SOVEREIGN_FLYWHEEL;
            default -> GRAND_FLYWHEEL;
        };
    }

    public static PartialModel flagshipCounterFlywheel(SteamEngineGradeTier tier) {
        return switch (tier) {
            case MK_VII -> MK_VII_COUNTER_FLYWHEEL;
            case LEVIATHAN -> LEVIATHAN_COUNTER_FLYWHEEL;
            case SOVEREIGN -> SOVEREIGN_COUNTER_FLYWHEEL;
            default -> GRAND_COUNTER_FLYWHEEL;
        };
    }

    public static PartialModel flagshipGovernor(SteamEngineGradeTier tier) {
        return switch (tier) {
            case MK_VII -> MK_VII_GOVERNOR;
            case LEVIATHAN -> LEVIATHAN_GOVERNOR;
            case SOVEREIGN -> SOVEREIGN_GOVERNOR;
            default -> GRAND_GOVERNOR;
        };
    }

    public static PartialModel flagshipValveGear(SteamEngineGradeTier tier) {
        return switch (tier) {
            case MK_VII -> MK_VII_VALVE_GEAR;
            case LEVIATHAN -> LEVIATHAN_VALVE_GEAR;
            case SOVEREIGN -> SOVEREIGN_VALVE_GEAR;
            default -> GRAND_VALVE_GEAR;
        };
    }

    public static PartialModel flagshipPowerCore(SteamEngineGradeTier tier) {
        return switch (tier) {
            case MK_VII -> MK_VII_POWER_CORE;
            case LEVIATHAN -> LEVIATHAN_POWER_CORE;
            default -> SOVEREIGN_POWER_CORE;
        };
    }

    public static PartialModel flagshipCrownRotor(SteamEngineGradeTier tier) {
        return switch (tier) {
            case MK_VII -> MK_VII_CROWN_ROTOR;
            case LEVIATHAN -> LEVIATHAN_CROWN_ROTOR;
            default -> SOVEREIGN_CROWN_ROTOR;
        };
    }

    private static PartialModel model(SteamEngineGradeTier tier, String part) {
        return PartialModel.of(tier.partialModel(part));
    }
}
