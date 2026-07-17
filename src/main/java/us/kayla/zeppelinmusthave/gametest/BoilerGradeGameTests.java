package us.kayla.zeppelinmusthave.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlockEntity;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BoilerGradeGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private BoilerGradeGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 40)
    public static void sameGradeTanksFormVerticalMultiblock(GameTestHelper helper) {
        BlockPos lower = new BlockPos(2, 1, 2);
        BlockPos upper = lower.above();
        helper.setBlock(lower, ZmhBlocks.COPPER_BOILER_BASE.get());
        helper.setBlock(upper, ZmhBlocks.COPPER_BOILER_BASE.get());

        helper.runAfterDelay(8, () -> {
            BoilerGradeBlockEntity lowerTank = helper.getBlockEntity(lower);
            BoilerGradeBlockEntity upperTank = helper.getBlockEntity(upper);
            BlockPos expectedController = helper.absolutePos(lower);

            assertPosition(helper, "lower controller", expectedController, lowerTank.getController());
            assertPosition(helper, "upper controller", expectedController, upperTank.getController());

            FluidTankBlockEntity controller = lowerTank.getControllerBE();
            if (controller == null) {
                helper.fail("Copper boiler controller was not resolved");
            }
            assertInt(helper, "combined height", 2, controller.getHeight());
            assertInt(
                    helper,
                    "combined capacity",
                    2 * FluidTankBlockEntity.getCapacityMultiplier(),
                    controller.getTankInventory().getCapacity()
            );
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 40)
    public static void differentGradesDoNotMerge(GameTestHelper helper) {
        BlockPos copperPos = new BlockPos(2, 1, 2);
        BlockPos brassPos = copperPos.above();
        helper.setBlock(copperPos, ZmhBlocks.COPPER_BOILER_BASE.get());
        helper.setBlock(brassPos, ZmhBlocks.BRASS_BOILER_BASE.get());

        helper.runAfterDelay(8, () -> {
            BoilerGradeBlockEntity copper = helper.getBlockEntity(copperPos);
            BoilerGradeBlockEntity brass = helper.getBlockEntity(brassPos);

            assertPosition(helper, "copper controller", helper.absolutePos(copperPos), copper.getController());
            assertPosition(helper, "brass controller", helper.absolutePos(brassPos), brass.getController());
            assertInt(helper, "copper height", 1, copper.getHeight());
            assertInt(helper, "brass height", 1, brass.getHeight());

            if (copper.getType() == brass.getType()) {
                helper.fail("Different boiler grades unexpectedly share one BlockEntityType");
            }
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 40)
    public static void gradeProfilesTransformNativeHeaterOutput(GameTestHelper helper) {
        BlockState kindled = AllBlocks.BLAZE_BURNER.get()
                .defaultBlockState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED);
        BlockState seething = AllBlocks.BLAZE_BURNER.get()
                .defaultBlockState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING);

        BlockPos copperHeater = new BlockPos(2, 1, 2);
        BlockPos industrialHeater = new BlockPos(5, 1, 2);
        BlockPos copperTank = copperHeater.above();
        BlockPos industrialTank = industrialHeater.above();
        helper.setBlock(copperHeater, kindled);
        helper.setBlock(industrialHeater, seething);
        helper.setBlock(copperTank, ZmhBlocks.COPPER_BOILER_BASE.get());
        helper.setBlock(industrialTank, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());

        helper.runAfterDelay(8, () -> {
            // A fuel-less Blaze Burner block entity cools itself after placement.
            // Re-apply the requested test state immediately before the native lookup.
            helper.setBlock(copperHeater, kindled);
            helper.setBlock(industrialHeater, seething);
            BoilerGradeBlockEntity copper = helper.getBlockEntity(copperTank);
            BoilerGradeBlockEntity industrial = helper.getBlockEntity(industrialTank);
            copper.boiler.updateTemperature(copper);
            industrial.boiler.updateTemperature(industrial);

            assertInt(helper, "Grade I kindled heat", 2, copper.boiler.activeHeat);
            assertInt(helper, "Grade III superheated heat", 8, industrial.boiler.activeHeat);
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 40)
    public static void passiveHeatIsNeverAmplified(GameTestHelper helper) {
        BlockPos heater = new BlockPos(2, 1, 2);
        BlockPos tank = heater.above();
        helper.setBlock(heater, Blocks.MAGMA_BLOCK);
        helper.setBlock(tank, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());

        helper.runAfterDelay(8, () -> {
            BoilerGradeBlockEntity industrial = helper.getBlockEntity(tank);
            industrial.boiler.updateTemperature(industrial);
            if (!industrial.boiler.passiveHeat) {
                helper.fail("Grade III must preserve Create passive heat semantics");
            }
            assertInt(helper, "passive active-heat contribution", 0, industrial.boiler.activeHeat);
            helper.succeed();
        });
    }

    private static void assertInt(GameTestHelper helper, String label, int expected, int actual) {
        if (expected != actual) {
            helper.fail(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertPosition(
            GameTestHelper helper,
            String label,
            BlockPos expected,
            BlockPos actual
    ) {
        if (!expected.equals(actual)) {
            helper.fail(label + ": expected " + expected + ", got " + actual);
        }
    }
}
