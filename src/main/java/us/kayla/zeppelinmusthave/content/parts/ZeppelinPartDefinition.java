package us.kayla.zeppelinmusthave.content.parts;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public record ZeppelinPartDefinition(
        ResourceLocation id,
        Supplier<? extends ItemLike> item,
        @Nullable Supplier<? extends Block> block,
        ZeppelinPartCategory category
) {
    public ZeppelinPartDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(category, "category");
        if (block != null && !category.supportsBlocks()) {
            throw new IllegalArgumentException(
                    "Category " + category + " cannot contain block part " + id
            );
        }
    }

    public boolean isBlockPart() {
        return this.block != null;
    }

    public String descriptionKey() {
        return "zeppelin_must_have.zeppelin_part.description." + this.id.getPath();
    }
}
