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
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlock;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerTier;
import us.kayla.zeppelinmusthave.content.helm.AirshipHelmBlock;

public final class ZmhBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ZeppelinMustHave.MOD_ID);

    public static final DeferredRegister.Items BLOCK_ITEMS =
            DeferredRegister.createItems(ZeppelinMustHave.MOD_ID);

    public static final DeferredBlock<AirshipHelmBlock> AIRSHIP_HELM = BLOCKS.register(
            "airship_helm",
            () -> new AirshipHelmBlock(metalProperties())
    );
    public static final DeferredItem<BlockItem> AIRSHIP_HELM_ITEM = registerBlockItem("airship_helm", AIRSHIP_HELM);

    public static final DeferredBlock<AirshipBurnerBlock> AIRSHIP_BURNER = BLOCKS.register(
            "airship_burner",
            () -> new AirshipBurnerBlock(burnerProperties(), AirshipBurnerTier.STANDARD)
    );
    public static final DeferredItem<BlockItem> AIRSHIP_BURNER_ITEM = registerBlockItem("airship_burner", AIRSHIP_BURNER);

    public static final DeferredBlock<AirshipBurnerBlock> FORCED_DRAFT_AIRSHIP_BURNER = BLOCKS.register(
            "forced_draft_airship_burner",
            () -> new AirshipBurnerBlock(burnerProperties(), AirshipBurnerTier.FORCED_DRAFT)
    );
    public static final DeferredItem<BlockItem> FORCED_DRAFT_AIRSHIP_BURNER_ITEM = registerBlockItem(
            "forced_draft_airship_burner",
            FORCED_DRAFT_AIRSHIP_BURNER
    );

    public static final DeferredBlock<AirshipBurnerBlock> INDUSTRIAL_AIRSHIP_BURNER = BLOCKS.register(
            "industrial_airship_burner",
            () -> new AirshipBurnerBlock(burnerProperties(), AirshipBurnerTier.INDUSTRIAL)
    );
    public static final DeferredItem<BlockItem> INDUSTRIAL_AIRSHIP_BURNER_ITEM = registerBlockItem(
            "industrial_airship_burner",
            INDUSTRIAL_AIRSHIP_BURNER
    );

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

    private static <T extends Block> DeferredItem<BlockItem> registerBlockItem(
            String name,
            DeferredBlock<T> block
    ) {
        return BLOCK_ITEMS.registerSimpleBlockItem(name, block);
    }

    private static BlockBehaviour.Properties metalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F, 8.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties burnerProperties() {
        return metalProperties()
                .lightLevel(AirshipBurnerBlock::getLightPower)
                .noOcclusion();
    }
}
