package us.kayla.zeppelinmusthave.content.burner;

public record AirshipHeatConsumptionResult(
        boolean changed,
        int requestedTicks,
        int consumedTicks,
        AirshipHeatGrade gradeBefore,
        AirshipHeatGrade gradeAfter,
        boolean gradeChanged,
        boolean depleted
) {
    public static AirshipHeatConsumptionResult unchanged(
            AirshipHeatGrade gradeBefore,
            AirshipHeatGrade gradeAfter
    ) {
        return new AirshipHeatConsumptionResult(
                false,
                0,
                0,
                gradeBefore,
                gradeAfter,
                gradeBefore != gradeAfter,
                gradeAfter == AirshipHeatGrade.NONE
        );
    }
}
