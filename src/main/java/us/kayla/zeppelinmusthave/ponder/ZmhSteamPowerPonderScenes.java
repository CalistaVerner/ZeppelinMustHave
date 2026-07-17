package us.kayla.zeppelinmusthave.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
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
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

final class ZmhSteamPowerPonderScenes {
    private ZmhSteamPowerPonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(
                        ZmhBlocks.COPPER_BOILER_BASE.getId(),
                        ZmhBlocks.BRASS_BOILER_BASE.getId(),
                        ZmhBlocks.INDUSTRIAL_BOILER_BASE.getId())
                .addStoryBoard("boiler/grades", ZmhSteamPowerPonderScenes::boilerGrades,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS);
        helper.forComponents(
                        ZmhBlocks.COPPER_STEAM_ENGINE.getId(),
                        ZmhBlocks.BRASS_STEAM_ENGINE.getId(),
                        ZmhBlocks.INDUSTRIAL_STEAM_ENGINE.getId())
                .addStoryBoard("steam_engine/grades", ZmhSteamPowerPonderScenes::steamEngineGrades,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS);
    }

    private static void boilerGrades(
            SceneBuilder scene,
            SceneBuildingUtil util
    ) {
        BlockPos heater = util.grid().at(3, 1, 3);
        BlockPos lowerTank = util.grid().at(3, 2, 3);
        BlockPos upperTank = util.grid().at(3, 3, 3);
        BlockPos engine = util.grid().at(4, 3, 3);

        scene.title("boiler_grades", "Graded Fluid-Tank Boilers");
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
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 5, 4, 5), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(80)
                .text("Each grade is a complete Create Fluid Tank and forms native multiblocks with matching blocks")
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
                .text("Grade I upgrades ordinary heater output while preserving passive heat as passive")
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
                .text("Grade II transfers more heat per heater column for compact high-power boilers")
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
                .text("Grade III is an industrial pressure vessel for maximum-output airship power plants")
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
                .text("Different grades never merge; every multiblock must be built from one grade")
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
                .text("Goggles show native boiler data and the active grade profile; the Wrench toggles tank windows")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(lowerTank))
                .placeNearTarget();
        scene.idle(95);
    }

    private static void steamEngineGrades(
            SceneBuilder scene,
            SceneBuildingUtil util
    ) {
        BlockPos boiler = util.grid().at(2, 2, 3);
        BlockPos engine = boiler.east();
        BlockPos shaft = engine.east(2);
        BlockState copper = ZmhBlocks.COPPER_STEAM_ENGINE.get()
                .defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST);
        BlockState brass = ZmhBlocks.BRASS_STEAM_ENGINE.get()
                .defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST);
        BlockState industrial = ZmhBlocks.INDUSTRIAL_STEAM_ENGINE.get()
                .defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST);

        scene.title("steam_engine_grades", "Graded Steam Engines");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.9F);
        scene.world().setBlock(boiler, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get().defaultBlockState(), false);
        scene.world().setBlock(engine, copper, false);
        scene.world().setBlock(
                shaft,
                AllBlocks.POWERED_SHAFT.getDefaultState().setValue(ShaftBlock.AXIS, Direction.Axis.Z),
                false
        );
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 6, 4, 5), Direction.DOWN);
        scene.idle(25);

        scene.overlay().showText(80)
                .text("Grade I uses one Create-style cylinder and provides the baseline 1024 SU capacity")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(engine))
                .placeNearTarget();
        scene.idle(95);

        scene.world().setBlock(engine, brass, true);
        scene.overlay().showText(85)
                .text("Grade II uses two narrower cylinders in 180-degree opposition for a smoother compound stroke")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(engine))
                .placeNearTarget();
        scene.idle(100);

        scene.world().setBlock(engine, industrial, true);
        scene.overlay().showText(90)
                .text("Grade III uses three cylinders phased 120 degrees apart and a reinforced industrial housing")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(Vec3.atCenterOf(engine))
                .placeNearTarget();
        scene.idle(105);

        scene.overlay().showText(90)
                .text("Higher grades provide more Stress Units but consume multiple boiler engine units, preserving heat and water limits")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(boiler))
                .placeNearTarget();
        scene.idle(105);

        scene.overlay().showControls(
                        util.vector().blockSurface(engine, Direction.NORTH),
                        Pointing.RIGHT,
                        35
                )
                .withItem(AllItems.GOGGLES.asStack());
        scene.idle(10);
        scene.overlay().showText(80)
                .text("Engineer's Goggles show capacity, boiler load, cylinder count, and the active data-pack profile")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(engine))
                .placeNearTarget();
        scene.idle(95);
    }
}
