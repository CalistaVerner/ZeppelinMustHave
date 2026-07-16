package us.kayla.zeppelinmusthave.content.burner;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Normalized heat contribution produced from an item accepted by Create or
 * NeoForge fuel APIs.
 */
public record AirshipHeatSource(
        ResourceLocation sourceId,
        AirshipHeatGrade grade,
        int burnTicks,
        boolean infinite
) {
    public AirshipHeatSource {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(grade, "grade");
        if (grade == AirshipHeatGrade.NONE) {
            throw new IllegalArgumentException("A heat source must have an active heat grade");
        }
        if (!infinite && burnTicks <= 0) {
            throw new IllegalArgumentException("A finite heat source must provide positive burn time");
        }
    }

    public static AirshipHeatSource finite(
            ResourceLocation sourceId,
            AirshipHeatGrade grade,
            int burnTicks
    ) {
        return new AirshipHeatSource(sourceId, grade, burnTicks, false);
    }

    public static AirshipHeatSource infinite(
            ResourceLocation sourceId,
            AirshipHeatGrade grade
    ) {
        return new AirshipHeatSource(sourceId, grade, 0, true);
    }
}
