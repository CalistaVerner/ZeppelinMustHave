package us.kayla.zeppelinmusthave.content.thruster;

import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.data.ZmhLang;

import java.util.List;
import java.util.Locale;

public final class VerticalThrusterBlockEntity extends BasePropellerBlockEntity {
    private static final VerticalThrusterProfile CONSTRUCTION_FALLBACK =
            VerticalThrusterProfile.unresolved(VerticalThrusterProfiles.DEFAULT_ID);

    private VerticalThrusterProfile activeProfile = CONSTRUCTION_FALLBACK;
    private long observedProfileRevision = Long.MIN_VALUE;

    public VerticalThrusterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.refreshProfile(true);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (this.level != null && !this.level.isClientSide) {
            this.refreshProfile(false);
        }
    }

    @Override
    public double getConfigThrust() {
        return this.profile().thrustScaling();
    }

    @Override
    public double getConfigAirflow() {
        return this.profile().airflowScaling();
    }

    @Override
    public float getRadius() {
        return this.profile().radius();
    }

    @Override
    public float getOffset() {
        return 3.0F / 16.0F;
    }

    public VerticalThrusterProfile activeProfile() {
        return this.profile();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean upstream = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (!upstream) {
            ZmhLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip, 1);
        } else {
            ZmhLang.emptyLine(tooltip);
        }

        ZmhLang.translate(
                        "goggles.vertical_thruster.direction",
                        Component.translatable("direction.minecraft." + this.getBlockDirection().getName())
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.vertical_thruster.thrust",
                        Component.literal(decimal(this.getThrust(), 2) + " pN")
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.vertical_thruster.airflow",
                        Component.literal(decimal(this.getAirflow(), 2) + " m/s")
                                .withStyle(ChatFormatting.GREEN)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        if (isPlayerSneaking) {
            ZmhLang.translate(
                            "goggles.vertical_thruster.profile",
                            Component.literal(this.activeProfile.id().toString())
                                    .withStyle(ChatFormatting.DARK_AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
        }
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (clientPacket) {
            this.activeProfile.writeClientSnapshot(tag);
        }
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (clientPacket && tag.contains("VerticalThrusterProfileId")) {
            this.activeProfile = VerticalThrusterProfile.readClientSnapshot(
                    tag,
                    VerticalThrusterProfiles.DEFAULT_ID
            );
        }
        super.read(tag, registries, clientPacket);
    }

    private VerticalThrusterProfile profile() {
        return this.activeProfile == null ? CONSTRUCTION_FALLBACK : this.activeProfile;
    }

    private void refreshProfile(boolean force) {
        long revision = VerticalThrusterProfiles.INSTANCE.revision();
        if (!force && revision == this.observedProfileRevision) {
            return;
        }
        VerticalThrusterProfile next = VerticalThrusterProfiles.INSTANCE.resolveDefault();
        boolean changed = !next.equals(this.activeProfile);
        this.activeProfile = next;
        this.observedProfileRevision = revision;
        if (changed || force) {
            this.setChanged();
            this.sendData();
        }
    }

    private static String decimal(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }
}
