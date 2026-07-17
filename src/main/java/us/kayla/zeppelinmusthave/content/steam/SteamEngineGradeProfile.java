package us.kayla.zeppelinmusthave.content.steam;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Objects;

public record SteamEngineGradeProfile(
        ResourceLocation id,
        double stressCapacity,
        int boilerLoadUnits,
        int cylinderCount,
        float crankRadius,
        float connectingRodLength,
        float pistonBaseOffset,
        float cylinderSpread,
        float steamParticleScale
) {
    private static final int CURRENT_SCHEMA_VERSION = 1;

    public SteamEngineGradeProfile {
        Objects.requireNonNull(id, "id");
        requireFinitePositive("stress_capacity", stressCapacity);
        if (boilerLoadUnits < 1 || boilerLoadUnits > 18) {
            throw new JsonParseException("boiler_load_units must be in [1, 18]");
        }
        if (cylinderCount < 1 || cylinderCount > 3) {
            throw new JsonParseException("cylinder_count must be in [1, 3]");
        }
        requireFinitePositive("crank_radius", crankRadius);
        requireFinitePositive("connecting_rod_length", connectingRodLength);
        if (connectingRodLength <= crankRadius) {
            throw new JsonParseException("connecting_rod_length must be greater than crank_radius");
        }
        requireFinitePositive("piston_base_offset", pistonBaseOffset);
        requireFiniteNonNegative("cylinder_spread", cylinderSpread);
        requireFiniteNonNegative("steam_particle_scale", steamParticleScale);
    }

    public static SteamEngineGradeProfile parse(ResourceLocation id, JsonObject json) {
        int schema = GsonHelper.getAsInt(json, "schema_version", CURRENT_SCHEMA_VERSION);
        if (schema != CURRENT_SCHEMA_VERSION) {
            throw new JsonParseException(
                    "Unsupported steam engine grade profile schema " + schema
                            + " for " + id + "; expected " + CURRENT_SCHEMA_VERSION
            );
        }

        return new SteamEngineGradeProfile(
                id,
                GsonHelper.getAsDouble(json, "stress_capacity"),
                GsonHelper.getAsInt(json, "boiler_load_units"),
                GsonHelper.getAsInt(json, "cylinder_count"),
                GsonHelper.getAsFloat(json, "crank_radius"),
                GsonHelper.getAsFloat(json, "connecting_rod_length"),
                GsonHelper.getAsFloat(json, "piston_base_offset"),
                GsonHelper.getAsFloat(json, "cylinder_spread", 0.0F),
                GsonHelper.getAsFloat(json, "steam_particle_scale", 1.0F)
        );
    }

    public float cylinderOffset(int cylinderIndex) {
        if (this.cylinderCount == 1) {
            return 0.0F;
        }
        float center = (this.cylinderCount - 1) / 2.0F;
        return (cylinderIndex - center) * this.cylinderSpread;
    }

    public float cylinderPhase(int cylinderIndex) {
        return (float) (Math.TAU * cylinderIndex / this.cylinderCount);
    }

    public void writeClientSnapshot(CompoundTag tag) {
        tag.putString("SteamEngineGradeProfileId", this.id.toString());
        tag.putDouble("SteamEngineStressCapacity", this.stressCapacity);
        tag.putInt("SteamEngineBoilerLoadUnits", this.boilerLoadUnits);
        tag.putInt("SteamEngineCylinderCount", this.cylinderCount);
        tag.putFloat("SteamEngineCrankRadius", this.crankRadius);
        tag.putFloat("SteamEngineConnectingRodLength", this.connectingRodLength);
        tag.putFloat("SteamEnginePistonBaseOffset", this.pistonBaseOffset);
        tag.putFloat("SteamEngineCylinderSpread", this.cylinderSpread);
        tag.putFloat("SteamEngineParticleScale", this.steamParticleScale);
    }

    public static SteamEngineGradeProfile readClientSnapshot(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("SteamEngineGradeProfileId"));
        if (id == null) {
            id = ResourceLocation.fromNamespaceAndPath("zeppelin_must_have", "unresolved");
        }
        float crankRadius = Math.max(0.01F, tag.getFloat("SteamEngineCrankRadius"));
        float rodLength = Math.max(crankRadius + 0.01F, tag.getFloat("SteamEngineConnectingRodLength"));
        return new SteamEngineGradeProfile(
                id,
                Math.max(1.0D, tag.getDouble("SteamEngineStressCapacity")),
                Math.clamp(tag.getInt("SteamEngineBoilerLoadUnits"), 1, 18),
                Math.clamp(tag.getInt("SteamEngineCylinderCount"), 1, 3),
                Math.max(0.01F, tag.getFloat("SteamEngineCrankRadius")),
                Math.max(0.02F, tag.getFloat("SteamEngineConnectingRodLength")),
                Math.max(0.01F, tag.getFloat("SteamEnginePistonBaseOffset")),
                Math.max(0.0F, tag.getFloat("SteamEngineCylinderSpread")),
                Math.max(0.0F, tag.getFloat("SteamEngineParticleScale"))
        );
    }

    public static SteamEngineGradeProfile unresolved(ResourceLocation id) {
        return new SteamEngineGradeProfile(id, 1.0D, 1, 1, 0.375F, 0.875F, 1.25F, 0.0F, 0.0F);
    }

    private static void requireFinitePositive(String name, double value) {
        if (!Double.isFinite(value) || value <= 0.0D) {
            throw new JsonParseException(name + " must be finite and greater than zero");
        }
    }

    private static void requireFiniteNonNegative(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new JsonParseException(name + " must be finite and non-negative");
        }
    }
}
