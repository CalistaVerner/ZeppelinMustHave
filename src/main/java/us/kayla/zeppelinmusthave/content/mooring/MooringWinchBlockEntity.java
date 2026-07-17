package us.kayla.zeppelinmusthave.content.mooring;

import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.data.ZmhLang;

import java.util.List;
import java.util.Locale;

public final class MooringWinchBlockEntity extends RopeWinchBlockEntity {
    public MooringWinchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public float getClientRopeAngle(float partialTicks) {
        return this.clientAngle.getValue(partialTicks);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean upstream = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (upstream) {
            ZmhLang.emptyLine(tooltip);
        } else {
            ZmhLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip, 1);
        }

        boolean attached = this.getRopeHolder() != null && this.getRopeHolder().isAttached();
        ZmhLang.translate(
                        "goggles.mooring_winch.rope",
                        ZmhLang.translate(attached
                                        ? "goggles.mooring_winch.attached"
                                        : "goggles.mooring_winch.detached")
                                .style(attached ? ChatFormatting.GREEN : ChatFormatting.GRAY)
                                .component()
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.mooring_winch.speed",
                        Component.literal(decimal(this.getMovementSpeed(), 3) + " m/t")
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        return true;
    }

    private static String decimal(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }
}
