package us.kayla.zeppelinmusthave.content.burner;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlockEntity;
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
import us.kayla.zeppelinmusthave.integration.BalloonHeatAggregate;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeModifiers;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSet;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSlot;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeTarget;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AirshipBurnerBlockEntity extends HotAirBurnerBlockEntity {
    private final AirshipHeatReservoir heatReservoir = new AirshipHeatReservoir();
    private final AirshipUpgradeSet upgrades = new AirshipUpgradeSet();

    private final AirshipBurnerConfiguration configuration;
    private BlockPos castPositionBridge;
    private BalloonHeatAggregate clientBalloonHeat = BalloonHeatAggregate.EMPTY;

    public AirshipBurnerBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
        this.configuration = new AirshipBurnerConfiguration(profileIdFor(state));
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
                this.configuration.profile().fuelUsePerTickAtFullPower()
                        * this.configuration.profile().throttleForSignal(this.signalStrength)
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
                this.configuration.profile().fuelCapacityTicks(),
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
                * this.configuration.profile().throttleForSignal(this.signalStrength)
                * this.configuration.profile().outputMultiplier(
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
                Vec3.upFromBottomCenterOf(pos, 1.0D + this.configuration.profile().castRange())
        );
    }

    @Override
    public BlockPos getCastPosition() {
        return this.castPositionBridge;
    }

    public int getFuelComparatorSignal() {
        return this.heatReservoir.comparatorSignal(this.configuration.profile().fuelCapacityTicks());
    }

    public void sendStatusTo(Player player) {
        AirshipBurnerPresentation.sendStatusTo(this, player);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean upstreamInformation = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return AirshipBurnerPresentation.addToGoggleTooltip(
                this,
                tooltip,
                isPlayerSneaking,
                upstreamInformation
        );
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

    AirshipUpgradeModifiers activeUpgradeModifiers() {
        return this.configuration.modifiers();
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
        return this.configuration.profile();
    }

    public int getRemainingFuelTicks() {
        return this.heatReservoir.totalTicks();
    }

    public double getFuelRatio() {
        return this.heatReservoir.fillRatio(this.configuration.profile().fuelCapacityTicks());
    }

    public AirshipHeatReservoir.Snapshot getHeatSnapshot() {
        return this.heatReservoir.snapshot(this.configuration.profile().fuelCapacityTicks());
    }

    @Override
    public void write(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        BalloonHeatAggregate network = clientPacket
                ? BalloonHeatAggregate.from(this.getBalloon())
                : BalloonHeatAggregate.EMPTY;
        AirshipBurnerStateCodec.write(
                tag,
                registries,
                clientPacket,
                this.heatReservoir,
                this.upgrades,
                this.configuration,
                network
        );
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        this.clientBalloonHeat = AirshipBurnerStateCodec.read(
                tag,
                registries,
                clientPacket,
                this.heatReservoir,
                this.upgrades,
                this.configuration,
                this.clientBalloonHeat
        );
        super.read(tag, registries, clientPacket);
    }

    AirshipBurnerMetrics captureMetrics() {
        BalloonHeatAggregate aggregate = this.level != null && !this.level.isClientSide
                ? BalloonHeatAggregate.from(this.getBalloon())
                : this.clientBalloonHeat;
        return AirshipBurnerMetrics.capture(
                this.configuration.profile(),
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

        AirshipBurnerConfiguration.RefreshResult refresh = this.configuration.refresh(
                this.getTier(),
                this.upgrades,
                forceSync
        );
        if (!refresh.evaluated()) {
            return;
        }

        boolean reservoirChanged = this.heatReservoir.clampToCapacity(
                this.configuration.profile().fuelCapacityTicks()
        );
        if (!refresh.changed() && !reservoirChanged && !forceSync) {
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
        return this.configuration.profile().gasOutputMultiplier() > 0.0D;
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

}
