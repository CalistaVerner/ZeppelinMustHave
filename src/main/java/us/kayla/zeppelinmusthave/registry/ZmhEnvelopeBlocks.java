package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.BlockItem;
import us.kayla.zeppelinmusthave.content.balloon.BalloonEnvelopeTier;
import us.kayla.zeppelinmusthave.content.balloon.TieredEnvelopeBlock;

/** Create Aeronautics-compatible envelope upgrade grades. */
final class ZmhEnvelopeBlocks {
    static final RegisteredBlock<TieredEnvelopeBlock, BlockItem> REINFORCED_ENVELOPE =
            envelope("reinforced_envelope", BalloonEnvelopeTier.REINFORCED);
    static final RegisteredBlock<TieredEnvelopeBlock, BlockItem> INDUSTRIAL_ENVELOPE =
            envelope("industrial_envelope", BalloonEnvelopeTier.INDUSTRIAL);

    private ZmhEnvelopeBlocks() {
    }

    private static RegisteredBlock<TieredEnvelopeBlock, BlockItem> envelope(
            String name,
            BalloonEnvelopeTier tier
    ) {
        return ZmhBlockRegistrar.register(
                name,
                () -> new TieredEnvelopeBlock(ZmhBlockProperties.envelope(tier), tier)
        );
    }
}
