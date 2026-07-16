package us.kayla.zeppelinmusthave.content.redstone.conduit;

import net.minecraft.resources.ResourceLocation;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

/** Stable block-to-profile mapping; gameplay tuning lives in data packs. */
public enum PipedRedstoneTier {
    COPPER("copper"),
    BRASS("brass"),
    RESONANT("resonant");

    private final ResourceLocation profileId;

    PipedRedstoneTier(String profilePath) {
        this.profileId = ZeppelinMustHave.id(profilePath);
    }

    public ResourceLocation profileId() {
        return this.profileId;
    }
}
