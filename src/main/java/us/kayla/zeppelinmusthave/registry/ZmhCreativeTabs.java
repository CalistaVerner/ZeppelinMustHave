package us.kayla.zeppelinmusthave.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

public final class ZmhCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ZeppelinMustHave.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.zeppelin_must_have.main"))
                    .withTabsBefore(CreativeModeTabs.REDSTONE_BLOCKS)
                    .icon(() -> ZmhBlocks.AIRSHIP_HELM_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ZmhBlocks.AIRSHIP_HELM_ITEM.get());
                        output.accept(ZmhBlocks.AIRSHIP_BURNER_ITEM.get());
                        output.accept(ZmhBlocks.FORCED_DRAFT_AIRSHIP_BURNER_ITEM.get());
                        output.accept(ZmhBlocks.INDUSTRIAL_AIRSHIP_BURNER_ITEM.get());
                        output.accept(ZmhItems.HEAT_RECUPERATOR_UPGRADE.get());
                        output.accept(ZmhItems.FORCED_INDUCTION_UPGRADE.get());
                        output.accept(ZmhItems.PRECISION_REGULATOR_UPGRADE.get());
                        output.accept(ZmhBlocks.BALLAST_TANK_ITEM.get());
                        output.accept(ZmhBlocks.MOORING_WINCH_ITEM.get());
                        output.accept(ZmhBlocks.ALTITUDE_GAUGE_ITEM.get());
                        output.accept(ZmhBlocks.VERTICAL_THRUSTER_ITEM.get());
                    })
                    .build()
    );

    private ZmhCreativeTabs() {
    }

    static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
