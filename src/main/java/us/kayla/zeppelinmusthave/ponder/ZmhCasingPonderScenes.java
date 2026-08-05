package us.kayla.zeppelinmusthave.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

/** Demonstrates native Create encasing with both airship alloy casing families. */
final class ZmhCasingPonderScenes {
    private ZmhCasingPonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhPonderRegistration.items(helper)
                .forComponents(
                        ZmhBlocks.AIRSHIP_BRASS_CASING_ITEM.get(),
                        ZmhBlocks.AIRSHIP_BRONZE_CASING_ITEM.get()
                )
                .addStoryBoard(
                        "steam_engine/grades",
                        ZmhCasingPonderScenes::airshipCasings,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
    }

    private static void airshipCasings(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        BlockPos shaftPos = util.grid().at(1, 1, 3);
        BlockPos smallCogPos = util.grid().at(2, 1, 3);
        BlockPos largeCogPos = util.grid().at(4, 1, 3);
        BlockPos pipePos = util.grid().at(5, 1, 3);

        BlockState shaft = AllBlocks.SHAFT.getDefaultState()
                .setValue(ShaftBlock.AXIS, Direction.Axis.X);
        BlockState smallCog = AllBlocks.COGWHEEL.getDefaultState()
                .setValue(CogWheelBlock.AXIS, Direction.Axis.Y);
        BlockState largeCog = AllBlocks.LARGE_COGWHEEL.getDefaultState()
                .setValue(CogWheelBlock.AXIS, Direction.Axis.Y);
        BlockState pipe = AllBlocks.FLUID_PIPE.get().getAxisState(Direction.Axis.X);

        scene.title("airship_casings", "Airship Brass and Bronze Casings");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.9F);
        scene.world().setBlock(shaftPos, shaft, false);
        scene.world().setBlock(smallCogPos, smallCog, false);
        scene.world().setBlock(largeCogPos, largeCog, false);
        scene.world().setBlock(pipePos, pipe, false);
        scene.world().showSection(util.select().everywhere(), Direction.UP);
        scene.idle(20);

        scene.overlay().showText(75)
                .text("Both casing blocks are native Create encasing materials, not decorative substitutes")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(smallCogPos))
                .placeNearTarget();
        scene.idle(85);

        scene.overlay().showControls(
                        util.vector().blockSurface(shaftPos, Direction.UP),
                        Pointing.DOWN,
                        35
                )
                .rightClick()
                .withItem(ZmhBlocks.AIRSHIP_BRASS_CASING_ITEM.get().getDefaultInstance());
        scene.idle(15);
        setEncasedFamily(scene, shaftPos, smallCogPos, largeCogPos, pipePos, true);
        scene.effects().indicateSuccess(smallCogPos);
        scene.overlay().showText(80)
                .text("Airship Brass Casing encloses shafts, small cogwheels, large cogwheels, and fluid pipes")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(Vec3.atCenterOf(shaftPos))
                .placeNearTarget();
        scene.idle(95);

        setEncasedFamily(scene, shaftPos, smallCogPos, largeCogPos, pipePos, false);
        scene.effects().indicateSuccess(pipePos);
        scene.overlay().showControls(
                        util.vector().blockSurface(pipePos, Direction.UP),
                        Pointing.DOWN,
                        35
                )
                .rightClick()
                .withItem(ZmhBlocks.AIRSHIP_BRONZE_CASING_ITEM.get().getDefaultInstance());
        scene.overlay().showText(80)
                .text("Airship Bronze Casing provides the same mechanics with a darker naval bronze finish")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(pipePos))
                .placeNearTarget();
        scene.idle(95);

        scene.overlay().showText(80)
                .text("Pipe connections and kinetic axes are preserved while the casing is applied")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(largeCogPos))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showControls(
                        util.vector().blockSurface(smallCogPos, Direction.UP),
                        Pointing.DOWN,
                        35
                )
                .rightClick()
                .whileSneaking()
                .withItem(AllItems.WRENCH.asStack());
        scene.overlay().showText(75)
                .text("Use the Create Wrench to remove a casing; shafts and cogwheels require sneaking")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(smallCogPos))
                .placeNearTarget();
        scene.idle(90);
    }

    private static void setEncasedFamily(
            CreateSceneBuilder scene,
            BlockPos shaftPos,
            BlockPos smallCogPos,
            BlockPos largeCogPos,
            BlockPos pipePos,
            boolean brass
    ) {
        scene.world().setBlock(
                shaftPos,
                (brass ? ZmhBlocks.AIRSHIP_BRASS_ENCASED_SHAFT : ZmhBlocks.AIRSHIP_BRONZE_ENCASED_SHAFT)
                        .get()
                        .defaultBlockState()
                        .setValue(ShaftBlock.AXIS, Direction.Axis.X),
                true
        );
        scene.world().setBlock(
                smallCogPos,
                (brass ? ZmhBlocks.AIRSHIP_BRASS_ENCASED_COGWHEEL : ZmhBlocks.AIRSHIP_BRONZE_ENCASED_COGWHEEL)
                        .get()
                        .defaultBlockState()
                        .setValue(EncasedCogwheelBlock.AXIS, Direction.Axis.Y),
                true
        );
        scene.world().setBlock(
                largeCogPos,
                (brass
                        ? ZmhBlocks.AIRSHIP_BRASS_ENCASED_LARGE_COGWHEEL
                        : ZmhBlocks.AIRSHIP_BRONZE_ENCASED_LARGE_COGWHEEL)
                        .get()
                        .defaultBlockState()
                        .setValue(EncasedCogwheelBlock.AXIS, Direction.Axis.Y),
                true
        );
        BlockState sourcePipe = AllBlocks.FLUID_PIPE.get().getAxisState(Direction.Axis.X);
        BlockState encasedPipe = (brass
                ? ZmhBlocks.AIRSHIP_BRASS_ENCASED_FLUID_PIPE
                : ZmhBlocks.AIRSHIP_BRONZE_ENCASED_FLUID_PIPE).get().defaultBlockState();
        scene.world().setBlock(
                pipePos,
                EncasedPipeBlock.transferSixWayProperties(sourcePipe, encasedPipe),
                true
        );
    }
}
