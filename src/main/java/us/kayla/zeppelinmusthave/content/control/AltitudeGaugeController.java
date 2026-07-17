package us.kayla.zeppelinmusthave.content.control;

import us.kayla.zeppelinmusthave.content.helm.AirshipFlightSnapshot;

/** Pure control calculation; contains no world access or synchronization. */
final class AltitudeGaugeController {
    private AltitudeGaugeController() {
    }

    static int desiredOutput(
            AltitudeGaugeMode mode,
            boolean altitudeHoldEnabled,
            int trimInput,
            double targetAltitude,
            AirshipFlightSnapshot snapshot,
            AltitudeControlProfile profile,
            int minimumBuildHeight,
            int maximumBuildHeight
    ) {
        if (!snapshot.attached()) {
            return 0;
        }

        return switch (mode) {
            case ALTITUDE -> AltitudeControlMath.altitudeSignal(
                    snapshot.worldY(),
                    minimumBuildHeight,
                    maximumBuildHeight
            );
            case VERTICAL_SPEED -> AltitudeControlMath.verticalSpeedSignal(
                    snapshot.velocityY(),
                    profile.verticalSpeedFullScale()
            );
            case BALLOON_FILL -> AltitudeControlMath.balloonFillSignal(snapshot.balloonFillRatio());
            case ALTITUDE_HOLD -> altitudeHoldEnabled
                    ? AltitudeControlMath.holdSignal(
                            trimInput,
                            targetAltitude,
                            snapshot.worldY(),
                            snapshot.velocityY(),
                            profile
                    )
                    : trimInput;
        };
    }
}
