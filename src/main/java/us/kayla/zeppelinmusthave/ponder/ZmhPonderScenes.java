package us.kayla.zeppelinmusthave.ponder;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** Root registration point; storyboards live in domain-specific modules. */
public final class ZmhPonderScenes {
    private ZmhPonderScenes() {
    }

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhAirshipPonderScenes.register(helper);
        ZmhLiftPonderScenes.register(helper);
        ZmhSteamPowerPonderScenes.register(helper);
        ZmhControlPonderScenes.register(helper);
        ZmhFlightControlPonderScenes.register(helper);
        ZmhFluidPonderScenes.register(helper);
        ZmhRedstonePonderScenes.register(helper);
        ZmhServicePonderScenes.register(helper);
    }
}
