package us.kayla.zeppelinmusthave.content.redstone.conduit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

final class PipedRedstoneWaterlogging {
    private PipedRedstoneWaterlogging() {
    }

    static BlockState applyPlacement(
            BlockState state,
            BooleanProperty waterlogged,
            BlockPlaceContext context
    ) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return state.setValue(waterlogged, fluidState.is(Fluids.WATER));
    }

    static void scheduleWaterTick(
            BlockState state,
            BooleanProperty waterlogged,
            LevelAccessor level,
            BlockPos pos
    ) {
        if (state.getValue(waterlogged)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
    }

    static FluidState fluidState(
            BlockState state,
            BooleanProperty waterlogged,
            FluidState fallback
    ) {
        return state.getValue(waterlogged) ? Fluids.WATER.getSource(false) : fallback;
    }
}
