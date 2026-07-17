package us.kayla.zeppelinmusthave.content.boiler;

import net.minecraft.resources.ResourceLocation;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

/** Stable block-to-profile mapping; tuning remains in data packs. */
public enum BoilerGradeTier {
    COPPER("copper"),
    BRASS("brass"),
    INDUSTRIAL("industrial");

    private final ResourceLocation profileId;

    BoilerGradeTier(String profilePath) {
        this.profileId = ZeppelinMustHave.id(profilePath);
    }

    public ResourceLocation profileId() {
        return this.profileId;
    }
}
