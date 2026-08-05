package us.kayla.zeppelinmusthave.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AirshipCasingGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private AirshipCasingGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 40)
    public static void airshipBrassEncasesEverySupportedCreateComponent(GameTestHelper helper) {
        CasingFixture fixture = placeCreateComponents(helper);
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack casing = new ItemStack(ZmhBlocks.AIRSHIP_BRASS_CASING_ITEM.get());

        useItem(helper, player, fixture.shaft(), casing, false);
        useItem(helper, player, fixture.smallCog(), casing, false);
        useItem(helper, player, fixture.largeCog(), casing, false);
        useItem(helper, player, fixture.pipe(), casing, false);

        assertEncasedFamily(
                helper,
                fixture,
                ZmhBlocks.AIRSHIP_BRASS_ENCASED_SHAFT.get(),
                ZmhBlocks.AIRSHIP_BRASS_ENCASED_COGWHEEL.get(),
                ZmhBlocks.AIRSHIP_BRASS_ENCASED_LARGE_COGWHEEL.get(),
                ZmhBlocks.AIRSHIP_BRASS_ENCASED_FLUID_PIPE.get()
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 40)
    public static void airshipBronzeEncasesAndWrenchesBackToCreateComponents(GameTestHelper helper) {
        CasingFixture fixture = placeCreateComponents(helper);
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack casing = new ItemStack(ZmhBlocks.AIRSHIP_BRONZE_CASING_ITEM.get());

        useItem(helper, player, fixture.shaft(), casing, false);
        useItem(helper, player, fixture.smallCog(), casing, false);
        useItem(helper, player, fixture.largeCog(), casing, false);
        useItem(helper, player, fixture.pipe(), casing, false);

        assertEncasedFamily(
                helper,
                fixture,
                ZmhBlocks.AIRSHIP_BRONZE_ENCASED_SHAFT.get(),
                ZmhBlocks.AIRSHIP_BRONZE_ENCASED_COGWHEEL.get(),
                ZmhBlocks.AIRSHIP_BRONZE_ENCASED_LARGE_COGWHEEL.get(),
                ZmhBlocks.AIRSHIP_BRONZE_ENCASED_FLUID_PIPE.get()
        );

        ItemStack wrench = AllItems.WRENCH.asStack();
        useItem(helper, player, fixture.shaft(), wrench, true);
        useItem(helper, player, fixture.smallCog(), wrench, true);
        useItem(helper, player, fixture.largeCog(), wrench, true);
        useItem(helper, player, fixture.pipe(), wrench, false);

        assertBlock(helper, fixture.shaft(), AllBlocks.SHAFT.get(), "shaft casing removal");
        assertBlock(helper, fixture.smallCog(), AllBlocks.COGWHEEL.get(), "small cog casing removal");
        assertBlock(helper, fixture.largeCog(), AllBlocks.LARGE_COGWHEEL.get(), "large cog casing removal");
        assertBlock(helper, fixture.pipe(), AllBlocks.FLUID_PIPE.get(), "pipe casing removal");
        assertPipeAxis(helper, fixture.pipe());
        helper.succeed();
    }

    private static CasingFixture placeCreateComponents(GameTestHelper helper) {
        CasingFixture fixture = new CasingFixture(
                new BlockPos(2, 2, 2),
                new BlockPos(4, 2, 2),
                new BlockPos(6, 2, 2),
                new BlockPos(8, 2, 2)
        );
        helper.setBlock(
                fixture.shaft(),
                AllBlocks.SHAFT.getDefaultState().setValue(ShaftBlock.AXIS, Direction.Axis.X)
        );
        helper.setBlock(
                fixture.smallCog(),
                AllBlocks.COGWHEEL.getDefaultState().setValue(CogWheelBlock.AXIS, Direction.Axis.Y)
        );
        helper.setBlock(
                fixture.largeCog(),
                AllBlocks.LARGE_COGWHEEL.getDefaultState().setValue(CogWheelBlock.AXIS, Direction.Axis.Z)
        );
        helper.setBlock(fixture.pipe(), AllBlocks.FLUID_PIPE.get().getAxisState(Direction.Axis.X));
        return fixture;
    }

    private static void assertEncasedFamily(
            GameTestHelper helper,
            CasingFixture fixture,
            Block shaft,
            Block smallCog,
            Block largeCog,
            Block pipe
    ) {
        assertBlock(helper, fixture.shaft(), shaft, "shaft encasing");
        assertBlock(helper, fixture.smallCog(), smallCog, "small cog encasing");
        assertBlock(helper, fixture.largeCog(), largeCog, "large cog encasing");
        assertBlock(helper, fixture.pipe(), pipe, "pipe encasing");

        BlockState shaftState = helper.getBlockState(fixture.shaft());
        if (shaftState.getValue(ShaftBlock.AXIS) != Direction.Axis.X) {
            helper.fail("Encased shaft did not preserve its X axis");
            return;
        }
        BlockState smallCogState = helper.getBlockState(fixture.smallCog());
        if (smallCogState.getValue(EncasedCogwheelBlock.AXIS) != Direction.Axis.Y) {
            helper.fail("Encased small cogwheel did not preserve its Y axis");
            return;
        }
        BlockState largeCogState = helper.getBlockState(fixture.largeCog());
        if (largeCogState.getValue(EncasedCogwheelBlock.AXIS) != Direction.Axis.Z) {
            helper.fail("Encased large cogwheel did not preserve its Z axis");
            return;
        }
        assertPipeAxis(helper, fixture.pipe());

        assertBlockEntityType(helper, fixture.shaft(), ZmhBlockEntityTypes.AIRSHIP_ENCASED_SHAFT.get());
        assertBlockEntityType(helper, fixture.smallCog(), ZmhBlockEntityTypes.AIRSHIP_ENCASED_COGWHEEL.get());
        assertBlockEntityType(helper, fixture.largeCog(), ZmhBlockEntityTypes.AIRSHIP_ENCASED_LARGE_COGWHEEL.get());
        assertBlockEntityType(helper, fixture.pipe(), ZmhBlockEntityTypes.AIRSHIP_ENCASED_FLUID_PIPE.get());
    }

    private static void assertPipeAxis(GameTestHelper helper, BlockPos pipePos) {
        BlockState pipeState = helper.getBlockState(pipePos);
        for (Direction direction : Direction.values()) {
            boolean expected = direction.getAxis() == Direction.Axis.X;
            boolean actual = pipeState.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(direction));
            if (actual != expected) {
                helper.fail("Encased pipe connectivity changed on " + direction + ": expected " + expected);
                return;
            }
        }
    }

    private static void assertBlockEntityType(
            GameTestHelper helper,
            BlockPos relativePos,
            BlockEntityType<?> expected
    ) {
        var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(relativePos));
        if (blockEntity == null || blockEntity.getType() != expected) {
            helper.fail("Wrong BlockEntityType at " + relativePos + ": expected " + expected
                    + ", got " + (blockEntity == null ? "null" : blockEntity.getType()));
        }
    }

    private static void assertBlock(
            GameTestHelper helper,
            BlockPos relativePos,
            Block expected,
            String operation
    ) {
        Block actual = helper.getBlockState(relativePos).getBlock();
        if (actual != expected) {
            helper.fail(operation + " produced " + actual + " instead of " + expected);
        }
    }

    private static void useItem(
            GameTestHelper helper,
            Player player,
            BlockPos relativePos,
            ItemStack stack,
            boolean sneaking
    ) {
        player.setShiftKeyDown(sneaking);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.useBlock(
                relativePos,
                player,
                new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.UP, absolutePos, false)
        );
        player.setShiftKeyDown(false);
    }

    private record CasingFixture(
            BlockPos shaft,
            BlockPos smallCog,
            BlockPos largeCog,
            BlockPos pipe
    ) {
    }
}
