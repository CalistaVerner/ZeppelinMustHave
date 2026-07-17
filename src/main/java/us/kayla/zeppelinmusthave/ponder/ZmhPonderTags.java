package us.kayla.zeppelinmusthave.ponder;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
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
                .title("Zeppelin Systems")
                .description("Control, lift, navigation, steam power, propulsion, protected redstone, and service equipment for Create Aeronautics zeppelins")
                .register();

        itemHelper.addToTag(ZEPPELIN_SYSTEMS)
                .add(ZmhBlocks.AIRSHIP_HELM_ITEM.get())
                .add(ZmhBlocks.AIRSHIP_BURNER_ITEM.get())
                .add(ZmhBlocks.FORCED_DRAFT_AIRSHIP_BURNER_ITEM.get())
                .add(ZmhBlocks.INDUSTRIAL_AIRSHIP_BURNER_ITEM.get())
                .add(ZmhBlocks.COPPER_BOILER_BASE_ITEM.get())
                .add(ZmhBlocks.BRASS_BOILER_BASE_ITEM.get())
                .add(ZmhBlocks.INDUSTRIAL_BOILER_BASE_ITEM.get())
                .add(ZmhBlocks.COPPER_PIPED_REDSTONE_ITEM.get())
                .add(ZmhBlocks.BRASS_PIPED_REDSTONE_ITEM.get())
                .add(ZmhBlocks.RESONANT_PIPED_REDSTONE_ITEM.get())
                .add(ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER_ITEM.get())
                .add(ZmhBlocks.PIPED_REDSTONE_REPEATER_ITEM.get())
                .add(ZmhBlocks.BALLAST_TANK_ITEM.get())
                .add(ZmhBlocks.MOORING_WINCH_ITEM.get())
                .add(ZmhBlocks.ALTITUDE_GAUGE_ITEM.get())
                .add(ZmhBlocks.VERTICAL_THRUSTER_ITEM.get());
    }
}
