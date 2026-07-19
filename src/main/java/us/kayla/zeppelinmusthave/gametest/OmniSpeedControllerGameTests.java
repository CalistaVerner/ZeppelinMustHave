package us.kayla.zeppelinmusthave.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.kinetics.OmniSpeedControllerBlock;
import us.kayla.zeppelinmusthave.content.kinetics.OmniSpeedControllerBlockEntity;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OmniSpeedControllerGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private OmniSpeedControllerGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void exposesShaftsOnEveryFace(GameTestHelper helper) {
        OmniSpeedControllerBlock block = ZmhBlocks.OMNI_SPEED_CONTROLLER.get();
        BlockPos pos = new BlockPos(2, 2, 2);
        for (Direction direction : Direction.values()) {
            if (!block.hasShaftTowards(helper.getLevel(), helper.absolutePos(pos),
                    block.defaultBlockState(), direction)) {
                helper.fail("Omni Speed Controller is missing shaft face " + direction);
                return;
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void computesAbsoluteOutputRatios(GameTestHelper helper) {
        assertFloat(helper, "double speed", 2.0F,
                OmniSpeedControllerBlockEntity.calculateOutputModifier(16.0F, 32));
        assertFloat(helper, "reverse output", -4.0F,
                OmniSpeedControllerBlockEntity.calculateOutputModifier(16.0F, -64));
        assertFloat(helper, "preserve signed target", -2.0F,
                OmniSpeedControllerBlockEntity.calculateOutputModifier(-16.0F, 32));
        assertFloat(helper, "zero input", 0.0F,
                OmniSpeedControllerBlockEntity.calculateOutputModifier(0.0F, 64));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void createsConfiguredBlockEntity(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, ZmhBlocks.OMNI_SPEED_CONTROLLER.get());
        helper.runAfterDelay(2, () -> {
            OmniSpeedControllerBlockEntity controller = helper.getBlockEntity(pos);
            if (controller.getTargetSpeed() != OmniSpeedControllerBlockEntity.DEFAULT_SPEED) {
                helper.fail("Unexpected default Omni target speed: " + controller.getTargetSpeed());
                return;
            }
            helper.succeed();
        });
    }


    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 60)
    public static void drivesNativeShaftAtConfiguredSpeed(GameTestHelper helper) {
        BlockPos motorPos = new BlockPos(1, 2, 2);
        BlockPos controllerPos = new BlockPos(2, 2, 2);
        BlockPos shaftPos = new BlockPos(3, 2, 2);

        helper.setBlock(
                motorPos,
                AllBlocks.CREATIVE_MOTOR.getDefaultState()
                        .setValue(CreativeMotorBlock.FACING, Direction.EAST)
        );
        helper.setBlock(controllerPos, ZmhBlocks.OMNI_SPEED_CONTROLLER.get());
        helper.setBlock(
                shaftPos,
                AllBlocks.SHAFT.getDefaultState()
                        .setValue(ShaftBlock.AXIS, Direction.Axis.X)
        );

        helper.runAfterDelay(6, () -> {
            OmniSpeedControllerBlockEntity controller = helper.getBlockEntity(controllerPos);
            controller.targetSpeed.setValue(64);

            helper.runAfterDelay(10, () -> {
                KineticBlockEntity shaft = helper.getBlockEntity(shaftPos);
                float actual = Math.abs(shaft.getTheoreticalSpeed());
                if (Math.abs(actual - 64.0F) > 1.0E-4F) {
                    helper.fail("Omni controller output: expected 64 RPM, got " + actual);
                    return;
                }
                helper.succeed();
            });
        });
    }

    private static void assertFloat(GameTestHelper helper, String label, float expected, float actual) {
        if (Math.abs(expected - actual) > 1.0E-4F) {
            helper.fail(label + ": expected " + expected + ", got " + actual);
        }
    }
}
