package us.kayla.zeppelinmusthave.content.burner;

public enum AirshipBurnerTier {
    STANDARD(1.0, 1.0, 12_000, 16),
    FORCED_DRAFT(2.25, 1.6, 24_000, 32),
    INDUSTRIAL(4.5, 3.0, 48_000, 64);

    private final double gasOutputMultiplier;
    private final double fuelUsePerTickAtFullPower;
    private final int fuelCapacityTicks;
    private final int castRange;

    AirshipBurnerTier(
            double gasOutputMultiplier,
            double fuelUsePerTickAtFullPower,
            int fuelCapacityTicks,
            int castRange
    ) {
        this.gasOutputMultiplier = gasOutputMultiplier;
        this.fuelUsePerTickAtFullPower = fuelUsePerTickAtFullPower;
        this.fuelCapacityTicks = fuelCapacityTicks;
        this.castRange = castRange;
    }

    public double gasOutputMultiplier() {
        return this.gasOutputMultiplier;
    }

    public double fuelUsePerTickAtFullPower() {
        return this.fuelUsePerTickAtFullPower;
    }

    public int fuelCapacityTicks() {
        return this.fuelCapacityTicks;
    }

    public int castRange() {
        return this.castRange;
    }
}
