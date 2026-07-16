package us.kayla.zeppelinmusthave.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

public final class ZmhPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return ZeppelinMustHave.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhPonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        ZmhPonderTags.register(helper);
    }
}
