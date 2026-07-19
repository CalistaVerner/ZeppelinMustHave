package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.BlockItem;
import us.kayla.zeppelinmusthave.content.kinetics.OmniSpeedControllerBlock;

/** General-purpose Create kinetic-control components. */
final class ZmhKineticBlocks {
    static final RegisteredBlock<OmniSpeedControllerBlock, BlockItem> OMNI_SPEED_CONTROLLER =
            ZmhBlockRegistrar.register(
                    "omni_speed_controller",
                    () -> new OmniSpeedControllerBlock(ZmhBlockProperties.kineticController())
            );

    private ZmhKineticBlocks() {
    }
}
