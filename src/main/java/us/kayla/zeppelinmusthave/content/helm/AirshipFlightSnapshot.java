package us.kayla.zeppelinmusthave.content.helm;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable, server-authored telemetry exposed by an Airship Helm.
 *
 * <p>Linear velocity is expressed in blocks per second and angular velocity
 * in radians per second, matching Sable's physics API.</p>
 */
public record AirshipFlightSnapshot(
        boolean attached,
        @Nullable UUID subLevelId,
        String subLevelName,
        double worldX,
        double worldY,
        double worldZ,
        double headingDegrees,
        double pitchDegrees,
        double rollDegrees,
        double velocityX,
        double velocityY,
        double velocityZ,
        double angularVelocityX,
        double angularVelocityY,
        double angularVelocityZ,
        double mass,
        double centerOfMassX,
        double centerOfMassY,
        double centerOfMassZ,
        int balloonCount,
        int balloonCapacity,
        double balloonFilledVolume,
        double balloonTargetVolume,
        double balloonLift,
        long sampledAtGameTime
) {
    private static final double POSITION_EPSILON = 1.0E-3;
    private static final double ANGLE_EPSILON = 1.0E-2;
    private static final double VELOCITY_EPSILON = 1.0E-3;
    private static final double AERONAUTICS_EPSILON = 1.0E-3;

    public AirshipFlightSnapshot {
        subLevelName = Objects.requireNonNullElse(subLevelName, "");
    }

    public static AirshipFlightSnapshot detached(long gameTime) {
        return new AirshipFlightSnapshot(
                false,
                null,
                "",
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0,
                0,
                0.0,
                0.0,
                0.0,
                gameTime
        );
    }

    public double speed() {
        return Math.sqrt(this.velocityX * this.velocityX
                + this.velocityY * this.velocityY
                + this.velocityZ * this.velocityZ);
    }

    public double balloonFillRatio() {
        if (this.balloonCapacity <= 0) {
            return 0.0;
        }
        return Math.clamp(this.balloonFilledVolume / this.balloonCapacity, 0.0, 1.0);
    }

    public String displayName() {
        if (!this.subLevelName.isBlank()) {
            return this.subLevelName;
        }
        if (this.subLevelId == null) {
            return "detached";
        }
        String id = this.subLevelId.toString();
        return "airship-" + id.substring(0, 8);
    }

    public boolean materiallyDiffersFrom(AirshipFlightSnapshot other) {
        if (other == null
                || this.attached != other.attached
                || !Objects.equals(this.subLevelId, other.subLevelId)
                || !Objects.equals(this.subLevelName, other.subLevelName)
                || this.balloonCount != other.balloonCount
                || this.balloonCapacity != other.balloonCapacity) {
            return true;
        }

        return differs(this.worldX, other.worldX, POSITION_EPSILON)
                || differs(this.worldY, other.worldY, POSITION_EPSILON)
                || differs(this.worldZ, other.worldZ, POSITION_EPSILON)
                || differs(this.headingDegrees, other.headingDegrees, ANGLE_EPSILON)
                || differs(this.pitchDegrees, other.pitchDegrees, ANGLE_EPSILON)
                || differs(this.rollDegrees, other.rollDegrees, ANGLE_EPSILON)
                || differs(this.velocityX, other.velocityX, VELOCITY_EPSILON)
                || differs(this.velocityY, other.velocityY, VELOCITY_EPSILON)
                || differs(this.velocityZ, other.velocityZ, VELOCITY_EPSILON)
                || differs(this.angularVelocityX, other.angularVelocityX, VELOCITY_EPSILON)
                || differs(this.angularVelocityY, other.angularVelocityY, VELOCITY_EPSILON)
                || differs(this.angularVelocityZ, other.angularVelocityZ, VELOCITY_EPSILON)
                || differs(this.mass, other.mass, AERONAUTICS_EPSILON)
                || differs(this.centerOfMassX, other.centerOfMassX, POSITION_EPSILON)
                || differs(this.centerOfMassY, other.centerOfMassY, POSITION_EPSILON)
                || differs(this.centerOfMassZ, other.centerOfMassZ, POSITION_EPSILON)
                || differs(this.balloonFilledVolume, other.balloonFilledVolume, AERONAUTICS_EPSILON)
                || differs(this.balloonTargetVolume, other.balloonTargetVolume, AERONAUTICS_EPSILON)
                || differs(this.balloonLift, other.balloonLift, AERONAUTICS_EPSILON);
    }

    public void write(CompoundTag tag) {
        tag.putBoolean("Attached", this.attached);
        if (this.subLevelId != null) {
            tag.putUUID("SubLevelId", this.subLevelId);
        }
        tag.putString("SubLevelName", this.subLevelName);
        tag.putDouble("WorldX", this.worldX);
        tag.putDouble("WorldY", this.worldY);
        tag.putDouble("WorldZ", this.worldZ);
        tag.putDouble("Heading", this.headingDegrees);
        tag.putDouble("Pitch", this.pitchDegrees);
        tag.putDouble("Roll", this.rollDegrees);
        tag.putDouble("VelocityX", this.velocityX);
        tag.putDouble("VelocityY", this.velocityY);
        tag.putDouble("VelocityZ", this.velocityZ);
        tag.putDouble("AngularVelocityX", this.angularVelocityX);
        tag.putDouble("AngularVelocityY", this.angularVelocityY);
        tag.putDouble("AngularVelocityZ", this.angularVelocityZ);
        tag.putDouble("Mass", this.mass);
        tag.putDouble("CenterOfMassX", this.centerOfMassX);
        tag.putDouble("CenterOfMassY", this.centerOfMassY);
        tag.putDouble("CenterOfMassZ", this.centerOfMassZ);
        tag.putInt("BalloonCount", this.balloonCount);
        tag.putInt("BalloonCapacity", this.balloonCapacity);
        tag.putDouble("BalloonFilledVolume", this.balloonFilledVolume);
        tag.putDouble("BalloonTargetVolume", this.balloonTargetVolume);
        tag.putDouble("BalloonLift", this.balloonLift);
        tag.putLong("SampledAtGameTime", this.sampledAtGameTime);
    }

    public static AirshipFlightSnapshot read(CompoundTag tag) {
        return new AirshipFlightSnapshot(
                tag.getBoolean("Attached"),
                tag.hasUUID("SubLevelId") ? tag.getUUID("SubLevelId") : null,
                tag.getString("SubLevelName"),
                tag.getDouble("WorldX"),
                tag.getDouble("WorldY"),
                tag.getDouble("WorldZ"),
                tag.getDouble("Heading"),
                tag.getDouble("Pitch"),
                tag.getDouble("Roll"),
                tag.getDouble("VelocityX"),
                tag.getDouble("VelocityY"),
                tag.getDouble("VelocityZ"),
                tag.getDouble("AngularVelocityX"),
                tag.getDouble("AngularVelocityY"),
                tag.getDouble("AngularVelocityZ"),
                tag.getDouble("Mass"),
                tag.getDouble("CenterOfMassX"),
                tag.getDouble("CenterOfMassY"),
                tag.getDouble("CenterOfMassZ"),
                tag.getInt("BalloonCount"),
                tag.getInt("BalloonCapacity"),
                tag.getDouble("BalloonFilledVolume"),
                tag.getDouble("BalloonTargetVolume"),
                tag.getDouble("BalloonLift"),
                tag.getLong("SampledAtGameTime")
        );
    }

    private static boolean differs(double first, double second, double epsilon) {
        return Math.abs(first - second) > epsilon;
    }
}
