package us.kayla.zeppelinmusthave.integration;

import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Read-only summary of Aeronautics' native heater collection.
 *
 * <p>This record does not form a parallel heat network. It observes the
 * providers already merged by {@link Balloon#getHeaters()}.</p>
 */
public record BalloonHeatAggregate(
        int connectedSources,
        int activeSources,
        double combinedGasOutput
) {
    public static final BalloonHeatAggregate EMPTY = new BalloonHeatAggregate(0, 0, 0.0D);

    public BalloonHeatAggregate {
        connectedSources = Math.max(0, connectedSources);
        activeSources = Math.max(0, Math.min(activeSources, connectedSources));
        combinedGasOutput = Math.max(0.0D, combinedGasOutput);
    }

    public static BalloonHeatAggregate from(@Nullable Balloon balloon) {
        if (balloon == null) {
            return EMPTY;
        }

        int connected = 0;
        int active = 0;
        double output = 0.0D;

        for (BlockEntityLiftingGasProvider provider : balloon.getHeaters()) {
            connected++;
            if (!provider.canOutputGas()) {
                continue;
            }
            active++;
            output += Math.max(0.0D, provider.getGasOutput());
        }

        return new BalloonHeatAggregate(connected, active, output);
    }

    public void write(CompoundTag tag) {
        tag.putInt("ConnectedHeatSources", this.connectedSources);
        tag.putInt("ActiveHeatSources", this.activeSources);
        tag.putDouble("CombinedHeatGasOutput", this.combinedGasOutput);
    }

    public static BalloonHeatAggregate read(CompoundTag tag) {
        return new BalloonHeatAggregate(
                tag.getInt("ConnectedHeatSources"),
                tag.getInt("ActiveHeatSources"),
                tag.getDouble("CombinedHeatGasOutput")
        );
    }
}
