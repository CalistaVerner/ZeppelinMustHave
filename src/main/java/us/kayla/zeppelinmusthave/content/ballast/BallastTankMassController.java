package us.kayla.zeppelinmusthave.content.ballast;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.integration.SableBallastMassBridge;

final class BallastTankMassController {
    private SableBallastMassBridge.Binding binding = SableBallastMassBridge.Binding.EMPTY;

    void reconcile(
            Level level,
            BlockPos pos,
            BlockState state,
            double desiredMass
    ) {
        this.binding = SableBallastMassBridge.reconcile(
                level,
                pos,
                state,
                this.binding,
                desiredMass
        );
    }

    void release(BlockState state, BlockPos pos) {
        this.binding = SableBallastMassBridge.release(this.binding, state, pos);
    }
}
