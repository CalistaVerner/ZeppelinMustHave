package us.kayla.zeppelinmusthave.client.renderer;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

/** Connected-texture sprite sets for the two airship casing materials. */
public final class AirshipCasingSpriteShifts {
    private static final Shifts BRASS = create("airship_brass");
    private static final Shifts BRONZE = create("airship_bronze");

    private AirshipCasingSpriteShifts() {
    }

    public static void init() {
        touch(BRASS);
        touch(BRONZE);
    }

    public static Shifts brass() {
        return BRASS;
    }

    public static Shifts bronze() {
        return BRONZE;
    }

    private static Shifts create(String material) {
        return new Shifts(
                shift(AllCTTypes.OMNIDIRECTIONAL, material + "_casing"),
                shift(AllCTTypes.VERTICAL, material + "_encased_cogwheel_side"),
                shift(AllCTTypes.HORIZONTAL, material + "_encased_cogwheel_side")
        );
    }

    private static CTSpriteShiftEntry shift(
            com.simibubi.create.foundation.block.connected.CTType type,
            String texture
    ) {
        ResourceLocation base = ZeppelinMustHave.id("block/" + texture);
        ResourceLocation connected = ZeppelinMustHave.id("block/" + texture + "_connected");
        return CTSpriteShifter.getCT(type, base, connected);
    }

    private static void touch(Shifts shifts) {
        shifts.casing().getOriginalResourceLocation();
        shifts.cogSideVertical().getOriginalResourceLocation();
        shifts.cogSideHorizontal().getOriginalResourceLocation();
    }

    public record Shifts(
            CTSpriteShiftEntry casing,
            CTSpriteShiftEntry cogSideVertical,
            CTSpriteShiftEntry cogSideHorizontal
    ) {
    }
}
