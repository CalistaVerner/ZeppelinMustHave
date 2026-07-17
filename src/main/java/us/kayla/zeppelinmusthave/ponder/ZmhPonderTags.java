package us.kayla.zeppelinmusthave.ponder;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCatalog;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

public final class ZmhPonderTags {
    public static final ResourceLocation ZEPPELIN_SYSTEMS =
            ZeppelinMustHave.id("zeppelin_systems");

    private ZmhPonderTags() {
    }

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(
                RegisteredObjectsHelper::getKeyOrThrow
        );

        helper.registerTag(ZEPPELIN_SYSTEMS)
                .item(ZmhBlocks.AIRSHIP_HELM_ITEM.get())
                .title("Zeppelin Parts")
                .description("Complete control, lift, steam power, propulsion, protected redstone, upgrades, and service equipment for engineered zeppelins")
                .register();

        var tag = itemHelper.addToTag(ZEPPELIN_SYSTEMS);
        ZeppelinPartCatalog.all().forEach(part -> tag.add(part.item().get()));
    }
}
