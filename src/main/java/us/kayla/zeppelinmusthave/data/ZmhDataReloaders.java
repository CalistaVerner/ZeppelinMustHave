package us.kayla.zeppelinmusthave.data;

import us.kayla.zeppelinmusthave.content.ballast.BallastTankProfiles;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeProfiles;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerProfiles;
import us.kayla.zeppelinmusthave.content.control.AltitudeControlProfiles;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneProfiles;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeProfiles;
import us.kayla.zeppelinmusthave.content.thruster.VerticalThrusterProfiles;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeDefinitions;

/** Single bootstrap point for every reloadable data-driven subsystem. */
public final class ZmhDataReloaders {
    private ZmhDataReloaders() {
    }

    public static void registerAll() {
        AirshipBurnerProfiles.register();
        BallastTankProfiles.register();
        BoilerGradeProfiles.register();
        SteamEngineGradeProfiles.register();
        VerticalThrusterProfiles.register();
        AirshipUpgradeDefinitions.register();
        AltitudeControlProfiles.register();
        PipedRedstoneProfiles.register();
    }
}
