package us.kayla.zeppelinmusthave.ponder;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

final class ZmhAirshipPonderScenes {
    private ZmhAirshipPonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhHelmPonderScenes.register(helper);
        ZmhBurnerPonderScenes.register(helper);
    }
}
