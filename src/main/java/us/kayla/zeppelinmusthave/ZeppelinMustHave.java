package us.kayla.zeppelinmusthave;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerProfiles;
import us.kayla.zeppelinmusthave.content.control.AltitudeControlProfiles;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneProfiles;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeDefinitions;
import us.kayla.zeppelinmusthave.integration.SimulatedStack;
import us.kayla.zeppelinmusthave.registry.ZmhRegistries;

import java.util.Map;

@Mod(ZeppelinMustHave.MOD_ID)
public final class ZeppelinMustHave {
    public static final String MOD_ID = "zeppelin_must_have";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ZeppelinMustHave(IEventBus modEventBus, ModContainer modContainer) {
        Map<String, String> dependencyVersions = SimulatedStack.loadedVersions();

        ZmhRegistries.register(modEventBus);
        AirshipBurnerProfiles.register();
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

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
