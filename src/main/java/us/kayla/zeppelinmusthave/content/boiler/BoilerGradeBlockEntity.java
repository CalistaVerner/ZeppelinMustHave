package us.kayla.zeppelinmusthave.content.boiler;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import us.kayla.zeppelinmusthave.data.ZmhLang;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

import java.util.List;
import java.util.Locale;

/**
 * Grade-aware variant of Create's FluidTankBlockEntity.
 *
 * <p>The inherited tank remains the single source of truth for fluid storage,
 * multiblock dimensions, boiler state, water input, attached engines, and
 * synchronization. This subclass only supplies the selected heat profile.</p>
 */
@SuppressWarnings("unchecked")
public final class BoilerGradeBlockEntity extends FluidTankBlockEntity {
    private BoilerGradeProfile activeProfile;
    private long observedProfileRevision = Long.MIN_VALUE;

    public BoilerGradeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.activeProfile = BoilerGradeProfile.unresolved(profileIdFor(state));
        this.boiler = new GradedBoilerData(() -> this.activeProfile);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerFluidCapability(event, ZmhBlockEntityTypes.COPPER_BOILER.get());
        registerFluidCapability(event, ZmhBlockEntityTypes.BRASS_BOILER.get());
        registerFluidCapability(event, ZmhBlockEntityTypes.INDUSTRIAL_BOILER.get());
    }

    private static void registerFluidCapability(
            RegisterCapabilitiesEvent event,
            BlockEntityType<BoilerGradeBlockEntity> type
    ) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                type,
                (blockEntity, context) -> blockEntity.fluidCapability()
        );
    }

    @Override
    public void initialize() {
        this.refreshProfile(true);
        super.initialize();
    }

    @Override
    public void lazyTick() {
        if (this.level != null && !this.level.isClientSide) {
            this.refreshProfile(false);
        }
        super.lazyTick();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(CommonComponents.EMPTY);

        ZmhLang.translate(
                        "goggles.boiler_grade.grade",
                        Component.translatable(this.getBlockState().getBlock().getDescriptionId())
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        ZmhLang.translate(
                        "goggles.boiler_grade.output",
                        Component.literal(Integer.toString(this.activeProfile.maximumHeatOutput()))
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        if (isPlayerSneaking) {
            ZmhLang.translate(
                            "goggles.boiler_grade.profile_id",
                            Component.literal(this.activeProfile.id().toString())
                                    .withStyle(ChatFormatting.DARK_AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
            ZmhLang.translate(
                            "goggles.boiler_grade.multiplier",
                            Component.literal(format(this.activeProfile.heatMultiplier(), 2) + "x")
                                    .withStyle(ChatFormatting.AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
            ZmhLang.translate(
                            "goggles.boiler_grade.additive",
                            Component.literal("+" + format(this.activeProfile.additiveHeat(), 2))
                                    .withStyle(ChatFormatting.AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
        }

        return true;
    }

    public BoilerGradeProfile activeProfile() {
        return this.activeProfile;
    }

    public BoilerGradeTier tier() {
        return tierFor(this.getBlockState());
    }

    public IFluidHandler fluidCapability() {
        return this.fluidCapability;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket && tag.contains("BoilerGradeProfileId")) {
            this.activeProfile = BoilerGradeProfile.readClientSnapshot(tag);
        }
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) {
            this.activeProfile.writeClientSnapshot(tag);
        }
    }

    private void refreshProfile(boolean force) {
        long revision = BoilerGradeProfiles.INSTANCE.revision();
        if (!force && this.observedProfileRevision == revision) {
            return;
        }

        BoilerGradeProfile nextProfile = BoilerGradeProfiles.INSTANCE.resolve(this.tier());
        boolean changed = !nextProfile.equals(this.activeProfile);
        this.activeProfile = nextProfile;
        this.observedProfileRevision = revision;

        if (this.level == null || this.level.isClientSide || (!force && !changed)) {
            return;
        }

        this.boiler.needsHeatLevelUpdate = true;
        this.setChanged();
        this.sendData();
    }

    private static BoilerGradeTier tierFor(BlockState state) {
        if (state.getBlock() instanceof BoilerGradeBlock block) {
            return block.tier();
        }
        throw new IllegalStateException("BoilerGradeBlockEntity is attached to " + state);
    }

    private static ResourceLocation profileIdFor(BlockState state) {
        return tierFor(state).profileId();
    }

    private static String format(double value, int decimals) {
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }
}
