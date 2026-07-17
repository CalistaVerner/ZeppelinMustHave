package us.kayla.zeppelinmusthave.content.redstone.conduit;

import net.minecraft.core.BlockPos;

import java.util.Set;

record PipedRedstoneNetwork(
        Set<BlockPos> positions,
        int delayTicks,
        int maxSignalDistance,
        boolean truncated
) {
    static final PipedRedstoneNetwork EMPTY = new PipedRedstoneNetwork(Set.of(), 1, 1, false);
}
