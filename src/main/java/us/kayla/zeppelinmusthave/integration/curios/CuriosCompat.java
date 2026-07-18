package us.kayla.zeppelinmusthave.integration.curios;

import com.simibubi.create.content.equipment.goggles.GogglesItem;
import dev.eriksonn.aeronautics.index.AeroItems;
import top.theillusivec4.curios.api.CuriosApi;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

/** Optional Curios integration, loaded only after a successful ModList check. */
public final class CuriosCompat {
    private CuriosCompat() {
    }

    public static void register() {
        GogglesItem.addIsWearingPredicate(player -> CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(AeroItems.AVIATORS_GOGGLES.get()))
                .orElse(false));
        ZeppelinMustHave.LOGGER.info(
                "Enabled Curios head-slot support for aeronautics:aviators_goggles"
        );
    }
}
