package us.kayla.zeppelinmusthave.content.burner;

import net.minecraft.resources.ResourceLocation;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

/**
 * Stable registry-to-profile mapping.
 *
 * <p>The enum deliberately contains no gameplay tuning. All numerical values
 * are supplied by the corresponding data-pack profile.</p>
 */
public enum AirshipBurnerTier {
    STANDARD("standard"),
    FORCED_DRAFT("forced_draft"),
    INDUSTRIAL("industrial");

    private final ResourceLocation profileId;

    AirshipBurnerTier(String profilePath) {
        this.profileId = ZeppelinMustHave.id(profilePath);
    }

    public ResourceLocation profileId() {
        return this.profileId;
    }
}
