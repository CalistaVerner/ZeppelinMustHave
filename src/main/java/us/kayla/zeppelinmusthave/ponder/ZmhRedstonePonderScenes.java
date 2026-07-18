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
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneRepeaterBlock;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

final class ZmhRedstonePonderScenes {
    private ZmhRedstonePonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhPonderRegistration.items(helper)
                .forComponents(
                        ZmhBlocks.COPPER_PIPED_REDSTONE_ITEM.get(),
                        ZmhBlocks.BRASS_PIPED_REDSTONE_ITEM.get(),
                        ZmhBlocks.RESONANT_PIPED_REDSTONE_ITEM.get(),
                        ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER_ITEM.get(),
                        ZmhBlocks.PIPED_REDSTONE_REPEATER_ITEM.get()
                )
                .addStoryBoard(
                        "redstone/conduits",
                        ZmhRedstonePonderScenes::pipedRedstone,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
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
                .text("MK I, MK II, and MK III conduits progressively reduce network delay and increase repeater-free distance")
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
                .text("Mixed-MK networks use the weakest segment's delay and maximum distance, so upgrading the whole trunk matters")
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
}
