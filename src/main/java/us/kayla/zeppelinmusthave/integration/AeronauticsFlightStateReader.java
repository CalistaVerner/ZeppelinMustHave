package us.kayla.zeppelinmusthave.integration;

import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.ServerBalloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.map.BalloonMap;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import us.kayla.zeppelinmusthave.content.helm.AirshipFlightSnapshot;

/**
 * Reads authoritative flight and buoyancy telemetry from Sable and
 * Create Aeronautics for the sub-level containing a Zeppelin Must Have block.
 */
public final class AeronauticsFlightStateReader {
    private AeronauticsFlightStateReader() {
    }

    public static AirshipFlightSnapshot read(Level level, BlockPos blockPos) {
        long gameTime = level.getGameTime();
        if (!(level instanceof ServerLevel serverLevel)) {
            return AirshipFlightSnapshot.detached(gameTime);
        }

        SubLevel containing = Sable.HELPER.getContaining(level, blockPos);
        if (!(containing instanceof ServerSubLevel serverSubLevel)) {
            return AirshipFlightSnapshot.detached(gameTime);
        }

        Vector3dc globalPosition = Sable.HELPER.projectOutOfSubLevel(
                level,
                JOMLConversion.atCenterOf(blockPos)
        );

        Vector3d eulerYXZ = serverSubLevel.logicalPose()
                .orientation()
                .getEulerAnglesYXZ(new Vector3d());

        Vector3d linearVelocity = new Vector3d();
        Vector3d angularVelocity = new Vector3d();
        RigidBodyHandle rigidBody = RigidBodyHandle.of(serverSubLevel);
        if (rigidBody != null && rigidBody.isValid()) {
            rigidBody.getLinearVelocity(linearVelocity);
            rigidBody.getAngularVelocity(angularVelocity);
        }

        AeronauticsBalloonTotals balloons = readBalloons(serverLevel, serverSubLevel);
        Vector3dc centerOfMass = serverSubLevel.getMassTracker().getCenterOfMass();

        return new AirshipFlightSnapshot(
                true,
                serverSubLevel.getUniqueId(),
                serverSubLevel.getName(),
                globalPosition.x(),
                globalPosition.y(),
                globalPosition.z(),
                normalizeDegrees(Math.toDegrees(eulerYXZ.y)),
                Math.toDegrees(eulerYXZ.x),
                Math.toDegrees(eulerYXZ.z),
                linearVelocity.x,
                linearVelocity.y,
                linearVelocity.z,
                angularVelocity.x,
                angularVelocity.y,
                angularVelocity.z,
                serverSubLevel.getMassTracker().getMass(),
                centerOfMass == null ? 0.0D : centerOfMass.x(),
                centerOfMass == null ? 0.0D : centerOfMass.y(),
                centerOfMass == null ? 0.0D : centerOfMass.z(),
                balloons.count,
                balloons.capacity,
                balloons.filledVolume,
                balloons.targetVolume,
                balloons.totalLift,
                gameTime
        );
    }

    private static AeronauticsBalloonTotals readBalloons(
            ServerLevel parentLevel,
            ServerSubLevel targetSubLevel
    ) {
        int count = 0;
        int capacity = 0;
        double filledVolume = 0.0;
        double targetVolume = 0.0;
        double totalLift = 0.0;

        for (Balloon balloon : BalloonMap.MAP.get(parentLevel).getBalloons()) {
            SubLevel balloonSubLevel = Sable.HELPER.getContaining(parentLevel, balloon.getControllerPos());
            if (balloonSubLevel != targetSubLevel) {
                continue;
            }

            count++;
            capacity += Math.max(0, balloon.getCapacity());

            if (balloon instanceof ServerBalloon serverBalloon) {
                filledVolume += Math.max(0.0, serverBalloon.getTotalFilledVolume());
                targetVolume += Math.max(0.0, serverBalloon.getTotalTargetVolume());
                totalLift += Math.max(0.0, serverBalloon.getTotalLift());
            }
        }

        return new AeronauticsBalloonTotals(
                count,
                capacity,
                filledVolume,
                targetVolume,
                totalLift
        );
    }

    private static double normalizeDegrees(double degrees) {
        double normalized = degrees % 360.0;
        return normalized < 0.0 ? normalized + 360.0 : normalized;
    }

    private record AeronauticsBalloonTotals(
            int count,
            int capacity,
            double filledVolume,
            double targetVolume,
            double totalLift
    ) {
    }
}
