package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import us.kayla.zeppelinmusthave.content.ballast.BallastTankBlock;
import us.kayla.zeppelinmusthave.content.balloon.TieredEnvelopeBlock;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlock;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeItem;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlock;
import us.kayla.zeppelinmusthave.content.control.AltitudeGaugeBlock;
import us.kayla.zeppelinmusthave.content.control.fcn.ControlReceiverBlock;
import us.kayla.zeppelinmusthave.content.control.fcn.ControlTransmitterBlock;
import us.kayla.zeppelinmusthave.content.control.fcn.EmergencyCutoffBlock;
import us.kayla.zeppelinmusthave.content.control.fcn.EngineTelegraphBlock;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightComputerBlock;
import us.kayla.zeppelinmusthave.content.fluid.TieredFluidPipeBlock;
import us.kayla.zeppelinmusthave.content.fluid.TieredGlassFluidPipeBlock;
import us.kayla.zeppelinmusthave.content.helm.AirshipHelmBlock;
import us.kayla.zeppelinmusthave.content.kinetics.OmniSpeedControllerBlock;
import us.kayla.zeppelinmusthave.content.mooring.MooringWinchBlock;
import us.kayla.zeppelinmusthave.content.steam.LeviathanSteamEngineBlock;
import us.kayla.zeppelinmusthave.content.steam.MkViiSteamEngineBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneRepeaterBlock;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeBlock;
import us.kayla.zeppelinmusthave.content.thruster.VerticalThrusterBlock;

/**
 * Stable public facade for block and block-item handles.
 *
 * <p>Actual definitions are split into domain catalogs. Keeping this facade
 * preserves all existing source references and registry IDs.</p>
 */
public final class ZmhBlocks {
    public static final DeferredBlock<AirshipHelmBlock> AIRSHIP_HELM = ZmhAirshipBlocks.HELM.block();
    public static final DeferredItem<BlockItem> AIRSHIP_HELM_ITEM = ZmhAirshipBlocks.HELM.item();

    public static final DeferredBlock<FlightComputerBlock> FLIGHT_COMPUTER = ZmhAirshipBlocks.FLIGHT_COMPUTER.block();
    public static final DeferredItem<BlockItem> FLIGHT_COMPUTER_ITEM = ZmhAirshipBlocks.FLIGHT_COMPUTER.item();
    public static final DeferredBlock<EngineTelegraphBlock> ENGINE_TELEGRAPH = ZmhAirshipBlocks.ENGINE_TELEGRAPH.block();
    public static final DeferredItem<BlockItem> ENGINE_TELEGRAPH_ITEM = ZmhAirshipBlocks.ENGINE_TELEGRAPH.item();
    public static final DeferredBlock<EmergencyCutoffBlock> EMERGENCY_CUTOFF = ZmhAirshipBlocks.EMERGENCY_CUTOFF.block();
    public static final DeferredItem<BlockItem> EMERGENCY_CUTOFF_ITEM = ZmhAirshipBlocks.EMERGENCY_CUTOFF.item();
    public static final DeferredBlock<ControlTransmitterBlock> CONTROL_TRANSMITTER = ZmhAirshipBlocks.CONTROL_TRANSMITTER.block();
    public static final DeferredItem<BlockItem> CONTROL_TRANSMITTER_ITEM = ZmhAirshipBlocks.CONTROL_TRANSMITTER.item();
    public static final DeferredBlock<ControlReceiverBlock> CONTROL_RECEIVER = ZmhAirshipBlocks.CONTROL_RECEIVER.block();
    public static final DeferredItem<BlockItem> CONTROL_RECEIVER_ITEM = ZmhAirshipBlocks.CONTROL_RECEIVER.item();

    public static final DeferredBlock<AirshipBurnerBlock> AIRSHIP_BURNER = ZmhAirshipBlocks.STANDARD_BURNER.block();
    public static final DeferredItem<BlockItem> AIRSHIP_BURNER_ITEM = ZmhAirshipBlocks.STANDARD_BURNER.item();
    public static final DeferredBlock<AirshipBurnerBlock> FORCED_DRAFT_AIRSHIP_BURNER =
            ZmhAirshipBlocks.FORCED_DRAFT_BURNER.block();
    public static final DeferredItem<BlockItem> FORCED_DRAFT_AIRSHIP_BURNER_ITEM =
            ZmhAirshipBlocks.FORCED_DRAFT_BURNER.item();
    public static final DeferredBlock<AirshipBurnerBlock> INDUSTRIAL_AIRSHIP_BURNER =
            ZmhAirshipBlocks.INDUSTRIAL_BURNER.block();
    public static final DeferredItem<BlockItem> INDUSTRIAL_AIRSHIP_BURNER_ITEM =
            ZmhAirshipBlocks.INDUSTRIAL_BURNER.item();

