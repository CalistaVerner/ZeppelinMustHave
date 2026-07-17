package us.kayla.zeppelinmusthave.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCatalog;

public final class ZmhCreativeTabs {
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
            ZmhRegistryContext.CREATIVE_TABS.register(
                    "main",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.zeppelin_must_have.main"))
                            .withTabsBefore(CreativeModeTabs.REDSTONE_BLOCKS)
                            .icon(() -> ZmhBlocks.AIRSHIP_HELM_ITEM.get().getDefaultInstance())
                            .displayItems((parameters, output) -> ZeppelinPartCatalog.orderedItems()
                                    .forEach(item -> output.accept(item.get())))
                            .build()
            );

    private ZmhCreativeTabs() {
    }

    static void bootstrap() {
    }
}
