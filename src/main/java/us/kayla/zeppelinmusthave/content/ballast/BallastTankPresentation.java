package us.kayla.zeppelinmusthave.content.ballast;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.data.ZmhLang;

import java.util.List;
import java.util.Locale;

final class BallastTankPresentation {
    private BallastTankPresentation() {
    }

    static void sendStatusTo(BallastTankStorage storage, Player player) {
        player.displayClientMessage(
                Component.translatable(
                        "message.zeppelin_must_have.ballast_tank.status",
                        storage.tank().getFluidAmount(),
                        storage.tank().getCapacity(),
                        decimal(storage.ballastMassKg(), 1)
                ).withStyle(ChatFormatting.AQUA),
                false
        );
    }

    static boolean addToGoggleTooltip(
            BlockState state,
            BallastTankStorage storage,
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        ZmhLang.blockName(state).text(":").forGoggles(tooltip, 1);
        ZmhLang.translate(
                        "goggles.ballast_tank.fluid",
                        Component.literal(
                                storage.tank().getFluidAmount()
                                        + " / "
                                        + storage.tank().getCapacity()
                                        + " mB"
                        ).withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.ballast_tank.mass",
                        Component.literal(decimal(storage.ballastMassKg(), 1) + " kg")
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.ballast_tank.fill",
                        Component.literal(decimal(storage.fillRatio() * 100.0D, 1) + "%")
                                .withStyle(ChatFormatting.GREEN)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        if (isPlayerSneaking) {
            ZmhLang.translate(
                            "goggles.ballast_tank.profile",
                            Component.literal(storage.profile().id().toString())
                                    .withStyle(ChatFormatting.DARK_AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
        }
        return true;
    }

    private static String decimal(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }
}
