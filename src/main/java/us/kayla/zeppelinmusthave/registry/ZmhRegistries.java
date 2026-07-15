package us.kayla.zeppelinmusthave.registry;

import net.neoforged.bus.api.IEventBus;

public final class ZmhRegistries {
    private ZmhRegistries() {
    }

    public static void register(IEventBus modEventBus) {
        ZmhBlocks.register(modEventBus);
        ZmhCreativeTabs.register(modEventBus);
    }
}
