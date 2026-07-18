package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.BlockItem;
import us.kayla.zeppelinmusthave.content.ballast.BallastTankBlock;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlock;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerTier;
import us.kayla.zeppelinmusthave.content.control.AltitudeGaugeBlock;
import us.kayla.zeppelinmusthave.content.control.fcn.ControlReceiverBlock;
import us.kayla.zeppelinmusthave.content.control.fcn.ControlTransmitterBlock;
import us.kayla.zeppelinmusthave.content.control.fcn.EmergencyCutoffBlock;
import us.kayla.zeppelinmusthave.content.control.fcn.EngineTelegraphBlock;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightComputerBlock;
import us.kayla.zeppelinmusthave.content.helm.AirshipHelmBlock;
import us.kayla.zeppelinmusthave.content.mooring.MooringWinchBlock;
import us.kayla.zeppelinmusthave.content.thruster.VerticalThrusterBlock;

/** Airship control, lift, and service blocks. */
final class ZmhAirshipBlocks {
    static final RegisteredBlock<AirshipHelmBlock, BlockItem> HELM =
            ZmhBlockRegistrar.register("airship_helm", () -> new AirshipHelmBlock(ZmhBlockProperties.metal()));

    static final RegisteredBlock<FlightComputerBlock, BlockItem> FLIGHT_COMPUTER =
            ZmhBlockRegistrar.register(
                    "flight_computer",
                    () -> new FlightComputerBlock(ZmhBlockProperties.metal().noOcclusion())
            );
    static final RegisteredBlock<EngineTelegraphBlock, BlockItem> ENGINE_TELEGRAPH =
            ZmhBlockRegistrar.register(
                    "engine_telegraph",
                    () -> new EngineTelegraphBlock(ZmhBlockProperties.metal().noOcclusion())
            );
    static final RegisteredBlock<EmergencyCutoffBlock, BlockItem> EMERGENCY_CUTOFF =
            ZmhBlockRegistrar.register(
                    "emergency_cutoff",
                    () -> new EmergencyCutoffBlock(ZmhBlockProperties.metal().noOcclusion())
            );
    static final RegisteredBlock<ControlTransmitterBlock, BlockItem> CONTROL_TRANSMITTER =
            ZmhBlockRegistrar.register(
                    "control_transmitter",
                    () -> new ControlTransmitterBlock(ZmhBlockProperties.metal().noOcclusion())
            );
    static final RegisteredBlock<ControlReceiverBlock, BlockItem> CONTROL_RECEIVER =
            ZmhBlockRegistrar.register(
                    "control_receiver",
                    () -> new ControlReceiverBlock(ZmhBlockProperties.metal().noOcclusion())
            );

    static final RegisteredBlock<AirshipBurnerBlock, BlockItem> STANDARD_BURNER =
            burner("airship_burner", AirshipBurnerTier.STANDARD);
    static final RegisteredBlock<AirshipBurnerBlock, BlockItem> FORCED_DRAFT_BURNER =
            burner("forced_draft_airship_burner", AirshipBurnerTier.FORCED_DRAFT);
    static final RegisteredBlock<AirshipBurnerBlock, BlockItem> INDUSTRIAL_BURNER =
            burner("industrial_airship_burner", AirshipBurnerTier.INDUSTRIAL);

    static final RegisteredBlock<BallastTankBlock, BlockItem> BALLAST_TANK =
            ZmhBlockRegistrar.register(
                    "ballast_tank",
                    () -> new BallastTankBlock(ZmhBlockProperties.ballastTank())
            );
    static final RegisteredBlock<MooringWinchBlock, BlockItem> MOORING_WINCH =
            ZmhBlockRegistrar.register(
                    "mooring_winch",
                    () -> new MooringWinchBlock(ZmhBlockProperties.mooringWinch())
            );
    static final RegisteredBlock<AltitudeGaugeBlock, BlockItem> ALTITUDE_GAUGE =
            ZmhBlockRegistrar.register(
                    "altitude_gauge",
                    () -> new AltitudeGaugeBlock(ZmhBlockProperties.altitudeGauge())
            );
    static final RegisteredBlock<VerticalThrusterBlock, BlockItem> VERTICAL_THRUSTER =
            ZmhBlockRegistrar.register(
                    "vertical_thruster",
                    () -> new VerticalThrusterBlock(ZmhBlockProperties.verticalThruster())
            );

    private ZmhAirshipBlocks() {
    }

    private static RegisteredBlock<AirshipBurnerBlock, BlockItem> burner(
            String name,
            AirshipBurnerTier tier
    ) {
        return ZmhBlockRegistrar.register(
                name,
                () -> new AirshipBurnerBlock(ZmhBlockProperties.burner(), tier)
        );
    }
}
