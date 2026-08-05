package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import us.kayla.zeppelinmusthave.content.casing.AirshipCasingBlock;
import us.kayla.zeppelinmusthave.content.casing.AirshipEncasedCogwheelBlock;
import us.kayla.zeppelinmusthave.content.casing.AirshipEncasedPipeBlock;
import us.kayla.zeppelinmusthave.content.casing.AirshipEncasedShaftBlock;

/** Public casing items and block-only Create encased variants. */
final class ZmhCasingBlocks {
    static final RegisteredBlock<AirshipCasingBlock, BlockItem> AIRSHIP_BRASS_CASING =
            ZmhBlockRegistrar.register(
                    "airship_brass_casing",
                    () -> new AirshipCasingBlock(ZmhBlockProperties.brassCasing())
            );
    static final RegisteredBlock<AirshipCasingBlock, BlockItem> AIRSHIP_BRONZE_CASING =
            ZmhBlockRegistrar.register(
                    "airship_bronze_casing",
                    () -> new AirshipCasingBlock(ZmhBlockProperties.bronzeCasing())
            );

    static final DeferredBlock<AirshipEncasedShaftBlock> AIRSHIP_BRASS_ENCASED_SHAFT =
            ZmhRegistryContext.BLOCKS.register(
                    "airship_brass_encased_shaft",
                    () -> new AirshipEncasedShaftBlock(
                            ZmhBlockProperties.brassEncasedMechanism(),
                            AIRSHIP_BRASS_CASING.block()::get
                    )
            );
    static final DeferredBlock<AirshipEncasedShaftBlock> AIRSHIP_BRONZE_ENCASED_SHAFT =
            ZmhRegistryContext.BLOCKS.register(
                    "airship_bronze_encased_shaft",
                    () -> new AirshipEncasedShaftBlock(
                            ZmhBlockProperties.bronzeEncasedMechanism(),
                            AIRSHIP_BRONZE_CASING.block()::get
                    )
            );

    static final DeferredBlock<AirshipEncasedCogwheelBlock> AIRSHIP_BRASS_ENCASED_COGWHEEL =
            ZmhRegistryContext.BLOCKS.register(
                    "airship_brass_encased_cogwheel",
                    () -> new AirshipEncasedCogwheelBlock(
                            ZmhBlockProperties.brassEncasedMechanism(),
                            false,
                            AIRSHIP_BRASS_CASING.block()::get
                    )
            );
    static final DeferredBlock<AirshipEncasedCogwheelBlock> AIRSHIP_BRONZE_ENCASED_COGWHEEL =
            ZmhRegistryContext.BLOCKS.register(
                    "airship_bronze_encased_cogwheel",
                    () -> new AirshipEncasedCogwheelBlock(
                            ZmhBlockProperties.bronzeEncasedMechanism(),
                            false,
                            AIRSHIP_BRONZE_CASING.block()::get
                    )
            );

    static final DeferredBlock<AirshipEncasedCogwheelBlock> AIRSHIP_BRASS_ENCASED_LARGE_COGWHEEL =
            ZmhRegistryContext.BLOCKS.register(
                    "airship_brass_encased_large_cogwheel",
                    () -> new AirshipEncasedCogwheelBlock(
                            ZmhBlockProperties.brassEncasedMechanism(),
                            true,
                            AIRSHIP_BRASS_CASING.block()::get
                    )
            );
    static final DeferredBlock<AirshipEncasedCogwheelBlock> AIRSHIP_BRONZE_ENCASED_LARGE_COGWHEEL =
            ZmhRegistryContext.BLOCKS.register(
                    "airship_bronze_encased_large_cogwheel",
                    () -> new AirshipEncasedCogwheelBlock(
                            ZmhBlockProperties.bronzeEncasedMechanism(),
                            true,
                            AIRSHIP_BRONZE_CASING.block()::get
                    )
            );

    static final DeferredBlock<AirshipEncasedPipeBlock> AIRSHIP_BRASS_ENCASED_FLUID_PIPE =
            ZmhRegistryContext.BLOCKS.register(
                    "airship_brass_encased_fluid_pipe",
                    () -> new AirshipEncasedPipeBlock(
                            ZmhBlockProperties.brassEncasedMechanism(),
                            AIRSHIP_BRASS_CASING.block()::get
                    )
            );
    static final DeferredBlock<AirshipEncasedPipeBlock> AIRSHIP_BRONZE_ENCASED_FLUID_PIPE =
            ZmhRegistryContext.BLOCKS.register(
                    "airship_bronze_encased_fluid_pipe",
                    () -> new AirshipEncasedPipeBlock(
                            ZmhBlockProperties.bronzeEncasedMechanism(),
                            AIRSHIP_BRONZE_CASING.block()::get
                    )
            );

    private ZmhCasingBlocks() {
    }
}
