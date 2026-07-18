package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import us.kayla.zeppelinmusthave.content.balloon.BalloonEnvelopeTier;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlock;
import us.kayla.zeppelinmusthave.content.control.AltitudeGaugeBlock;
import us.kayla.zeppelinmusthave.content.fluid.FluidPipeTier;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneRepeaterBlock;

/** Centralized material and durability policy for registered blocks. */
final class ZmhBlockProperties {
    private ZmhBlockProperties() {
    }

    static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F, 8.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    static BlockBehaviour.Properties envelope(BalloonEnvelopeTier tier) {
        return BlockBehaviour.Properties.of()
                .mapColor(tier == BalloonEnvelopeTier.REINFORCED
                        ? MapColor.COLOR_LIGHT_GRAY
                        : MapColor.COLOR_GRAY)
                .strength(tier.structuralRating(), tier.structuralRating() * 4.0F)
                .sound(SoundType.WOOL);
    }

    static BlockBehaviour.Properties fluidPipe(FluidPipeTier tier) {
        return metal()
                .strength(
                        tier == FluidPipeTier.REINFORCED ? 3.5F : 5.0F,
                        tier == FluidPipeTier.REINFORCED ? 8.0F : 14.0F
                )
                .sound(tier == FluidPipeTier.REINFORCED ? SoundType.COPPER : SoundType.NETHERITE_BLOCK)
                .noOcclusion();
    }

    static BlockBehaviour.Properties burner() {
        return metal()
                .lightLevel(AirshipBurnerBlock::getLightPower)
                .noOcclusion();
    }

    static BlockBehaviour.Properties boiler() {
        return metal()
                .strength(4.0F, 10.0F)
                .noOcclusion()
                .isRedstoneConductor((state, level, pos) -> true);
    }

    static BlockBehaviour.Properties steamEngine() {
        return metal()
                .strength(4.0F, 10.0F)
                .noOcclusion();
    }

    static BlockBehaviour.Properties conduit() {
        return metal()
                .strength(2.5F, 6.0F)
                .lightLevel(PipedRedstoneBlock::getLightPower)
                .noOcclusion();
    }

    static BlockBehaviour.Properties nativeLever() {
        return metal()
                .strength(2.0F, 4.0F)
                .lightLevel(PipedRedstoneNativeLeverBlock::getLightPower)
                .noOcclusion();
    }

    static BlockBehaviour.Properties repeater() {
        return metal()
                .strength(3.0F, 6.0F)
                .lightLevel(PipedRedstoneRepeaterBlock::getLightPower)
                .noOcclusion();
    }

    static BlockBehaviour.Properties altitudeGauge() {
        return metal()
                .lightLevel(AltitudeGaugeBlock::getLightPower)
                .noOcclusion();
    }

    static BlockBehaviour.Properties ballastTank() {
        return metal()
                .strength(4.0F, 10.0F)
                .noOcclusion();
    }

    static BlockBehaviour.Properties mooringWinch() {
        return metal()
                .strength(5.0F, 12.0F)
                .noOcclusion();
    }

    static BlockBehaviour.Properties verticalThruster() {
        return metal()
                .strength(4.0F, 10.0F)
                .noOcclusion();
    }
}
