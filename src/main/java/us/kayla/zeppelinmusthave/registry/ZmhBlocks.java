package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

public final class ZmhBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ZeppelinMustHave.MOD_ID);

    public static final DeferredRegister.Items BLOCK_ITEMS =
            DeferredRegister.createItems(ZeppelinMustHave.MOD_ID);

    public static final DeferredBlock<Block> AIRSHIP_HELM = registerMetalBlock("airship_helm");
    public static final DeferredItem<BlockItem> AIRSHIP_HELM_ITEM = registerBlockItem("airship_helm", AIRSHIP_HELM);

    public static final DeferredBlock<Block> BALLAST_TANK = registerMetalBlock("ballast_tank");
    public static final DeferredItem<BlockItem> BALLAST_TANK_ITEM = registerBlockItem("ballast_tank", BALLAST_TANK);

    public static final DeferredBlock<Block> MOORING_WINCH = registerMetalBlock("mooring_winch");
    public static final DeferredItem<BlockItem> MOORING_WINCH_ITEM = registerBlockItem("mooring_winch", MOORING_WINCH);

    public static final DeferredBlock<Block> ALTITUDE_GAUGE = registerMetalBlock("altitude_gauge");
    public static final DeferredItem<BlockItem> ALTITUDE_GAUGE_ITEM = registerBlockItem("altitude_gauge", ALTITUDE_GAUGE);

    public static final DeferredBlock<Block> VERTICAL_THRUSTER = registerMetalBlock("vertical_thruster");
    public static final DeferredItem<BlockItem> VERTICAL_THRUSTER_ITEM = registerBlockItem("vertical_thruster", VERTICAL_THRUSTER);

    private ZmhBlocks() {
    }

    static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
    }

    private static DeferredBlock<Block> registerMetalBlock(String name) {
        return BLOCKS.registerSimpleBlock(name, metalProperties());
    }

    private static DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<Block> block) {
        return BLOCK_ITEMS.registerSimpleBlockItem(name, block);
    }

    private static BlockBehaviour.Properties metalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F, 8.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }
}
