package us.kayla.zeppelinmusthave.content.upgrade;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerProfile;

/**
 * Multiplicative and additive modifiers contributed by installed upgrades.
 */
public record AirshipUpgradeModifiers(
        double gasOutputMultiplier,
        double fuelUseMultiplier,
        double fuelCapacityMultiplier,
        int castRangeAdd,
        double superheatedOutputMultiplier,
        double throttleExponentMultiplier
) {
    public static final AirshipUpgradeModifiers IDENTITY = new AirshipUpgradeModifiers(
            1.0D,
            1.0D,
            1.0D,
            0,
            1.0D,
            1.0D
    );

    public AirshipUpgradeModifiers {
        requirePositive("gas_output_multiplier", gasOutputMultiplier);
        requirePositive("fuel_use_multiplier", fuelUseMultiplier);
        requirePositive("fuel_capacity_multiplier", fuelCapacityMultiplier);
        if (castRangeAdd < -255 || castRangeAdd > 255) {
            throw new JsonParseException("cast_range_add must be in [-255, 255]");
        }
        requirePositive("superheated_output_multiplier", superheatedOutputMultiplier);
        requirePositive("throttle_exponent_multiplier", throttleExponentMultiplier);
    }

    public static AirshipUpgradeModifiers parse(JsonObject json) {
        return new AirshipUpgradeModifiers(
                GsonHelper.getAsDouble(json, "gas_output_multiplier", 1.0D),
                GsonHelper.getAsDouble(json, "fuel_use_multiplier", 1.0D),
                GsonHelper.getAsDouble(json, "fuel_capacity_multiplier", 1.0D),
                GsonHelper.getAsInt(json, "cast_range_add", 0),
                GsonHelper.getAsDouble(json, "superheated_output_multiplier", 1.0D),
                GsonHelper.getAsDouble(json, "throttle_exponent_multiplier", 1.0D)
        );
    }

    public AirshipUpgradeModifiers combine(AirshipUpgradeModifiers other) {
        return new AirshipUpgradeModifiers(
                this.gasOutputMultiplier * other.gasOutputMultiplier,
                this.fuelUseMultiplier * other.fuelUseMultiplier,
                this.fuelCapacityMultiplier * other.fuelCapacityMultiplier,
                Mth.clamp(this.castRangeAdd + other.castRangeAdd, -255, 255),
                this.superheatedOutputMultiplier * other.superheatedOutputMultiplier,
                this.throttleExponentMultiplier * other.throttleExponentMultiplier
        );
    }

    public AirshipBurnerProfile apply(AirshipBurnerProfile base) {
        int capacity = Mth.clamp(
                (int) Math.round(base.fuelCapacityTicks() * this.fuelCapacityMultiplier),
                1,
                1_728_000
        );
        int range = Mth.clamp(base.castRange() + this.castRangeAdd, 1, 256);

        return new AirshipBurnerProfile(
                base.id(),
                base.gasOutputMultiplier() * this.gasOutputMultiplier,
                base.fuelUsePerTickAtFullPower() * this.fuelUseMultiplier,
                capacity,
                range,
                base.superheatedOutputMultiplier() * this.superheatedOutputMultiplier,
                base.throttleExponent() * this.throttleExponentMultiplier
        );
    }

    public void write(CompoundTag tag) {
        tag.putDouble("UpgradeGasOutputMultiplier", this.gasOutputMultiplier);
        tag.putDouble("UpgradeFuelUseMultiplier", this.fuelUseMultiplier);
        tag.putDouble("UpgradeFuelCapacityMultiplier", this.fuelCapacityMultiplier);
        tag.putInt("UpgradeCastRangeAdd", this.castRangeAdd);
        tag.putDouble("UpgradeSuperheatedOutputMultiplier", this.superheatedOutputMultiplier);
        tag.putDouble("UpgradeThrottleExponentMultiplier", this.throttleExponentMultiplier);
    }

    public static AirshipUpgradeModifiers read(CompoundTag tag) {
        if (!tag.contains("UpgradeGasOutputMultiplier")) {
            return IDENTITY;
        }
        return new AirshipUpgradeModifiers(
                Math.max(0.01D, tag.getDouble("UpgradeGasOutputMultiplier")),
                Math.max(0.01D, tag.getDouble("UpgradeFuelUseMultiplier")),
                Math.max(0.01D, tag.getDouble("UpgradeFuelCapacityMultiplier")),
                Mth.clamp(tag.getInt("UpgradeCastRangeAdd"), -255, 255),
                Math.max(0.01D, tag.getDouble("UpgradeSuperheatedOutputMultiplier")),
                Math.max(0.01D, tag.getDouble("UpgradeThrottleExponentMultiplier"))
        );
    }

    private static void requirePositive(String name, double value) {
        if (!Double.isFinite(value) || value <= 0.0D) {
            throw new JsonParseException(name + " must be finite and greater than zero");
        }
    }
}
