package us.kayla.zeppelinmusthave.gametest;

import com.simibubi.create.api.stress.BlockStressValues;
import dev.ryanhcode.sable.api.block.propeller.BlockEntitySubLevelPropellerActor;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.ballast.BallastTankBlockEntity;
import us.kayla.zeppelinmusthave.content.ballast.BallastTankProfiles;
import us.kayla.zeppelinmusthave.content.mooring.MooringWinchBlockEntity;
import us.kayla.zeppelinmusthave.content.thruster.VerticalThrusterBlockEntity;
import us.kayla.zeppelinmusthave.content.thruster.VerticalThrusterProfiles;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AirshipServiceSystemsGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private AirshipServiceSystemsGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void ballastTankStoresWaterAndCalculatesMass(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, ZmhBlocks.BALLAST_TANK.get());

        helper.runAfterDelay(4, () -> {
            BallastTankBlockEntity tank = helper.getBlockEntity(pos);
            int accepted = tank.fluidHandler().fill(
                    new FluidStack(Fluids.WATER, 1_000),
                    IFluidHandler.FluidAction.EXECUTE
            );
            int rejected = tank.fluidHandler().fill(
                    new FluidStack(Fluids.LAVA, 1_000),
                    IFluidHandler.FluidAction.EXECUTE
            );

            assertInt(helper, "accepted water", 1_000, accepted);
            assertInt(helper, "rejected lava", 0, rejected);
            assertInt(helper, "profile capacity", 8_000, tank.tank().getCapacity());
            assertDouble(helper, "one bucket ballast mass", 1_000.0D, tank.getBallastMassKg());
            assertInt(helper, "comparator at one eighth", 2, tank.getComparatorSignal());

            tank.fluidHandler().fill(
                    new FluidStack(Fluids.WATER, 1_000),
                    IFluidHandler.FluidAction.EXECUTE
            );
            assertDouble(helper, "two bucket ballast mass", 2_000.0D, tank.getBallastMassKg());
            assertInt(helper, "comparator at one quarter", 4, tank.getComparatorSignal());
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void mooringWinchUsesNativeRopeSystem(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, ZmhBlocks.MOORING_WINCH.get());

        helper.runAfterDelay(4, () -> {
            if (!(helper.getBlockState(pos).getBlock() instanceof RopeWinchBlock)) {
                helper.fail("Mooring Winch does not extend Create Simulated RopeWinchBlock");
                return;
            }
            MooringWinchBlockEntity winch = helper.getBlockEntity(pos);
            if (winch.getRopeHolder() == null) {
                helper.fail("Mooring Winch did not initialize RopeStrandHolderBehavior");
                return;
            }
            assertDouble(helper, "stationary line speed", 0.0D, winch.getMovementSpeed());
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 30)
    public static void verticalThrusterUsesNativePropellerActor(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(
                pos,
                ZmhBlocks.VERTICAL_THRUSTER.get()
                        .defaultBlockState()
                        .setValue(dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock.FACING,
                                Direction.UP)
        );

        helper.runAfterDelay(4, () -> {
            VerticalThrusterBlockEntity thruster = helper.getBlockEntity(pos);
            if (!(thruster instanceof BlockEntitySubLevelPropellerActor)) {
                helper.fail("Vertical Thruster is not a Sable sub-level propeller actor");
                return;
            }
            if (thruster.getPropeller() != thruster) {
                helper.fail("Vertical Thruster does not expose itself as the native propeller contract");
                return;
            }
            if (!thruster.getBlockDirection().getAxis().isVertical()) {
                helper.fail("Vertical Thruster direction is not vertical");
                return;
            }
            assertDouble(helper, "default thrust scaling", 1.75D, thruster.getConfigThrust());
            assertDouble(
                    helper,
                    "Create stress impact",
                    VerticalThrusterProfiles.INSTANCE.resolveDefault().stressImpact(),
                    BlockStressValues.getImpact(ZmhBlocks.VERTICAL_THRUSTER.get())
            );
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void serviceProfilesLoadBundledDefaults(GameTestHelper helper) {
        assertInt(helper, "ballast capacity", 8_000,
                BallastTankProfiles.INSTANCE.resolveDefault().capacityMb());
        assertDouble(helper, "ballast density", 1_000.0D,
                BallastTankProfiles.INSTANCE.resolveDefault().massPerBucketKg());
        assertDouble(helper, "thruster thrust", 1.75D,
                VerticalThrusterProfiles.INSTANCE.resolveDefault().thrustScaling());
        helper.succeed();
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
