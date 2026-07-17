package us.kayla.zeppelinmusthave.content.boiler;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.data.ZmhLang;

import java.util.List;
import java.util.Locale;

public final class BoilerGradeBlockEntity extends SmartBlockEntity
        implements IHaveGoggleInformation {
    private static final int SAMPLE_INTERVAL_TICKS = 5;

    private BoilerGradeProfile activeProfile;
    private long observedProfileRevision = Long.MIN_VALUE;
    private int ticksUntilSample;
    private float sourceHeat = BoilerHeater.NO_HEAT;
    private int transferredHeat = BoilerHeater.NO_HEAT;

    public BoilerGradeBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
        this.activeProfile = BoilerGradeProfile.unresolved(profileIdFor(state));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void initialize() {
        super.initialize();
        this.ticksUntilSample = 0;
        this.refreshProfile(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        this.refreshProfile(false);
        if (this.ticksUntilSample > 0) {
            this.ticksUntilSample--;
            return;
        }
        this.ticksUntilSample = SAMPLE_INTERVAL_TICKS - 1;
        this.sampleHeat(false);
    }

    public void requestImmediateSample() {
        this.ticksUntilSample = 0;
    }

    public int getComparatorSignal() {
        if (this.transferredHeat <= 0) {
            return 0;
        }
        return Math.clamp(
                (int) Math.ceil(15.0D * this.transferredHeat / this.activeProfile.maximumHeatOutput()),
                1,
                15
        );
    }

    public void sendStatusTo(Player player) {
        Component source = sourceDescription(this.sourceHeat);
        Component output = this.transferredHeat < 0
                ? Component.translatable("message.zeppelin_must_have.boiler_grade.no_output")
                : Component.literal(Integer.toString(this.transferredHeat));

        player.displayClientMessage(
                Component.translatable(
                        "message.zeppelin_must_have.boiler_grade.status",
                        Component.translatable(this.getBlockState().getBlock().getDescriptionId()),
                        source,
                        output,
                        this.activeProfile.maximumHeatOutput()
                ).withStyle(this.transferredHeat > 0
                        ? ChatFormatting.GOLD
                        : ChatFormatting.GRAY),
                false
        );
    }

    @Override
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        ZmhLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip, 1);

        ZmhLang.translate(
                        "goggles.boiler_grade.source",
                        sourceDescription(this.sourceHeat)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        Component output = this.transferredHeat < 0
                ? ZmhLang.translate("goggles.boiler_grade.no_output")
                        .style(ChatFormatting.DARK_GRAY)
                        .component()
                : Component.literal(Integer.toString(this.transferredHeat))
                        .withStyle(this.transferredHeat > 0
                                ? ChatFormatting.GOLD
                                : ChatFormatting.GRAY);
        ZmhLang.translate(
                        "goggles.boiler_grade.output",
                        output,
                        Component.literal(Integer.toString(this.activeProfile.maximumHeatOutput()))
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        if (isPlayerSneaking) {
            ZmhLang.emptyLine(tooltip);
            ZmhLang.translate("goggles.boiler_grade.profile").forGoggles(tooltip, 1);
            ZmhLang.translate(
                            "goggles.boiler_grade.multiplier",
                            Component.literal(format(this.activeProfile.heatMultiplier(), 2) + "x")
                                    .withStyle(ChatFormatting.AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
            ZmhLang.translate(
                            "goggles.boiler_grade.additive",
                            Component.literal("+" + format(this.activeProfile.additiveHeat(), 2))
                                    .withStyle(ChatFormatting.AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
            ZmhLang.translate("goggles.boiler_grade.placement")
                    .style(ChatFormatting.DARK_GRAY)
                    .forGoggles(tooltip, 2);
        }

        return true;
    }

    @Override
    public ItemStack getIcon(boolean isPlayerSneaking) {
        return new ItemStack(this.getBlockState().getBlock().asItem());
    }

    public float getSourceHeat() {
        return this.sourceHeat;
    }

    public int getTransferredHeat() {
        return this.transferredHeat;
    }

    public BoilerGradeProfile getActiveProfile() {
        return this.activeProfile;
    }

    private void sampleHeat(boolean force) {
        if (this.level == null) {
            return;
        }

        BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof BoilerGradeBlock block)) {
            return;
        }

        BlockState sourceState = this.level.getBlockState(this.worldPosition.below());
        float nextSource = sourceState.getBlock() instanceof BoilerGradeBlock
                ? BoilerHeater.NO_HEAT
                : BoilerHeater.findHeat(this.level, this.worldPosition.below(), sourceState);
        int nextTransferred = this.activeProfile.transfer(nextSource);

        boolean changed = force
                || Float.compare(this.sourceHeat, nextSource) != 0
                || this.transferredHeat != nextTransferred;
        this.sourceHeat = nextSource;
        this.transferredHeat = nextTransferred;

        boolean active = nextTransferred > 0;
        if (state.getValue(BoilerGradeBlock.ACTIVE) != active) {
            this.level.setBlock(
                    this.worldPosition,
                    state.setValue(BoilerGradeBlock.ACTIVE, active),
                    Block.UPDATE_CLIENTS
            );
        }

        if (!changed) {
            return;
        }

        BoilerGradeBlock.notifyBoilerAbove(this.level, this.worldPosition);
        this.level.updateNeighborsAt(this.worldPosition, block);
        this.setChanged();
        this.sendData();
    }

    private void refreshProfile(boolean force) {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        long revision = BoilerGradeProfiles.INSTANCE.revision();
        if (!force && revision == this.observedProfileRevision) {
            return;
        }

        BoilerGradeTier tier = this.getBlockState().getBlock() instanceof BoilerGradeBlock block
                ? block.tier()
                : BoilerGradeTier.COPPER;
        this.activeProfile = BoilerGradeProfiles.INSTANCE.resolve(tier);
        this.observedProfileRevision = revision;
        this.sampleHeat(true);
    }

    @Override
    protected void write(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        if (clientPacket) {
            tag.putFloat("BoilerSourceHeat", this.sourceHeat);
            tag.putInt("BoilerTransferredHeat", this.transferredHeat);
            CompoundTag profileTag = new CompoundTag();
            this.activeProfile.writeClientSnapshot(profileTag);
            tag.put("ResolvedBoilerGradeProfile", profileTag);
        }
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        if (clientPacket) {
            this.sourceHeat = tag.getFloat("BoilerSourceHeat");
            this.transferredHeat = tag.getInt("BoilerTransferredHeat");
            if (tag.contains("ResolvedBoilerGradeProfile")) {
                this.activeProfile = BoilerGradeProfile.readClientSnapshot(
                        tag.getCompound("ResolvedBoilerGradeProfile")
                );
            }
        }
        super.read(tag, registries, clientPacket);
    }

    private static net.minecraft.resources.ResourceLocation profileIdFor(BlockState state) {
        if (state.getBlock() instanceof BoilerGradeBlock block) {
            return block.tier().profileId();
        }
        return BoilerGradeTier.COPPER.profileId();
    }

    private static Component sourceDescription(float sourceHeat) {
        if (sourceHeat < 0.0F) {
            return Component.translatable("goggles.boiler_grade.source.none")
                    .withStyle(ChatFormatting.DARK_GRAY);
        }
        if (sourceHeat == BoilerHeater.PASSIVE_HEAT) {
            return Component.translatable("goggles.boiler_grade.source.passive")
                    .withStyle(ChatFormatting.GRAY);
        }
        return Component.translatable(
                        "goggles.boiler_grade.source.active",
                        format(sourceHeat, 1)
                )
                .withStyle(ChatFormatting.RED);
    }

    private static String format(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }
}
