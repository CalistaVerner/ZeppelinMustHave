package us.kayla.zeppelinmusthave.content.burner;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeModifiers;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSet;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSlot;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeTarget;
import us.kayla.zeppelinmusthave.integration.BalloonHeatAggregate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Owns all mutable burner domain state independently of world side effects. */
final class AirshipBurnerRuntimeState {
    private final AirshipHeatReservoir heat = new AirshipHeatReservoir();
    private final AirshipUpgradeSet upgrades = new AirshipUpgradeSet();
    private final AirshipBurnerConfiguration configuration;
    private BalloonHeatAggregate clientBalloonHeat = BalloonHeatAggregate.EMPTY;

    AirshipBurnerRuntimeState(ResourceLocation profileId) {
        this.configuration = new AirshipBurnerConfiguration(profileId);
    }

    AirshipBurnerProfile profile() {
        return this.configuration.profile();
    }

    AirshipUpgradeModifiers modifiers() {
        return this.configuration.modifiers();
    }

    boolean profileAllowsOperation() {
        return this.profile().gasOutputMultiplier() > 0.0D;
    }

    FuelInsertion tryInsertFuel(ItemStack stack, boolean simulate) {
        Optional<AirshipHeatSource> resolved = AirshipHeatSources.resolve(stack);
        if (resolved.isEmpty() || !this.profileAllowsOperation()) {
            return FuelInsertion.REJECTED;
        }

        AirshipHeatSource source = resolved.get();
        AirshipHeatInsertionResult result = this.heat.insert(
                source,
                this.profile().fuelCapacityTicks(),
                simulate
        );
        return result.accepted()
                ? new FuelInsertion(true, source)
                : FuelInsertion.REJECTED;
    }

    AirshipHeatConsumptionResult consume(int signalStrength) {
        return this.heat.consume(
                this.profile().fuelUsePerTickAtFullPower()
                        * this.profile().throttleForSignal(signalStrength)
        );
    }

    RefreshResult refresh(
            AirshipBurnerTier tier,
            boolean forceSync
    ) {
        AirshipBurnerConfiguration.RefreshResult profileRefresh = this.configuration.refresh(
                tier,
                this.upgrades,
                forceSync
        );
        if (!profileRefresh.evaluated()) {
            return RefreshResult.NOT_EVALUATED;
        }
        boolean reservoirChanged = this.heat.clampToCapacity(this.profile().fuelCapacityTicks());
        return new RefreshResult(
                true,
                profileRefresh.changed() || reservoirChanged || forceSync
        );
    }

    AirshipUpgradeSet.InstallResult tryInstallUpgrade(ItemStack stack, boolean simulate) {
        return this.upgrades.install(
                stack,
                AirshipUpgradeTarget.AIRSHIP_BURNER,
                simulate
        );
    }

    ItemStack removeLastUpgrade() {
        return this.upgrades.removeLast();
    }

    List<ItemStack> extractAllUpgrades() {
        return this.upgrades.removeAll();
    }

    Map<AirshipUpgradeSlot, ItemStack> installedUpgradeSlots() {
        return this.upgrades.slotSnapshot();
    }

    void configureInfinitePreview(AirshipHeatGrade grade) {
        this.heat.configureInfinitePreview(grade);
    }

    boolean hasHeat() {
        return this.heat.hasHeat();
    }

    boolean isInfinite() {
        return this.heat.isInfinite();
    }

    AirshipHeatGrade activeGrade() {
        return this.heat.activeGrade();
    }

    int totalFuelTicks() {
        return this.heat.totalTicks();
    }

    double fuelRatio() {
        return this.heat.fillRatio(this.profile().fuelCapacityTicks());
    }

    int comparatorSignal() {
        return this.heat.comparatorSignal(this.profile().fuelCapacityTicks());
    }

    AirshipHeatSnapshot heatSnapshot() {
        return this.heat.snapshot(this.profile().fuelCapacityTicks());
    }

    AirshipBurnerMetrics captureMetrics(
            int signalStrength,
            double gasOutput,
            BalloonHeatAggregate serverNetwork
    ) {
        return AirshipBurnerMetrics.capture(
                this.profile(),
                this.heat,
                signalStrength,
                gasOutput,
                serverNetwork
        );
    }

    BalloonHeatAggregate clientBalloonHeat() {
        return this.clientBalloonHeat;
    }

    void write(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket,
            BalloonHeatAggregate network
    ) {
        AirshipBurnerStateCodec.write(
                tag,
                registries,
                clientPacket,
                this.heat,
                this.upgrades,
                this.configuration,
                network
        );
    }

    void read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        this.clientBalloonHeat = AirshipBurnerStateCodec.read(
                tag,
                registries,
                clientPacket,
                this.heat,
                this.upgrades,
                this.configuration,
                this.clientBalloonHeat
        );
    }

    record FuelInsertion(boolean accepted, AirshipHeatSource source) {
        private static final FuelInsertion REJECTED = new FuelInsertion(false, null);
    }

    record RefreshResult(boolean evaluated, boolean changed) {
        private static final RefreshResult NOT_EVALUATED = new RefreshResult(false, false);
    }
}
