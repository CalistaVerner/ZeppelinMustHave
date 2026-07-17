package us.kayla.zeppelinmusthave.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCatalog;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartDefinition;

public final class ZeppelinPartTooltip {
    private ZeppelinPartTooltip() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ZeppelinPartTooltip::appendTooltip);
    }

    private static void appendTooltip(ItemTooltipEvent event) {
        ZeppelinPartCatalog.find(event.getItemStack().getItem()).ifPresent(part -> {
            event.getToolTip().add(
                    Component.translatable(
                                    "zeppelin_must_have.tooltip.zeppelin_part.category",
                                    Component.translatable(part.category().translationKey())
                                            .withStyle(ChatFormatting.AQUA)
                            )
                            .withStyle(ChatFormatting.DARK_AQUA)
            );
            event.getToolTip().add(
                    Component.translatable(part.descriptionKey())
                            .withStyle(ChatFormatting.GRAY)
            );
            if (event.getFlags().isAdvanced()) {
                event.getToolTip().add(
                        Component.literal(part.id().toString())
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
            }
        });
    }
}
