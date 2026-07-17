package us.kayla.zeppelinmusthave.client;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerRenderer;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.client.renderer.AltitudeGaugeRenderer;
import us.kayla.zeppelinmusthave.client.renderer.BoilerGaugePartialModels;
import us.kayla.zeppelinmusthave.client.renderer.BoilerGradeRenderer;
import us.kayla.zeppelinmusthave.client.renderer.BoilerGradeSpriteShifts;
import us.kayla.zeppelinmusthave.client.renderer.GradedFluidTankModel;
import us.kayla.zeppelinmusthave.client.renderer.PipedRedstoneNativeLeverRenderer;
import us.kayla.zeppelinmusthave.client.renderer.SteamEngineGradePartialModels;
import us.kayla.zeppelinmusthave.client.renderer.SteamEngineGradeRenderer;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeTier;
import us.kayla.zeppelinmusthave.ponder.ZmhPonderPlugin;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

@Mod(value = ZeppelinMustHave.MOD_ID, dist = Dist.CLIENT)
public final class ZeppelinMustHaveClient {
    public ZeppelinMustHaveClient(IEventBus modEventBus) {
        PipedRedstoneNativeLeverRenderer.init();
        BoilerGradeSpriteShifts.init();
        BoilerGaugePartialModels.init();
        SteamEngineGradePartialModels.init();
        PonderIndex.addPlugin(new ZmhPonderPlugin());
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::modifyBakedModels);
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
        event.registerBlockEntityRenderer(
                ZmhBlockEntityTypes.COPPER_BOILER.get(),
                context -> (BlockEntityRenderer) new BoilerGradeRenderer(context)
        );
        event.registerBlockEntityRenderer(
                ZmhBlockEntityTypes.BRASS_BOILER.get(),
                context -> (BlockEntityRenderer) new BoilerGradeRenderer(context)
        );
        event.registerBlockEntityRenderer(
                ZmhBlockEntityTypes.INDUSTRIAL_BOILER.get(),
                context -> (BlockEntityRenderer) new BoilerGradeRenderer(context)
        );
        event.registerBlockEntityRenderer(
                ZmhBlockEntityTypes.STEAM_ENGINE_GRADE.get(),
                SteamEngineGradeRenderer::new
        );
    }

    private void modifyBakedModels(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll((location, model) -> {
            String key = location.toString();
            if (key.endsWith("#inventory")) {
                return model;
            }

            BoilerGradeTier boilerTier = boilerTierForModel(key);
            if (boilerTier != null) {
                BoilerGradeSpriteShifts.Shifts shifts = BoilerGradeSpriteShifts.forTier(boilerTier);
                return new GradedFluidTankModel(
                        model,
                        shifts.side(),
                        shifts.top(),
                        shifts.inner()
                );
            }
            return model;
        });
    }

    private static BoilerGradeTier boilerTierForModel(String key) {
        if (key.startsWith(ZeppelinMustHave.MOD_ID + ":copper_boiler_base#")) {
            return BoilerGradeTier.COPPER;
        }
        if (key.startsWith(ZeppelinMustHave.MOD_ID + ":brass_boiler_base#")) {
            return BoilerGradeTier.BRASS;
        }
        if (key.startsWith(ZeppelinMustHave.MOD_ID + ":industrial_boiler_base#")) {
            return BoilerGradeTier.INDUSTRIAL;
        }
        return null;
    }
}
