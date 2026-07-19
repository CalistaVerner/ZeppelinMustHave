package us.kayla.zeppelinmusthave.content.steam;

import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import us.kayla.zeppelinmusthave.advancement.ZmhAdvancements;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightControlActuator;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightControlChannel;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightControlNetworkManager;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightSystemStatus;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightSystemType;

import java.util.List;

@SuppressWarnings("unchecked")
public final class SteamEngineGradeBlockEntity extends SteamEngineBlockEntity implements FlightControlActuator {
    private final SteamEngineGradeConfiguration configuration;
    private boolean flightControlOverride;
    private int flightControlCommand;
    private boolean advancementActive;

    public SteamEngineGradeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.configuration = new SteamEngineGradeConfiguration(profileIdFor(state));
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
    public void tick() {
        super.tick();
        if (this.level == null
                || !(this.getBlockState().getBlock() instanceof SteamEngineGradeBlock)
                || this.isMkViiAuxiliary()) {
            return;
        }

        FluidTankBlockEntity tank = this.getTank();
        PoweredShaftBlockEntity shaft = this.getPrimaryShaft();
        if (tank == null || shaft == null || !this.isValid()) {
            this.handleInactiveEngine();
            return;
        }

        float efficiency = SteamEnginePowerController.efficiency(
                this.level,
                this.worldPosition,
                tank,
                this.flightControlOverride,
                this.flightControlCommand
        );
        if (efficiency > 0.0F) {
            this.award(AllAdvancements.STEAM_ENGINE);
        }
        boolean flipDirection = SteamEngineShaftController.update(
                this,
                shaft,
                efficiency,
                this.movementDirection.get(),
                this.flightControlOverride && this.flightControlCommand < 0
        );
        if (flipDirection) {
            this.movementDirection.setValue(1 - this.movementDirection.get().ordinal());
        }

        if (this.level.isClientSide) {
            SteamEngineGradeClientEffects.spawnParticles(
                    this,
                    shaft,
                    efficiency,
                    this.configuration.profile()
            );
            return;
        }
        this.reportServerState(shaft, efficiency);
    }

    private void handleInactiveEngine() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        SteamEngineShaftController.deactivateOwnedShaft(this);
        this.advancementActive = false;
    }

    private void reportServerState(PoweredShaftBlockEntity shaft, float efficiency) {
        boolean active = efficiency > 0.0F;
        if (active && !this.advancementActive) {
            ZmhAdvancements.activateNearby(
                    this.level,
                    this.worldPosition,
                    ZmhAdvancements.STEAM_POWER_ONLINE,
                    8.0D
            );
        }
        this.advancementActive = active;
        FlightControlNetworkManager.reportSystem(
                this.level,
                this.worldPosition,
                new FlightSystemStatus(
                        FlightSystemType.ENGINE,
                        active,
                        this.flightControlOverride ? this.flightControlCommand : 15.0D,
                        Math.abs(shaft.getTheoreticalSpeed()) * efficiency,
                        efficiency
                )
        );
    }

    public @Nullable PoweredShaftBlockEntity getPrimaryShaft() {
        return SteamEngineShaftController.primaryShaft(this);
    }

    private boolean isMkViiAuxiliary() {
        return MkViiSteamEngineBlock.isAuxiliary(this.getBlockState());
    }

    @Override
    public void remove() {
        SteamEngineShaftController.removeOwnedShaft(this);
        super.remove();
    }

    @Override
    public void applyFlightControl(FlightControlChannel channel, int value, boolean emergencyLatched) {
        if (channel != FlightControlChannel.ENGINE_THROTTLE || this.isMkViiAuxiliary()) {
            return;
        }
        this.flightControlOverride = true;
        this.flightControlCommand = emergencyLatched ? 0 : Math.clamp(value, -15, 15);
        this.setChanged();
        this.sendData();
    }

    @Override
    public void clearFlightControl(FlightControlChannel channel) {
        if (channel != FlightControlChannel.ENGINE_THROTTLE || this.isMkViiAuxiliary()) {
            return;
        }
        this.flightControlOverride = false;
        this.flightControlCommand = 0;
        this.setChanged();
        this.sendData();
    }

    @Override
    public boolean isValid() {
        Level level = this.getLevel();
        return level != null && SteamEngineAssemblyValidator.isValid(
                level,
                this.getBlockPos(),
                this.getBlockState()
        );
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public Float getTargetAngle() {
        return SteamEngineGradeClientEffects.targetAngle(this);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return SteamEngineGradePresentation.addToGoggleTooltip(
                this.getBlockState(),
                this.configuration.profile(),
                tooltip,
                isPlayerSneaking,
                super.addToGoggleTooltip(tooltip, isPlayerSneaking)
        );
    }

    public SteamEngineGradeProfile activeProfile() {
        return this.configuration.profile();
    }

    public SteamEngineGradeTier tier() {
        return tierFor(this.getBlockState());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) {
            this.configuration.readClientSnapshot(tag);
            this.flightControlOverride = tag.getBoolean("FlightControlOverride");
            this.flightControlCommand = tag.getInt("FlightControlCommand");
        }
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) {
            this.configuration.writeClientSnapshot(tag);
            tag.putBoolean("FlightControlOverride", this.flightControlOverride);
            tag.putInt("FlightControlCommand", this.flightControlCommand);
        }
    }

    private void refreshProfile(boolean force) {
        SteamEngineGradeConfiguration.RefreshResult refresh = this.configuration.refresh(this.tier(), force);
        if (!refresh.evaluated()
                || this.level == null
                || this.level.isClientSide
                || (!force && !refresh.changed())) {
            return;
        }
        SteamEngineShaftController.refreshCapacity(this);
        this.setChanged();
        this.sendData();
    }

    private static SteamEngineGradeTier tierFor(BlockState state) {
        if (state.getBlock() instanceof SteamEngineGradeBlock block) {
            return block.tier();
        }
        throw new IllegalStateException("SteamEngineGradeBlockEntity is attached to " + state);
    }

    private static ResourceLocation profileIdFor(BlockState state) {
        return tierFor(state).profileId();
    }
}
