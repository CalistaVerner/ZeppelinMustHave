package us.kayla.zeppelinmusthave.content.burner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

import java.util.Objects;

/**
 * Immutable tuning profile for an Airship Burner.
 *
 * <p>Gameplay values are loaded from data packs. The Java type only defines
 * validation, interpolation, and synchronization semantics.</p>
 */
public record AirshipBurnerProfile(
        ResourceLocation id,
        double gasOutputMultiplier,
        double fuelUsePerTickAtFullPower,
        int fuelCapacityTicks,
        int castRange,
        double superheatedOutputMultiplier,
        double throttleExponent
) {
    private static final int CURRENT_SCHEMA_VERSION = 1;

    public AirshipBurnerProfile {
        Objects.requireNonNull(id, "id");
        requireFinitePositive("gas_output_multiplier", gasOutputMultiplier, false);
        requireFinitePositive("fuel_use_per_tick_at_full_power", fuelUsePerTickAtFullPower, false);
        requireRange("fuel_capacity_ticks", fuelCapacityTicks, 1, 1_728_000);
        requireRange("cast_range", castRange, 1, 256);
        requireFinitePositive("superheated_output_multiplier", superheatedOutputMultiplier, true);
        requireFinitePositive("throttle_exponent", throttleExponent, true);
    }

    public static AirshipBurnerProfile parse(ResourceLocation id, JsonObject json) {
        int schemaVersion = GsonHelper.getAsInt(json, "schema_version", CURRENT_SCHEMA_VERSION);
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            throw new JsonParseException(
                    "Unsupported Airship Burner profile schema " + schemaVersion
                            + " for " + id + "; expected " + CURRENT_SCHEMA_VERSION
            );
        }

        return new AirshipBurnerProfile(
                id,
                GsonHelper.getAsDouble(json, "gas_output_multiplier"),
                GsonHelper.getAsDouble(json, "fuel_use_per_tick_at_full_power"),
                GsonHelper.getAsInt(json, "fuel_capacity_ticks"),
                GsonHelper.getAsInt(json, "cast_range"),
                GsonHelper.getAsDouble(json, "superheated_output_multiplier", 1.0D),
                GsonHelper.getAsDouble(json, "throttle_exponent", 1.0D)
        );
    }

    /**
     * Converts the standard redstone range into the profile's throttle curve.
     */
    public double throttleForSignal(int signalStrength) {
        double normalized = Mth.clamp(signalStrength, 0, 15) / 15.0D;
        return Math.pow(normalized, this.throttleExponent);
    }

    public double outputMultiplier(boolean superheated) {
        return this.gasOutputMultiplier
                * (superheated ? this.superheatedOutputMultiplier : 1.0D);
    }

    public int clampFuelTicks(int fuelTicks) {
        return Mth.clamp(fuelTicks, 0, this.fuelCapacityTicks);
    }

    public void writeClientSnapshot(CompoundTag tag) {
        tag.putString("ProfileId", this.id.toString());
        tag.putDouble("GasOutputMultiplier", this.gasOutputMultiplier);
        tag.putDouble("FuelUsePerTick", this.fuelUsePerTickAtFullPower);
        tag.putInt("FuelCapacityTicks", this.fuelCapacityTicks);
        tag.putInt("CastRange", this.castRange);
        tag.putDouble("SuperheatedOutputMultiplier", this.superheatedOutputMultiplier);
        tag.putDouble("ThrottleExponent", this.throttleExponent);
    }

    public static AirshipBurnerProfile readClientSnapshot(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("ProfileId"));
        if (id == null) {
            id = ResourceLocation.fromNamespaceAndPath("zeppelin_must_have", "unresolved");
        }

        return new AirshipBurnerProfile(
                id,
                Math.max(0.0D, tag.getDouble("GasOutputMultiplier")),
                Math.max(0.0D, tag.getDouble("FuelUsePerTick")),
                Math.max(1, tag.getInt("FuelCapacityTicks")),
                Mth.clamp(tag.getInt("CastRange"), 1, 256),
                Math.max(0.01D, tag.getDouble("SuperheatedOutputMultiplier")),
                Math.max(0.01D, tag.getDouble("ThrottleExponent"))
        );
    }

    /**
     * Fail-closed profile used only before resource loading or after invalid data.
     * It cannot generate lift or consume fuel.
     */
    public static AirshipBurnerProfile unresolved(ResourceLocation id) {
        return new AirshipBurnerProfile(id, 0.0D, 0.0D, 1, 1, 1.0D, 1.0D);
    }

    private static void requireFinitePositive(String name, double value, boolean strictlyPositive) {
        if (!Double.isFinite(value) || (strictlyPositive ? value <= 0.0D : value < 0.0D)) {
            throw new JsonParseException(name + " has invalid value " + value);
        }
    }

    private static void requireRange(String name, int value, int minimum, int maximum) {
        if (value < minimum || value > maximum) {
            throw new JsonParseException(
                    name + " must be in [" + minimum + ", " + maximum + "], got " + value
            );
        }
    }
}
