package us.kayla.zeppelinmusthave.content.thruster;

import com.simibubi.create.api.stress.BlockStressValues;
import net.minecraft.world.level.block.Block;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

/** Registers the reload-aware Create stress impact of the vertical thruster. */
public final class VerticalThrusterStressRegistration {
    private VerticalThrusterStressRegistration() {
    }

    public static void register() {
        Block block = ZmhBlocks.VERTICAL_THRUSTER.get();
        BlockStressValues.IMPACTS.register(
                block,
                () -> VerticalThrusterProfiles.INSTANCE.resolveDefault().stressImpact()
        );
    }
}