    public static final DeferredBlock<TieredEnvelopeBlock> REINFORCED_ENVELOPE =
            ZmhEnvelopeBlocks.REINFORCED_ENVELOPE.block();
    public static final DeferredItem<BlockItem> REINFORCED_ENVELOPE_ITEM =
            ZmhEnvelopeBlocks.REINFORCED_ENVELOPE.item();
    public static final DeferredBlock<TieredEnvelopeBlock> INDUSTRIAL_ENVELOPE =
            ZmhEnvelopeBlocks.INDUSTRIAL_ENVELOPE.block();
    public static final DeferredItem<BlockItem> INDUSTRIAL_ENVELOPE_ITEM =
            ZmhEnvelopeBlocks.INDUSTRIAL_ENVELOPE.item();

    public static final DeferredBlock<BoilerGradeBlock> COPPER_BOILER_BASE = ZmhSteamPowerBlocks.COPPER_BOILER.block();
    public static final DeferredItem<BoilerGradeItem> COPPER_BOILER_BASE_ITEM = ZmhSteamPowerBlocks.COPPER_BOILER.item();
    public static final DeferredBlock<BoilerGradeBlock> BRASS_BOILER_BASE = ZmhSteamPowerBlocks.BRASS_BOILER.block();
    public static final DeferredItem<BoilerGradeItem> BRASS_BOILER_BASE_ITEM = ZmhSteamPowerBlocks.BRASS_BOILER.item();
    public static final DeferredBlock<BoilerGradeBlock> INDUSTRIAL_BOILER_BASE =
            ZmhSteamPowerBlocks.INDUSTRIAL_BOILER.block();
    public static final DeferredItem<BoilerGradeItem> INDUSTRIAL_BOILER_BASE_ITEM =
            ZmhSteamPowerBlocks.INDUSTRIAL_BOILER.item();

    public static final DeferredBlock<SteamEngineGradeBlock> COPPER_STEAM_ENGINE =
            ZmhSteamPowerBlocks.COPPER_ENGINE.block();
    public static final DeferredItem<BlockItem> COPPER_STEAM_ENGINE_ITEM = ZmhSteamPowerBlocks.COPPER_ENGINE.item();
    public static final DeferredBlock<SteamEngineGradeBlock> BRASS_STEAM_ENGINE =
            ZmhSteamPowerBlocks.BRASS_ENGINE.block();
    public static final DeferredItem<BlockItem> BRASS_STEAM_ENGINE_ITEM = ZmhSteamPowerBlocks.BRASS_ENGINE.item();
    public static final DeferredBlock<SteamEngineGradeBlock> INDUSTRIAL_STEAM_ENGINE =
            ZmhSteamPowerBlocks.INDUSTRIAL_ENGINE.block();
    public static final DeferredItem<BlockItem> INDUSTRIAL_STEAM_ENGINE_ITEM =
            ZmhSteamPowerBlocks.INDUSTRIAL_ENGINE.item();
    public static final DeferredBlock<SteamEngineGradeBlock> GRAND_STEAM_ENGINE =
            ZmhSteamPowerBlocks.GRAND_ENGINE.block();
    public static final DeferredItem<BlockItem> GRAND_STEAM_ENGINE_ITEM =
            ZmhSteamPowerBlocks.GRAND_ENGINE.item();
    public static final DeferredBlock<SteamEngineGradeBlock> SOVEREIGN_STEAM_ENGINE =
            ZmhSteamPowerBlocks.SOVEREIGN_ENGINE.block();
    public static final DeferredItem<BlockItem> SOVEREIGN_STEAM_ENGINE_ITEM =
            ZmhSteamPowerBlocks.SOVEREIGN_ENGINE.item();
    public static final DeferredBlock<LeviathanSteamEngineBlock> LEVIATHAN_STEAM_ENGINE =
            ZmhSteamPowerBlocks.LEVIATHAN_ENGINE.block();
    public static final DeferredItem<BlockItem> LEVIATHAN_STEAM_ENGINE_ITEM =
            ZmhSteamPowerBlocks.LEVIATHAN_ENGINE.item();
    public static final DeferredBlock<MkViiSteamEngineBlock> MK_VII_STEAM_ENGINE =
            ZmhSteamPowerBlocks.MK_VII_ENGINE.block();
    public static final DeferredItem<BlockItem> MK_VII_STEAM_ENGINE_ITEM =
            ZmhSteamPowerBlocks.MK_VII_ENGINE.item();


