package us.kayla.zeppelinmusthave.content.boiler;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.fluids.tank.BoilerData;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeBlock;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeProfiles;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Create's native boiler calculation with one deliberate extension: every
 * heater column is transformed by the owning boiler grade profile.
 */
final class GradedBoilerData extends BoilerData {
    private final Supplier<BoilerGradeProfile> profileSupplier;

    GradedBoilerData(Supplier<BoilerGradeProfile> profileSupplier) {
        this.profileSupplier = Objects.requireNonNull(profileSupplier, "profileSupplier");
    }

    @Override
    public boolean evaluate(FluidTankBlockEntity controller) {
        int previousEngines = this.attachedEngines;
        int previousWhistles = this.attachedWhistles;
        boolean nativeChanged = super.evaluate(controller);

        Level level = controller.getLevel();
        if (level == null) {
            return nativeChanged;
        }

        BlockPos controllerPos = controller.getBlockPos();
        int width = controller.getWidth();
        int height = controller.getHeight();
        for (int yOffset = 0; yOffset < height; yOffset++) {
            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {
                    BlockPos tankPos = controllerPos.offset(xOffset, yOffset, zOffset);
                    for (Direction direction : Iterate.directions) {
                        BlockState attachedState = level.getBlockState(tankPos.relative(direction));
                        if (!(attachedState.getBlock() instanceof SteamEngineGradeBlock engine)) {
                            continue;
                        }
                        if (SteamEngineBlock.getFacing(attachedState) != direction) {
                            continue;
                        }
                        this.attachedEngines += SteamEngineGradeProfiles.INSTANCE
                                .resolve(engine.tier())
                                .boilerLoadUnits();
                    }
                }
            }
        }

        this.needsHeatLevelUpdate = true;
        return nativeChanged
                || previousEngines != this.attachedEngines
                || previousWhistles != this.attachedWhistles;
    }

    @Override
    public boolean updateTemperature(FluidTankBlockEntity controller) {
        Level level = controller.getLevel();
        if (level == null) {
            return false;
        }

        BlockPos controllerPos = controller.getBlockPos();
        BoilerGradeProfile profile = this.profileSupplier.get();
        this.needsHeatLevelUpdate = false;

        boolean previousPassive = this.passiveHeat;
        int previousActive = this.activeHeat;
        this.passiveHeat = false;
        this.activeHeat = 0;

        int width = controller.getWidth();
        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos heaterPos = controllerPos.offset(xOffset, -1, zOffset);
                BlockState heaterState = level.getBlockState(heaterPos);
                float sourceHeat = BoilerHeater.findHeat(level, heaterPos, heaterState);
                int transferredHeat = profile.transfer(sourceHeat);

                if (transferredHeat == BoilerHeater.PASSIVE_HEAT) {
                    this.passiveHeat = true;
                } else if (transferredHeat > BoilerHeater.PASSIVE_HEAT) {
                    this.activeHeat += transferredHeat;
                }
            }
        }

        this.passiveHeat &= this.activeHeat == 0;
        return previousActive != this.activeHeat || previousPassive != this.passiveHeat;
    }
}
