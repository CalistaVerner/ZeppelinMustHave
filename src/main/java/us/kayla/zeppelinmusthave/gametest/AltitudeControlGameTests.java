package us.kayla.zeppelinmusthave.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.control.AltitudeControlMath;
import us.kayla.zeppelinmusthave.content.control.AltitudeControlProfile;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AltitudeControlGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private AltitudeControlGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void telemetryModesScaleToAnalogRange(GameTestHelper helper) {
        assertEqual(helper, "minimum altitude", 0,
                AltitudeControlMath.altitudeSignal(-64.0D, -64, 320));
        assertEqual(helper, "mid altitude", 8,
                AltitudeControlMath.altitudeSignal(128.0D, -64, 320));
        assertEqual(helper, "maximum altitude", 15,
                AltitudeControlMath.altitudeSignal(320.0D, -64, 320));

        assertEqual(helper, "maximum descent", 0,
                AltitudeControlMath.verticalSpeedSignal(-6.0D, 6.0D));
        assertEqual(helper, "neutral vertical speed", 8,
                AltitudeControlMath.verticalSpeedSignal(0.0D, 6.0D));
        assertEqual(helper, "maximum climb", 15,
                AltitudeControlMath.verticalSpeedSignal(6.0D, 6.0D));

        assertEqual(helper, "half balloon fill", 8,
                AltitudeControlMath.balloonFillSignal(0.5D));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void altitudeHoldCorrectsAroundTrimSignal(GameTestHelper helper) {
        AltitudeControlProfile profile = testProfile();

        int belowTarget = AltitudeControlMath.holdSignal(
                8, 100.0D, 95.0D, 0.0D, profile
        );
        int aboveTarget = AltitudeControlMath.holdSignal(
                8, 100.0D, 105.0D, 0.0D, profile
        );
        int insideDeadband = AltitudeControlMath.holdSignal(
                8, 100.0D, 100.2D, 0.0D, profile
        );

        if (belowTarget <= 8) {
            helper.fail("Controller did not increase burner signal below target: " + belowTarget);
            return;
        }
        if (aboveTarget >= 8) {
            helper.fail("Controller did not reduce burner signal above target: " + aboveTarget);
            return;
        }
        assertEqual(helper, "deadband trim pass-through", 8, insideDeadband);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void verticalDampingOpposesMotion(GameTestHelper helper) {
        AltitudeControlProfile profile = testProfile();

        int descending = AltitudeControlMath.holdSignal(
                8, 100.0D, 100.0D, -2.0D, profile
        );
        int ascending = AltitudeControlMath.holdSignal(
                8, 100.0D, 100.0D, 2.0D, profile
        );

        if (descending <= 8) {
            helper.fail("Descending airship did not receive positive damping: " + descending);
            return;
        }
        if (ascending >= 8) {
            helper.fail("Ascending airship did not receive negative damping: " + ascending);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void signalSlewPreventsAbruptThrottleChanges(GameTestHelper helper) {
        assertEqual(helper, "upward slew", 7,
                AltitudeControlMath.slew(5, 15, 2));
        assertEqual(helper, "downward slew", 8,
                AltitudeControlMath.slew(10, 0, 2));
        assertEqual(helper, "settled slew", 9,
                AltitudeControlMath.slew(9, 9, 2));
        helper.succeed();
    }

    private static AltitudeControlProfile testProfile() {
        return new AltitudeControlProfile(
                ZeppelinMustHave.id("gametest"),
                2,
                6.0D,
                0.35D,
                1.25D,
                2.0D,
                7.0D,
                2
        );
    }

    private static void assertEqual(
            GameTestHelper helper,
            String label,
            int expected,
            int actual
    ) {
        if (expected != actual) {
            helper.fail(label + ": expected " + expected + ", got " + actual);
        }
    }
}
