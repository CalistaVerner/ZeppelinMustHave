package us.kayla.zeppelinmusthave.content.burner;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeModifiers;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSlot;
import us.kayla.zeppelinmusthave.data.ZmhLang;
import us.kayla.zeppelinmusthave.integration.BalloonHeatAggregate;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Chat and Engineer's Goggles presentation for an airship burner. */
final class AirshipBurnerPresentation {
    private AirshipBurnerPresentation() {
    }

    static void sendStatusTo(AirshipBurnerBlockEntity burner, Player player) {
        AirshipBurnerMetrics metrics = burner.captureMetrics();
        AirshipHeatSnapshot heat = metrics.reservoir();

        player.displayClientMessage(
                Component.translatable(
                                "message.zeppelin_must_have.burner.status",
                                Component.translatable(burner.getBlockState().getBlock().getDescriptionId()),
                                heat.infinite()
                                        ? Component.translatable("message.zeppelin_must_have.burner.infinite")
                                        : Component.literal(formatSeconds(heat.totalTicks())),
                                Component.translatable(heat.activeGrade().statusTranslationKey()),
                                decimal(metrics.individualGasOutput(), 1),
                                metrics.profile().castRange()
                        )
                        .withStyle(burner.canOutputGas() ? ChatFormatting.GOLD : ChatFormatting.GRAY),
                false
        );

        BalloonHeatAggregate network = metrics.balloonHeat();
        if (network.connectedSources() > 0) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.zeppelin_must_have.burner.network",
                            network.activeSources(),
                            network.connectedSources(),
                            decimal(network.combinedGasOutput(), 1)
                    ).withStyle(ChatFormatting.AQUA),
                    false
            );
        }
    }

    static boolean addToGoggleTooltip(
            AirshipBurnerBlockEntity burner,
            List<Component> tooltip,
            boolean isPlayerSneaking,
            boolean upstreamInformation
    ) {
        if (!upstreamInformation) {
            ZmhLang.blockName(burner.getBlockState()).text(":").forGoggles(tooltip, 1);
        } else {
            ZmhLang.emptyLine(tooltip);
        }

        AirshipBurnerMetrics metrics = burner.captureMetrics();
        AirshipHeatSnapshot heat = metrics.reservoir();

        ZmhLang.translate("goggles.burner.heat_reservoir").forGoggles(tooltip, 1);
        Component fuelValue = heat.infinite()
                ? ZmhLang.translate("goggles.value.infinite")
                        .style(ChatFormatting.LIGHT_PURPLE)
                        .component()
                : Component.literal(formatSeconds(heat.totalTicks()))
                        .withStyle(ChatFormatting.GOLD);
        ZmhLang.translate("goggles.burner.fuel", fuelValue)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        Component grade = ZmhLang.translate(heat.activeGrade().goggleTranslationKey())
                .style(heat.activeGrade().isSuperheated()
                        ? ChatFormatting.GOLD
                        : ChatFormatting.GRAY)
                .component();
        ZmhLang.translate("goggles.burner.grade", grade)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        ZmhLang.translate(
                        "goggles.burner.throttle",
                        Component.literal(metrics.signalStrength() + " / 15")
                                .withStyle(ChatFormatting.RED),
                        Component.literal(decimal(metrics.throttle() * 100.0D, 0) + "%")
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.burner.output",
                        Component.literal(decimal(metrics.individualGasOutput(), 2) + " m³")
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        addHeatNetworkTooltip(tooltip, metrics.balloonHeat());
        if (isPlayerSneaking) {
            addReservoirDiagnostics(tooltip, metrics);
        }
        addUpgradeTooltip(burner, tooltip, isPlayerSneaking);
        return true;
    }

    private static void addHeatNetworkTooltip(
            List<Component> tooltip,
            BalloonHeatAggregate network
    ) {
        if (network.connectedSources() <= 0) {
            return;
        }

        ZmhLang.emptyLine(tooltip);
        ZmhLang.translate("goggles.burner.heat_network").forGoggles(tooltip, 1);
        ZmhLang.translate(
                        "goggles.burner.sources",
                        Component.literal(Integer.toString(network.activeSources()))
                                .withStyle(ChatFormatting.GREEN),
                        Component.literal(Integer.toString(network.connectedSources()))
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.burner.combined_output",
                        Component.literal(decimal(network.combinedGasOutput(), 2) + " m³")
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
    }

    private static void addReservoirDiagnostics(
            List<Component> tooltip,
            AirshipBurnerMetrics metrics
    ) {
        AirshipHeatSnapshot heat = metrics.reservoir();

        ZmhLang.emptyLine(tooltip);
        ZmhLang.translate("goggles.burner.profile").forGoggles(tooltip, 1);
        ZmhLang.translate(
                        "goggles.burner.profile_id",
                        Component.literal(metrics.profile().id().toString())
                                .withStyle(ChatFormatting.DARK_AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.burner.capacity",
                        Component.literal(formatSeconds(heat.capacityTicks()))
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.burner.regular_heat",
                        Component.literal(formatSeconds(heat.regularTicks()))
                                .withStyle(ChatFormatting.GRAY)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.burner.superheated_heat",
                        Component.literal(formatSeconds(heat.superheatedTicks()))
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.burner.range",
                        Component.literal(Integer.toString(metrics.profile().castRange()))
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.burner.fuel_rate",
                        Component.literal(decimal(metrics.fuelUsePerTick(), 2))
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
    }

    private static void addUpgradeTooltip(
            AirshipBurnerBlockEntity burner,
            List<Component> tooltip,
            boolean detailed
    ) {
        Map<AirshipUpgradeSlot, ItemStack> installed = burner.getInstalledUpgradeSlots();
        if (installed.isEmpty()) {
            return;
        }

        ZmhLang.emptyLine(tooltip);
        ZmhLang.translate("goggles.upgrades.title").forGoggles(tooltip, 1);
        installed.forEach((slot, stack) -> ZmhLang.translate(
                        "goggles.upgrades.slot",
                        ZmhLang.translate(slot.translationKey()).component(),
                        stack.getHoverName()
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2));

        if (!detailed) {
            return;
        }

        AirshipUpgradeModifiers modifiers = burner.activeUpgradeModifiers();
        ZmhLang.translate(
                        "goggles.upgrades.output_multiplier",
                        Component.literal(decimal(modifiers.gasOutputMultiplier(), 2) + "x")
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.upgrades.fuel_multiplier",
                        Component.literal(decimal(modifiers.fuelUseMultiplier(), 2) + "x")
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.upgrades.capacity_multiplier",
                        Component.literal(decimal(modifiers.fuelCapacityMultiplier(), 2) + "x")
                                .withStyle(ChatFormatting.GREEN)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.upgrades.range_add",
                        Component.literal(String.format(Locale.ROOT, "%+d", modifiers.castRangeAdd()))
                                .withStyle(ChatFormatting.DARK_AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
    }

    private static String formatSeconds(int ticks) {
        return String.format(Locale.ROOT, "%.1f s", ticks / 20.0D);
    }

    private static String decimal(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }
}
