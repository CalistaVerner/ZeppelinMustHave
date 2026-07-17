package us.kayla.zeppelinmusthave.content.parts;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

import java.util.EnumMap;
import java.util.Map;

public final class ZeppelinPartTags {
    public static final TagKey<Item> ALL_ITEMS = TagKey.create(
            Registries.ITEM,
            ZeppelinMustHave.id("zeppelin_parts")
    );
    public static final TagKey<Block> ALL_BLOCKS = TagKey.create(
            Registries.BLOCK,
            ZeppelinMustHave.id("zeppelin_parts")
    );

    private static final Map<ZeppelinPartCategory, TagKey<Item>> ITEM_CATEGORIES =
            new EnumMap<>(ZeppelinPartCategory.class);
    private static final Map<ZeppelinPartCategory, TagKey<Block>> BLOCK_CATEGORIES =
            new EnumMap<>(ZeppelinPartCategory.class);

    static {
        for (ZeppelinPartCategory category : ZeppelinPartCategory.values()) {
            ITEM_CATEGORIES.put(
                    category,
                    TagKey.create(
                            Registries.ITEM,
                            ZeppelinMustHave.id("zeppelin_parts/" + category.path())
                    )
            );
            if (category.supportsBlocks()) {
                BLOCK_CATEGORIES.put(
                        category,
                        TagKey.create(
                                Registries.BLOCK,
                                ZeppelinMustHave.id("zeppelin_parts/" + category.path())
                        )
                );
            }
        }
    }

    private ZeppelinPartTags() {
    }

    public static TagKey<Item> items(ZeppelinPartCategory category) {
        return ITEM_CATEGORIES.get(category);
    }

    public static TagKey<Block> blocks(ZeppelinPartCategory category) {
        TagKey<Block> tag = BLOCK_CATEGORIES.get(category);
        if (tag == null) {
            throw new IllegalArgumentException("No block tag for " + category);
        }
        return tag;
    }
}