    public static final DeferredBlock<OmniSpeedControllerBlock> OMNI_SPEED_CONTROLLER =
            ZmhKineticBlocks.OMNI_SPEED_CONTROLLER.block();
    public static final DeferredItem<BlockItem> OMNI_SPEED_CONTROLLER_ITEM =
            ZmhKineticBlocks.OMNI_SPEED_CONTROLLER.item();

    public static final DeferredBlock<PipedRedstoneBlock> COPPER_PIPED_REDSTONE = ZmhRedstoneBlocks.COPPER_CONDUIT.block();
    public static final DeferredItem<BlockItem> COPPER_PIPED_REDSTONE_ITEM = ZmhRedstoneBlocks.COPPER_CONDUIT.item();
    public static final DeferredBlock<PipedRedstoneBlock> BRASS_PIPED_REDSTONE = ZmhRedstoneBlocks.BRASS_CONDUIT.block();
    public static final DeferredItem<BlockItem> BRASS_PIPED_REDSTONE_ITEM = ZmhRedstoneBlocks.BRASS_CONDUIT.item();
    public static final DeferredBlock<PipedRedstoneBlock> RESONANT_PIPED_REDSTONE = ZmhRedstoneBlocks.RESONANT_CONDUIT.block();
    public static final DeferredItem<BlockItem> RESONANT_PIPED_REDSTONE_ITEM = ZmhRedstoneBlocks.RESONANT_CONDUIT.item();
    public static final DeferredBlock<TieredFluidPipeBlock> REINFORCED_FLUID_PIPE =
            ZmhFluidBlocks.REINFORCED_FLUID_PIPE.block();
    public static final DeferredItem<BlockItem> REINFORCED_FLUID_PIPE_ITEM =
            ZmhFluidBlocks.REINFORCED_FLUID_PIPE.item();
    public static final DeferredBlock<TieredFluidPipeBlock> INDUSTRIAL_FLUID_PIPE =
            ZmhFluidBlocks.INDUSTRIAL_FLUID_PIPE.block();
    public static final DeferredItem<BlockItem> INDUSTRIAL_FLUID_PIPE_ITEM =
            ZmhFluidBlocks.INDUSTRIAL_FLUID_PIPE.item();
    public static final DeferredBlock<TieredGlassFluidPipeBlock> REINFORCED_GLASS_FLUID_PIPE =
            ZmhFluidBlocks.REINFORCED_GLASS_FLUID_PIPE;
    public static final DeferredBlock<TieredGlassFluidPipeBlock> INDUSTRIAL_GLASS_FLUID_PIPE =
            ZmhFluidBlocks.INDUSTRIAL_GLASS_FLUID_PIPE;

    public static final DeferredBlock<PipedRedstoneNativeLeverBlock> PIPED_REDSTONE_NATIVE_LEVER =
            ZmhRedstoneBlocks.NATIVE_LEVER.block();
    public static final DeferredItem<BlockItem> PIPED_REDSTONE_NATIVE_LEVER_ITEM = ZmhRedstoneBlocks.NATIVE_LEVER.item();
    public static final DeferredBlock<PipedRedstoneRepeaterBlock> PIPED_REDSTONE_REPEATER =
            ZmhRedstoneBlocks.REPEATER.block();
    public static final DeferredItem<BlockItem> PIPED_REDSTONE_REPEATER_ITEM = ZmhRedstoneBlocks.REPEATER.item();

    public static final DeferredBlock<BallastTankBlock> BALLAST_TANK = ZmhAirshipBlocks.BALLAST_TANK.block();
    public static final DeferredItem<BlockItem> BALLAST_TANK_ITEM = ZmhAirshipBlocks.BALLAST_TANK.item();
    public static final DeferredBlock<MooringWinchBlock> MOORING_WINCH = ZmhAirshipBlocks.MOORING_WINCH.block();
    public static final DeferredItem<BlockItem> MOORING_WINCH_ITEM = ZmhAirshipBlocks.MOORING_WINCH.item();
    public static final DeferredBlock<AltitudeGaugeBlock> ALTITUDE_GAUGE = ZmhAirshipBlocks.ALTITUDE_GAUGE.block();
    public static final DeferredItem<BlockItem> ALTITUDE_GAUGE_ITEM = ZmhAirshipBlocks.ALTITUDE_GAUGE.item();
    public static final DeferredBlock<VerticalThrusterBlock> VERTICAL_THRUSTER = ZmhAirshipBlocks.VERTICAL_THRUSTER.block();
    public static final DeferredItem<BlockItem> VERTICAL_THRUSTER_ITEM = ZmhAirshipBlocks.VERTICAL_THRUSTER.item();

    private ZmhBlocks() {
    }

    static void bootstrap() {
        // Loading this facade initializes every domain catalog.
    }

}
