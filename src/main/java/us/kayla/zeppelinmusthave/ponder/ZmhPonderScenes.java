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
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlock;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlockEntity;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

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
    }
}
