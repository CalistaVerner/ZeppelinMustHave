package us.kayla.zeppelinmusthave.ponder;

import com.simibubi.create.AllBlocks;
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

final class ZmhFluidPonderScenes {
    private ZmhFluidPonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhPonderRegistration.items(helper)
                .forComponents(
                        ZmhBlocks.REINFORCED_FLUID_PIPE_ITEM.get(),
                        ZmhBlocks.INDUSTRIAL_FLUID_PIPE_ITEM.get()
                )
                .addStoryBoard(
                        "fluid/pipes",
                        ZmhFluidPonderScenes::fluidPipeGrades,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
    }

    private static void fluidPipeGrades(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos reinforcedSource = util.grid().at(1, 1, 2);
        BlockPos reinforcedStart = util.grid().at(2, 1, 2);
        BlockPos reinforcedMiddle = util.grid().at(4, 1, 2);
        BlockPos reinforcedEnd = util.grid().at(6, 1, 2);
        BlockPos reinforcedTarget = util.grid().at(7, 1, 2);
        BlockPos industrialSource = util.grid().at(1, 1, 5);
        BlockPos industrialStart = util.grid().at(2, 1, 5);
        BlockPos industrialMiddle = util.grid().at(4, 1, 5);
        BlockPos industrialEnd = util.grid().at(6, 1, 5);
        BlockPos industrialTarget = util.grid().at(7, 1, 5);

        scene.title("fluid_pipe_grades", "Connected MK-Series Fluid Lines");
        scene.configureBasePlate(0, 0, 9);
        scene.scaleSceneView(0.80F);

        BlockState reinforcedPipe = ZmhBlocks.REINFORCED_FLUID_PIPE.get().getAxisState(Direction.Axis.X);
        BlockState industrialPipe = ZmhBlocks.INDUSTRIAL_FLUID_PIPE.get().getAxisState(Direction.Axis.X);
        scene.world().setBlock(reinforcedSource, AllBlocks.FLUID_TANK.getDefaultState(), false);
        scene.world().setBlock(reinforcedTarget, ZmhBlocks.BALLAST_TANK.get().defaultBlockState(), false);
        scene.world().setBlock(industrialSource, AllBlocks.FLUID_TANK.getDefaultState(), false);
        scene.world().setBlock(industrialTarget, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get().defaultBlockState(), false);
        for (int x = 2; x <= 6; x++) {
            scene.world().setBlock(util.grid().at(x, 1, 2), reinforcedPipe, false);
            scene.world().setBlock(util.grid().at(x, 1, 5), industrialPipe, false);
        }
        scene.idle(1);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 7, 2, 6), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(90)
                .text("MK II Reinforced Fluid Pipes open along their axis and render as one continuous protected line")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(reinforcedMiddle))
                .placeNearTarget();
        scene.overlay().showOutline(
                PonderPalette.INPUT,
                "reinforced_pipe_line",
                util.select().fromTo(reinforcedStart, reinforcedEnd),
                90
        );
        scene.idle(105);

        scene.overlay().showText(95)
                .text("MK III Industrial Fluid Trunks use the same Create network behavior for high-throughput boiler and ballast systems")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(Vec3.atCenterOf(industrialMiddle))
                .placeNearTarget();
        scene.overlay().showOutline(
                PonderPalette.OUTPUT,
                "industrial_pipe_line",
                util.select().fromTo(industrialStart, industrialEnd),
                95
        );
        scene.effects().indicateSuccess(industrialTarget);
        scene.idle(110);
    }
}
