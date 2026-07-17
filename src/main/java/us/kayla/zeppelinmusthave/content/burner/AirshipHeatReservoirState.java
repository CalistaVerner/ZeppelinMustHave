package us.kayla.zeppelinmusthave.content.burner;

record AirshipHeatReservoirState(
        int regularTicks,
        int superheatedTicks,
        double consumptionRemainder,
        boolean infinite,
        AirshipHeatGrade infiniteGrade
) {
    AirshipHeatReservoirState {
        regularTicks = Math.max(0, regularTicks);
        superheatedTicks = Math.max(0, superheatedTicks);
        consumptionRemainder = Math.max(0.0D, consumptionRemainder);
        infiniteGrade = infiniteGrade == null || infiniteGrade == AirshipHeatGrade.NONE
                ? AirshipHeatGrade.SUPERHEATED
                : infiniteGrade;
    }
}
