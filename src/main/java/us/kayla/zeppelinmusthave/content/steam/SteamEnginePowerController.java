package us.kayla.zeppelinmusthave.content.steam;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightControlNetworkManager;

/** Resolves boiler efficiency, emergency state and FCN throttle into one operating point. */
final class SteamEnginePowerController {
    private SteamEnginePowerController() {
    }

    static float efficiency(
            Level level,
            BlockPos enginePos,
            FluidTankBlockEntity tank,
            boolean flightControlOverride,
            int flightControlCommand
    ) {
        float efficiency = Mth.clamp(
                tank.boiler.getEngineEfficiency(tank.getTotalTankSize()),
                0.0F,
                1.0F
        );
        if (!level.isClientSide && FlightControlNetworkManager.isEmergencyLatched(level, enginePos)) {
            return 0.0F;
        }
        if (flightControlOverride) {
            efficiency *= Math.abs(flightControlCommand) / 15.0F;
        }
        return efficiency;
    }
}
