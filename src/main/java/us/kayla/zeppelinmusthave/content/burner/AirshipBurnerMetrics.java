package us.kayla.zeppelinmusthave.content.burner;

import us.kayla.zeppelinmusthave.integration.BalloonHeatAggregate;

/**
 * Derived operating values shared by chat status, Engineer's Goggles, and
 * diagnostics. Keeping the calculations here prevents UI surfaces from
 * implementing their own burner logic.
 */
public record AirshipBurnerMetrics(
        AirshipBurnerProfile profile,
        AirshipHeatSnapshot reservoir,
        int signalStrength,
        double throttle,
        double individualGasOutput,
        double fuelUsePerTick,
        BalloonHeatAggregate balloonHeat
) {
    public static AirshipBurnerMetrics capture(
            AirshipBurnerProfile profile,
            AirshipHeatReservoir reservoir,
            int signalStrength,
            double individualGasOutput,
            BalloonHeatAggregate balloonHeat
    ) {
        double throttle = profile.throttleForSignal(signalStrength);
        return new AirshipBurnerMetrics(
                profile,
                reservoir.snapshot(profile.fuelCapacityTicks()),
                signalStrength,
                throttle,
                Math.max(0.0D, individualGasOutput),
                profile.fuelUsePerTickAtFullPower() * throttle,
                balloonHeat
        );
    }
}
