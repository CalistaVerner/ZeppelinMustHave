package us.kayla.zeppelinmusthave.content.upgrade;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record AirshipUpgradeDefinition(
        ResourceLocation id,
        Set<AirshipUpgradeTarget> targets,
        AirshipUpgradeSlot slot,
        String exclusiveGroup,
        Set<ResourceLocation> conflicts,
        AirshipUpgradeModifiers modifiers
) {
    private static final int CURRENT_SCHEMA_VERSION = 1;

    public AirshipUpgradeDefinition {
        Objects.requireNonNull(id, "id");
        targets = Set.copyOf(targets);
        if (targets.isEmpty()) {
            throw new JsonParseException("Upgrade " + id + " must declare at least one target");
        }
        Objects.requireNonNull(slot, "slot");
        exclusiveGroup = Objects.requireNonNullElse(exclusiveGroup, "");
        conflicts = Set.copyOf(conflicts);
        Objects.requireNonNull(modifiers, "modifiers");
    }

    public static AirshipUpgradeDefinition parse(ResourceLocation id, JsonObject json) {
        int schema = GsonHelper.getAsInt(json, "schema_version", CURRENT_SCHEMA_VERSION);
        if (schema != CURRENT_SCHEMA_VERSION) {
            throw new JsonParseException(
                    "Unsupported airship upgrade schema " + schema
                            + " for " + id + "; expected " + CURRENT_SCHEMA_VERSION
            );
        }

        Set<AirshipUpgradeTarget> targets = new HashSet<>();
        JsonArray targetArray = GsonHelper.getAsJsonArray(json, "targets");
        for (JsonElement element : targetArray) {
            targets.add(AirshipUpgradeTarget.parse(element.getAsString()));
        }

        Set<ResourceLocation> conflicts = new HashSet<>();
        if (json.has("conflicts")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "conflicts")) {
                ResourceLocation conflict = ResourceLocation.tryParse(element.getAsString());
                if (conflict == null) {
                    throw new JsonParseException(
                            "Invalid conflict ID in airship upgrade " + id + ": " + element
                    );
                }
                conflicts.add(conflict);
            }
        }

        JsonObject modifiers = json.has("modifiers")
                ? GsonHelper.getAsJsonObject(json, "modifiers")
                : new JsonObject();

        return new AirshipUpgradeDefinition(
                id,
                targets,
                AirshipUpgradeSlot.parse(GsonHelper.getAsString(json, "slot")),
                GsonHelper.getAsString(json, "exclusive_group", ""),
                conflicts,
                AirshipUpgradeModifiers.parse(modifiers)
        );
    }

    public boolean supports(AirshipUpgradeTarget target) {
        return this.targets.contains(target);
    }

    public boolean conflictsWith(AirshipUpgradeDefinition other) {
        if (this.id.equals(other.id)) {
            return false;
        }
        if (this.conflicts.contains(other.id) || other.conflicts.contains(this.id)) {
            return true;
        }
        return !this.exclusiveGroup.isBlank()
                && this.exclusiveGroup.equals(other.exclusiveGroup);
    }
}
