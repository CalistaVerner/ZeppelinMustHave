package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** A block and the item registered for placing it. */
record RegisteredBlock<B extends Block, I extends BlockItem>(
        DeferredBlock<B> block,
        DeferredItem<I> item
) {
}
