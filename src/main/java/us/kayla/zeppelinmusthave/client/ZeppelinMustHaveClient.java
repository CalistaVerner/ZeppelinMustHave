package us.kayla.zeppelinmusthave.client;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerRenderer;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderTooltipHandler;
import com.simibubi.create.content.fluids.pipes.TransparentStraightPipeRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.client.renderer.AltitudeGaugeRenderer;
import us.kayla.zeppelinmusthave.client.renderer.BallastTankRenderer;
import us.kayla.zeppelinmusthave.client.renderer.BoilerGaugePartialModels;
import us.kayla.zeppelinmusthave.client.renderer.BoilerGradeRenderer;
import us.kayla.zeppelinmusthave.client.renderer.BoilerGradeSpriteShifts;
import us.kayla.zeppelinmusthave.client.renderer.GradedFluidTankModel;
import us.kayla.zeppelinmusthave.client.renderer.MooringWinchPartialModels;
import us.kayla.zeppelinmusthave.client.renderer.MooringWinchRenderer;
import us.kayla.zeppelinmusthave.client.renderer.OmniSpeedControllerPartialModels;
import us.kayla.zeppelinmusthave.client.renderer.OmniSpeedControllerRenderer;
import us.kayla.zeppelinmusthave.client.renderer.PipedRedstoneNativeLeverRenderer;
import us.kayla.zeppelinmusthave.client.renderer.SteamEngineGradePartialModels;
import us.kayla.zeppelinmusthave.client.renderer.SteamEngineGradeRenderer;
import us.kayla.zeppelinmusthave.client.renderer.TieredFluidPipePartialModels;
import us.kayla.zeppelinmusthave.client.renderer.TieredPipeAttachmentModel;
import us.kayla.zeppelinmusthave.client.renderer.VerticalThrusterPartialModels;
import us.kayla.zeppelinmusthave.client.renderer.VerticalThrusterRenderer;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeTier;
import us.kayla.zeppelinmusthave.content.fluid.FluidPipeTier;
import us.kayla.zeppelinmusthave.integration.curios.client.CuriosClientCompat;
import us.kayla.zeppelinmusthave.ponder.ZmhPonderPlugin;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

@Mod(value = ZeppelinMustHave.MOD_ID, dist = Dist.CLIENT)
public final class ZeppelinMustHaveClient {
    public ZeppelinMustHaveClient(IEventBus modEventBus) {
        PonderTooltipHandler.enable = true;
        PipedRedstoneNativeLeverRenderer.init();
        BoilerGradeSpriteShifts.init();
        BoilerGaugePartialModels.init();
        SteamEngineGradePartialModels.init();
        TieredFluidPipePartialModels.init();
        VerticalThrusterPartialModels.init();
        MooringWinchPartialModels.init();
        OmniSpeedControllerPartialModels.init();
        ZeppelinPartTooltip.register();
        PonderIndex.addPlugin(new ZmhPonderPlugin());
        if (ModList.get().isLoaded("curios")) {
            CuriosClientCompat.register(modEventBus);
        }
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
                ZmhBlockEntityTypes.BALLAST_TANK.get(),
                BallastTankRenderer::new
        );
        event.registerBlockEntityRenderer(
                ZmhBlockEntityTypes.MOORING_WINCH.get(),
                context -> (BlockEntityRenderer) new MooringWinchRenderer(context)
        );
        event.registerBlockEntityRenderer(
                ZmhBlockEntityTypes.VERTICAL_THRUSTER.get(),
                VerticalThrusterRenderer::new
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
        event.registerBlockEntityRenderer(
                ZmhBlockEntityTypes.OMNI_SPEED_CONTROLLER.get(),
                OmniSpeedControllerRenderer::new
        );
        event.registerBlockEntityRenderer(
                ZmhBlockEntityTypes.TIERED_GLASS_FLUID_PIPE.get(),
                TransparentStraightPipeRenderer::new
        );
    }

    private void modifyBakedModels(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll((location, model) -> {
            String key = location.toString();
            if (key.endsWith("#inventory")) {
                return model;
            }

            FluidPipeTier pipeTier = fluidPipeTierForModel(key);
            if (pipeTier != null) {
                return TieredPipeAttachmentModel.withoutAO(model);
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

    private static FluidPipeTier fluidPipeTierForModel(String key) {
        if (key.startsWith(ZeppelinMustHave.MOD_ID + ":reinforced_fluid_pipe#")) {
            return FluidPipeTier.REINFORCED;
        }
        if (key.startsWith(ZeppelinMustHave.MOD_ID + ":industrial_fluid_pipe#")) {
            return FluidPipeTier.INDUSTRIAL;
        }
        return null;
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
