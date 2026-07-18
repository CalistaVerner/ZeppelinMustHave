package us.kayla.zeppelinmusthave.ponder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCatalog;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;
import us.kayla.zeppelinmusthave.registry.ZmhItems;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Canonical Ponder coverage and packaged storyboard resource manifest. */
public final class ZmhPonderManifest {
    private static final Map<String, Integer> SCENE_TEXT_COUNTS = Map.ofEntries(
            Map.entry("airship_helm_telemetry", 5),
            Map.entry("airship_burners", 14),
            Map.entry("altitude_control", 7),
            Map.entry("flight_control_network", 5),
            Map.entry("fluid_pipe_grades", 2),
            Map.entry("envelope_grades", 3),
            Map.entry("piped_redstone", 10),
            Map.entry("ballast_tank", 3),
            Map.entry("mooring_winch", 3),
            Map.entry("vertical_thruster", 3),
            Map.entry("boiler_grades", 7),
            Map.entry("steam_engine_grades", 8)
    );
    private static final List<String> SUPPORTED_LOCALES = List.of(
            "en_us", "ru_ru", "it_it", "pl_pl"
    );

    private static final List<String> STORYBOARDS = List.of(
            "helm/telemetry",
            "burner/operation",
            "lift/envelopes",
            "boiler/grades",
            "steam_engine/grades",
            "control/altitude_hold",
            "flight_control/network",
            "fluid/pipes",
            "redstone/conduits",
            "service/ballast_tank",
            "service/mooring_winch",
            "service/vertical_thruster"
    );

    private ZmhPonderManifest() {
    }

    public static ResourceLocation[] allPartIds() {
        return ZeppelinPartCatalog.all().stream()
                .map(part -> part.id())
                .toArray(ResourceLocation[]::new);
    }

    public static Set<ResourceLocation> allPartIdSet() {
        return Set.of(allPartIds());
    }

    public static Set<ResourceLocation> specificPartIdSet() {
        return Set.of(
                ZmhBlocks.AIRSHIP_HELM.getId(),
                ZmhBlocks.FLIGHT_COMPUTER.getId(),
                ZmhBlocks.ENGINE_TELEGRAPH.getId(),
                ZmhBlocks.EMERGENCY_CUTOFF.getId(),
                ZmhBlocks.CONTROL_TRANSMITTER.getId(),
                ZmhBlocks.CONTROL_RECEIVER.getId(),
                ZmhBlocks.AIRSHIP_BURNER.getId(),
                ZmhBlocks.FORCED_DRAFT_AIRSHIP_BURNER.getId(),
                ZmhBlocks.INDUSTRIAL_AIRSHIP_BURNER.getId(),
                ZmhBlocks.REINFORCED_ENVELOPE.getId(),
                ZmhBlocks.INDUSTRIAL_ENVELOPE.getId(),
                ZmhItems.HEAT_RECUPERATOR_UPGRADE.getId(),
                ZmhItems.FORCED_INDUCTION_UPGRADE.getId(),
                ZmhItems.PRECISION_REGULATOR_UPGRADE.getId(),
                ZmhBlocks.COPPER_BOILER_BASE.getId(),
                ZmhBlocks.BRASS_BOILER_BASE.getId(),
                ZmhBlocks.INDUSTRIAL_BOILER_BASE.getId(),
                ZmhBlocks.COPPER_STEAM_ENGINE.getId(),
                ZmhBlocks.BRASS_STEAM_ENGINE.getId(),
                ZmhBlocks.INDUSTRIAL_STEAM_ENGINE.getId(),
                ZmhBlocks.GRAND_STEAM_ENGINE.getId(),
                ZmhBlocks.SOVEREIGN_STEAM_ENGINE.getId(),
                ZmhBlocks.LEVIATHAN_STEAM_ENGINE.getId(),
                ZmhBlocks.MK_VII_STEAM_ENGINE.getId(),
                ZmhBlocks.REINFORCED_FLUID_PIPE.getId(),
                ZmhBlocks.INDUSTRIAL_FLUID_PIPE.getId(),
                ZmhBlocks.COPPER_PIPED_REDSTONE.getId(),
                ZmhBlocks.BRASS_PIPED_REDSTONE.getId(),
                ZmhBlocks.RESONANT_PIPED_REDSTONE.getId(),
                ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER.getId(),
                ZmhBlocks.PIPED_REDSTONE_REPEATER.getId(),
                ZmhBlocks.BALLAST_TANK.getId(),
                ZmhBlocks.MOORING_WINCH.getId(),
                ZmhBlocks.ALTITUDE_GAUGE.getId(),
                ZmhBlocks.VERTICAL_THRUSTER.getId()
        );
    }

    public static List<String> storyboards() {
        return STORYBOARDS;
    }

    public static void validate() {
        validateCatalogCoverage();
        validatePackagedTemplates();
        validateLanguageFiles();
    }

