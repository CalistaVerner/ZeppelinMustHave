package us.kayla.zeppelinmusthave.content.mooring;

import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlock;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

/** Create Simulated rope winch specialized as a zeppelin mooring device. */
public final class MooringWinchBlock extends RopeWinchBlock {
    public MooringWinchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends RopeWinchBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.MOORING_WINCH.get();
    }
}
