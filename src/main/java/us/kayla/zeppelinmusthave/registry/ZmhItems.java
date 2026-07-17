package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredItem;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeItem;

import java.util.List;
import java.util.function.Supplier;

/** Standalone items; block items are registered by the domain block catalogs. */
public final class ZmhItems {
    public static final DeferredItem<AirshipUpgradeItem> HEAT_RECUPERATOR_UPGRADE =
            registerUpgrade("heat_recuperator_upgrade");
    public static final DeferredItem<AirshipUpgradeItem> FORCED_INDUCTION_UPGRADE =
            registerUpgrade("forced_induction_upgrade");
    public static final DeferredItem<AirshipUpgradeItem> PRECISION_REGULATOR_UPGRADE =
            registerUpgrade("precision_regulator_upgrade");

    private ZmhItems() {
    }

    static void bootstrap() {
    }

    static List<Supplier<? extends ItemLike>> creativeItems() {
        return List.of(
                HEAT_RECUPERATOR_UPGRADE,
                FORCED_INDUCTION_UPGRADE,
                PRECISION_REGULATOR_UPGRADE
        );
    }

    private static DeferredItem<AirshipUpgradeItem> registerUpgrade(String name) {
        return ZmhRegistryContext.ITEMS.register(
                name,
                () -> new AirshipUpgradeItem(new Item.Properties().stacksTo(16))
        );
    }
}
