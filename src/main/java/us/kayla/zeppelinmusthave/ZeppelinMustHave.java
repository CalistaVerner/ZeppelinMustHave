package us.kayla.zeppelinmusthave;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.boiler.BoilerHeater;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerProfiles;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlock;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeProfiles;
import us.kayla.zeppelinmusthave.content.control.AltitudeControlProfiles;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneProfiles;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeDefinitions;
import us.kayla.zeppelinmusthave.integration.SimulatedStack;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;
import us.kayla.zeppelinmusthave.registry.ZmhRegistries;

import java.util.Map;

@Mod(ZeppelinMustHave.MOD_ID)
public final class ZeppelinMustHave {
    public static final String MOD_ID = "zeppelin_must_have";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ZeppelinMustHave(IEventBus modEventBus, ModContainer modContainer) {
        Map<String, String> dependencyVersions = SimulatedStack.loadedVersions();

        ZmhRegistries.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        AirshipBurnerProfiles.register();
        BoilerGradeProfiles.register();
        AirshipUpgradeDefinitions.register();
        AltitudeControlProfiles.register();
        PipedRedstoneProfiles.register();

        LOGGER.info(
                "Initializing {} {} by us.Kayla on stack {}",
                modContainer.getModInfo().getDisplayName(),
                modContainer.getModInfo().getVersion(),
                dependencyVersions
        );
    }


    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BoilerHeater.REGISTRY.register(
                    ZmhBlocks.COPPER_BOILER_BASE.get(),
                    (level, pos, state) -> ((BoilerGradeBlock) state.getBlock())
                            .getTransferredHeat(level, pos, state)
            );
            BoilerHeater.REGISTRY.register(
                    ZmhBlocks.BRASS_BOILER_BASE.get(),
                    (level, pos, state) -> ((BoilerGradeBlock) state.getBlock())
                            .getTransferredHeat(level, pos, state)
            );
            BoilerHeater.REGISTRY.register(
                    ZmhBlocks.INDUSTRIAL_BOILER_BASE.get(),
                    (level, pos, state) -> ((BoilerGradeBlock) state.getBlock())
                            .getTransferredHeat(level, pos, state)
            );
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
