package us.kayla.zeppelinmusthave.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.content.steam.LeviathanSteamEngineBlock;
import us.kayla.zeppelinmusthave.content.steam.LeviathanSteamEnginePart;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

/** Ponder storyboard for graded boiler construction and operation. */
final class ZmhBoilerPonderScenes {
    private ZmhBoilerPonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhPonderRegistration.items(helper)
                .forComponents(
                        ZmhBlocks.COPPER_BOILER_BASE_ITEM.get(),
                        ZmhBlocks.BRASS_BOILER_BASE_ITEM.get(),
                        ZmhBlocks.INDUSTRIAL_BOILER_BASE_ITEM.get()
                )
                .addStoryBoard(
                        "boiler/grades",
                        ZmhBoilerPonderScenes::boilerGrades,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
    }


    private static void boilerGrades(
            SceneBuilder scene,
            SceneBuildingUtil util
    ) {
        BlockPos heater = util.grid().at(3, 1, 3);
        BlockPos lowerTank = util.grid().at(3, 2, 3);
        BlockPos upperTank = util.grid().at(3, 3, 3);
        BlockPos engine = util.grid().at(4, 3, 3);

        scene.title("boiler_grades", "MK-Series Fluid-Tank Boilers");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.9F);

        scene.world().setBlock(
                lowerTank,
                ZmhBlocks.COPPER_BOILER_BASE.get().defaultBlockState(),
                false
        );
        scene.world().setBlock(
                upperTank,
                ZmhBlocks.COPPER_BOILER_BASE.get().defaultBlockState(),
                false
        );
        scene.idle(1);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 5, 4, 5), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Each MK variant is a complete Create Fluid Tank and forms native multiblocks with matching blocks")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(lowerTank))
                .placeNearTarget();
        scene.overlay().showOutline(
                PonderPalette.INPUT,
                "graded_boiler",
                util.select().fromTo(heater, upperTank),
                80
        );
        scene.idle(95);

        scene.overlay().showText(85)
                .text("Create still calculates capacity, water input, boiler size, engines, efficiency, and Stress Units")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(engine))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(75)
                .text("MK I upgrades ordinary heater output while preserving passive heat as passive")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(lowerTank))
                .placeNearTarget();
        scene.idle(90);

        scene.world().setBlock(
                lowerTank,
                ZmhBlocks.BRASS_BOILER_BASE.get().defaultBlockState(),
                true
        );
        scene.world().setBlock(
                upperTank,
                ZmhBlocks.BRASS_BOILER_BASE.get().defaultBlockState(),
                true
        );
        scene.overlay().showText(80)
                .text("MK II transfers more heat per heater column for compact high-power boilers")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(lowerTank))
                .placeNearTarget();
        scene.idle(95);

        scene.world().setBlock(
                lowerTank,
                ZmhBlocks.INDUSTRIAL_BOILER_BASE.get().defaultBlockState(),
                true
        );
        scene.world().setBlock(
                upperTank,
                ZmhBlocks.INDUSTRIAL_BOILER_BASE.get().defaultBlockState(),
                true
        );
        scene.overlay().showText(85)
                .text("MK III is an industrial pressure vessel for maximum-output airship power plants")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(Vec3.atCenterOf(lowerTank))
                .placeNearTarget();
        scene.idle(100);

        scene.world().setBlock(
                upperTank,
                ZmhBlocks.COPPER_BOILER_BASE.get().defaultBlockState(),
                true
        );
        scene.overlay().showText(75)
                .text("Different MK variants never merge; every multiblock must be built from one MK series")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(Vec3.atCenterOf(upperTank))
                .placeNearTarget();
        scene.effects().indicateRedstone(upperTank);
        scene.idle(90);

        scene.world().setBlock(
                upperTank,
                ZmhBlocks.INDUSTRIAL_BOILER_BASE.get().defaultBlockState(),
                true
        );
        scene.world().setBlock(heater, Blocks.MAGMA_BLOCK.defaultBlockState(), true);
        scene.overlay().showControls(
                        util.vector().blockSurface(lowerTank, Direction.NORTH),
                        Pointing.RIGHT,
                        35
                )
                .withItem(AllItems.GOGGLES.asStack());
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Goggles show native boiler data and the active MK profile; the Wrench toggles tank windows")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(lowerTank))
                .placeNearTarget();
        scene.idle(95);
    }
}
