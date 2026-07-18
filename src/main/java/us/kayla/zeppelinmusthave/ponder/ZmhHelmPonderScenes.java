package us.kayla.zeppelinmusthave.ponder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

final class ZmhHelmPonderScenes {
    private ZmhHelmPonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhPonderRegistration.items(helper)
                .forComponents(ZmhBlocks.AIRSHIP_HELM_ITEM.get())
                .addStoryBoard(
                        "helm/telemetry",
                        ZmhHelmPonderScenes::airshipHelmTelemetry,
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
}
