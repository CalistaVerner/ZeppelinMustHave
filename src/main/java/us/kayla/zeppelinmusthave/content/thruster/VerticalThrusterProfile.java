package us.kayla.zeppelinmusthave.content.thruster;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Objects;

public record VerticalThrusterProfile(
        ResourceLocation id,
        double thrustScaling,
        double airflowScaling,
        float radius,
        double stressImpact
) {
    private static final int CURRENT_SCHEMA_VERSION = 1;

    public VerticalThrusterProfile {
        Objects.requireNonNull(id, "id");
        requireRange("thrust_scaling", thrustScaling, 0.0D, 100.0D);
        requireRange("airflow_scaling", airflowScaling, 0.0D, 10.0D);
        requireRange("radius", radius, 0.1D, 8.0D);
        requireRange("stress_impact", stressImpact, 0.0D, 1024.0D);
    }

    public static VerticalThrusterProfile parse(ResourceLocation id, JsonObject json) {
        int schema = GsonHelper.getAsInt(json, "schema_version", CURRENT_SCHEMA_VERSION);
        if (schema != CURRENT_SCHEMA_VERSION) {
            throw new JsonParseException("Unsupported vertical thruster profile schema " + schema + " for " + id);
        }
        return new VerticalThrusterProfile(
                id,
                GsonHelper.getAsDouble(json, "thrust_scaling"),
                GsonHelper.getAsDouble(json, "airflow_scaling"),
                GsonHelper.getAsFloat(json, "radius"),
                GsonHelper.getAsDouble(json, "stress_impact")
        );
    }

    public static VerticalThrusterProfile unresolved(ResourceLocation id) {
        return new VerticalThrusterProfile(id, 0.0D, 0.0D, 1.0F, 8.0D);
    }

    public void writeClientSnapshot(CompoundTag tag) {
        tag.putString("VerticalThrusterProfileId", this.id.toString());
        tag.putDouble("VerticalThrusterThrust", this.thrustScaling);
        tag.putDouble("VerticalThrusterAirflow", this.airflowScaling);
        tag.putFloat("VerticalThrusterRadius", this.radius);
        tag.putDouble("VerticalThrusterStress", this.stressImpact);
    }

    public static VerticalThrusterProfile readClientSnapshot(CompoundTag tag, ResourceLocation fallbackId) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("VerticalThrusterProfileId"));
        if (id == null) {
            id = fallbackId;
        }
        return new VerticalThrusterProfile(
                id,
                Math.clamp(tag.getDouble("VerticalThrusterThrust"), 0.0D, 100.0D),
                Math.clamp(tag.getDouble("VerticalThrusterAirflow"), 0.0D, 10.0D),
                Math.clamp(tag.getFloat("VerticalThrusterRadius"), 0.1F, 8.0F),
                Math.clamp(tag.getDouble("VerticalThrusterStress"), 0.0D, 1024.0D)
        );
    }

    private static void requireRange(String name, double value, double minimum, double maximum) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new JsonParseException(name + " must be finite and in [" + minimum + ", " + maximum + "]");
        }
    }
}
