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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlockEntity;
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

        assertInt(helper, "Copper cylinders", 1, copper.cylinderCount());
        assertInt(helper, "Brass cylinders", 2, brass.cylinderCount());
        assertInt(helper, "Industrial cylinders", 3, industrial.cylinderCount());
        assertIncreasing(helper, "stress capacity", copper.stressCapacity(), brass.stressCapacity(), industrial.stressCapacity());
        assertIncreasing(helper, "boiler load", copper.boilerLoadUnits(), brass.boilerLoadUnits(), industrial.boilerLoadUnits());
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
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 40)
    public static void gradedBoilerCountsEngineLoadUnits(GameTestHelper helper) {
        BlockPos boilerPos = new BlockPos(3, 2, 3);
        BlockPos enginePos = boilerPos.east();
        BlockState engineState = ZmhBlocks.INDUSTRIAL_STEAM_ENGINE.get()
                .defaultBlockState()
                .setValue(SteamEngineBlock.FACE, AttachFace.WALL)
                .setValue(SteamEngineBlock.FACING, Direction.EAST);

        helper.setBlock(boilerPos, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());
        helper.setBlock(enginePos, engineState);

        helper.runAfterDelay(8, () -> {
            BoilerGradeBlockEntity boiler = helper.getBlockEntity(boilerPos);
            boiler.boiler.evaluate(boiler);
            assertInt(helper, "Industrial engine boiler load", 3, boiler.boiler.attachedEngines);
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 60)
    public static void industrialEngineDrivesNativePoweredShaft(GameTestHelper helper) {
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
                ZmhBlocks.INDUSTRIAL_STEAM_ENGINE.get()
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
                    helper.fail("Industrial engine did not transfer boiler efficiency to the powered shaft");
                }
                if (shaft.capacityKey != ZmhBlocks.INDUSTRIAL_STEAM_ENGINE.get()) {
                    helper.fail("Powered shaft capacity key does not reference the Industrial Steam Engine");
                }
                if (shaft.getGeneratedSpeed() == 0.0F) {
                    helper.fail("Industrial engine did not generate shaft speed");
                }
                helper.succeed();
            });
        });
    }

    private static void assertIncreasing(
            GameTestHelper helper,
            String label,
            double first,
            double second,
            double third
    ) {
        if (!(first < second && second < third)) {
            helper.fail(label + " must increase by grade: " + first + ", " + second + ", " + third);
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
