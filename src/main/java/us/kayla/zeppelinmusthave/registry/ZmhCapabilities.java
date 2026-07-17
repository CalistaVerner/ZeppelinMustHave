package us.kayla.zeppelinmusthave.registry;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import us.kayla.zeppelinmusthave.content.ballast.BallastTankBlockEntity;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlockEntity;

/** Single capability-registration entry point for block entities. */
public final class ZmhCapabilities {
    private ZmhCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        BoilerGradeBlockEntity.registerCapabilities(event);
        BallastTankBlockEntity.registerCapabilities(event);
    }
}
