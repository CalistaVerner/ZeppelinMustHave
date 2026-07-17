package us.kayla.zeppelinmusthave.content.control;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import us.kayla.zeppelinmusthave.content.helm.AirshipFlightSnapshot;
import us.kayla.zeppelinmusthave.data.ZmhLang;

import java.util.List;
import java.util.Locale;

/** Player messages and Engineer's Goggles output for the altitude gauge. */
final class AltitudeGaugePresentation {
    private AltitudeGaugePresentation() {
    }

    static void notifyMode(AltitudeGaugeBlockEntity gauge, Player player) {
        if (player == null) {
            return;
        }
        player.displayClientMessage(
                Component.translatable(
                        "message.zeppelin_must_have.altitude_gauge.mode",
                        Component.translatable(gauge.getMode().translationKey())
                ).withStyle(ChatFormatting.GOLD),
                true
        );
    }

    static void notifyDetached(Player player, boolean overlay) {
        player.displayClientMessage(
                Component.translatable("message.zeppelin_must_have.altitude_gauge.detached")
                        .withStyle(ChatFormatting.RED),
                overlay
        );
    }

    static void notifyHoldState(AltitudeGaugeBlockEntity gauge, Player player) {
        player.displayClientMessage(
                gauge.isAltitudeHoldEnabled()
                        ? Component.translatable(
                                "message.zeppelin_must_have.altitude_gauge.hold_enabled",
                                decimal(gauge.getTargetAltitude(), 1)
                        ).withStyle(ChatFormatting.AQUA)
                        : Component.translatable(
                                "message.zeppelin_must_have.altitude_gauge.hold_disabled"
                        ).withStyle(ChatFormatting.GRAY),
                true
        );
    }

    static void sendStatusTo(AltitudeGaugeBlockEntity gauge, Player player) {
        AirshipFlightSnapshot snapshot = gauge.snapshot();
        if (!snapshot.attached()) {
            notifyDetached(player, false);
            return;
        }

        player.displayClientMessage(
                Component.translatable(
                        "message.zeppelin_must_have.altitude_gauge.status",
                        Component.translatable(gauge.getMode().translationKey()),
                        decimal(snapshot.worldY(), 1),
                        signedDecimal(snapshot.velocityY(), 2),
                        gauge.getTrimInput(),
                        gauge.getOutputSignal()
                ).withStyle(ChatFormatting.GOLD),
                false
        );

        if (gauge.getMode() == AltitudeGaugeMode.ALTITUDE_HOLD) {
            player.displayClientMessage(
                    Component.translatable(
                            gauge.isAltitudeHoldEnabled()
                                    ? "message.zeppelin_must_have.altitude_gauge.hold_status"
                                    : "message.zeppelin_must_have.altitude_gauge.hold_standby",
                            decimal(gauge.getTargetAltitude(), 1),
                            signedDecimal(gauge.getTargetAltitude() - snapshot.worldY(), 1)
                    ).withStyle(gauge.isAltitudeHoldEnabled()
                            ? ChatFormatting.AQUA
                            : ChatFormatting.GRAY),
                    false
            );
        }
    }

    static boolean addToGoggleTooltip(
            AltitudeGaugeBlockEntity gauge,
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        AirshipFlightSnapshot snapshot = gauge.snapshot();
        ZmhLang.blockName(gauge.getBlockState()).text(":").forGoggles(tooltip, 1);

        ZmhLang.translate(
                        "goggles.altitude_gauge.mode",
                        ZmhLang.translate(gauge.getMode().translationKey())
                                .style(ChatFormatting.GOLD)
                                .component()
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        if (!snapshot.attached()) {
            ZmhLang.translate("goggles.altitude_gauge.detached")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 2);
            return true;
        }

        ZmhLang.translate(
                        "goggles.altitude_gauge.altitude",
                        Component.literal(decimal(snapshot.worldY(), 1) + " m")
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.altitude_gauge.vertical_speed",
                        Component.literal(signedDecimal(snapshot.velocityY(), 2) + " m/s")
                                .withStyle(snapshot.velocityY() >= 0.0D
                                        ? ChatFormatting.GREEN
                                        : ChatFormatting.RED)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.altitude_gauge.signal",
                        Component.literal(Integer.toString(gauge.getTrimInput()))
                                .withStyle(ChatFormatting.DARK_RED),
                        Component.literal(Integer.toString(gauge.getOutputSignal()))
                                .withStyle(ChatFormatting.RED)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        if (gauge.getMode() == AltitudeGaugeMode.BALLOON_FILL) {
            ZmhLang.translate(
                            "goggles.altitude_gauge.balloon_fill",
                            Component.literal(decimal(snapshot.balloonFillRatio() * 100.0D, 1) + "%")
                                    .withStyle(ChatFormatting.AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
        }

        if (gauge.getMode() == AltitudeGaugeMode.ALTITUDE_HOLD) {
            ZmhLang.emptyLine(tooltip);
            ZmhLang.translate("goggles.altitude_gauge.hold").forGoggles(tooltip, 1);
            ZmhLang.translate(
                            "goggles.altitude_gauge.hold_state",
                            ZmhLang.translate(gauge.isAltitudeHoldEnabled()
                                            ? "goggles.altitude_gauge.hold_enabled"
                                            : "goggles.altitude_gauge.hold_disabled")
                                    .style(gauge.isAltitudeHoldEnabled()
                                            ? ChatFormatting.GREEN
                                            : ChatFormatting.GRAY)
                                    .component()
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
            ZmhLang.translate(
                            "goggles.altitude_gauge.target",
                            Component.literal(decimal(gauge.getTargetAltitude(), 1) + " m")
                                    .withStyle(ChatFormatting.AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
            ZmhLang.translate(
                            "goggles.altitude_gauge.error",
                            Component.literal(signedDecimal(
                                    gauge.getTargetAltitude() - snapshot.worldY(),
                                    1
                            ) + " m").withStyle(ChatFormatting.GOLD)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
        }

        if (isPlayerSneaking) {
            ZmhLang.emptyLine(tooltip);
            ZmhLang.translate("goggles.altitude_gauge.controls").forGoggles(tooltip, 1);
            ZmhLang.translate("goggles.altitude_gauge.controls.wrench")
                    .style(ChatFormatting.DARK_GRAY)
                    .forGoggles(tooltip, 2);
            ZmhLang.translate("goggles.altitude_gauge.controls.capture")
                    .style(ChatFormatting.DARK_GRAY)
                    .forGoggles(tooltip, 2);
        }
        return true;
    }

    private static String decimal(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }

    private static String signedDecimal(double value, int precision) {
        return String.format(Locale.ROOT, "%+." + precision + "f", value);
    }
}
