package us.kayla.zeppelinmusthave.client.renderer;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeTier;

/**
 * Connected-texture sprites for each graded boiler vessel.
 *
 * <p>The base sprite is used by the JSON tank model. The 4x4 connected sheet
 * is selected by Create's rectangle CT algorithm, so frames are emitted only
 * around the outside of the complete tank multiblock.</p>
 */
public final class BoilerGradeSpriteShifts {
    private static final Shifts COPPER = create("copper");
    private static final Shifts BRASS = create("brass");
    private static final Shifts INDUSTRIAL = create("industrial");

    private BoilerGradeSpriteShifts() {
    }

    public static void init() {
        // Trigger static initialization before the texture atlas is stitched.
        COPPER.side().getOriginalResourceLocation();
        BRASS.side().getOriginalResourceLocation();
        INDUSTRIAL.side().getOriginalResourceLocation();
    }

    public static Shifts forTier(BoilerGradeTier tier) {
        return switch (tier) {
            case COPPER -> COPPER;
            case BRASS -> BRASS;
            case INDUSTRIAL -> INDUSTRIAL;
        };
    }

    private static Shifts create(String tier) {
        return new Shifts(
                connected(tier),
                connected(tier + "_top"),
                connected(tier + "_inner")
        );
    }

    private static CTSpriteShiftEntry connected(String name) {
        ResourceLocation base = ZeppelinMustHave.id("block/boiler/" + name);
        ResourceLocation connected = ZeppelinMustHave.id("block/boiler/" + name + "_connected");
        return CTSpriteShifter.getCT(AllCTTypes.RECTANGLE, base, connected);
    }

    public record Shifts(
            CTSpriteShiftEntry side,
            CTSpriteShiftEntry top,
            CTSpriteShiftEntry inner
    ) {
    }
}
