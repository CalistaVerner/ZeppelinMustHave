package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeItem;

public final class ZmhItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ZeppelinMustHave.MOD_ID);

    public static final DeferredItem<AirshipUpgradeItem> HEAT_RECUPERATOR_UPGRADE =
            registerUpgrade("heat_recuperator_upgrade");

    public static final DeferredItem<AirshipUpgradeItem> FORCED_INDUCTION_UPGRADE =
            registerUpgrade("forced_induction_upgrade");

    public static final DeferredItem<AirshipUpgradeItem> PRECISION_REGULATOR_UPGRADE =
            registerUpgrade("precision_regulator_upgrade");

    private ZmhItems() {
    }

    static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    private static DeferredItem<AirshipUpgradeItem> registerUpgrade(String name) {
        return ITEMS.register(
                name,
                () -> new AirshipUpgradeItem(new Item.Properties().stacksTo(16))
        );
    }
}
