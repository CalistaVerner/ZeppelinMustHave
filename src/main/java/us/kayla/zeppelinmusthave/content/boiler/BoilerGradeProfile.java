package us.kayla.zeppelinmusthave.content.boiler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Objects;

public record BoilerGradeProfile(
        ResourceLocation id,
        double heatMultiplier,
        double additiveHeat,
        int maximumHeatOutput
) {
    private static final int CURRENT_SCHEMA_VERSION = 1;

    public BoilerGradeProfile {
        Objects.requireNonNull(id, "id");
        requirePositive("heat_multiplier", heatMultiplier);
        requireNonNegative("additive_heat", additiveHeat);
        if (maximumHeatOutput < 1 || maximumHeatOutput > 18) {
            throw new JsonParseException("maximum_heat_output must be in [1, 18]");
        }
    }

    public static BoilerGradeProfile parse(ResourceLocation id, JsonObject json) {
        int schema = GsonHelper.getAsInt(json, "schema_version", CURRENT_SCHEMA_VERSION);
        if (schema != CURRENT_SCHEMA_VERSION) {
            throw new JsonParseException(
                    "Unsupported boiler grade profile schema " + schema
                            + " for " + id + "; expected " + CURRENT_SCHEMA_VERSION
            );
        }

        return new BoilerGradeProfile(
                id,
                GsonHelper.getAsDouble(json, "heat_multiplier"),
                GsonHelper.getAsDouble(json, "additive_heat", 0.0D),
                GsonHelper.getAsInt(json, "maximum_heat_output")
        );
    }

    public int transfer(float sourceHeat) {
        if (sourceHeat < 0.0F) {
            return com.simibubi.create.api.boiler.BoilerHeater.NO_HEAT;
        }
        if (sourceHeat == com.simibubi.create.api.boiler.BoilerHeater.PASSIVE_HEAT) {
            return com.simibubi.create.api.boiler.BoilerHeater.PASSIVE_HEAT;
        }

        int transferred = (int) Math.round(sourceHeat * this.heatMultiplier + this.additiveHeat);
        return Math.clamp(transferred, 1, this.maximumHeatOutput);
    }

    public void writeClientSnapshot(CompoundTag tag) {
        tag.putString("BoilerGradeProfileId", this.id.toString());
        tag.putDouble("BoilerHeatMultiplier", this.heatMultiplier);
        tag.putDouble("BoilerAdditiveHeat", this.additiveHeat);
        tag.putInt("BoilerMaximumHeatOutput", this.maximumHeatOutput);
    }

    public static BoilerGradeProfile readClientSnapshot(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("BoilerGradeProfileId"));
        if (id == null) {
            id = ResourceLocation.fromNamespaceAndPath("zeppelin_must_have", "unresolved");
        }
        return new BoilerGradeProfile(
                id,
                Math.max(0.01D, tag.getDouble("BoilerHeatMultiplier")),
                Math.max(0.0D, tag.getDouble("BoilerAdditiveHeat")),
                Math.clamp(tag.getInt("BoilerMaximumHeatOutput"), 1, 18)
        );
    }

    public static BoilerGradeProfile unresolved(ResourceLocation id) {
        return new BoilerGradeProfile(id, 1.0D, 0.0D, 1);
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
