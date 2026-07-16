package us.kayla.zeppelinmusthave.content.redstone.conduit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Objects;

public record PipedRedstoneProfile(
        ResourceLocation id,
        int propagationDelayTicks,
        int maxSignalDistance
) {
    private static final int CURRENT_SCHEMA_VERSION = 1;

    public PipedRedstoneProfile {
        Objects.requireNonNull(id, "id");
        if (propagationDelayTicks < 1 || propagationDelayTicks > 40) {
            throw new JsonParseException("propagation_delay_ticks must be in [1, 40]");
        }
        if (maxSignalDistance < 1 || maxSignalDistance > 1024) {
            throw new JsonParseException("max_signal_distance must be in [1, 1024]");
        }
    }

    public static PipedRedstoneProfile parse(ResourceLocation id, JsonObject json) {
        int schema = GsonHelper.getAsInt(json, "schema_version", CURRENT_SCHEMA_VERSION);
        if (schema != CURRENT_SCHEMA_VERSION) {
            throw new JsonParseException(
                    "Unsupported Piped Redstone profile schema " + schema
                            + " for " + id + "; expected " + CURRENT_SCHEMA_VERSION
            );
        }

        return new PipedRedstoneProfile(
                id,
                GsonHelper.getAsInt(json, "propagation_delay_ticks"),
                GsonHelper.getAsInt(json, "max_signal_distance")
        );
    }

    public static PipedRedstoneProfile unresolved(ResourceLocation id) {
        return new PipedRedstoneProfile(id, 40, 1);
    }
}
