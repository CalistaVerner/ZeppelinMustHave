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

final class ZmhSteamPowerPonderScenes {
    private ZmhSteamPowerPonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        var items = ZmhPonderRegistration.items(helper);
        items.forComponents(
                        ZmhBlocks.COPPER_BOILER_BASE_ITEM.get(),
                        ZmhBlocks.BRASS_BOILER_BASE_ITEM.get(),
                        ZmhBlocks.INDUSTRIAL_BOILER_BASE_ITEM.get()
                )
                .addStoryBoard(
                        "boiler/grades",
                        ZmhSteamPowerPonderScenes::boilerGrades,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
        items.forComponents(
                        ZmhBlocks.COPPER_STEAM_ENGINE_ITEM.get(),
                        ZmhBlocks.BRASS_STEAM_ENGINE_ITEM.get(),
                        ZmhBlocks.INDUSTRIAL_STEAM_ENGINE_ITEM.get(),
                        ZmhBlocks.GRAND_STEAM_ENGINE_ITEM.get(),
                        ZmhBlocks.SOVEREIGN_STEAM_ENGINE_ITEM.get(),
                        ZmhBlocks.LEVIATHAN_STEAM_ENGINE_ITEM.get(),
                        ZmhBlocks.MK_VII_STEAM_ENGINE_ITEM.get()
                )
                .addStoryBoard(
                        "steam_engine/grades",
                        ZmhSteamPowerPonderScenes::steamEngineGrades,
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

    private static void steamEngineGrades(
            SceneBuilder builder,
            SceneBuildingUtil util
    ) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        BlockPos lowerBoiler = util.grid().at(2, 1, 3);
        BlockPos upperBoiler = util.grid().at(2, 2, 3);
        BlockPos engine = util.grid().at(3, 2, 3);
        BlockPos shaft = util.grid().at(5, 2, 3);
        BlockPos shaftSupport = shaft.below();

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
        BlockState grand = ZmhBlocks.GRAND_STEAM_ENGINE.get()
                .defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST);
        BlockState sovereign = ZmhBlocks.SOVEREIGN_STEAM_ENGINE.get()
                .defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST);
        BlockState leviathan = ZmhBlocks.LEVIATHAN_STEAM_ENGINE.get()
                .defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST)
                .setValue(LeviathanSteamEngineBlock.PART, LeviathanSteamEnginePart.CONTROLLER);

        scene.title("steam_engine_grades", "MK-Series Steam Engines");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.78F);

        setEngineInstallation(
                scene,
                lowerBoiler,
                upperBoiler,
                engine,
                ZmhBlocks.COPPER_BOILER_BASE.get().defaultBlockState(),
                copper,
                false
        );
        scene.world().setBlock(
                shaft,
                AllBlocks.POWERED_SHAFT.getDefaultState().setValue(ShaftBlock.AXIS, Direction.Axis.Z),
                false
        );
        scene.world().setBlock(shaftSupport, AllBlocks.ANDESITE_CASING.getDefaultState(), false);
        scene.idle(1);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(12);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 6, 3, 4), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().position(shaft), 32);
        scene.idle(25);

        scene.overlay().showOutline(
                PonderPalette.INPUT,
                "mk_i_engine_installation",
                util.select().fromTo(lowerBoiler, upperBoiler).add(util.select().position(engine)),
                80
        );
        scene.overlay().showText(80)
                .text("MK I uses one Create-style cylinder and provides the baseline 1024 SU capacity")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(engine))
                .placeNearTarget();
        scene.idle(95);

        setEngineInstallation(
                scene,
                lowerBoiler,
                upperBoiler,
                engine,
                ZmhBlocks.BRASS_BOILER_BASE.get().defaultBlockState(),
                brass,
                true
        );
        scene.effects().indicateSuccess(engine);
        scene.overlay().showText(85)
                .text("MK II uses two narrower cylinders in 180-degree opposition for a smoother compound stroke")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(engine))
                .placeNearTarget();
        scene.idle(100);

        setEngineInstallation(
                scene,
                lowerBoiler,
                upperBoiler,
                engine,
                ZmhBlocks.INDUSTRIAL_BOILER_BASE.get().defaultBlockState(),
                industrial,
                true
        );
        scene.effects().indicateSuccess(engine);
        scene.overlay().showText(90)
                .text("MK III uses three cylinders phased 120 degrees apart and a reinforced industrial housing")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(Vec3.atCenterOf(engine))
                .placeNearTarget();
        scene.idle(105);

        scene.world().setBlock(engine, grand, true);
        scene.effects().indicateSuccess(engine);
        scene.overlay().showText(100)
                .text("MK IV is the four-cylinder flagship engine for the largest boiler and propulsion installations")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(Vec3.atCenterOf(engine))
                .placeNearTarget();
        scene.idle(115);

        scene.world().setBlock(engine, sovereign, true);
        scene.effects().indicateSuccess(engine);
        scene.overlay().showText(110)
                .text("MK V uses five cylinders phased 72 degrees apart, a rotating pressure core, and 12288 SU for capital-class airships")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(Vec3.atCenterOf(engine))
                .placeNearTarget();
        scene.idle(125);

        setLeviathanInstallation(scene, engine, leviathan, true);
        scene.effects().indicateSuccess(engine);
        LeviathanSteamEngineBlock.AssemblyPositions leviathanPositions =
                LeviathanSteamEngineBlock.assemblyPositions(leviathan, engine);
        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "mk_vi_t_frame",
                util.select().position(engine)
                        .add(util.select().position(leviathanPositions.leftCylinder()))
                        .add(util.select().position(leviathanPositions.rightCylinder()))
                        .add(util.select().position(leviathanPositions.shaftNose())),
                110
        );
        scene.overlay().showText(110)
                .text("MK VI is an eight-cylinder T-frame engine: two lateral cylinder banks and one forward shaft nose must all have free space")
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .pointAt(Vec3.atCenterOf(engine))
                .placeNearTarget();
        scene.idle(125);

        scene.overlay().showOutline(
                PonderPalette.RED,
                "boiler_engine_load",
                util.select().fromTo(lowerBoiler, upperBoiler),
                90
        );
        scene.overlay().showText(90)
                .text("Higher MK series provide more Stress Units but consume multiple boiler engine units, preserving heat and water limits")
                .attachKeyFrame()
                .pointAt(Vec3.atCenterOf(upperBoiler))
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

    private static void setLeviathanInstallation(
            CreateSceneBuilder scene,
            BlockPos engine,
            BlockState controller,
            boolean redraw
    ) {
        LeviathanSteamEngineBlock.AssemblyPositions positions =
                LeviathanSteamEngineBlock.assemblyPositions(controller, engine);
        scene.world().setBlock(engine, controller, redraw);
        scene.world().setBlock(
                positions.leftCylinder(),
                LeviathanSteamEngineBlock.stateForPart(
                        controller,
                        LeviathanSteamEnginePart.LEFT_CYLINDER,
                        false
                ),
                redraw
        );
        scene.world().setBlock(
                positions.rightCylinder(),
                LeviathanSteamEngineBlock.stateForPart(
                        controller,
                        LeviathanSteamEnginePart.RIGHT_CYLINDER,
                        false
                ),
                redraw
        );
        scene.world().setBlock(
                positions.shaftNose(),
                LeviathanSteamEngineBlock.stateForPart(
                        controller,
                        LeviathanSteamEnginePart.SHAFT_NOSE,
                        false
                ),
                redraw
        );
    }

    private static void setEngineInstallation(
            CreateSceneBuilder scene,
            BlockPos lowerBoiler,
            BlockPos upperBoiler,
            BlockPos engine,
            BlockState boilerState,
            BlockState engineState,
            boolean redraw
    ) {
        scene.world().setBlock(lowerBoiler, boilerState, redraw);
        scene.world().setBlock(upperBoiler, boilerState, redraw);
        scene.world().setBlock(engine, engineState, redraw);
    }
}
