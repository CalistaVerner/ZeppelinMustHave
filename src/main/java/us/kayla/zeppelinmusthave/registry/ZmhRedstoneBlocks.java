package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.BlockItem;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneRepeaterBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneTier;

/** Protected redstone transport and control blocks. */
final class ZmhRedstoneBlocks {
    static final RegisteredBlock<PipedRedstoneBlock, BlockItem> COPPER_CONDUIT =
            conduit("copper_piped_redstone", PipedRedstoneTier.COPPER);
    static final RegisteredBlock<PipedRedstoneBlock, BlockItem> BRASS_CONDUIT =
            conduit("brass_piped_redstone", PipedRedstoneTier.BRASS);
    static final RegisteredBlock<PipedRedstoneBlock, BlockItem> RESONANT_CONDUIT =
            conduit("resonant_piped_redstone", PipedRedstoneTier.RESONANT);

    static final RegisteredBlock<PipedRedstoneNativeLeverBlock, BlockItem> NATIVE_LEVER =
            ZmhBlockRegistrar.register(
                    "piped_redstone_native_lever",
                    () -> new PipedRedstoneNativeLeverBlock(ZmhBlockProperties.nativeLever())
            );
    static final RegisteredBlock<PipedRedstoneRepeaterBlock, BlockItem> REPEATER =
            ZmhBlockRegistrar.register(
                    "piped_redstone_repeater",
                    () -> new PipedRedstoneRepeaterBlock(ZmhBlockProperties.repeater())
            );

    private ZmhRedstoneBlocks() {
    }

    private static RegisteredBlock<PipedRedstoneBlock, BlockItem> conduit(
            String name,
            PipedRedstoneTier tier
    ) {
        return ZmhBlockRegistrar.register(
                name,
                () -> new PipedRedstoneBlock(ZmhBlockProperties.conduit(), tier)
        );
    }
}
