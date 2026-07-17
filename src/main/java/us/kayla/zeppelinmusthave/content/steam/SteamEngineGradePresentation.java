package us.kayla.zeppelinmusthave.content.steam;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.data.ZmhLang;

import java.util.List;

final class SteamEngineGradePresentation {
    private SteamEngineGradePresentation() {
    }

    static boolean addToGoggleTooltip(
            BlockState state,
            SteamEngineGradeProfile profile,
            List<Component> tooltip,
            boolean isPlayerSneaking,
            boolean upstreamHandled
    ) {
        if (upstreamHandled) {
            tooltip.add(CommonComponents.EMPTY);
        }

        ZmhLang.translate(
                        "goggles.steam_engine_grade.grade",
                        Component.translatable(state.getBlock().getDescriptionId())
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        ZmhLang.translate(
                        "goggles.steam_engine_grade.capacity",
                        Component.literal(Integer.toString((int) Math.round(profile.stressCapacity())))
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        ZmhLang.translate(
                        "goggles.steam_engine_grade.load",
                        Component.literal(Integer.toString(profile.boilerLoadUnits()))
                                .withStyle(ChatFormatting.YELLOW)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        ZmhLang.translate(
                        "goggles.steam_engine_grade.cylinders",
                        Component.literal(Integer.toString(profile.cylinderCount()))
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        if (isPlayerSneaking) {
            ZmhLang.translate(
                            "goggles.steam_engine_grade.profile_id",
                            Component.literal(profile.id().toString())
                                    .withStyle(ChatFormatting.DARK_AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
        }
        return true;
    }
}
