package us.kayla.zeppelinmusthave.ponder;

import com.simibubi.create.AllItems;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlock;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlock;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlockEntity;
import us.kayla.zeppelinmusthave.content.control.AltitudeGaugeBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneRepeaterBlock;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;
import us.kayla.zeppelinmusthave.registry.ZmhItems;

public final class ZmhPonderScenes {
    private ZmhPonderScenes() {
    }

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ZmhBlocks.AIRSHIP_HELM.getId())
                .addStoryBoard(
                        "helm/telemetry",
                        ZmhPonderScenes::airshipHelmTelemetry,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );

        helper.forComponents(
                        ZmhBlocks.AIRSHIP_BURNER.getId(),
                        ZmhBlocks.FORCED_DRAFT_AIRSHIP_BURNER.getId(),
                        ZmhBlocks.INDUSTRIAL_AIRSHIP_BURNER.getId()
                )
                .addStoryBoard(
                        "burner/operation",
                        ZmhPonderScenes::airshipBurners,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );


        helper.forComponents(ZmhBlocks.ALTITUDE_GAUGE.getId())
                .addStoryBoard(
                        "control/altitude_hold",
                        ZmhPonderScenes::altitudeControl,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );

        helper.forComponents(
                        ZmhBlocks.COPPER_PIPED_REDSTONE.getId(),
                        ZmhBlocks.BRASS_PIPED_REDSTONE.getId(),
                        ZmhBlocks.RESONANT_PIPED_REDSTONE.getId(),
                        ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER.getId(),
                        ZmhBlocks.PIPED_REDSTONE_REPEATER.getId()
                )
                .addStoryBoard(
                        "redstone/conduits",
                        ZmhPonderScenes::pipedRedstone,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
    }

    private static void airshipHelmTelemetry(
            SceneBuilder scene,
            SceneBuildingUtil util
    ) {
        BlockPos helmPos = util.grid().at(3, 1, 3);
        BlockPos gimbalPos = util.grid().at(2, 1, 3);
        BlockPos gaugePos = util.grid().at(4, 1, 3);
        BlockPos altitudeSensorPos = util.grid().at(3, 1, 4);

        scene.title("airship_helm_telemetry", "Reading an Airship with the Airship Helm");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.9F);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 5, 2, 5), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(75)
                .text("The Airship Helm identifies the Sable sub-level containing it")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(helmPos))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.INPUT, "helm", util.select().position(helmPos), 75);
        scene.idle(90);

        scene.overlay().showText(80)
                .text("It reads position, heading, pitch, roll, linear velocity, angular velocity, and mass directly from Sable")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(gimbalPos))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.BLUE, "instruments", util.select().fromTo(gimbalPos, gaugePos), 80);
        scene.idle(95);

        scene.overlay().showText(80)
                .text("The Helm also aggregates balloon capacity, gas fill, and lift from Create Aeronautics")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(altitudeSensorPos))
                .placeNearTarget();
        scene.effects().indicateSuccess(helmPos);
        scene.idle(95);

        scene.overlay().showControls(
                        util.vector().blockSurface(helmPos, Direction.NORTH),
                        Pointing.RIGHT,
                        30
                )
                .rightClick();
        scene.idle(10);
        scene.overlay().showText(75)
                .text("Right-click with an empty hand to inspect the current flight telemetry")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(helmPos, Direction.NORTH))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(70)
                .text("A Helm placed outside an assembled sub-level reports that it is detached")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(Vec3.atCenterOf(helmPos))
                .placeNearTarget();
        scene.idle(85);
    }

    private static void airshipBurners(
            SceneBuilder scene,
            SceneBuildingUtil util
    ) {
        BlockPos burnerPos = util.grid().at(3, 1, 3);
        BlockPos leftBurnerPos = util.grid().at(2, 1, 3);
        BlockPos rightBurnerPos = util.grid().at(4, 1, 3);
        BlockPos leverPos = util.grid().at(1, 1, 3);
        BlockPos envelopeCenter = util.grid().at(3, 5, 3);

        scene.title("airship_burners", "Supplying Lift with Airship Burners");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.85F);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 5, 6, 5), Direction.DOWN);
        scene.world().modifyBlockEntity(
                burnerPos,
                AirshipBurnerBlockEntity.class,
                burner -> burner.configurePonderPreview(0, false)
        );
        scene.idle(20);

        scene.overlay().showText(75)
                .text("Airship Burners are fuel-powered lifting-gas providers for Create Aeronautics balloons")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(burnerPos))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.INPUT, "burner", util.select().position(burnerPos), 75);
        scene.idle(90);

        scene.overlay().showControls(
                        util.vector().blockSurface(burnerPos, Direction.NORTH),
                        Pointing.RIGHT,
                        35
                )
                .withItem(new ItemStack(Items.COAL))
                .rightClick();
        scene.idle(10);
        scene.overlay().showText(70)
                .text("Insert furnace fuel, Blaze Burner fuel, or Blaze Cake into the burner")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(burnerPos, Direction.NORTH))
                .placeNearTarget();
        scene.idle(85);

        BlockState poweredLever = Blocks.LEVER.defaultBlockState()
                .setValue(LeverBlock.FACE, net.minecraft.world.level.block.state.properties.AttachFace.FLOOR)
                .setValue(LeverBlock.FACING, Direction.EAST)
                .setValue(LeverBlock.POWERED, true);
        scene.world().setBlock(leverPos, poweredLever, true);

        BlockState litBurner = ZmhBlocks.AIRSHIP_BURNER.get().defaultBlockState()
                .setValue(HotAirBurnerBlock.POWERED, true)
                .setValue(AirshipBurnerBlock.LIT, true);
        scene.world().setBlock(burnerPos, litBurner, true);
        scene.world().modifyBlockEntity(
                burnerPos,
                AirshipBurnerBlockEntity.class,
                burner -> burner.configurePonderPreview(12, false)
        );
        scene.effects().indicateRedstone(leverPos);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Redstone enables the flame and proportionally controls gas output and fuel consumption")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(leverPos))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.RED, "redstone", util.select().fromTo(leverPos, burnerPos), 80);
        scene.idle(95);

        scene.overlay().showText(75)
                .text("The burner must point into a sealed airtight envelope within its operating range")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(envelopeCenter))
                .placeNearTarget();
        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "envelope",
                util.select().fromTo(2, 4, 2, 4, 6, 4),
                75
        );
        scene.idle(90);

        scene.world().setBlock(
                burnerPos,
                ZmhBlocks.FORCED_DRAFT_AIRSHIP_BURNER.get().defaultBlockState()
                        .setValue(HotAirBurnerBlock.POWERED, true)
                        .setValue(AirshipBurnerBlock.LIT, true),
                true
        );
        scene.world().modifyBlockEntity(
                burnerPos,
                AirshipBurnerBlockEntity.class,
                burner -> burner.configurePonderPreview(12, false)
        );
        scene.idle(20);
        scene.overlay().showText(70)
                .text("The Forced-Draft Burner uses its data-pack profile to provide greater output and operating range")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(burnerPos))
                .placeNearTarget();
        scene.idle(85);

        scene.world().setBlock(
                burnerPos,
                ZmhBlocks.INDUSTRIAL_AIRSHIP_BURNER.get().defaultBlockState()
                        .setValue(HotAirBurnerBlock.POWERED, true)
                        .setValue(AirshipBurnerBlock.LIT, true),
                true
        );
        scene.world().modifyBlockEntity(
                burnerPos,
                AirshipBurnerBlockEntity.class,
                burner -> burner.configurePonderPreview(12, false)
        );
        scene.idle(20);
        scene.overlay().showText(75)
                .text("The Industrial Burner is a configurable high-output tier with a correspondingly higher fuel demand")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(Vec3.atCenterOf(burnerPos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showControls(
                        util.vector().blockSurface(burnerPos, Direction.WEST),
                        Pointing.RIGHT,
                        30
                )
                .withItem(new ItemStack(Blocks.SOUL_SAND))
                .rightClick();
        scene.idle(10);
        scene.world().setBlock(
                burnerPos,
                ZmhBlocks.INDUSTRIAL_AIRSHIP_BURNER.get().defaultBlockState()
                        .setValue(HotAirBurnerBlock.POWERED, true)
                        .setValue(HotAirBurnerBlock.VARIANT, HotAirBurnerBlock.Variant.SOUL_FIRE)
                        .setValue(AirshipBurnerBlock.LIT, true),
                true
        );
        scene.world().modifyBlockEntity(
                burnerPos,
                AirshipBurnerBlockEntity.class,
                burner -> burner.configurePonderPreview(12, false)
        );
        scene.overlay().showText(70)
                .text("Soul-fire materials switch the burner to its blue-flame appearance")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(burnerPos))
                .placeNearTarget();
        scene.idle(85);

        scene.world().setBlock(
                leftBurnerPos,
                ZmhBlocks.AIRSHIP_BURNER.get().defaultBlockState()
                        .setValue(HotAirBurnerBlock.POWERED, true)
                        .setValue(AirshipBurnerBlock.LIT, true),
                true
        );
        scene.world().setBlock(
                rightBurnerPos,
                ZmhBlocks.FORCED_DRAFT_AIRSHIP_BURNER.get().defaultBlockState()
                        .setValue(HotAirBurnerBlock.POWERED, true)
                        .setValue(AirshipBurnerBlock.LIT, true),
                true
        );
        scene.world().modifyBlockEntity(
                leftBurnerPos,
                AirshipBurnerBlockEntity.class,
                burner -> burner.configurePonderPreview(8, false)
        );
        scene.world().modifyBlockEntity(
                burnerPos,
                AirshipBurnerBlockEntity.class,
                burner -> burner.configurePonderPreview(15, true)
        );
        scene.world().modifyBlockEntity(
                rightBurnerPos,
                AirshipBurnerBlockEntity.class,
                burner -> burner.configurePonderPreview(12, false)
        );
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Each burner remains an independent Aeronautics provider; the balloon combines every connected source in its native heater collection")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(Vec3.atCenterOf(envelopeCenter))
                .placeNearTarget();
        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "combined_heat_sources",
                util.select().fromTo(leftBurnerPos, rightBurnerPos),
                80
        );
        scene.idle(95);

        scene.overlay().showControls(
                        util.vector().blockSurface(burnerPos, Direction.NORTH),
                        Pointing.RIGHT,
                        35
                )
                .withItem(AllItems.GOGGLES.asStack());
        scene.idle(10);
        scene.overlay().showText(75)
                .text("Engineer's Goggles show both the selected burner reservoir and the combined output of all sources connected to the balloon")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(burnerPos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(75)
                .text("Every Airship Burner has Thermal, Airflow, and Control upgrade sockets; each socket accepts one compatible data-driven module")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(burnerPos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showControls(
                        util.vector().blockSurface(burnerPos, Direction.NORTH),
                        Pointing.RIGHT,
                        35
                )
                .withItem(ZmhItems.HEAT_RECUPERATOR_UPGRADE.get().getDefaultInstance())
                .rightClick();
        scene.overlay().showText(70)
                .text("The Heat Recuperator trades a little peak output for lower fuel use and a larger shared heat reserve")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(burnerPos))
                .placeNearTarget();
        scene.idle(85);

        scene.overlay().showControls(
                        util.vector().blockSurface(burnerPos, Direction.EAST),
                        Pointing.RIGHT,
                        35
                )
                .withItem(ZmhItems.FORCED_INDUCTION_UPGRADE.get().getDefaultInstance())
                .rightClick();
        scene.overlay().showText(70)
                .text("Forced Induction raises gas output and envelope range, but increases fuel demand")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(burnerPos))
                .placeNearTarget();
        scene.idle(85);

        scene.overlay().showControls(
                        util.vector().blockSurface(burnerPos, Direction.WEST),
                        Pointing.RIGHT,
                        35
                )
                .withItem(ZmhItems.PRECISION_REGULATOR_UPGRADE.get().getDefaultInstance())
                .rightClick();
        scene.overlay().showText(70)
                .text("The Precision Regulator reshapes the redstone throttle curve for finer low-power control")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(burnerPos))
                .placeNearTarget();
        scene.idle(85);

        scene.overlay().showControls(
                        util.vector().blockSurface(burnerPos, Direction.SOUTH),
                        Pointing.RIGHT,
                        35
                )
                .withItem(AllItems.WRENCH.asStack())
                .rightClick()
                .whileSneaking();
        scene.overlay().showText(70)
                .text("Sneak-use the Create Wrench to remove the most recently occupied upgrade socket before dismantling the burner")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(burnerPos))
                .placeNearTarget();
        scene.idle(85);
    }


    private static void pipedRedstone(
            SceneBuilder scene,
            SceneBuildingUtil util
    ) {
        BlockPos inputPos = util.grid().at(1, 1, 2);
        BlockPos lineStart = util.grid().at(2, 1, 2);
        BlockPos lineMiddle = util.grid().at(4, 1, 2);
        BlockPos lineEnd = util.grid().at(6, 1, 2);
        BlockPos outputPos = util.grid().at(7, 1, 2);
        BlockPos parallelStart = util.grid().at(2, 1, 4);
        BlockPos parallelEnd = util.grid().at(6, 1, 4);

        scene.title("piped_redstone", "Protected Redstone for Airships");
        scene.configureBasePlate(0, 0, 9);
        scene.scaleSceneView(0.8F);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 7, 1, 4), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(75)
                .text("Piped Redstone carries the complete analog signal through sealed, waterloggable conduits")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(lineMiddle))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.RED, "main_conduit", util.select().fromTo(lineStart, lineEnd), 75);
        scene.idle(90);

        scene.overlay().showText(80)
                .text("Connections are explicit ports, not automatic adjacency; parallel lines can pass beside each other without merging")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(parallelStart))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.BLUE, "isolated_lines", util.select().fromTo(parallelStart, parallelEnd), 80);
        scene.idle(95);

        scene.overlay().showControls(
                        util.vector().blockSurface(lineMiddle, Direction.SOUTH),
                        Pointing.RIGHT,
                        35
                )
                .withItem(AllItems.WRENCH.asStack())
                .rightClick();
        scene.idle(10);
        scene.world().modifyBlock(
                lineMiddle,
                state -> state.setValue(PipedRedstoneBlock.SOUTH, true),
                false
        );
        scene.overlay().showText(75)
                .text("Use the Create Wrench on a face to open or close that exact port")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(lineMiddle, Direction.SOUTH))
                .placeNearTarget();
        scene.idle(90);

        scene.world().modifyBlock(
                lineStart,
                state -> state.setValue(PipedRedstoneBlock.WATERLOGGED, true),
                false
        );
        scene.overlay().showText(70)
                .text("Waterlogging does not wash away the conduit or interrupt its signal")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(Vec3.atCenterOf(lineStart))
                .placeNearTarget();
        scene.idle(85);

        scene.world().setBlock(
                lineMiddle,
                ZmhBlocks.BRASS_PIPED_REDSTONE.get().defaultBlockState()
                        .setValue(PipedRedstoneBlock.EAST, true)
                        .setValue(PipedRedstoneBlock.WEST, true)
                        .setValue(PipedRedstoneBlock.POWER, 15),
                true
        );
        scene.idle(15);
        scene.world().setBlock(
                lineMiddle,
                ZmhBlocks.RESONANT_PIPED_REDSTONE.get().defaultBlockState()
                        .setValue(PipedRedstoneBlock.EAST, true)
                        .setValue(PipedRedstoneBlock.WEST, true)
                        .setValue(PipedRedstoneBlock.POWER, 15),
                true
        );
        scene.overlay().showText(85)
                .text("Copper, Brass, and Resonant tiers progressively reduce network delay and increase repeater-free distance")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(lineMiddle))
                .placeNearTarget();
        scene.idle(100);

        scene.world().setBlock(
                lineMiddle,
                ZmhBlocks.PIPED_REDSTONE_REPEATER.get().defaultBlockState()
                        .setValue(PipedRedstoneRepeaterBlock.FACING, Direction.EAST)
                        .setValue(PipedRedstoneRepeaterBlock.POWER, 15),
                true
        );
        scene.overlay().showText(80)
                .text("A Piped Redstone Repeater preserves analog strength and starts a new distance segment after its own delay")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(lineMiddle))
                .placeNearTarget();
        scene.effects().indicateSuccess(outputPos);
        scene.idle(90);

        scene.overlay().showControls(
                        util.vector().blockSurface(lineMiddle, Direction.UP),
                        Pointing.DOWN,
                        35
                )
                .rightClick();
        scene.idle(10);
        scene.world().modifyBlock(
                lineMiddle,
                state -> state.setValue(PipedRedstoneRepeaterBlock.DELAY, 4),
                false
        );
        scene.overlay().showText(75)
                .text("Right-click the repeater to cycle through one to four redstone ticks, equal to two to eight game ticks")
                .attachKeyFrame()
                .pointAt(util.vector().blockSurface(lineMiddle, Direction.UP))
                .placeNearTarget();
        scene.idle(90);

        scene.world().modifyBlocks(
                util.select().fromTo(lineStart, lineEnd),
                state -> state.hasProperty(PipedRedstoneBlock.POWER)
                        ? state.setValue(PipedRedstoneBlock.POWER, 15)
                        : state,
                false
        );
        scene.effects().indicateRedstone(inputPos);
        scene.overlay().showText(75)
                .text("Mixed-tier networks use the weakest segment's delay and maximum distance, so upgrading the whole trunk matters")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(Vec3.atCenterOf(lineEnd))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showControls(
                        util.vector().blockSurface(parallelEnd, Direction.EAST),
                        Pointing.RIGHT,
                        35
                )
                .withItem(ZmhBlocks.COPPER_PIPED_REDSTONE_ITEM.get().getDefaultInstance())
                .rightClick();
        scene.idle(10);
        scene.world().setBlock(
                parallelEnd.east(),
                ZmhBlocks.COPPER_PIPED_REDSTONE.get().defaultBlockState()
                        .setValue(PipedRedstoneBlock.EAST, true)
                        .setValue(PipedRedstoneBlock.WEST, true),
                true
        );
        scene.overlay().showText(80)
                .text("With another conduit in hand, Create Placement Assist extends the far end of the line like a Shaft and preserves its axis")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(parallelEnd.east()))
                .placeNearTarget();
        scene.idle(95);

        scene.world().setBlock(
                inputPos,
                ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER.get().defaultBlockState()
                        .setValue(PipedRedstoneNativeLeverBlock.FACE, AttachFace.WALL)
                        .setValue(PipedRedstoneNativeLeverBlock.FACING, Direction.WEST)
                        .setValue(PipedRedstoneNativeLeverBlock.POWERED, false),
                true
        );
        scene.overlay().showControls(
                        util.vector().blockSurface(inputPos, Direction.WEST),
                        Pointing.RIGHT,
                        35
                )
                .rightClick();
        scene.idle(10);
        scene.world().modifyBlock(
                inputPos,
                state -> state.setValue(PipedRedstoneNativeLeverBlock.POWERED, true),
                false
        );
        scene.effects().indicateRedstone(lineStart);
        scene.overlay().showText(80)
                .text("The Native Lever mounts directly to a conduit port, opens that port automatically, and powers only the attached isolated line")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(inputPos))
                .placeNearTarget();
        scene.idle(95);
    }

    private static void altitudeControl(
            SceneBuilder scene,
            SceneBuildingUtil util
    ) {
        BlockPos nativeLever = util.grid().at(2, 1, 2);
        BlockPos trimLine = util.grid().at(3, 1, 2);
        BlockPos gauge = util.grid().at(4, 1, 2);
        BlockPos outputStart = util.grid().at(5, 1, 2);
        BlockPos outputEnd = util.grid().at(6, 1, 2);
        BlockPos burner = util.grid().at(7, 1, 2);

        scene.title("altitude_control", "Automatic Altitude Control");
        scene.configureBasePlate(0, 0, 9);
        scene.scaleSceneView(0.9F);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 7, 2, 3), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("The Altitude Gauge reads authoritative height, vertical speed, and balloon fill from the containing airship")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(gauge))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.BLUE, "altitude_gauge", util.select().position(gauge), 80);
        scene.idle(95);

        scene.overlay().showControls(
                        util.vector().blockSurface(gauge, Direction.UP),
                        Pointing.DOWN,
                        35
                )
                .withItem(AllItems.WRENCH.asStack())
                .rightClick();
        scene.idle(10);
        scene.overlay().showText(85)
                .text("Use the Create Wrench to cycle between altitude, vertical-speed, balloon-fill, and altitude-hold output modes")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(gauge))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(80)
                .text("The rear face accepts a manual trim signal from a Native Lever or another analog redstone source")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(trimLine))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.INPUT, "trim_input", util.select().fromTo(nativeLever, trimLine), 80);
        scene.idle(95);

        scene.overlay().showControls(
                        util.vector().blockSurface(gauge, Direction.NORTH),
                        Pointing.RIGHT,
                        35
                )
                .rightClick();
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Sneak-right-click with an empty hand to capture the current altitude and arm the controller")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(gauge))
                .placeNearTarget();
        scene.idle(95);

        scene.world().modifyBlock(
                gauge,
                state -> state.setValue(AltitudeGaugeBlock.POWER, 11),
                false
        );
        scene.overlay().showText(85)
                .text("The controller adds proportional altitude correction and vertical-speed damping to the trim signal")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(Vec3.atCenterOf(gauge))
                .placeNearTarget();
        scene.idle(100);

        scene.world().modifyBlocks(
                util.select().fromTo(outputStart, outputEnd),
                state -> state.hasProperty(PipedRedstoneBlock.POWER)
                        ? state.setValue(PipedRedstoneBlock.POWER, 11)
                        : state,
                false
        );
        scene.effects().indicateRedstone(outputStart);
        scene.effects().indicateSuccess(burner);
        scene.overlay().showText(85)
                .text("Route the front output through Piped Redstone to the airship burners for a closed-loop altitude hold")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(outputEnd))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(80)
                .text("Signal slew limiting prevents abrupt burner changes and reduces oscillation")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(Vec3.atCenterOf(gauge))
                .placeNearTarget();
        scene.idle(95);
    }

}
