package us.kayla.zeppelinmusthave.integration.curios.client;

import dev.eriksonn.aeronautics.index.AeroItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/** Client-only Curios registration, isolated from the base client entry point. */
public final class CuriosClientCompat {
    private CuriosClientCompat() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CuriosClientCompat::clientSetup);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> CuriosRendererRegistry.register(
                AeroItems.AVIATORS_GOGGLES.get(),
                AviatorsGogglesCurioRenderer::new
        ));
    }
}
