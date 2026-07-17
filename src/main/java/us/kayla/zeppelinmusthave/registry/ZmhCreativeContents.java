package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.function.Supplier;

/** Declarative ordering for the mod creative tab. */
final class ZmhCreativeContents {
    private ZmhCreativeContents() {
    }

    static List<Supplier<? extends ItemLike>> orderedItems() {
        return List.of(
                ZmhBlocks.AIRSHIP_HELM_ITEM,
                ZmhBlocks.AIRSHIP_BURNER_ITEM,
                ZmhBlocks.FORCED_DRAFT_AIRSHIP_BURNER_ITEM,
                ZmhBlocks.INDUSTRIAL_AIRSHIP_BURNER_ITEM,
                ZmhItems.HEAT_RECUPERATOR_UPGRADE,
                ZmhItems.FORCED_INDUCTION_UPGRADE,
                ZmhBlocks.COPPER_BOILER_BASE_ITEM,
                ZmhBlocks.BRASS_BOILER_BASE_ITEM,
                ZmhBlocks.INDUSTRIAL_BOILER_BASE_ITEM,
                ZmhBlocks.COPPER_STEAM_ENGINE_ITEM,
                ZmhBlocks.BRASS_STEAM_ENGINE_ITEM,
                ZmhBlocks.INDUSTRIAL_STEAM_ENGINE_ITEM,
                ZmhItems.PRECISION_REGULATOR_UPGRADE,
                ZmhBlocks.COPPER_PIPED_REDSTONE_ITEM,
                ZmhBlocks.BRASS_PIPED_REDSTONE_ITEM,
                ZmhBlocks.RESONANT_PIPED_REDSTONE_ITEM,
                ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER_ITEM,
                ZmhBlocks.PIPED_REDSTONE_REPEATER_ITEM,
                ZmhBlocks.BALLAST_TANK_ITEM,
                ZmhBlocks.MOORING_WINCH_ITEM,
                ZmhBlocks.ALTITUDE_GAUGE_ITEM,
                ZmhBlocks.VERTICAL_THRUSTER_ITEM
        );
    }
}
