package us.kayla.zeppelinmusthave.gametest;

import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneBlock;

final class PipedRedstoneGameTestFixtures {
    static final String TEMPLATE = "piped_redstone_empty";

    private PipedRedstoneGameTestFixtures() {
    }

    static BlockState horizontalConduit(BlockState state) {
        return state
                .setValue(PipedRedstoneBlock.EAST, true)
                .setValue(PipedRedstoneBlock.WEST, true);
    }
}
