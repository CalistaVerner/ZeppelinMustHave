package us.kayla.zeppelinmusthave.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneRepeaterBlock;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PipedRedstoneDeviceGameTests {
    private PipedRedstoneDeviceGameTests() {
    }

    @GameTest(template = PipedRedstoneGameTestFixtures.TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void nativeLeverPowersOnlyAttachedConduit(GameTestHelper helper) {
        BlockState conduit = PipedRedstoneGameTestFixtures.horizontalConduit(
                ZmhBlocks.COPPER_PIPED_REDSTONE.get().defaultBlockState()
        );
        for (int x = 2; x <= 8; x++) {
            helper.setBlock(new BlockPos(x, 1, 2), conduit);
        }
        helper.setBlock(new BlockPos(9, 1, 2), Blocks.REDSTONE_LAMP);
        helper.setBlock(new BlockPos(1, 1, 3), Blocks.REDSTONE_LAMP);
        helper.setBlock(
                new BlockPos(1, 1, 2),
                ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER.get()
                        .defaultBlockState()
                        .setValue(PipedRedstoneNativeLeverBlock.FACE, AttachFace.WALL)
                        .setValue(PipedRedstoneNativeLeverBlock.FACING, Direction.WEST)
                        .setValue(PipedRedstoneNativeLeverBlock.POWERED, false)
        );

        helper.useBlock(new BlockPos(1, 1, 2), helper.makeMockPlayer(GameType.CREATIVE));
        helper.runAtTickTime(6L, () -> {
            helper.assertBlockProperty(
                    new BlockPos(1, 1, 2),
                    PipedRedstoneNativeLeverBlock.POWERED,
                    true
            );
            helper.assertBlockProperty(new BlockPos(9, 1, 2), RedstoneLampBlock.LIT, true);
            helper.assertBlockProperty(new BlockPos(1, 1, 3), RedstoneLampBlock.LIT, false);
            helper.succeed();
        });
    }

    @GameTest(template = PipedRedstoneGameTestFixtures.TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void repeaterUsesVanillaDelaySteps(GameTestHelper helper) {
        BlockState fastRepeater = ZmhBlocks.PIPED_REDSTONE_REPEATER.get()
                .defaultBlockState()
                .setValue(PipedRedstoneRepeaterBlock.FACING, Direction.EAST)
                .setValue(PipedRedstoneRepeaterBlock.DELAY, 1);
        BlockState slowRepeater = fastRepeater.setValue(PipedRedstoneRepeaterBlock.DELAY, 4);

        helper.setBlock(new BlockPos(1, 1, 2), Blocks.REDSTONE_BLOCK);
        helper.setBlock(new BlockPos(2, 1, 2), fastRepeater);
        helper.setBlock(new BlockPos(1, 1, 4), Blocks.REDSTONE_BLOCK);
        helper.setBlock(new BlockPos(2, 1, 4), slowRepeater);
        helper.setBlock(new BlockPos(3, 1, 2), Blocks.REDSTONE_LAMP);
        helper.setBlock(new BlockPos(3, 1, 4), Blocks.REDSTONE_LAMP);

        helper.runAtTickTime(3L, () -> {
            helper.assertBlockProperty(new BlockPos(2, 1, 2), PipedRedstoneRepeaterBlock.POWER, 15);
            helper.assertBlockProperty(new BlockPos(2, 1, 4), PipedRedstoneRepeaterBlock.POWER, 0);
            helper.assertBlockProperty(new BlockPos(3, 1, 2), RedstoneLampBlock.LIT, true);
            helper.assertBlockProperty(new BlockPos(3, 1, 4), RedstoneLampBlock.LIT, false);
        });
        helper.runAtTickTime(9L, () -> {
            helper.assertBlockProperty(new BlockPos(2, 1, 4), PipedRedstoneRepeaterBlock.POWER, 15);
            helper.assertBlockProperty(new BlockPos(3, 1, 4), RedstoneLampBlock.LIT, true);
            helper.succeed();
        });
    }
}
