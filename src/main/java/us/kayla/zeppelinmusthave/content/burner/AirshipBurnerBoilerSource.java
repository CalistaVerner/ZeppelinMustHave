package us.kayla.zeppelinmusthave.content.burner;

import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Resolves a Create boiler directly below an Airship Burner. */
final class AirshipBurnerBoilerSource {
    private AirshipBurnerBoilerSource() {
    }

    static double efficiency(Level level, BlockPos burnerPos) {
        if (level == null) {
            return 0.0D;
        }
        BlockEntity blockEntity = level.getBlockEntity(burnerPos.below());
        if (!(blockEntity instanceof FluidTankBlockEntity tank)) {
            return 0.0D;
        }
        FluidTankBlockEntity controller = tank.getControllerBE();
        if (controller == null || controller.isRemoved() || !controller.boiler.isActive()) {
            return 0.0D;
        }
        return Mth.clamp(
                controller.boiler.getEngineEfficiency(controller.getTotalTankSize()),
                0.0F,
                1.0F
        );
    }

    static void notifyBoiler(Level level, BlockPos burnerPos, BlockState burnerState) {
        if (level == null || level.isClientSide) {
            return;
        }
        FluidTankBlock.updateBoilerState(burnerState, level, burnerPos.below());
    }
}
