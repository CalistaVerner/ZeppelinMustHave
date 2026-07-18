package us.kayla.zeppelinmusthave.content.fluid;

import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.StraightPipeBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Transparent graded pipe with the same pressure policy as its opaque form. */
public final class TieredGlassFluidPipeBlockEntity extends StraightPipeBlockEntity {
    public TieredGlassFluidPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public FluidPipeTier tier() {
        if (getBlockState().getBlock() instanceof TieredGlassFluidPipeBlock pipe) {
            return pipe.tier();
        }
        return FluidPipeTier.REINFORCED;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new TieredStraightPipeFluidTransportBehaviour(this));
        behaviours.add(new BracketedBlockEntityBehaviour(this));
        registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
    }

    private final class TieredStraightPipeFluidTransportBehaviour
            extends StraightPipeFluidTransportBehaviour {
        private TieredStraightPipeFluidTransportBehaviour(TieredGlassFluidPipeBlockEntity blockEntity) {
            super(blockEntity);
        }

        @Override
        public void addPressure(Direction side, boolean inbound, float pressure) {
            super.addPressure(side, inbound,
                    TieredGlassFluidPipeBlockEntity.this.tier().applyPressure(pressure));
        }
    }
}
