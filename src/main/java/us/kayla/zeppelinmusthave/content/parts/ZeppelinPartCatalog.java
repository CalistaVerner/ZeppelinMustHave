package us.kayla.zeppelinmusthave.content.parts;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;
import us.kayla.zeppelinmusthave.registry.ZmhItems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Canonical manifest for every public Zeppelin Must Have block and item.
 *
 * <p>Creative-tab ordering, tooltips, Ponder membership, public tags, and
 * coverage tests must derive from this catalog rather than duplicate lists.</p>
 */
public final class ZeppelinPartCatalog {
    private static final Set<ResourceLocation> INTERNAL_BLOCKS = Set.of(
            ZeppelinMustHave.id("reinforced_glass_fluid_pipe"),
            ZeppelinMustHave.id("industrial_glass_fluid_pipe")
    );
    private static final List<ZeppelinPartDefinition> PARTS = List.of(
            block("airship_helm", ZmhBlocks.AIRSHIP_HELM_ITEM, ZmhBlocks.AIRSHIP_HELM,
                    ZeppelinPartCategory.FLIGHT_CONTROL),
            block("flight_computer", ZmhBlocks.FLIGHT_COMPUTER_ITEM, ZmhBlocks.FLIGHT_COMPUTER,
                    ZeppelinPartCategory.FLIGHT_CONTROL),
            block("engine_telegraph", ZmhBlocks.ENGINE_TELEGRAPH_ITEM, ZmhBlocks.ENGINE_TELEGRAPH,
                    ZeppelinPartCategory.FLIGHT_CONTROL),
            block("emergency_cutoff", ZmhBlocks.EMERGENCY_CUTOFF_ITEM, ZmhBlocks.EMERGENCY_CUTOFF,
                    ZeppelinPartCategory.FLIGHT_CONTROL),
            block("control_transmitter", ZmhBlocks.CONTROL_TRANSMITTER_ITEM, ZmhBlocks.CONTROL_TRANSMITTER,
                    ZeppelinPartCategory.FLIGHT_CONTROL),
            block("control_receiver", ZmhBlocks.CONTROL_RECEIVER_ITEM, ZmhBlocks.CONTROL_RECEIVER,
                    ZeppelinPartCategory.FLIGHT_CONTROL),


            block("airship_burner", ZmhBlocks.AIRSHIP_BURNER_ITEM, ZmhBlocks.AIRSHIP_BURNER,
                    ZeppelinPartCategory.LIFT),
            block("forced_draft_airship_burner", ZmhBlocks.FORCED_DRAFT_AIRSHIP_BURNER_ITEM,
                    ZmhBlocks.FORCED_DRAFT_AIRSHIP_BURNER, ZeppelinPartCategory.LIFT),
            block("industrial_airship_burner", ZmhBlocks.INDUSTRIAL_AIRSHIP_BURNER_ITEM,
                    ZmhBlocks.INDUSTRIAL_AIRSHIP_BURNER, ZeppelinPartCategory.LIFT),
            block("reinforced_envelope", ZmhBlocks.REINFORCED_ENVELOPE_ITEM,
                    ZmhBlocks.REINFORCED_ENVELOPE, ZeppelinPartCategory.LIFT),
            block("industrial_envelope", ZmhBlocks.INDUSTRIAL_ENVELOPE_ITEM,
                    ZmhBlocks.INDUSTRIAL_ENVELOPE, ZeppelinPartCategory.LIFT),

            item("heat_recuperator_upgrade", ZmhItems.HEAT_RECUPERATOR_UPGRADE,
                    ZeppelinPartCategory.UPGRADE),
            item("forced_induction_upgrade", ZmhItems.FORCED_INDUCTION_UPGRADE,
                    ZeppelinPartCategory.UPGRADE),

            block("copper_boiler_base", ZmhBlocks.COPPER_BOILER_BASE_ITEM,
                    ZmhBlocks.COPPER_BOILER_BASE, ZeppelinPartCategory.STEAM_POWER),
            block("brass_boiler_base", ZmhBlocks.BRASS_BOILER_BASE_ITEM,
                    ZmhBlocks.BRASS_BOILER_BASE, ZeppelinPartCategory.STEAM_POWER),
            block("industrial_boiler_base", ZmhBlocks.INDUSTRIAL_BOILER_BASE_ITEM,
                    ZmhBlocks.INDUSTRIAL_BOILER_BASE, ZeppelinPartCategory.STEAM_POWER),
            block("copper_steam_engine", ZmhBlocks.COPPER_STEAM_ENGINE_ITEM,
                    ZmhBlocks.COPPER_STEAM_ENGINE, ZeppelinPartCategory.STEAM_POWER),
            block("brass_steam_engine", ZmhBlocks.BRASS_STEAM_ENGINE_ITEM,
                    ZmhBlocks.BRASS_STEAM_ENGINE, ZeppelinPartCategory.STEAM_POWER),
            block("industrial_steam_engine", ZmhBlocks.INDUSTRIAL_STEAM_ENGINE_ITEM,
                    ZmhBlocks.INDUSTRIAL_STEAM_ENGINE, ZeppelinPartCategory.STEAM_POWER),
            block("grand_steam_engine", ZmhBlocks.GRAND_STEAM_ENGINE_ITEM,
                    ZmhBlocks.GRAND_STEAM_ENGINE, ZeppelinPartCategory.STEAM_POWER),
            block("sovereign_steam_engine", ZmhBlocks.SOVEREIGN_STEAM_ENGINE_ITEM,
                    ZmhBlocks.SOVEREIGN_STEAM_ENGINE, ZeppelinPartCategory.STEAM_POWER),
            block("leviathan_steam_engine", ZmhBlocks.LEVIATHAN_STEAM_ENGINE_ITEM,
                    ZmhBlocks.LEVIATHAN_STEAM_ENGINE, ZeppelinPartCategory.STEAM_POWER),
            block("mk_vii_steam_engine", ZmhBlocks.MK_VII_STEAM_ENGINE_ITEM,
                    ZmhBlocks.MK_VII_STEAM_ENGINE, ZeppelinPartCategory.STEAM_POWER),

            item("precision_regulator_upgrade", ZmhItems.PRECISION_REGULATOR_UPGRADE,
                    ZeppelinPartCategory.UPGRADE),

            block("reinforced_fluid_pipe", ZmhBlocks.REINFORCED_FLUID_PIPE_ITEM,
                    ZmhBlocks.REINFORCED_FLUID_PIPE, ZeppelinPartCategory.FLUID_SYSTEMS),
            block("industrial_fluid_pipe", ZmhBlocks.INDUSTRIAL_FLUID_PIPE_ITEM,
                    ZmhBlocks.INDUSTRIAL_FLUID_PIPE, ZeppelinPartCategory.FLUID_SYSTEMS),

            block("copper_piped_redstone", ZmhBlocks.COPPER_PIPED_REDSTONE_ITEM,
                    ZmhBlocks.COPPER_PIPED_REDSTONE, ZeppelinPartCategory.REDSTONE_CONTROL),
            block("brass_piped_redstone", ZmhBlocks.BRASS_PIPED_REDSTONE_ITEM,
                    ZmhBlocks.BRASS_PIPED_REDSTONE, ZeppelinPartCategory.REDSTONE_CONTROL),
            block("resonant_piped_redstone", ZmhBlocks.RESONANT_PIPED_REDSTONE_ITEM,
                    ZmhBlocks.RESONANT_PIPED_REDSTONE, ZeppelinPartCategory.REDSTONE_CONTROL),
            block("piped_redstone_native_lever", ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER_ITEM,
                    ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER, ZeppelinPartCategory.REDSTONE_CONTROL),
            block("piped_redstone_repeater", ZmhBlocks.PIPED_REDSTONE_REPEATER_ITEM,
                    ZmhBlocks.PIPED_REDSTONE_REPEATER, ZeppelinPartCategory.REDSTONE_CONTROL),

            block("ballast_tank", ZmhBlocks.BALLAST_TANK_ITEM, ZmhBlocks.BALLAST_TANK,
                    ZeppelinPartCategory.BALLAST),
            block("mooring_winch", ZmhBlocks.MOORING_WINCH_ITEM, ZmhBlocks.MOORING_WINCH,
                    ZeppelinPartCategory.MOORING),
            block("altitude_gauge", ZmhBlocks.ALTITUDE_GAUGE_ITEM, ZmhBlocks.ALTITUDE_GAUGE,
                    ZeppelinPartCategory.FLIGHT_CONTROL),
            block("vertical_thruster", ZmhBlocks.VERTICAL_THRUSTER_ITEM,
                    ZmhBlocks.VERTICAL_THRUSTER, ZeppelinPartCategory.PROPULSION)
    );

