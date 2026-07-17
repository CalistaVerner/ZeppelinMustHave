package us.kayla.zeppelinmusthave.content.ballast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Objects;

public record BallastTankProfile(
        ResourceLocation id,
        int capacityMb,
        double massPerBucketKg
) {
    private static final int CURRENT_SCHEMA_VERSION = 1;

    public BallastTankProfile {
        Objects.requireNonNull(id, "id");
        if (capacityMb < 1_000 || capacityMb > 64_000) {
            throw new JsonParseException("capacity_mb must be in [1000, 64000]");
        }
        if (!Double.isFinite(massPerBucketKg) || massPerBucketKg < 0.0D || massPerBucketKg > 100_000.0D) {
            throw new JsonParseException("mass_per_bucket_kg must be finite and in [0, 100000]");
        }
    }

    public static BallastTankProfile parse(ResourceLocation id, JsonObject json) {
        int schema = GsonHelper.getAsInt(json, "schema_version", CURRENT_SCHEMA_VERSION);
        if (schema != CURRENT_SCHEMA_VERSION) {
            throw new JsonParseException("Unsupported ballast tank profile schema " + schema + " for " + id);
        }
        return new BallastTankProfile(
                id,
                GsonHelper.getAsInt(json, "capacity_mb"),
                GsonHelper.getAsDouble(json, "mass_per_bucket_kg")
        );
    }

    public static BallastTankProfile unresolved(ResourceLocation id) {
        return new BallastTankProfile(id, 8_000, 0.0D);
    }

    public double massForAmount(int amountMb) {
        return Math.max(0, amountMb) / 1_000.0D * this.massPerBucketKg;
    }

    public void writeClientSnapshot(CompoundTag tag) {
        tag.putString("BallastProfileId", this.id.toString());
        tag.putInt("BallastCapacityMb", this.capacityMb);
        tag.putDouble("BallastMassPerBucketKg", this.massPerBucketKg);
    }

    public static BallastTankProfile readClientSnapshot(CompoundTag tag, ResourceLocation fallbackId) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("BallastProfileId"));
        if (id == null) {
            id = fallbackId;
        }
        return new BallastTankProfile(
                id,
                Math.clamp(tag.getInt("BallastCapacityMb"), 1_000, 64_000),
                Math.clamp(tag.getDouble("BallastMassPerBucketKg"), 0.0D, 100_000.0D)
        );
    }
}
