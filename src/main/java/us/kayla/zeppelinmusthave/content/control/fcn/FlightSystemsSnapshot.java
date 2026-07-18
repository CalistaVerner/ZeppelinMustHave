package us.kayla.zeppelinmusthave.content.control.fcn;

import net.minecraft.nbt.CompoundTag;

/** Vessel-wide aggregate of loaded engines, burners, thrusters, and ballast. */
public record FlightSystemsSnapshot(
        int engineCount,
        int activeEngines,
        int burnerCount,
        int activeBurners,
        int thrusterCount,
        int activeThrusters,
        int ballastTankCount,
        double averageBallastFill,
        long sampledAtGameTime
) {
    public static FlightSystemsSnapshot empty(long gameTime) {
        return new FlightSystemsSnapshot(0, 0, 0, 0, 0, 0, 0, 0.0D, gameTime);
    }

    public void write(CompoundTag tag) {
        tag.putInt("EngineCount", this.engineCount);
        tag.putInt("ActiveEngines", this.activeEngines);
        tag.putInt("BurnerCount", this.burnerCount);
        tag.putInt("ActiveBurners", this.activeBurners);
        tag.putInt("ThrusterCount", this.thrusterCount);
        tag.putInt("ActiveThrusters", this.activeThrusters);
        tag.putInt("BallastTankCount", this.ballastTankCount);
        tag.putDouble("AverageBallastFill", this.averageBallastFill);
        tag.putLong("SampledAtGameTime", this.sampledAtGameTime);
    }

    public static FlightSystemsSnapshot read(CompoundTag tag) {
        return new FlightSystemsSnapshot(
                tag.getInt("EngineCount"),
                tag.getInt("ActiveEngines"),
                tag.getInt("BurnerCount"),
                tag.getInt("ActiveBurners"),
                tag.getInt("ThrusterCount"),
                tag.getInt("ActiveThrusters"),
                tag.getInt("BallastTankCount"),
                tag.getDouble("AverageBallastFill"),
                tag.getLong("SampledAtGameTime")
        );
    }
}
