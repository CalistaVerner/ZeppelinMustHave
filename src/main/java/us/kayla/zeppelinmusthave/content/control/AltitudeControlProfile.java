package us.kayla.zeppelinmusthave.content.control;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Objects;

public record AltitudeControlProfile(
        ResourceLocation id,
        int sampleIntervalTicks,
        double verticalSpeedFullScale,
        double holdDeadbandBlocks,
        double holdProportionalGain,
        double holdVerticalDampingGain,
        double holdMaximumCorrection,
        int maximumSignalStep
) {
    private static final int CURRENT_SCHEMA_VERSION = 1;

    public AltitudeControlProfile {
        Objects.requireNonNull(id, "id");
        requireRange("sample_interval_ticks", sampleIntervalTicks, 1, 40);
        requirePositive("vertical_speed_full_scale", verticalSpeedFullScale);
        requireNonNegative("hold_deadband_blocks", holdDeadbandBlocks);
        requirePositive("hold_proportional_gain", holdProportionalGain);
        requireNonNegative("hold_vertical_damping_gain", holdVerticalDampingGain);
        requirePositive("hold_maximum_correction", holdMaximumCorrection);
        requireRange("maximum_signal_step", maximumSignalStep, 1, 15);
    }

    public static AltitudeControlProfile parse(ResourceLocation id, JsonObject json) {
        int schema = GsonHelper.getAsInt(json, "schema_version", CURRENT_SCHEMA_VERSION);
        if (schema != CURRENT_SCHEMA_VERSION) {
            throw new JsonParseException(
                    "Unsupported altitude control profile schema " + schema
                            + " for " + id + "; expected " + CURRENT_SCHEMA_VERSION
            );
        }

        return new AltitudeControlProfile(
                id,
                GsonHelper.getAsInt(json, "sample_interval_ticks"),
                GsonHelper.getAsDouble(json, "vertical_speed_full_scale"),
                GsonHelper.getAsDouble(json, "hold_deadband_blocks"),
                GsonHelper.getAsDouble(json, "hold_proportional_gain"),
                GsonHelper.getAsDouble(json, "hold_vertical_damping_gain"),
                GsonHelper.getAsDouble(json, "hold_maximum_correction"),
                GsonHelper.getAsInt(json, "maximum_signal_step")
        );
    }

    public static AltitudeControlProfile unresolved(ResourceLocation id) {
        return new AltitudeControlProfile(
                id,
                20,
                1.0D,
                0.5D,
                0.01D,
                0.0D,
                1.0D,
                1
        );
    }

    private static void requireRange(String name, int value, int minimum, int maximum) {
        if (value < minimum || value > maximum) {
            throw new JsonParseException(name + " must be in [" + minimum + ", " + maximum + "]");
        }
    }

    private static void requirePositive(String name, double value) {
        if (!Double.isFinite(value) || value <= 0.0D) {
            throw new JsonParseException(name + " must be finite and greater than zero");
        }
    }

    private static void requireNonNegative(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new JsonParseException(name + " must be finite and non-negative");
        }
    }
}
