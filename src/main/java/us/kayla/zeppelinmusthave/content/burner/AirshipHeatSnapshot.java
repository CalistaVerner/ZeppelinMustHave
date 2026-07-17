package us.kayla.zeppelinmusthave.content.burner;

public record AirshipHeatSnapshot(
        int regularTicks,
        int superheatedTicks,
        int totalTicks,
        int capacityTicks,
        double fillRatio,
        boolean infinite,
        AirshipHeatGrade activeGrade
) {
}
