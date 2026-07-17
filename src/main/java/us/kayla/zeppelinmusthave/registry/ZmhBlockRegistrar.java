package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** Shared, type-safe block + item registration primitives. */
final class ZmhBlockRegistrar {
    private ZmhBlockRegistrar() {
    }

    static <B extends Block> RegisteredBlock<B, BlockItem> register(
            String name,
            Supplier<? extends B> blockFactory
    ) {
        return register(name, blockFactory, BlockItem::new);
    }

    static <B extends Block, I extends BlockItem> RegisteredBlock<B, I> register(
            String name,
            Supplier<? extends B> blockFactory,
            BiFunction<B, Item.Properties, I> itemFactory
    ) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(blockFactory, "blockFactory");
        Objects.requireNonNull(itemFactory, "itemFactory");

        DeferredBlock<B> block = ZmhRegistryContext.BLOCKS.register(name, blockFactory);
        DeferredItem<I> item = ZmhRegistryContext.ITEMS.register(
                name,
                () -> itemFactory.apply(block.get(), new Item.Properties())
        );
        return new RegisteredBlock<>(block, item);
    }
}
