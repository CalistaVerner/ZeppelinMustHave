package us.kayla.zeppelinmusthave.content.burner;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightControlActuator;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightControlChannel;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightControlNetworkManager;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightSystemStatus;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightSystemType;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeModifiers;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSet;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSlot;
import us.kayla.zeppelinmusthave.integration.BalloonHeatAggregate;

import java.util.List;
import java.util.Map;

public final class AirshipBurnerBlockEntity extends HotAirBurnerBlockEntity implements FlightControlActuator {
    private final AirshipBurnerRuntimeState runtimeState;
    private BlockPos castPositionBridge;
    private boolean flightControlOverride;
    private int flightControlSignal;

    public AirshipBurnerBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
        this.runtimeState = new AirshipBurnerRuntimeState(profileIdFor(state));
    }

    @Override
    public void initialize() {
        super.initialize();
        this.refreshProfile(true);
        AirshipBurnerWorldEffects.updateLitState(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        this.refreshProfile(false);
        this.reportFlightSystemStatus();
        if (this.runtimeState.isInfinite()
                || !this.runtimeState.hasHeat()
                || this.effectiveSignalStrength() <= 0) {
            AirshipBurnerWorldEffects.updateLitState(this);
            return;
        }

        AirshipHeatConsumptionResult consumption = this.runtimeState.consume(this.effectiveSignalStrength());
        if (!consumption.changed()) {
            return;
        }

        this.setChanged();
        if (consumption.depleted()) {
            this.removeFromBalloon();
            AirshipBurnerWorldEffects.playDepleted(this);
            AirshipBurnerWorldEffects.publish(this, true);
            return;
        }

        if (consumption.gradeChanged() || this.level.getGameTime() % 20L == 0L) {
            AirshipBurnerWorldEffects.publish(this, true);
        }
    }


    public boolean tryInsertFuel(ItemStack stack, boolean simulate) {
        AirshipBurnerRuntimeState.FuelInsertion insertion = this.runtimeState.tryInsertFuel(
                stack,
                simulate
        );
        if (!insertion.accepted()) {
            return false;
        }
        if (!simulate) {
            this.onHeatInserted(insertion.source());
        }
        return true;
    }

    private void onHeatInserted(AirshipHeatSource source) {
        if (this.level == null) {
            return;
        }

        AirshipBurnerWorldEffects.playFuelInserted(this, source);
        if (!this.level.isClientSide && !this.isVirtual() && this.canOutputGas()) {
            this.tickBalloonLogic();
        }
        AirshipBurnerWorldEffects.publish(this, true);
    }

    @Override
    public double getGasOutput() {
        if (!this.canOutputGas()) {
            return 0.0D;
        }

        double linearThrottle = this.effectiveSignalStrength() / 15.0D;
        if (linearThrottle <= 0.0D) {
            return 0.0D;
        }

        AirshipBurnerProfile profile = this.runtimeState.profile();
        double aeronauticsSelectedOutput = super.getGasOutput() / linearThrottle;
        return aeronauticsSelectedOutput
                * profile.throttleForSignal(this.effectiveSignalStrength())
                * profile.outputMultiplier(this.runtimeState.activeGrade().isSuperheated());
    }

    @Override
    public boolean canOutputGas() {
        boolean profileAvailable = this.isVirtual() || this.runtimeState.profileAllowsOperation();
        return this.effectiveSignalStrength() > 0
                && this.runtimeState.hasHeat()
                && profileAvailable
                && !this.isRemoved();
    }

    @Override
    public void doRaycast() {
        BlockPos pos = this.getBlockPos();
        this.castPositionBridge = this.getRaycastedPosition(
                this.level,
                Vec3.upFromBottomCenterOf(pos, 1.0D),
                Vec3.upFromBottomCenterOf(pos, 1.0D + this.runtimeState.profile().castRange())
        );
    }

    @Override
    public BlockPos getCastPosition() {
        return this.castPositionBridge;
    }

    public int getFuelComparatorSignal() {
        return this.runtimeState.comparatorSignal();
    }

    public void sendStatusTo(Player player) {
        AirshipBurnerPresentation.sendStatusTo(this, player);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return AirshipBurnerPresentation.addToGoggleTooltip(
                this,
                tooltip,
                isPlayerSneaking,
                super.addToGoggleTooltip(tooltip, isPlayerSneaking)
        );
    }

    public AirshipUpgradeSet.InstallResult tryInstallUpgrade(ItemStack stack, boolean simulate) {
        AirshipUpgradeSet.InstallResult result = this.runtimeState.tryInstallUpgrade(stack, simulate);
        if (result.installed() && !simulate) {
            this.refreshProfile(true);
        }
        return result;
    }

    public ItemStack removeLastUpgrade() {
        ItemStack removed = this.runtimeState.removeLastUpgrade();
        if (!removed.isEmpty()) {
            this.refreshProfile(true);
        }
        return removed;
    }

    public List<ItemStack> extractAllUpgrades() {
        return this.runtimeState.extractAllUpgrades();
    }

    public Map<AirshipUpgradeSlot, ItemStack> getInstalledUpgradeSlots() {
        return this.runtimeState.installedUpgradeSlots();
    }

    AirshipUpgradeModifiers activeUpgradeModifiers() {
        return this.runtimeState.modifiers();
    }

    @Override
    public ItemStack getIcon(boolean isPlayerSneaking) {
        return new ItemStack(this.getBlockState().getBlock().asItem());
    }

    public void configurePonderPreview(int signal, boolean superheated) {
        this.markVirtual();
        this.setSignalStrength(Math.clamp(signal, 0, 15));
        this.powered = signal > 0;
        this.runtimeState.configureInfinitePreview(
                superheated ? AirshipHeatGrade.SUPERHEATED : AirshipHeatGrade.REGULAR
        );
    }

    public AirshipBurnerProfile getActiveProfile() {
        return this.runtimeState.profile();
    }

    public int getRemainingFuelTicks() {
        return this.runtimeState.totalFuelTicks();
    }

    public double getFuelRatio() {
        return this.runtimeState.fuelRatio();
    }

    public AirshipHeatSnapshot getHeatSnapshot() {
        return this.runtimeState.heatSnapshot();
    }

    @Override
    public void applyFlightControl(FlightControlChannel channel, int value, boolean emergencyLatched) {
        if (channel != FlightControlChannel.LIFT) return;
        int nextSignal = emergencyLatched ? 0 : Math.clamp(value, 0, 15);
        boolean changed = !this.flightControlOverride || this.flightControlSignal != nextSignal;
        this.flightControlOverride = true;
        this.flightControlSignal = nextSignal;
        this.signalStrength = nextSignal;
        this.powered = nextSignal > 0;
        if (changed) {
            this.setChanged();
            this.sendData();
            if (this.level != null && !this.level.isClientSide && !this.isVirtual()) this.tickBalloonLogic();
        }
    }

    @Override
    public void clearFlightControl(FlightControlChannel channel) {
        if (channel != FlightControlChannel.LIFT || !this.flightControlOverride) return;
        this.flightControlOverride = false;
        this.flightControlSignal = 0;
        if (this.level != null) this.updateSignal();
        this.setChanged();
        this.sendData();
    }

    @Override
    public void updateSignal() {
        if (this.flightControlOverride) {
            this.signalStrength = this.flightControlSignal;
            this.powered = this.flightControlSignal > 0;
            AirshipBurnerWorldEffects.updateLitState(this);
            return;
        }
        super.updateSignal();
        AirshipBurnerWorldEffects.updateLitState(this);
    }

    private int effectiveSignalStrength() {
        if (this.level != null && !this.level.isClientSide
                && FlightControlNetworkManager.isEmergencyLatched(this.level, this.worldPosition)) {
            return 0;
        }
        return this.flightControlOverride ? this.flightControlSignal : this.signalStrength;
    }

    private void reportFlightSystemStatus() {
        if (this.level == null || this.level.isClientSide) return;
        FlightControlNetworkManager.reportSystem(
                this.level,
                this.worldPosition,
                new FlightSystemStatus(
                        FlightSystemType.BURNER,
                        this.canOutputGas(),
                        this.effectiveSignalStrength(),
                        this.getGasOutput(),
                        this.getFuelRatio()
                )
        );
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
        this.runtimeState.write(tag, registries, clientPacket, network);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        this.runtimeState.read(tag, registries, clientPacket);
        super.read(tag, registries, clientPacket);
    }

    AirshipBurnerMetrics captureMetrics() {
        BalloonHeatAggregate aggregate = this.level != null && !this.level.isClientSide
                ? BalloonHeatAggregate.from(this.getBalloon())
                : this.runtimeState.clientBalloonHeat();
        return this.runtimeState.captureMetrics(
                this.effectiveSignalStrength(),
                this.getGasOutput(),
                aggregate
        );
    }

    private void refreshProfile(boolean forceSync) {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        AirshipBurnerRuntimeState.RefreshResult refresh = this.runtimeState.refresh(
                this.getTier(),
                forceSync
        );
        if (!refresh.evaluated() || !refresh.changed()) {
            return;
        }

        if (!this.canOutputGas()) {
            this.removeFromBalloon();
        } else if (!this.isVirtual()) {
            this.tickBalloonLogic();
        }
        AirshipBurnerWorldEffects.publish(this, true);
    }

    private AirshipBurnerTier getTier() {
        return this.getBlockState().getBlock() instanceof AirshipBurnerBlock burner
                ? burner.tier()
                : AirshipBurnerTier.STANDARD;
    }

    private static net.minecraft.resources.ResourceLocation profileIdFor(BlockState state) {
        return state.getBlock() instanceof AirshipBurnerBlock burner
                ? burner.tier().profileId()
                : AirshipBurnerTier.STANDARD.profileId();
    }
}
