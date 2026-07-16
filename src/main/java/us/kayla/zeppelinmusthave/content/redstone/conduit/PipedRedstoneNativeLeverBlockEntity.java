package us.kayla.zeppelinmusthave.content.redstone.conduit;

import java.util.List;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.data.ZmhLang;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

/** Client-side animation state for the native conduit lever handle. */
public final class PipedRedstoneNativeLeverBlockEntity extends SmartBlockEntity
        implements IHaveGoggleInformation {
    private final LerpedFloat handle = LerpedFloat.linear();

    public PipedRedstoneNativeLeverBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
        this.handle.startWithValue(isPowered(state) ? 1.0F : 0.0F);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void initialize() {
        super.initialize();
        this.handle.startWithValue(isPowered(this.getBlockState()) ? 1.0F : 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || !this.level.isClientSide) {
            return;
        }

        this.handle.chase(
                isPowered(this.getBlockState()) ? 1.0F : 0.0F,
                0.28F,
                Chaser.EXP
        );
        this.handle.tickChaser();
    }

    public float getHandlePosition(float partialTicks) {
        return this.handle.getValue(partialTicks);
    }

    @Override
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        boolean powered = isPowered(this.getBlockState());
        ZmhLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip, 1);
        ZmhLang.translate(
                        "goggles.piped_redstone.native_lever.state",
                        ZmhLang.translate(powered
                                        ? "goggles.piped_redstone.native_lever.on"
                                        : "goggles.piped_redstone.native_lever.off")
                                .style(powered ? ChatFormatting.RED : ChatFormatting.GRAY)
                                .component()
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.piped_redstone.native_lever.output",
                        Component.literal(powered ? "15" : "0")
                                .withStyle(powered ? ChatFormatting.RED : ChatFormatting.DARK_GRAY)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        return true;
    }

    @Override
    public ItemStack getIcon(boolean isPlayerSneaking) {
        return ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER_ITEM.get().getDefaultInstance();
    }

    private static boolean isPowered(BlockState state) {
        return state.hasProperty(PipedRedstoneNativeLeverBlock.POWERED)
                && state.getValue(PipedRedstoneNativeLeverBlock.POWERED);
    }
}
