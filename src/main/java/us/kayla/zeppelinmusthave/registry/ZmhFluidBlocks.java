package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import us.kayla.zeppelinmusthave.content.fluid.FluidPipeTier;
import us.kayla.zeppelinmusthave.content.fluid.TieredFluidPipeBlock;
import us.kayla.zeppelinmusthave.content.fluid.TieredGlassFluidPipeBlock;

/** Create fluid-network-compatible pipe upgrade grades. */
final class ZmhFluidBlocks {
    static final RegisteredBlock<TieredFluidPipeBlock, BlockItem> REINFORCED_FLUID_PIPE =
            pipe("reinforced_fluid_pipe", FluidPipeTier.REINFORCED);
    static final RegisteredBlock<TieredFluidPipeBlock, BlockItem> INDUSTRIAL_FLUID_PIPE =
            pipe("industrial_fluid_pipe", FluidPipeTier.INDUSTRIAL);

    static final DeferredBlock<TieredGlassFluidPipeBlock> REINFORCED_GLASS_FLUID_PIPE =
            window("reinforced_glass_fluid_pipe", FluidPipeTier.REINFORCED);
    static final DeferredBlock<TieredGlassFluidPipeBlock> INDUSTRIAL_GLASS_FLUID_PIPE =
            window("industrial_glass_fluid_pipe", FluidPipeTier.INDUSTRIAL);

    private ZmhFluidBlocks() {
    }

    private static DeferredBlock<TieredGlassFluidPipeBlock> window(String name, FluidPipeTier tier) {
        return ZmhRegistryContext.BLOCKS.register(
                name,
                () -> new TieredGlassFluidPipeBlock(ZmhBlockProperties.fluidPipe(tier), tier)
        );
    }

    private static RegisteredBlock<TieredFluidPipeBlock, BlockItem> pipe(String name, FluidPipeTier tier) {
        return ZmhBlockRegistrar.register(
                name,
                () -> new TieredFluidPipeBlock(ZmhBlockProperties.fluidPipe(tier), tier)
        );
    }
}
