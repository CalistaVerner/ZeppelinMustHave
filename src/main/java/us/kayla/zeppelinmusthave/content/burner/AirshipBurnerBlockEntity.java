package us.kayla.zeppelinmusthave.content.burner;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.data.ZmhLang;
import us.kayla.zeppelinmusthave.integration.BalloonHeatAggregate;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeDefinitions;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeModifiers;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSet;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSlot;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeTarget;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class AirshipBurnerBlockEntity extends HotAirBurnerBlockEntity {
    private final AirshipHeatReservoir heatReservoir = new AirshipHeatReservoir();
    private final AirshipUpgradeSet upgrades = new AirshipUpgradeSet();

    private AirshipBurnerProfile baseProfile;
    private AirshipBurnerProfile activeProfile;
    private AirshipUpgradeModifiers activeUpgradeModifiers = AirshipUpgradeModifiers.IDENTITY;
    private long observedProfileRevision = Long.MIN_VALUE;
    private long observedUpgradeDefinitionRevision = Long.MIN_VALUE;
    private long observedUpgradeSetRevision = Long.MIN_VALUE;
    private BlockPos castPositionBridge;
    private BalloonHeatAggregate clientBalloonHeat = BalloonHeatAggregate.EMPTY;

    public AirshipBurnerBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
        this.baseProfile = AirshipBurnerProfile.unresolved(profileIdFor(state));
        this.activeProfile = this.baseProfile;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.refreshProfile(true);
        this.updateLitState();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        this.refreshProfile(false);
        if (this.heatReservoir.isInfinite()
                || !this.heatReservoir.hasHeat()
                || this.signalStrength <= 0) {
            this.updateLitState();
            return;
        }

        AirshipHeatReservoir.ConsumptionResult consumption = this.heatReservoir.consume(
                this.activeProfile.fuelUsePerTickAtFullPower()
                        * this.activeProfile.throttleForSignal(this.signalStrength)
        );
        if (!consumption.changed()) {
            return;
        }

        this.setChanged();
        if (consumption.depleted()) {
            this.removeFromBalloon();
            this.level.playSound(
                    null,
                    this.worldPosition,
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    0.35F,
                    0.8F
            );
            this.publishState(true);
            return;
        }

        if (consumption.gradeChanged() || this.level.getGameTime() % 20L == 0L) {
            this.publishState(true);
        }
    }

    @Override
    public void updateSignal() {
        super.updateSignal();
        this.updateLitState();
    }

    public boolean tryInsertFuel(ItemStack stack, boolean simulate) {
        Optional<AirshipHeatSource> resolved = AirshipHeatSources.resolve(stack);
        if (resolved.isEmpty() || !this.profileAllowsOperation()) {
            return false;
        }

        AirshipHeatSource source = resolved.get();
        AirshipHeatReservoir.InsertionResult result = this.heatReservoir.insert(
                source,
                this.activeProfile.fuelCapacityTicks(),
                simulate
        );
        if (!result.accepted()) {
            return false;
        }

        if (!simulate) {
            this.onHeatInserted(source);
        }
        return true;
    }

    private void onHeatInserted(AirshipHeatSource source) {
        if (this.level == null) {
            return;
        }

        boolean superheated = source.grade().isSuperheated();
        this.level.playSound(
                null,
                this.worldPosition,
                superheated ? SoundEvents.BLAZE_SHOOT : SoundEvents.FIRECHARGE_USE,
                SoundSource.BLOCKS,
                0.35F,
                superheated ? 1.15F : 0.9F
        );

        if (!this.level.isClientSide && !this.isVirtual() && this.canOutputGas()) {
            this.tickBalloonLogic();
        }
        this.publishState(true);
    }

    @Override
    public double getGasOutput() {
        if (!this.canOutputGas()) {
            return 0.0D;
        }

        double linearThrottle = this.signalStrength / 15.0D;
        if (linearThrottle <= 0.0D) {
            return 0.0D;
        }

        double aeronauticsSelectedOutput = super.getGasOutput() / linearThrottle;
        return aeronauticsSelectedOutput
                * this.activeProfile.throttleForSignal(this.signalStrength)
                * this.activeProfile.outputMultiplier(
                        this.heatReservoir.activeGrade().isSuperheated()
                );
    }

    @Override
    public boolean canOutputGas() {
        boolean profileAvailable = this.isVirtual() || this.profileAllowsOperation();
        return this.signalStrength > 0
                && this.heatReservoir.hasHeat()
                && profileAvailable
                && !this.isRemoved();
    }

    @Override
    public void doRaycast() {
        BlockPos pos = this.getBlockPos();
        this.castPositionBridge = this.getRaycastedPosition(
                this.level,
                Vec3.upFromBottomCenterOf(pos, 1.0D),
                Vec3.upFromBottomCenterOf(pos, 1.0D + this.activeProfile.castRange())
        );
    }

    @Override
    public BlockPos getCastPosition() {
        return this.castPositionBridge;
    }

    public int getFuelComparatorSignal() {
        return this.heatReservoir.comparatorSignal(this.activeProfile.fuelCapacityTicks());
    }

    public void sendStatusTo(Player player) {
        AirshipBurnerMetrics metrics = this.captureMetrics();
        AirshipHeatReservoir.Snapshot heat = metrics.reservoir();

        player.displayClientMessage(
                Component.translatable(
                                "message.zeppelin_must_have.burner.status",
                                Component.translatable(this.getBlockState().getBlock().getDescriptionId()),
                                heat.infinite()
                                        ? Component.translatable("message.zeppelin_must_have.burner.infinite")
                                        : Component.literal(formatSeconds(heat.totalTicks())),
                                Component.translatable(heat.activeGrade().statusTranslationKey()),
                                decimal(metrics.individualGasOutput(), 1),
                                metrics.profile().castRange()
                        )
                        .withStyle(this.canOutputGas() ? ChatFormatting.GOLD : ChatFormatting.GRAY),
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

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean upstreamInformation = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (!upstreamInformation) {
            ZmhLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip, 1);
        } else {
            ZmhLang.emptyLine(tooltip);
        }

        AirshipBurnerMetrics metrics = this.captureMetrics();
        AirshipHeatReservoir.Snapshot heat = metrics.reservoir();

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

        this.addHeatNetworkTooltip(tooltip, metrics.balloonHeat());

        if (isPlayerSneaking) {
            this.addReservoirDiagnostics(tooltip, metrics);
        }
        this.addUpgradeTooltip(tooltip, isPlayerSneaking);
        return true;
    }

    private void addHeatNetworkTooltip(
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

    private void addReservoirDiagnostics(
            List<Component> tooltip,
            AirshipBurnerMetrics metrics
    ) {
        AirshipHeatReservoir.Snapshot heat = metrics.reservoir();

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

    public AirshipUpgradeSet.InstallResult tryInstallUpgrade(
            ItemStack stack,
            boolean simulate
    ) {
        AirshipUpgradeSet.InstallResult result = this.upgrades.install(
                stack,
                AirshipUpgradeTarget.AIRSHIP_BURNER,
                simulate
        );
        if (result.installed() && !simulate) {
            this.refreshProfile(true);
        }
        return result;
    }

    public ItemStack removeLastUpgrade() {
        ItemStack removed = this.upgrades.removeLast();
        if (!removed.isEmpty()) {
            this.refreshProfile(true);
        }
        return removed;
    }

    public List<ItemStack> extractAllUpgrades() {
        return this.upgrades.removeAll();
    }

    public Map<AirshipUpgradeSlot, ItemStack> getInstalledUpgradeSlots() {
        return this.upgrades.slotSnapshot();
    }

    private void addUpgradeTooltip(List<Component> tooltip, boolean detailed) {
        Map<AirshipUpgradeSlot, ItemStack> installed = this.upgrades.slotSnapshot();
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

        ZmhLang.translate(
                        "goggles.upgrades.output_multiplier",
                        Component.literal(decimal(this.activeUpgradeModifiers.gasOutputMultiplier(), 2) + "x")
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.upgrades.fuel_multiplier",
                        Component.literal(decimal(this.activeUpgradeModifiers.fuelUseMultiplier(), 2) + "x")
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.upgrades.capacity_multiplier",
                        Component.literal(decimal(this.activeUpgradeModifiers.fuelCapacityMultiplier(), 2) + "x")
                                .withStyle(ChatFormatting.GREEN)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.upgrades.range_add",
                        Component.literal(String.format(Locale.ROOT, "%+d", this.activeUpgradeModifiers.castRangeAdd()))
                                .withStyle(ChatFormatting.DARK_AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
    }

    @Override
    public ItemStack getIcon(boolean isPlayerSneaking) {
        return new ItemStack(this.getBlockState().getBlock().asItem());
    }

    /**
     * Configures a virtual block entity for a Ponder scene without joining or
     * mutating an actual Aeronautics balloon.
     */
    public void configurePonderPreview(int signal, boolean superheated) {
        this.markVirtual();
        this.setSignalStrength(Math.clamp(signal, 0, 15));
        this.powered = signal > 0;
        this.heatReservoir.configureInfinitePreview(
                superheated ? AirshipHeatGrade.SUPERHEATED : AirshipHeatGrade.REGULAR
        );
    }

    public AirshipBurnerProfile getActiveProfile() {
        return this.activeProfile;
    }

    public int getRemainingFuelTicks() {
        return this.heatReservoir.totalTicks();
    }

    public double getFuelRatio() {
        return this.heatReservoir.fillRatio(this.activeProfile.fuelCapacityTicks());
    }

    public AirshipHeatReservoir.Snapshot getHeatSnapshot() {
        return this.heatReservoir.snapshot(this.activeProfile.fuelCapacityTicks());
    }

    @Override
    public void write(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        this.heatReservoir.write(tag);
        this.upgrades.write(tag, registries);

        if (clientPacket) {
            CompoundTag profileTag = new CompoundTag();
            this.activeProfile.writeClientSnapshot(profileTag);
            tag.put("ResolvedBurnerProfile", profileTag);

            CompoundTag modifierTag = new CompoundTag();
            this.activeUpgradeModifiers.write(modifierTag);
            tag.put("ResolvedUpgradeModifiers", modifierTag);

            CompoundTag networkTag = new CompoundTag();
            BalloonHeatAggregate.from(this.getBalloon()).write(networkTag);
            tag.put("BalloonHeatAggregate", networkTag);
        }

        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        this.heatReservoir.read(tag);
        this.upgrades.read(tag, registries);

        if (clientPacket && tag.contains("ResolvedBurnerProfile")) {
            this.activeProfile = AirshipBurnerProfile.readClientSnapshot(
                    tag.getCompound("ResolvedBurnerProfile")
            );
        }
        if (clientPacket && tag.contains("ResolvedUpgradeModifiers")) {
            this.activeUpgradeModifiers = AirshipUpgradeModifiers.read(
                    tag.getCompound("ResolvedUpgradeModifiers")
            );
        }
        if (clientPacket && tag.contains("BalloonHeatAggregate")) {
            this.clientBalloonHeat = BalloonHeatAggregate.read(
                    tag.getCompound("BalloonHeatAggregate")
            );
        }

        super.read(tag, registries, clientPacket);
    }

    private AirshipBurnerMetrics captureMetrics() {
        BalloonHeatAggregate aggregate = this.level != null && !this.level.isClientSide
                ? BalloonHeatAggregate.from(this.getBalloon())
                : this.clientBalloonHeat;
        return AirshipBurnerMetrics.capture(
                this.activeProfile,
                this.heatReservoir,
                this.signalStrength,
                this.getGasOutput(),
                aggregate
        );
    }

    private void refreshProfile(boolean forceSync) {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        long profileRevision = AirshipBurnerProfiles.INSTANCE.revision();
        long definitionRevision = AirshipUpgradeDefinitions.INSTANCE.revision();
        long upgradeRevision = this.upgrades.localRevision();
        if (!forceSync
                && profileRevision == this.observedProfileRevision
                && definitionRevision == this.observedUpgradeDefinitionRevision
                && upgradeRevision == this.observedUpgradeSetRevision) {
            return;
        }

        AirshipBurnerProfile nextBase = AirshipBurnerProfiles.INSTANCE.resolve(this.getTier());
        AirshipUpgradeModifiers nextModifiers = this.upgrades.modifiers(
                AirshipUpgradeTarget.AIRSHIP_BURNER
        );
        AirshipBurnerProfile nextEffective = nextModifiers.apply(nextBase);

        boolean changed = !nextEffective.equals(this.activeProfile)
                || !nextModifiers.equals(this.activeUpgradeModifiers);
        this.baseProfile = nextBase;
        this.activeProfile = nextEffective;
        this.activeUpgradeModifiers = nextModifiers;
        this.observedProfileRevision = profileRevision;
        this.observedUpgradeDefinitionRevision = definitionRevision;
        this.observedUpgradeSetRevision = upgradeRevision;

        boolean reservoirChanged = this.heatReservoir.clampToCapacity(
                nextEffective.fuelCapacityTicks()
        );
        if (!changed && !reservoirChanged && !forceSync) {
            return;
        }

        if (!this.canOutputGas()) {
            this.removeFromBalloon();
        } else if (!this.isVirtual()) {
            this.tickBalloonLogic();
        }
        this.publishState(true);
    }

    private AirshipBurnerTier getTier() {
        if (this.getBlockState().getBlock() instanceof AirshipBurnerBlock burner) {
            return burner.tier();
        }
        return AirshipBurnerTier.STANDARD;
    }

    private boolean profileAllowsOperation() {
        return this.activeProfile.gasOutputMultiplier() > 0.0D;
    }

    private void publishState(boolean synchronizeClient) {
        this.updateLitState();
        this.setChanged();
        if (synchronizeClient) {
            this.sendData();
        }
        if (this.level != null) {
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    private void updateLitState() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        BlockState state = this.getBlockState();
        if (!state.hasProperty(AirshipBurnerBlock.LIT)) {
            return;
        }

        boolean lit = this.canOutputGas();
        if (state.getValue(AirshipBurnerBlock.LIT) != lit) {
            this.level.setBlock(this.worldPosition, state.setValue(AirshipBurnerBlock.LIT, lit), 3);
        }
    }

    private static net.minecraft.resources.ResourceLocation profileIdFor(BlockState state) {
        if (state.getBlock() instanceof AirshipBurnerBlock burner) {
            return burner.tier().profileId();
        }
        return AirshipBurnerTier.STANDARD.profileId();
    }

    private static String formatSeconds(int ticks) {
        return String.format(Locale.ROOT, "%.1f s", ticks / 20.0D);
    }

    private static String decimal(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }
}
