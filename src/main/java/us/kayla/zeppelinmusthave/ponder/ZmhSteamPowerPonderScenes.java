package us.kayla.zeppelinmusthave.ponder;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** Registers steam-power storyboards while keeping boiler and engine scenes isolated. */
final class ZmhSteamPowerPonderScenes {
    private ZmhSteamPowerPonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhBoilerPonderScenes.register(helper);
        ZmhSteamEnginePonderScenes.register(helper);
    }
}