    public static void validateCatalogCoverage() {
        Set<ResourceLocation> catalog = new LinkedHashSet<>();
        ZeppelinPartCatalog.all().forEach(part -> catalog.add(part.id()));
        Set<ResourceLocation> ponder = new LinkedHashSet<>(List.of(allPartIds()));
        if (!catalog.equals(ponder)) {
            Set<ResourceLocation> missing = new LinkedHashSet<>(catalog);
            missing.removeAll(ponder);
            Set<ResourceLocation> stale = new LinkedHashSet<>(ponder);
            stale.removeAll(catalog);
            throw new IllegalStateException(
                    "Ponder overview coverage differs from ZeppelinPartCatalog; missing=" + missing + ", stale=" + stale
            );
        }

        Set<ResourceLocation> specific = new LinkedHashSet<>(specificPartIdSet());
        if (!catalog.equals(specific)) {
            Set<ResourceLocation> missing = new LinkedHashSet<>(catalog);
            missing.removeAll(specific);
            Set<ResourceLocation> stale = new LinkedHashSet<>(specific);
            stale.removeAll(catalog);
            throw new IllegalStateException(
                    "Specialized Ponder coverage differs from ZeppelinPartCatalog; missing="
                            + missing + ", stale=" + stale
            );
        }
    }

    public static Set<String> requiredLocalizationKeys() {
        Set<String> keys = new LinkedHashSet<>();
        SCENE_TEXT_COUNTS.forEach((sceneId, textCount) -> {
            String prefix = ZeppelinMustHave.MOD_ID + ".ponder." + sceneId + ".";
            keys.add(prefix + "header");
            for (int index = 1; index <= textCount; index++) {
                keys.add(prefix + "text_" + index);
            }
        });
        return Set.copyOf(keys);
    }

    public static void validateLanguageFiles() {
        ClassLoader loader = ZmhPonderManifest.class.getClassLoader();
        Set<String> required = requiredLocalizationKeys();
        for (String locale : SUPPORTED_LOCALES) {
            String path = "assets/" + ZeppelinMustHave.MOD_ID + "/lang/" + locale + ".json";
            try (InputStream stream = loader.getResourceAsStream(path)) {
                if (stream == null) {
                    throw new IllegalStateException("Missing language file required by Ponder: " + path);
                }
                JsonObject language = JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)
                ).getAsJsonObject();
                Set<String> missing = new LinkedHashSet<>();
                Set<String> empty = new LinkedHashSet<>();
                for (String key : required) {
                    if (!language.has(key)) {
                        missing.add(key);
                        continue;
                    }
                    String value = language.get(key).getAsString();
                    if (value.isBlank() || value.equals(key)) {
                        empty.add(key);
                    }
                }
                if (!missing.isEmpty() || !empty.isEmpty()) {
                    throw new IllegalStateException(
                            "Incomplete Ponder localization " + locale
                                    + "; missing=" + missing + ", empty=" + empty
                    );
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to read Ponder localization " + path, exception);
            }
        }
    }

    public static void validatePackagedTemplates() {
        ClassLoader loader = ZmhPonderManifest.class.getClassLoader();
        for (String storyboard : STORYBOARDS) {
            String path = "assets/" + ZeppelinMustHave.MOD_ID + "/ponder/" + storyboard + ".nbt";
            try (InputStream stream = loader.getResourceAsStream(path)) {
                if (stream == null) {
                    throw new IllegalStateException("Missing Ponder storyboard template: " + path);
                }
                CompoundTag template = NbtIo.readCompressed(
                        stream,
                        NbtAccounter.create(4L * 1024L * 1024L)
                );
                validateTemplate(path, template);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to read Ponder storyboard template " + path, exception);
            }
        }
    }

    private static void validateTemplate(String path, CompoundTag template) {
        ListTag size = template.getList("size", Tag.TAG_INT);
        if (size.size() != 3) {
            throw new IllegalStateException(path + " has invalid structure size list: " + size);
        }
        for (int axis = 0; axis < 3; axis++) {
            int extent = size.getInt(axis);
            if (extent < 1 || extent > 64) {
                throw new IllegalStateException(
                        path + " has unsafe structure extent " + extent + " on axis " + axis
                );
            }
        }
        ListTag palette = template.getList("palette", Tag.TAG_COMPOUND);
        if (palette.isEmpty()) {
            throw new IllegalStateException(path + " has no structure palette");
        }
        ListTag blocks = template.getList("blocks", Tag.TAG_COMPOUND);
        if (blocks.isEmpty()) {
            throw new IllegalStateException(path + " has no structure blocks");
        }

        boolean[] visibleState = new boolean[palette.size()];
        for (int index = 0; index < palette.size(); index++) {
            String blockName = palette.getCompound(index).getString("Name");
            visibleState[index] = !blockName.equals("minecraft:air")
                    && !blockName.equals("minecraft:void_air")
                    && !blockName.equals("minecraft:cave_air")
                    && !blockName.equals("minecraft:structure_void");
        }

        int visibleBlocks = 0;
        int visibleBaseBlocks = 0;
        for (int index = 0; index < blocks.size(); index++) {
            CompoundTag block = blocks.getCompound(index);
            int state = block.getInt("state");
            if (state < 0 || state >= visibleState.length || !visibleState[state]) {
                continue;
            }
            visibleBlocks++;
            ListTag position = block.getList("pos", Tag.TAG_INT);
            if (position.size() == 3 && position.getInt(1) == 0) {
                visibleBaseBlocks++;
            }
        }
        if (visibleBlocks < 9 || visibleBaseBlocks < 9) {
            throw new IllegalStateException(
                    path + " is a visually empty Ponder placeholder; visibleBlocks="
                            + visibleBlocks + ", visibleBaseBlocks=" + visibleBaseBlocks
            );
        }
    }
}
