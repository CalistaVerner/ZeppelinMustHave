package us.kayla.zeppelinmusthave.client;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerRenderer;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.client.renderer.AltitudeGaugeRenderer;
import us.kayla.zeppelinmusthave.client.renderer.PipedRedstoneNativeLeverRenderer;
import us.kayla.zeppelinmusthave.ponder.ZmhPonderPlugin;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

@Mod(value = ZeppelinMustHave.MOD_ID, dist = Dist.CLIENT)
public final class ZeppelinMustHaveClient {
    public ZeppelinMustHaveClient(IEventBus modEventBus) {
        PonderIndex.addPlugin(new ZmhPonderPlugin());
        modEventBus.addListener(this::registerRenderers);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ZmhBlockEntityTypes.AIRSHIP_BURNER.get(),
                context -> (BlockEntityRenderer) new HotAirBurnerRenderer(context)
        );
        event.registerBlockEntityRenderer(
                ZmhBlockEntityTypes.PIPED_REDSTONE_NATIVE_LEVER.get(),
                PipedRedstoneNativeLeverRenderer::new
        );
        event.registerBlockEntityRenderer(
                ZmhBlockEntityTypes.ALTITUDE_GAUGE.get(),
                AltitudeGaugeRenderer::new
        );
    }
}
