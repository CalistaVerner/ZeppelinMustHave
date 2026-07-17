package us.kayla.zeppelinmusthave.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneBlock;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PipedRedstoneSignalGameTests {
    private PipedRedstoneSignalGameTests() {
    }

    @GameTest(template = PipedRedstoneGameTestFixtures.TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void copperRangeEndsAfterThirtyTwoEdges(GameTestHelper helper) {
        BlockState conduit = PipedRedstoneGameTestFixtures.horizontalConduit(
                ZmhBlocks.COPPER_PIPED_REDSTONE.get().defaultBlockState()
        );
        for (int x = 2; x <= 35; x++) {
            helper.setBlock(new BlockPos(x, 1, 2), conduit);
        }
        helper.setBlock(new BlockPos(1, 1, 2), Blocks.REDSTONE_BLOCK);

        helper.runAtTickTime(6L, () -> {
            helper.assertBlockProperty(new BlockPos(34, 1, 2), PipedRedstoneBlock.POWER, 15);
            helper.assertBlockProperty(new BlockPos(35, 1, 2), PipedRedstoneBlock.POWER, 0);
            helper.succeed();
        });
    }

    @GameTest(template = PipedRedstoneGameTestFixtures.TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void conduitPowersVanillaRedstoneLamp(GameTestHelper helper) {
        BlockState conduit = PipedRedstoneGameTestFixtures.horizontalConduit(
                ZmhBlocks.COPPER_PIPED_REDSTONE.get().defaultBlockState()
        );
        for (int x = 2; x <= 8; x++) {
            helper.setBlock(new BlockPos(x, 1, 2), conduit);
        }
        helper.setBlock(new BlockPos(1, 1, 2), Blocks.REDSTONE_BLOCK);
        helper.setBlock(new BlockPos(9, 1, 2), Blocks.REDSTONE_LAMP);

        helper.runAtTickTime(6L, () -> {
            helper.assertBlockProperty(new BlockPos(8, 1, 2), PipedRedstoneBlock.POWER, 15);
            helper.assertBlockProperty(new BlockPos(9, 1, 2), RedstoneLampBlock.LIT, true);
            helper.succeed();
        });
    }
}
