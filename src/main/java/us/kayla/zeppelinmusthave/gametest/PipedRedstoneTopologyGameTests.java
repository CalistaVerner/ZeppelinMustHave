package us.kayla.zeppelinmusthave.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneBlock;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PipedRedstoneTopologyGameTests {
    private PipedRedstoneTopologyGameTests() {
    }

    @GameTest(template = PipedRedstoneGameTestFixtures.TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void parallelLinesRemainIsolated(GameTestHelper helper) {
        BlockState conduit = PipedRedstoneGameTestFixtures.horizontalConduit(
                ZmhBlocks.COPPER_PIPED_REDSTONE.get().defaultBlockState()
        );
        for (int x = 2; x <= 8; x++) {
            helper.setBlock(new BlockPos(x, 1, 2), conduit);
            helper.setBlock(new BlockPos(x, 1, 3), conduit);
        }
        helper.setBlock(new BlockPos(1, 1, 2), Blocks.REDSTONE_BLOCK);

        helper.runAtTickTime(6L, () -> {
            helper.assertBlockProperty(new BlockPos(8, 1, 2), PipedRedstoneBlock.POWER, 15);
            helper.assertBlockProperty(new BlockPos(8, 1, 3), PipedRedstoneBlock.POWER, 0);
            helper.succeed();
        });
    }

    @GameTest(template = PipedRedstoneGameTestFixtures.TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void sidePlacementCreatesTerminalElbow(GameTestHelper helper) {
        BlockState horizontal = PipedRedstoneGameTestFixtures.horizontalConduit(
                ZmhBlocks.COPPER_PIPED_REDSTONE.get().defaultBlockState()
        );
        BlockState verticalTurn = ZmhBlocks.COPPER_PIPED_REDSTONE.get()
                .defaultBlockState()
                .setValue(PipedRedstoneBlock.NORTH, true)
                .setValue(PipedRedstoneBlock.SOUTH, true);

        helper.setBlock(new BlockPos(2, 1, 3), horizontal);
        helper.setBlock(new BlockPos(3, 1, 3), horizontal);
        helper.setBlock(new BlockPos(3, 1, 2), verticalTurn);

        helper.runAtTickTime(2L, () -> {
            helper.assertBlockProperty(new BlockPos(3, 1, 3), PipedRedstoneBlock.WEST, true);
            helper.assertBlockProperty(new BlockPos(3, 1, 3), PipedRedstoneBlock.NORTH, true);
            helper.assertBlockProperty(new BlockPos(3, 1, 3), PipedRedstoneBlock.EAST, false);
            helper.assertBlockProperty(new BlockPos(3, 1, 2), PipedRedstoneBlock.SOUTH, true);
            helper.succeed();
        });
    }

    @GameTest(template = PipedRedstoneGameTestFixtures.TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void waterloggedConduitsRemainOperational(GameTestHelper helper) {
        BlockState conduit = PipedRedstoneGameTestFixtures.horizontalConduit(
                ZmhBlocks.COPPER_PIPED_REDSTONE.get().defaultBlockState()
        ).setValue(PipedRedstoneBlock.WATERLOGGED, true);

        for (int x = 2; x <= 8; x++) {
            helper.setBlock(new BlockPos(x, 1, 2), conduit);
        }
        helper.setBlock(new BlockPos(1, 1, 2), Blocks.REDSTONE_BLOCK);

        helper.runAtTickTime(6L, () -> {
            helper.assertBlockProperty(new BlockPos(8, 1, 2), PipedRedstoneBlock.WATERLOGGED, true);
            helper.assertBlockProperty(new BlockPos(8, 1, 2), PipedRedstoneBlock.POWER, 15);
            helper.succeed();
        });
    }

    @GameTest(template = PipedRedstoneGameTestFixtures.TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void placementAssistExtendsFarEnd(GameTestHelper helper) {
        BlockState conduit = PipedRedstoneGameTestFixtures.horizontalConduit(
                ZmhBlocks.COPPER_PIPED_REDSTONE.get().defaultBlockState()
        );
        helper.setBlock(new BlockPos(2, 1, 2), conduit);
        helper.setBlock(new BlockPos(3, 1, 2), conduit);
        helper.setBlock(new BlockPos(4, 1, 2), conduit);

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setItemInHand(
                InteractionHand.MAIN_HAND,
                new ItemStack(ZmhBlocks.BRASS_PIPED_REDSTONE_ITEM.get())
        );

        BlockPos clickedRelative = new BlockPos(2, 1, 2);
        BlockPos clickedAbsolute = helper.absolutePos(clickedRelative);
        BlockHitResult hitResult = new BlockHitResult(
                Vec3.atCenterOf(clickedAbsolute).add(0.49D, 0.0D, 0.0D),
                Direction.EAST,
                clickedAbsolute,
                false
        );
        helper.useBlock(clickedRelative, player, hitResult);

        helper.assertBlockPresent(ZmhBlocks.BRASS_PIPED_REDSTONE.get(), new BlockPos(5, 1, 2));
        helper.assertBlockProperty(new BlockPos(5, 1, 2), PipedRedstoneBlock.EAST, true);
        helper.assertBlockProperty(new BlockPos(5, 1, 2), PipedRedstoneBlock.WEST, true);
        helper.succeed();
    }
}
