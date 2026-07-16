package us.kayla.zeppelinmusthave.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

public final class ZmhTags {
    private ZmhTags() {
    }

    public static final class Items {
        public static final TagKey<Item> AIRSHIP_UPGRADES = TagKey.create(
                Registries.ITEM,
                ZeppelinMustHave.id("airship_upgrades")
        );

        private Items() {
        }
    }
}
