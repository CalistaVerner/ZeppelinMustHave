package us.kayla.zeppelinmusthave;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineStressRegistration;
import us.kayla.zeppelinmusthave.content.thruster.VerticalThrusterStressRegistration;
import us.kayla.zeppelinmusthave.data.ZmhDataReloaders;
import us.kayla.zeppelinmusthave.integration.SimulatedStack;
import us.kayla.zeppelinmusthave.registry.ZmhCapabilities;
import us.kayla.zeppelinmusthave.registry.ZmhRegistries;

import java.util.Map;

@Mod(ZeppelinMustHave.MOD_ID)
public final class ZeppelinMustHave {
    public static final String MOD_ID = "zeppelin_must_have";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ZeppelinMustHave(IEventBus modEventBus, ModContainer modContainer) {
        Map<String, String> dependencyVersions = SimulatedStack.loadedVersions();

        ZmhRegistries.register(modEventBus);
        modEventBus.addListener(ZmhCapabilities::register);
        modEventBus.addListener(this::commonSetup);
        ZmhDataReloaders.registerAll();

        LOGGER.info(
                "Initializing {} {} by us.Kayla on stack {}",
                modContainer.getModInfo().getDisplayName(),
                modContainer.getModInfo().getVersion(),
                dependencyVersions
        );
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SteamEngineStressRegistration.registerAll();
            VerticalThrusterStressRegistration.register();
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
