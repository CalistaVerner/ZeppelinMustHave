package us.kayla.zeppelinmusthave.registry;

import net.neoforged.bus.api.IEventBus;

public final class ZmhRegistries {
    private ZmhRegistries() {
    }

    public static void register(IEventBus modEventBus) {
        // Force definition classes to populate the shared DeferredRegisters.
        ZmhBlocks.bootstrap();
        ZmhItems.bootstrap();
        ZmhBlockEntityTypes.bootstrap();
        ZmhCreativeTabs.bootstrap();
        ZmhRegistryContext.register(modEventBus);
    }
}
