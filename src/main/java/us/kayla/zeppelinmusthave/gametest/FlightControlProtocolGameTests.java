package us.kayla.zeppelinmusthave.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.control.fcn.EngineTelegraphOrder;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightControlAddress;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightControlAuthority;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightControlChannel;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FlightControlProtocolGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private FlightControlProtocolGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void signedAnalogProtocolPreservesEndpointsAndNeutral(GameTestHelper helper) {
        FlightControlChannel channel = FlightControlChannel.ENGINE_THROTTLE;
        assertInt(helper, "full astern encoding", 0, channel.toAnalogSignal(-15));
        assertInt(helper, "neutral encoding", 8, channel.toAnalogSignal(0));
        assertInt(helper, "full ahead encoding", 15, channel.toAnalogSignal(15));
        assertInt(helper, "full astern decoding", -15, channel.fromAnalogSignal(0));
        assertInt(helper, "neutral decoding", 0, channel.fromAnalogSignal(8));
        assertInt(helper, "full ahead decoding", 15, channel.fromAnalogSignal(15));

        for (int command = -15; command <= 15; command++) {
            int roundTrip = channel.fromAnalogSignal(channel.toAnalogSignal(command));
            if (Math.abs(roundTrip - command) > 1) {
                helper.fail("Bipolar command round-trip exceeded one command step: "
                        + command + " -> " + roundTrip);
                return;
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void engineTelegraphHasCanonicalSevenOrders(GameTestHelper helper) {
        int[] expected = {-15, -10, -5, 0, 5, 10, 15};
        EngineTelegraphOrder[] orders = EngineTelegraphOrder.values();
        assertInt(helper, "telegraph order count", expected.length, orders.length);
        for (int index = 0; index < expected.length; index++) {
            assertInt(helper, orders[index].name(), expected[index], orders[index].command());
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void networkAddressIsBoundedAndNormalized(GameTestHelper helper) {
        FlightControlAddress address = new FlightControlAddress("  Grand Zeppelin / Main  ", 99);
        if (!"grand-zeppelin-main".equals(address.networkName())) {
            helper.fail("Unexpected normalized network name: " + address.networkName());
            return;
        }
        assertInt(helper, "frequency clamp", 15, address.frequency());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void safetyAuthorityDominatesManualAndAutomatic(GameTestHelper helper) {
        if (!(FlightControlAuthority.SAFETY.priority() > FlightControlAuthority.MANUAL.priority()
                && FlightControlAuthority.MANUAL.priority() > FlightControlAuthority.AUTOMATIC.priority())) {
            helper.fail("FCN authority ordering is not SAFETY > MANUAL > AUTOMATIC");
            return;
        }
        helper.succeed();
    }

    private static void assertInt(GameTestHelper helper, String label, int expected, int actual) {
        if (expected != actual) {
            helper.fail(label + ": expected " + expected + ", got " + actual);
        }
    }
}
