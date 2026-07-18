package us.kayla.zeppelinmusthave.ponder;

import com.simibubi.create.AllItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.content.control.AltitudeGaugeBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneBlock;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

final class ZmhControlPonderScenes {
    private ZmhControlPonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhPonderRegistration.items(helper)
                .forComponents(ZmhBlocks.ALTITUDE_GAUGE_ITEM.get())
                .addStoryBoard(
                        "control/altitude_hold",
                        ZmhControlPonderScenes::altitudeControl,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
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
