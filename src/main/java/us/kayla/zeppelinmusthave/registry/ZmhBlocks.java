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
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlock;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeTier;
import us.kayla.zeppelinmusthave.content.control.AltitudeGaugeBlock;
import us.kayla.zeppelinmusthave.content.helm.AirshipHelmBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneRepeaterBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneTier;

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

    public static final DeferredBlock<BoilerGradeBlock> COPPER_BOILER_BASE = BLOCKS.register(
            "copper_boiler_base",
            () -> new BoilerGradeBlock(boilerGradeProperties(), BoilerGradeTier.COPPER)
    );
    public static final DeferredItem<BlockItem> COPPER_BOILER_BASE_ITEM = registerBlockItem(
            "copper_boiler_base",
            COPPER_BOILER_BASE
    );

    public static final DeferredBlock<BoilerGradeBlock> BRASS_BOILER_BASE = BLOCKS.register(
            "brass_boiler_base",
            () -> new BoilerGradeBlock(boilerGradeProperties(), BoilerGradeTier.BRASS)
    );
    public static final DeferredItem<BlockItem> BRASS_BOILER_BASE_ITEM = registerBlockItem(
            "brass_boiler_base",
            BRASS_BOILER_BASE
    );

    public static final DeferredBlock<BoilerGradeBlock> INDUSTRIAL_BOILER_BASE = BLOCKS.register(
            "industrial_boiler_base",
            () -> new BoilerGradeBlock(boilerGradeProperties(), BoilerGradeTier.INDUSTRIAL)
    );
    public static final DeferredItem<BlockItem> INDUSTRIAL_BOILER_BASE_ITEM = registerBlockItem(
            "industrial_boiler_base",
            INDUSTRIAL_BOILER_BASE
    );

    public static final DeferredBlock<PipedRedstoneBlock> COPPER_PIPED_REDSTONE = BLOCKS.register(
            "copper_piped_redstone",
            () -> new PipedRedstoneBlock(conduitProperties(), PipedRedstoneTier.COPPER)
    );
    public static final DeferredItem<BlockItem> COPPER_PIPED_REDSTONE_ITEM = registerBlockItem(
            "copper_piped_redstone",
            COPPER_PIPED_REDSTONE
    );

    public static final DeferredBlock<PipedRedstoneBlock> BRASS_PIPED_REDSTONE = BLOCKS.register(
            "brass_piped_redstone",
            () -> new PipedRedstoneBlock(conduitProperties(), PipedRedstoneTier.BRASS)
    );
    public static final DeferredItem<BlockItem> BRASS_PIPED_REDSTONE_ITEM = registerBlockItem(
            "brass_piped_redstone",
            BRASS_PIPED_REDSTONE
    );

    public static final DeferredBlock<PipedRedstoneBlock> RESONANT_PIPED_REDSTONE = BLOCKS.register(
            "resonant_piped_redstone",
            () -> new PipedRedstoneBlock(conduitProperties(), PipedRedstoneTier.RESONANT)
    );
    public static final DeferredItem<BlockItem> RESONANT_PIPED_REDSTONE_ITEM = registerBlockItem(
            "resonant_piped_redstone",
            RESONANT_PIPED_REDSTONE
    );

    public static final DeferredBlock<PipedRedstoneNativeLeverBlock> PIPED_REDSTONE_NATIVE_LEVER = BLOCKS.register(
            "piped_redstone_native_lever",
            () -> new PipedRedstoneNativeLeverBlock(nativeLeverProperties())
    );
    public static final DeferredItem<BlockItem> PIPED_REDSTONE_NATIVE_LEVER_ITEM = registerBlockItem(
            "piped_redstone_native_lever",
            PIPED_REDSTONE_NATIVE_LEVER
    );

    public static final DeferredBlock<PipedRedstoneRepeaterBlock> PIPED_REDSTONE_REPEATER = BLOCKS.register(
            "piped_redstone_repeater",
            () -> new PipedRedstoneRepeaterBlock(repeaterProperties())
    );
    public static final DeferredItem<BlockItem> PIPED_REDSTONE_REPEATER_ITEM = registerBlockItem(
            "piped_redstone_repeater",
            PIPED_REDSTONE_REPEATER
    );

    public static final DeferredBlock<Block> BALLAST_TANK = registerMetalBlock("ballast_tank");
    public static final DeferredItem<BlockItem> BALLAST_TANK_ITEM = registerBlockItem("ballast_tank", BALLAST_TANK);

    public static final DeferredBlock<Block> MOORING_WINCH = registerMetalBlock("mooring_winch");
    public static final DeferredItem<BlockItem> MOORING_WINCH_ITEM = registerBlockItem("mooring_winch", MOORING_WINCH);

    public static final DeferredBlock<AltitudeGaugeBlock> ALTITUDE_GAUGE = BLOCKS.register(
            "altitude_gauge",
            () -> new AltitudeGaugeBlock(altitudeGaugeProperties())
    );
    public static final DeferredItem<BlockItem> ALTITUDE_GAUGE_ITEM = registerBlockItem(
            "altitude_gauge",
            ALTITUDE_GAUGE
    );

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


    private static BlockBehaviour.Properties boilerGradeProperties() {
        return metalProperties()
                .strength(4.0F, 10.0F)
                .lightLevel(BoilerGradeBlock::getLightPower)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties conduitProperties() {
        return metalProperties()
                .strength(2.5F, 6.0F)
                .lightLevel(PipedRedstoneBlock::getLightPower)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties nativeLeverProperties() {
        return metalProperties()
                .strength(2.0F, 4.0F)
                .lightLevel(PipedRedstoneNativeLeverBlock::getLightPower)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties repeaterProperties() {
        return metalProperties()
                .strength(3.0F, 6.0F)
                .lightLevel(PipedRedstoneRepeaterBlock::getLightPower)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties altitudeGaugeProperties() {
        return metalProperties()
                .lightLevel(AltitudeGaugeBlock::getLightPower)
                .noOcclusion();
    }
}
