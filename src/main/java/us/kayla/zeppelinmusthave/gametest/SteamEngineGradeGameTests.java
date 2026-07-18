package us.kayla.zeppelinmusthave.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlockEntity;
import us.kayla.zeppelinmusthave.content.steam.LeviathanSteamEngineBlock;
import us.kayla.zeppelinmusthave.content.steam.LeviathanSteamEnginePart;
import us.kayla.zeppelinmusthave.content.steam.MkViiSteamEngineBlock;
import us.kayla.zeppelinmusthave.content.steam.MkViiSteamEnginePart;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeProfile;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeProfiles;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeTier;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SteamEngineGradeGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private SteamEngineGradeGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void profilesDefineIncreasingCapacityAndKinematics(GameTestHelper helper) {
        SteamEngineGradeProfile copper = SteamEngineGradeProfiles.INSTANCE.resolve(SteamEngineGradeTier.COPPER);
        SteamEngineGradeProfile brass = SteamEngineGradeProfiles.INSTANCE.resolve(SteamEngineGradeTier.BRASS);
        SteamEngineGradeProfile industrial = SteamEngineGradeProfiles.INSTANCE.resolve(SteamEngineGradeTier.INDUSTRIAL);
        SteamEngineGradeProfile grand = SteamEngineGradeProfiles.INSTANCE.resolve(SteamEngineGradeTier.GRAND);
        SteamEngineGradeProfile sovereign = SteamEngineGradeProfiles.INSTANCE.resolve(SteamEngineGradeTier.SOVEREIGN);
        SteamEngineGradeProfile leviathan = SteamEngineGradeProfiles.INSTANCE.resolve(SteamEngineGradeTier.LEVIATHAN);
        SteamEngineGradeProfile mkVii = SteamEngineGradeProfiles.INSTANCE.resolve(SteamEngineGradeTier.MK_VII);

        assertInt(helper, "Copper cylinders", 1, copper.cylinderCount());
        assertInt(helper, "Brass cylinders", 2, brass.cylinderCount());
        assertInt(helper, "Industrial cylinders", 3, industrial.cylinderCount());
        assertInt(helper, "Grand cylinders", 4, grand.cylinderCount());
        assertInt(helper, "Sovereign cylinders", 5, sovereign.cylinderCount());
        assertInt(helper, "Leviathan cylinders", 8, leviathan.cylinderCount());
        assertInt(helper, "MK VII aggregate cylinders", 9, mkVii.cylinderCount());
        assertIncreasing(helper, "stress capacity", copper.stressCapacity(), brass.stressCapacity(), industrial.stressCapacity(), grand.stressCapacity(), sovereign.stressCapacity(), leviathan.stressCapacity());
        assertIncreasing(helper, "boiler load", copper.boilerLoadUnits(), brass.boilerLoadUnits(), industrial.boilerLoadUnits(), grand.boilerLoadUnits(), sovereign.boilerLoadUnits(), leviathan.boilerLoadUnits(), mkVii.boilerLoadUnits());
        if (mkVii.stressCapacity() <= leviathan.stressCapacity()) {
            helper.fail("MK VII output capacity must exceed Leviathan capacity");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void stressRegistryUsesGradeProfiles(GameTestHelper helper) {
        assertDouble(
                helper,
                "Copper stress capacity",
                1024.0D,
                BlockStressValues.getCapacity(ZmhBlocks.COPPER_STEAM_ENGINE.get())
        );
        assertDouble(
                helper,
                "Brass stress capacity",
                2560.0D,
                BlockStressValues.getCapacity(ZmhBlocks.BRASS_STEAM_ENGINE.get())
        );
        assertDouble(
                helper,
                "Industrial stress capacity",
                4608.0D,
                BlockStressValues.getCapacity(ZmhBlocks.INDUSTRIAL_STEAM_ENGINE.get())
        );
        assertDouble(
                helper,
                "Grand stress capacity",
                8192.0D,
                BlockStressValues.getCapacity(ZmhBlocks.GRAND_STEAM_ENGINE.get())
        );
        assertDouble(
                helper,
                "Sovereign stress capacity",
                12288.0D,
                BlockStressValues.getCapacity(ZmhBlocks.SOVEREIGN_STEAM_ENGINE.get())
        );
        assertDouble(
                helper,
                "Leviathan stress capacity",
                20480.0D,
                BlockStressValues.getCapacity(ZmhBlocks.LEVIATHAN_STEAM_ENGINE.get())
        );
        assertDouble(
                helper,
                "MK VII output stress capacity",
                36864.0D,
                BlockStressValues.getCapacity(ZmhBlocks.MK_VII_STEAM_ENGINE.get())
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 40)
    public static void grandEngineCountsFourBoilerLoadUnits(GameTestHelper helper) {
        BlockPos boilerPos = new BlockPos(3, 2, 3);
        BlockPos enginePos = boilerPos.east();
        BlockState engineState = ZmhBlocks.GRAND_STEAM_ENGINE.get()
                .defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST);

        helper.setBlock(boilerPos, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());
        helper.setBlock(enginePos, engineState);

        helper.runAfterDelay(8, () -> {
            BoilerGradeBlockEntity boiler = helper.getBlockEntity(boilerPos);
            boiler.boiler.evaluate(boiler);
            assertInt(helper, "Grand engine boiler load", 4, boiler.boiler.attachedEngines);
            helper.succeed();
        });
    }


    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 40)
    public static void sovereignEngineCountsSixBoilerLoadUnits(GameTestHelper helper) {
        BlockPos boilerPos = new BlockPos(3, 2, 3);
        BlockPos enginePos = boilerPos.east();
        BlockState engineState = ZmhBlocks.SOVEREIGN_STEAM_ENGINE.get()
                .defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST);

        helper.setBlock(boilerPos, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());
        helper.setBlock(enginePos, engineState);

        helper.runAfterDelay(8, () -> {
            BoilerGradeBlockEntity boiler = helper.getBlockEntity(boilerPos);
            boiler.boiler.evaluate(boiler);
            assertInt(helper, "Sovereign engine boiler load", 6, boiler.boiler.attachedEngines);
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void leviathanRequiresIndustrialBoilerAndClearTFrame(GameTestHelper helper) {
        BlockPos boilerPos = new BlockPos(3, 2, 3);
        BlockPos controllerPos = boilerPos.east();
        LeviathanSteamEngineBlock engine = ZmhBlocks.LEVIATHAN_STEAM_ENGINE.get();
        BlockPos absoluteControllerPos = helper.absolutePos(controllerPos);
        BlockState controller = engine.defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST)
                .setValue(LeviathanSteamEngineBlock.PART, LeviathanSteamEnginePart.CONTROLLER);

        helper.setBlock(boilerPos, ZmhBlocks.BRASS_BOILER_BASE.get());
        if (engine.hasClearAssemblySpace(helper.getLevel(), absoluteControllerPos, controller)) {
            helper.fail("Leviathan accepted a boiler below Industrial MK III");
            return;
        }

        helper.setBlock(boilerPos, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());
        LeviathanSteamEngineBlock.AssemblyPositions positions =
                LeviathanSteamEngineBlock.assemblyPositions(controller, absoluteControllerPos);
        helper.getLevel().setBlockAndUpdate(positions.leftCylinder(), Blocks.STONE.defaultBlockState());
        if (engine.hasClearAssemblySpace(helper.getLevel(), absoluteControllerPos, controller)) {
            helper.fail("Leviathan accepted an obstructed left cylinder bank");
            return;
        }

        helper.getLevel().setBlockAndUpdate(positions.leftCylinder(), Blocks.AIR.defaultBlockState());
        if (!engine.hasClearAssemblySpace(helper.getLevel(), absoluteControllerPos, controller)) {
            helper.fail("Leviathan rejected a clear T-frame footprint");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 50)
    public static void leviathanBuildsCompleteTFrameAndCountsTenBoilerUnits(GameTestHelper helper) {
        BlockPos boilerPos = new BlockPos(3, 2, 3);
        BlockPos controllerPos = boilerPos.east();
        LeviathanSteamEngineBlock engine = ZmhBlocks.LEVIATHAN_STEAM_ENGINE.get();
        BlockPos absoluteControllerPos = helper.absolutePos(controllerPos);
        BlockState controller = engine.defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST)
                .setValue(LeviathanSteamEngineBlock.PART, LeviathanSteamEnginePart.CONTROLLER);

        helper.setBlock(boilerPos, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());
        helper.setBlock(controllerPos, controller);
        engine.placeAuxiliaryParts(helper.getLevel(), absoluteControllerPos, controller);

        if (!engine.isAssemblyComplete(helper.getLevel(), absoluteControllerPos, controller)) {
            helper.fail("Leviathan did not create its complete T-frame footprint");
            return;
        }

        helper.runAfterDelay(8, () -> {
            BoilerGradeBlockEntity boiler = helper.getBlockEntity(boilerPos);
            boiler.boiler.evaluate(boiler);
            assertInt(helper, "Leviathan engine boiler load", 10, boiler.boiler.attachedEngines);
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void mkViiRejectsMissingBoilerWidthAndBlockedDriveSpace(GameTestHelper helper) {
        BlockPos boilerCenter = new BlockPos(1, 2, 3);
        BlockPos controllerPos = boilerCenter.east();
        BlockPos absoluteControllerPos = helper.absolutePos(controllerPos);
        MkViiSteamEngineBlock engine = ZmhBlocks.MK_VII_STEAM_ENGINE.get();
        BlockState controller = engine.defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST)
                .setValue(MkViiSteamEngineBlock.PART, MkViiSteamEnginePart.CONTROLLER);

        helper.setBlock(boilerCenter, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());
        if (engine.hasClearAssemblySpace(helper.getLevel(), absoluteControllerPos, controller)) {
            helper.fail("MK VII accepted a boiler face narrower than three blocks");
            return;
        }

        helper.setBlock(boilerCenter.north(), ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());
        helper.setBlock(boilerCenter.south(), ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());
        MkViiSteamEngineBlock.AssemblyPositions positions =
                MkViiSteamEngineBlock.assemblyPositions(controller, absoluteControllerPos);
        BlockPos blockedClearance = positions.banks().getFirst().serviceClearance();
        helper.getLevel().setBlockAndUpdate(blockedClearance, Blocks.STONE.defaultBlockState());
        if (engine.hasClearAssemblySpace(helper.getLevel(), absoluteControllerPos, controller)) {
            helper.fail("MK VII accepted an occupied service-clearance cell");
            return;
        }

        helper.getLevel().setBlockAndUpdate(blockedClearance, Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(positions.outputShaft(), Blocks.STONE.defaultBlockState());
        if (engine.hasClearAssemblySpace(helper.getLevel(), absoluteControllerPos, controller)) {
            helper.fail("MK VII accepted an occupied central output position");
            return;
        }

        helper.getLevel().setBlockAndUpdate(positions.outputShaft(), Blocks.AIR.defaultBlockState());
        if (!engine.hasClearAssemblySpace(helper.getLevel(), absoluteControllerPos, controller)) {
            helper.fail("MK VII rejected a clear three-bank drive footprint");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 40)
    public static void mkViiBuildsThreeWideBodyAndOneCentralOutput(GameTestHelper helper) {
        BlockPos boilerCenter = new BlockPos(1, 2, 3);
        BlockPos controllerPos = boilerCenter.east();
        BlockPos absoluteControllerPos = helper.absolutePos(controllerPos);
        MkViiSteamEngineBlock engine = ZmhBlocks.MK_VII_STEAM_ENGINE.get();
        BlockState controller = engine.defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST)
                .setValue(MkViiSteamEngineBlock.PART, MkViiSteamEnginePart.CONTROLLER);

        helper.setBlock(boilerCenter.north(), ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());
        helper.setBlock(boilerCenter, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());
        helper.setBlock(boilerCenter.south(), ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());
        helper.setBlock(controllerPos, controller);
        engine.placeAssembly(helper.getLevel(), absoluteControllerPos, controller);

        MkViiSteamEngineBlock.AssemblyPositions positions =
                MkViiSteamEngineBlock.assemblyPositions(controller, absoluteControllerPos);
        assertInt(helper, "MK VII body width", 3, positions.banks().size());
        assertInt(helper, "MK VII internal shaft count", 9, MkViiSteamEngineBlock.INTERNAL_SHAFT_COUNT);
        if (!engine.isAssemblyComplete(helper.getLevel(), absoluteControllerPos, controller)) {
            helper.fail("MK VII did not create a complete three-bank assembly");
            return;
        }
        if (!AllBlocks.POWERED_SHAFT.has(helper.getLevel().getBlockState(positions.outputShaft()))) {
            helper.fail("MK VII did not create its central powered output shaft");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 60)
    public static void grandEngineDrivesNativePoweredShaft(GameTestHelper helper) {
        BlockPos controllerPos = new BlockPos(2, 2, 2);
        BlockPos enginePos = new BlockPos(4, 2, 2);
        BlockPos shaftPos = new BlockPos(6, 2, 2);

        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                helper.setBlock(
                        controllerPos.offset(x, 0, z),
                        ZmhBlocks.INDUSTRIAL_BOILER_BASE.get()
                );
            }
        }

        helper.setBlock(
                enginePos,
                ZmhBlocks.GRAND_STEAM_ENGINE.get()
                        .defaultBlockState()
                        .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                        .setValue(SteamEngineBlock.FACING, Direction.EAST)
        );
        helper.setBlock(
                shaftPos,
                AllBlocks.POWERED_SHAFT.getDefaultState()
                        .setValue(ShaftBlock.AXIS, Direction.Axis.Z)
        );

        helper.runAfterDelay(10, () -> {
            BoilerGradeBlockEntity boiler = helper.getBlockEntity(controllerPos);
            boiler.boiler.evaluate(boiler);
            boiler.boiler.activeHeat = 3;
            boiler.boiler.waterSupply = 30.0F;
            boiler.boiler.needsHeatLevelUpdate = false;

            helper.runAfterDelay(6, () -> {
                PoweredShaftBlockEntity shaft = helper.getBlockEntity(shaftPos);
                if (shaft.engineEfficiency <= 0.0F) {
                    helper.fail("Grand engine did not transfer boiler efficiency to the powered shaft");
                }
                if (shaft.capacityKey != ZmhBlocks.GRAND_STEAM_ENGINE.get()) {
                    helper.fail("Powered shaft capacity key does not reference the Grand Steam Engine");
                }
                if (shaft.getGeneratedSpeed() == 0.0F) {
                    helper.fail("Grand engine did not generate shaft speed");
                }
                helper.succeed();
            });
        });
    }

    private static void assertIncreasing(
            GameTestHelper helper,
            String label,
            double... values
    ) {
        for (int index = 1; index < values.length; index++) {
            if (values[index - 1] >= values[index]) {
                helper.fail(label + " must increase by grade: " + java.util.Arrays.toString(values));
                return;
            }
        }
    }

    private static void assertInt(GameTestHelper helper, String label, int expected, int actual) {
        if (expected != actual) {
            helper.fail(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertDouble(GameTestHelper helper, String label, double expected, double actual) {
        if (Math.abs(expected - actual) > 0.0001D) {
            helper.fail(label + ": expected " + expected + ", got " + actual);
        }
    }
}
