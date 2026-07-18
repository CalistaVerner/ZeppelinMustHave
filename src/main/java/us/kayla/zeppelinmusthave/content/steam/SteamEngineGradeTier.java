package us.kayla.zeppelinmusthave.content.steam;

import net.minecraft.resources.ResourceLocation;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

/** Stable block-to-profile and block-to-model mapping for graded steam engines. */
public enum SteamEngineGradeTier {
    COPPER("copper"),
    BRASS("brass"),
    INDUSTRIAL("industrial"),
    GRAND("grand"),
    SOVEREIGN("sovereign"),
    LEVIATHAN("leviathan"),
    MK_VII("mk_vii");

    private final String path;
    private final ResourceLocation profileId;

    SteamEngineGradeTier(String path) {
        this.path = path;
        this.profileId = ZeppelinMustHave.id(path);
    }

    public String path() {
        return this.path;
    }

    public ResourceLocation profileId() {
        return this.profileId;
    }

    public ResourceLocation partialModel(String part) {
        return ZeppelinMustHave.id("block/steam_engine/" + this.path + "/" + part);
    }
}
