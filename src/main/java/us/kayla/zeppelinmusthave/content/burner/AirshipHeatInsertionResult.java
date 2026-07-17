package us.kayla.zeppelinmusthave.content.burner;

public record AirshipHeatInsertionResult(
        boolean accepted,
        int acceptedTicks,
        AirshipHeatGrade grade,
        boolean infinite
) {
    public static final AirshipHeatInsertionResult REJECTED = new AirshipHeatInsertionResult(
            false,
            0,
            AirshipHeatGrade.NONE,
            false
    );
}