    private static final Map<ResourceLocation, ZeppelinPartDefinition> BY_ID = buildIndex();
    private static final Map<ZeppelinPartCategory, List<ZeppelinPartDefinition>> BY_CATEGORY =
            buildCategoryIndex();

    private ZeppelinPartCatalog() {
    }

    public static List<ZeppelinPartDefinition> all() {
        return PARTS;
    }

    public static List<ZeppelinPartDefinition> blocks() {
        return PARTS.stream().filter(ZeppelinPartDefinition::isBlockPart).toList();
    }

    public static List<ZeppelinPartDefinition> category(ZeppelinPartCategory category) {
        return BY_CATEGORY.getOrDefault(category, List.of());
    }

    public static List<Supplier<? extends ItemLike>> orderedItems() {
        return PARTS.stream().map(ZeppelinPartDefinition::item).toList();
    }

    public static Optional<ZeppelinPartDefinition> find(Item item) {
        return Optional.ofNullable(BY_ID.get(BuiltInRegistries.ITEM.getKey(item)));
    }

    /**
     * Fails fast when a registry entry is added without complete Zeppelin Parts
     * catalog coverage, or when a catalog entry points at the wrong handle.
     */
    public static void validateRegistryCoverage() {
        Set<ResourceLocation> expectedItems = PARTS.stream()
                .map(ZeppelinPartDefinition::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<ResourceLocation> expectedBlocks = blocks().stream()
                .map(ZeppelinPartDefinition::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<ResourceLocation> registeredItems = BuiltInRegistries.ITEM.keySet().stream()
                .filter(id -> id.getNamespace().equals(ZeppelinMustHave.MOD_ID))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<ResourceLocation> registeredBlocks = BuiltInRegistries.BLOCK.keySet().stream()
                .filter(id -> id.getNamespace().equals(ZeppelinMustHave.MOD_ID))
                .filter(id -> !INTERNAL_BLOCKS.contains(id))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        assertExactCoverage("items", expectedItems, registeredItems);
        assertExactCoverage("blocks", expectedBlocks, registeredBlocks);

        for (ZeppelinPartDefinition part : PARTS) {
            ResourceLocation actualItem = BuiltInRegistries.ITEM.getKey(part.item().get().asItem());
            if (!part.id().equals(actualItem)) {
                throw new IllegalStateException(
                        "Zeppelin Part " + part.id() + " points at item " + actualItem
                );
            }
            if (part.block() != null) {
                ResourceLocation actualBlock = BuiltInRegistries.BLOCK.getKey(part.block().get());
                if (!part.id().equals(actualBlock)) {
                    throw new IllegalStateException(
                            "Zeppelin Part " + part.id() + " points at block " + actualBlock
                    );
                }
            }
        }
    }

    private static ZeppelinPartDefinition block(
            String path,
            Supplier<? extends ItemLike> item,
            Supplier<? extends net.minecraft.world.level.block.Block> block,
            ZeppelinPartCategory category
    ) {
        return new ZeppelinPartDefinition(ZeppelinMustHave.id(path), item, block, category);
    }

    private static ZeppelinPartDefinition item(
            String path,
            Supplier<? extends ItemLike> item,
            ZeppelinPartCategory category
    ) {
        return new ZeppelinPartDefinition(ZeppelinMustHave.id(path), item, null, category);
    }

    private static Map<ResourceLocation, ZeppelinPartDefinition> buildIndex() {
        Map<ResourceLocation, ZeppelinPartDefinition> index = new LinkedHashMap<>();
        for (ZeppelinPartDefinition part : PARTS) {
            ZeppelinPartDefinition previous = index.put(part.id(), part);
            if (previous != null) {
                throw new IllegalStateException("Duplicate Zeppelin Part ID " + part.id());
            }
        }
        return Collections.unmodifiableMap(index);
    }

    private static Map<ZeppelinPartCategory, List<ZeppelinPartDefinition>> buildCategoryIndex() {
        Map<ZeppelinPartCategory, List<ZeppelinPartDefinition>> mutable =
                new EnumMap<>(ZeppelinPartCategory.class);
        for (ZeppelinPartDefinition part : PARTS) {
            mutable.computeIfAbsent(part.category(), ignored -> new ArrayList<>()).add(part);
        }
        Map<ZeppelinPartCategory, List<ZeppelinPartDefinition>> immutable =
                new EnumMap<>(ZeppelinPartCategory.class);
        mutable.forEach((category, values) -> immutable.put(category, List.copyOf(values)));
        return Collections.unmodifiableMap(immutable);
    }

    private static void assertExactCoverage(
            String label,
            Set<ResourceLocation> expected,
            Set<ResourceLocation> actual
    ) {
        Set<ResourceLocation> missing = new LinkedHashSet<>(actual);
        missing.removeAll(expected);
        Set<ResourceLocation> stale = new LinkedHashSet<>(expected);
        stale.removeAll(actual);
        if (!missing.isEmpty() || !stale.isEmpty()) {
            throw new IllegalStateException(
                    "Incomplete Zeppelin Parts " + label + " coverage; uncatalogued="
                            + missing + ", stale=" + stale
            );
        }
    }
}
