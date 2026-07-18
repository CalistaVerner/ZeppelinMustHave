package us.kayla.zeppelinmusthave.content.fluid;

import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Reuses Create's pipe graph and flow simulation while applying the selected
 * grade to pump pressure before a connection enters the network.
 */
public final class TieredFluidPipeBlockEntity extends FluidPipeBlockEntity {
    public TieredFluidPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public FluidPipeTier tier() {
        if (getBlockState().getBlock() instanceof TieredFluidPipeBlock pipe) {
            return pipe.tier();
        }
        return FluidPipeTier.REINFORCED;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new TieredPipeFluidTransportBehaviour(this));
        behaviours.add(new BracketedBlockEntityBehaviour(this, state -> true));
        registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
    }

    private final class TieredPipeFluidTransportBehaviour extends FluidTransportBehaviour {
        private TieredPipeFluidTransportBehaviour(TieredFluidPipeBlockEntity blockEntity) {
            super(blockEntity);
        }

        @Override
        public void addPressure(Direction side, boolean inbound, float pressure) {
            super.addPressure(side, inbound, TieredFluidPipeBlockEntity.this.tier().applyPressure(pressure));
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return (FluidPipeBlock.isPipe(state) || state.getBlock() instanceof EncasedPipeBlock)
                    && state.getValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
        }

        @Override
        public AttachmentTypes getRenderedRimAttachment(
                BlockAndTintGetter world,
                BlockPos pos,
                BlockState state,
                Direction direction
        ) {
            BlockPos offsetPos = pos.relative(direction);
            BlockState otherState = world.getBlockState(offsetPos);

            if (otherState.getBlock() instanceof TieredFluidPipeBlock otherPipe
                    && otherPipe.tier() != TieredFluidPipeBlockEntity.this.tier()) {
                return AttachmentTypes.DETAILED_CONNECTION;
            }

            AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);

            if (state.getBlock() instanceof EncasedPipeBlock && attachment != AttachmentTypes.DRAIN) {
                return AttachmentTypes.NONE;
            }

            if (attachment == AttachmentTypes.RIM) {
                if (!FluidPipeBlock.isPipe(otherState)
                        && !(otherState.getBlock() instanceof EncasedPipeBlock)
                        && !(otherState.getBlock() instanceof GlassFluidPipeBlock)) {
                    FluidTransportBehaviour pipeBehaviour =
                            BlockEntityBehaviour.get(world, offsetPos, FluidTransportBehaviour.TYPE);
                    if (pipeBehaviour != null
                            && pipeBehaviour.canHaveFlowToward(otherState, direction.getOpposite())) {
                        return AttachmentTypes.DETAILED_CONNECTION;
                    }
                }

                if (!FluidPipeBlock.shouldDrawRim(world, pos, state, direction)) {
                    return FluidPropagator.getStraightPipeAxis(state) == direction.getAxis()
                            ? AttachmentTypes.CONNECTION
                            : AttachmentTypes.DETAILED_CONNECTION;
                }
            }

            if (attachment == AttachmentTypes.NONE
                    && state.getValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
                return AttachmentTypes.DETAILED_CONNECTION;
            }

            return attachment;
        }
    }
}
