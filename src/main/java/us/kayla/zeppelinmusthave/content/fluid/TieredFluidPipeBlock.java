package us.kayla.zeppelinmusthave.content.fluid;

import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

/** Create-compatible fluid pipe with a fixed structural/pressure grade. */
public final class TieredFluidPipeBlock extends FluidPipeBlock {
    private final FluidPipeTier tier;

    public TieredFluidPipeBlock(Properties properties, FluidPipeTier tier) {
        super(properties);
        this.tier = tier;
    }

    public FluidPipeTier tier() {
        return this.tier;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (tryRemoveBracket(context)) {
            return InteractionResult.SUCCESS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        Axis axis = FluidPropagator.getStraightPipeAxis(state);

        if (axis == null) {
            Vec3 clickLocation = context.getClickLocation()
                    .subtract(pos.getX(), pos.getY(), pos.getZ());
            double closest = Double.MAX_VALUE;
            Direction closestDirection = Direction.UP;
            for (Direction direction : Iterate.directions) {
                if (clickedFace.getAxis() == direction.getAxis()) {
                    continue;
                }
                double distance = Vec3.atCenterOf(direction.getNormal())
                        .distanceToSqr(clickLocation);
                if (distance < closest) {
                    closest = distance;
                    closestDirection = direction;
                }
            }
            axis = closestDirection.getAxis();
        }

        if (clickedFace.getAxis() == axis) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            FluidTransportBehaviour.cacheFlows(level, pos);
            BlockState window = windowPipe().defaultBlockState()
                    .setValue(TieredGlassFluidPipeBlock.AXIS, axis)
                    .setValue(TieredGlassFluidPipeBlock.ALT, false)
                    .setValue(
                            BlockStateProperties.WATERLOGGED,
                            state.getValue(BlockStateProperties.WATERLOGGED)
                    );
            level.setBlockAndUpdate(pos, window);
            FluidTransportBehaviour.loadFlows(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.TIERED_FLUID_PIPE.get();
    }

    private TieredGlassFluidPipeBlock windowPipe() {
        return switch (this.tier) {
            case REINFORCED -> ZmhBlocks.REINFORCED_GLASS_FLUID_PIPE.get();
            case INDUSTRIAL -> ZmhBlocks.INDUSTRIAL_GLASS_FLUID_PIPE.get();
        };
    }
}
