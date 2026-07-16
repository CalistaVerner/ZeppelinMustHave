package us.kayla.zeppelinmusthave.content.control;

import net.minecraft.util.Mth;

public final class AltitudeControlMath {
    private AltitudeControlMath() {
    }

    public static int altitudeSignal(
            double altitude,
            int minimumBuildHeight,
            int maximumBuildHeight
    ) {
        int span = Math.max(1, maximumBuildHeight - minimumBuildHeight);
        double normalized = (altitude - minimumBuildHeight) / span;
        return analog(normalized);
    }

    public static int verticalSpeedSignal(
            double verticalSpeed,
            double fullScale
    ) {
        double safeScale = Math.max(1.0E-6D, fullScale);
        double normalized = 0.5D + 0.5D * Mth.clamp(verticalSpeed / safeScale, -1.0D, 1.0D);
        return analog(normalized);
    }

    public static int balloonFillSignal(double fillRatio) {
        return analog(fillRatio);
    }

    public static int holdSignal(
            int trimSignal,
            double targetAltitude,
            double currentAltitude,
            double verticalSpeed,
            AltitudeControlProfile profile
    ) {
        double error = targetAltitude - currentAltitude;
        if (Math.abs(error) <= profile.holdDeadbandBlocks()) {
            error = 0.0D;
        }

        double correction = error * profile.holdProportionalGain()
                - verticalSpeed * profile.holdVerticalDampingGain();
        correction = Mth.clamp(
                correction,
                -profile.holdMaximumCorrection(),
                profile.holdMaximumCorrection()
        );

        return Mth.clamp((int) Math.round(trimSignal + correction), 0, 15);
    }

    public static int slew(
            int previousSignal,
            int targetSignal,
            int maximumStep
    ) {
        int step = Mth.clamp(maximumStep, 1, 15);
        if (targetSignal > previousSignal) {
            return Math.min(targetSignal, previousSignal + step);
        }
        if (targetSignal < previousSignal) {
            return Math.max(targetSignal, previousSignal - step);
        }
        return previousSignal;
    }

    private static int analog(double normalized) {
        return Mth.clamp((int) Math.round(Mth.clamp(normalized, 0.0D, 1.0D) * 15.0D), 0, 15);
    }
}
