package us.kayla.zeppelinmusthave.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BoilerGradeGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private BoilerGradeGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void activeBlazeBurnerUsesGradeTransfer(GameTestHelper helper) {
        BlockState activeBurner = AllBlocks.BLAZE_BURNER.get()
                .defaultBlockState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED);

        assertGrade(helper, new BlockPos(2, 1, 2), activeBurner,
                ZmhBlocks.COPPER_BOILER_BASE.get().defaultBlockState(), 2);
        assertGrade(helper, new BlockPos(5, 1, 2), activeBurner,
                ZmhBlocks.BRASS_BOILER_BASE.get().defaultBlockState(), 3);
        assertGrade(helper, new BlockPos(8, 1, 2), activeBurner,
                ZmhBlocks.INDUSTRIAL_BOILER_BASE.get().defaultBlockState(), 5);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void superheatedBlazeBurnerReachesGradeCaps(GameTestHelper helper) {
        BlockState superheated = AllBlocks.BLAZE_BURNER.get()
                .defaultBlockState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SEETHING);

        assertGrade(helper, new BlockPos(2, 1, 2), superheated,
                ZmhBlocks.COPPER_BOILER_BASE.get().defaultBlockState(), 3);
        assertGrade(helper, new BlockPos(5, 1, 2), superheated,
                ZmhBlocks.BRASS_BOILER_BASE.get().defaultBlockState(), 5);
        assertGrade(helper, new BlockPos(8, 1, 2), superheated,
                ZmhBlocks.INDUSTRIAL_BOILER_BASE.get().defaultBlockState(), 8);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void passiveHeatIsNotAmplified(GameTestHelper helper) {
        BlockPos source = new BlockPos(2, 1, 2);
        BlockPos base = source.above();
        helper.setBlock(source, Blocks.MAGMA_BLOCK);
        helper.setBlock(base, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());

        float heat = BoilerHeater.findHeat(
                helper.getLevel(),
                helper.absolutePos(base),
                helper.getBlockState(base)
        );
        assertHeat(helper, "passive heat", BoilerHeater.PASSIVE_HEAT, heat);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void boilerBasesCannotBeStacked(GameTestHelper helper) {
        BlockPos lower = new BlockPos(2, 1, 2);
        BlockPos upper = lower.above();
        helper.setBlock(lower, ZmhBlocks.COPPER_BOILER_BASE.get());
        helper.setBlock(upper, ZmhBlocks.INDUSTRIAL_BOILER_BASE.get());

        float heat = BoilerHeater.findHeat(
                helper.getLevel(),
                helper.absolutePos(upper),
                helper.getBlockState(upper)
        );
        assertHeat(helper, "stacked boiler bases", BoilerHeater.NO_HEAT, heat);
        helper.succeed();
    }

    private static void assertGrade(
            GameTestHelper helper,
            BlockPos source,
            BlockState sourceState,
            BlockState gradeState,
            int expected
    ) {
        BlockPos base = source.above();
        helper.setBlock(source, sourceState);
        helper.setBlock(base, gradeState);
        float heat = BoilerHeater.findHeat(
                helper.getLevel(),
                helper.absolutePos(base),
                helper.getBlockState(base)
        );
        assertHeat(helper, gradeState.getBlock().getDescriptionId(), expected, heat);
    }

    private static void assertHeat(
            GameTestHelper helper,
            String label,
            float expected,
            float actual
    ) {
        if (Float.compare(expected, actual) != 0) {
            helper.fail(label + ": expected heat " + expected + ", got " + actual);
        }
    }
}
