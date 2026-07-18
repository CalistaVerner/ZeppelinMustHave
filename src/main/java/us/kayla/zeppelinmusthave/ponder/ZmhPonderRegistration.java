package us.kayla.zeppelinmusthave.ponder;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCatalog;

import java.util.List;

/** Resolves Ponder components from the same concrete Item objects used by ItemStack lookup. */
final class ZmhPonderRegistration {
    private ZmhPonderRegistration() {
    }

    static PonderSceneRegistrationHelper<ItemLike> items(
            PonderSceneRegistrationHelper<ResourceLocation> helper
    ) {
        return helper.withKeyFunction(RegisteredObjectsHelper::getKeyOrThrow);
    }

    static List<ItemLike> allPartItems() {
        return ZeppelinPartCatalog.all().stream()
                .map(part -> part.item().get())
                .map(ItemLike.class::cast)
                .toList();
    }
}
