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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.content.control.fcn.EmergencyCutoffBlock;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

final class ZmhFlightControlPonderScenes {
    private ZmhFlightControlPonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhPonderRegistration.items(helper)
                .forComponents(
                        ZmhBlocks.FLIGHT_COMPUTER_ITEM.get(),
                        ZmhBlocks.ENGINE_TELEGRAPH_ITEM.get(),
                        ZmhBlocks.EMERGENCY_CUTOFF_ITEM.get(),
                        ZmhBlocks.CONTROL_TRANSMITTER_ITEM.get(),
                        ZmhBlocks.CONTROL_RECEIVER_ITEM.get()
                )
                .addStoryBoard(
                        "flight_control/network",
                        ZmhFlightControlPonderScenes::flightControlNetwork,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
    }

    private static void flightControlNetwork(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos telegraph = util.grid().at(2, 1, 3);
        BlockPos transmitter = util.grid().at(4, 1, 5);
        BlockPos computer = util.grid().at(5, 1, 3);
        BlockPos receiver = util.grid().at(7, 1, 5);
        BlockPos cutoff = util.grid().at(8, 1, 3);

        scene.title("flight_control_network", "Routing Commands through the Flight Control Network");
        scene.configureBasePlate(0, 0, 11);
        scene.scaleSceneView(0.78F);
        scene.world().setBlock(telegraph, ZmhBlocks.ENGINE_TELEGRAPH.get().defaultBlockState(), false);
        scene.world().setBlock(transmitter, ZmhBlocks.CONTROL_TRANSMITTER.get().defaultBlockState(), false);
        scene.world().setBlock(computer, ZmhBlocks.FLIGHT_COMPUTER.get().defaultBlockState(), false);
        scene.world().setBlock(receiver, ZmhBlocks.CONTROL_RECEIVER.get().defaultBlockState(), false);
        scene.world().setBlock(cutoff, ZmhBlocks.EMERGENCY_CUTOFF.get().defaultBlockState(), false);
        scene.idle(1);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 9, 2, 5), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("The Flight Computer combines vessel telemetry and publishes independent control channels")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(computer))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.BLUE, "flight_computer", util.select().position(computer), 80);
        scene.idle(95);

        scene.overlay().showText(80)
                .text("The Engine Telegraph sends manual ahead, stop, and astern orders")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(telegraph))
                .placeNearTarget();
        scene.idle(95);

        scene.overlay().showControls(util.vector().blockSurface(transmitter, Direction.UP), Pointing.DOWN, 40)
                .withItem(new ItemStack(Items.NAME_TAG))
                .rightClick();
        scene.idle(45);
        scene.overlay().showControls(util.vector().blockSurface(receiver, Direction.UP), Pointing.DOWN, 40)
                .withItem(new ItemStack(Items.RED_DYE))
                .rightClick();
        scene.overlay().showText(90)
                .text("Transmitters and receivers share a network name and one of sixteen dye frequencies inside one vessel")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(transmitter))
                .placeNearTarget();
        scene.idle(105);

        scene.overlay().showControls(util.vector().blockSurface(receiver, Direction.UP), Pointing.DOWN, 35)
                .withItem(AllItems.WRENCH.asStack())
                .rightClick();
        scene.overlay().showText(80)
                .text("Use the Create Wrench to select the command channel carried by a transmitter or receiver")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(receiver))
                .placeNearTarget();
        scene.idle(95);

        scene.world().setBlock(
                cutoff,
                ZmhBlocks.EMERGENCY_CUTOFF.get().defaultBlockState()
                        .setValue(EmergencyCutoffBlock.LATCHED, true),
                true
        );
        scene.overlay().showText(90)
                .text("The Emergency Cutoff latches a vessel-wide stop and requires a manual reset")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(Vec3.atCenterOf(cutoff))
                .placeNearTarget();
        scene.effects().indicateRedstone(cutoff);
        scene.idle(105);
    }
}
