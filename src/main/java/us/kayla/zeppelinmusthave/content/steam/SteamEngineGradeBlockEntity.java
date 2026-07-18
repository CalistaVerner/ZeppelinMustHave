package us.kayla.zeppelinmusthave.content.steam;

import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlock;

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
        if (this.level == null || !(this.getBlockState().getBlock() instanceof SteamEngineGradeBlock)) {
            return;
        }
        if (this.isMkViiAuxiliary()) {
            return;
        }

        FluidTankBlockEntity tank = this.getTank();
        PoweredShaftBlockEntity shaft = this.getPrimaryShaft();
        if (tank == null || shaft == null || !this.isValid()) {
            if (!this.level.isClientSide) {
                this.deactivateOwnedShaft();
                this.advancementActive = false;
            }
            return;
        }

        float efficiency = this.resolveEfficiency(tank);
        this.updatePoweredShaft(shaft, efficiency);

        if (!this.level.isClientSide) {
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
                            efficiency > 0.0F,
                            this.flightControlOverride ? this.flightControlCommand : 15.0D,
                            Math.abs(shaft.getTheoreticalSpeed()) * efficiency,
                            efficiency
                    )
            );
        } else {
            SteamEngineGradeClientEffects.spawnParticles(
                    this,
                    shaft,
                    efficiency,
                    this.configuration.profile()
            );
        }
    }

    private float resolveEfficiency(FluidTankBlockEntity tank) {
        float efficiency = Mth.clamp(
                tank.boiler.getEngineEfficiency(tank.getTotalTankSize()),
                0.0F,
                1.0F
        );
        boolean emergency = this.level != null
                && !this.level.isClientSide
                && FlightControlNetworkManager.isEmergencyLatched(this.level, this.worldPosition);
        if (emergency) {
            efficiency = 0.0F;
        } else if (this.flightControlOverride) {
            efficiency *= Math.abs(this.flightControlCommand) / 15.0F;
        }
        if (efficiency > 0.0F) {
            this.award(AllAdvancements.STEAM_ENGINE);
        }
        return efficiency;
    }

    private void updatePoweredShaft(PoweredShaftBlockEntity shaft, float efficiency) {
        BlockState shaftState = shaft.getBlockState();
        Axis targetAxis = shaftState.getBlock() instanceof IRotate rotate
                ? rotate.getRotationAxis(shaftState)
                : Axis.X;
        boolean verticalTarget = targetAxis == Axis.Y;

        BlockState engineState = this.getBlockState();
        Direction facing = SteamEngineBlock.getFacing(engineState);
        if (facing.getAxis() == Axis.Y) {
            facing = engineState.getValue(SteamEngineBlock.FACING);
        }

        int conveyedDirection = efficiency == 0.0F
                ? 1
                : verticalTarget
                        ? 1
                        : (int) GeneratingKineticBlockEntity.convertToDirection(1, facing);
        if (targetAxis == Axis.Z) {
            conveyedDirection *= -1;
        }
        if (this.movementDirection.get() == RotationDirection.COUNTER_CLOCKWISE) {
            conveyedDirection *= -1;
        }
        if (this.flightControlOverride && this.flightControlCommand < 0) {
            conveyedDirection *= -1;
        }

        float shaftSpeed = shaft.getTheoreticalSpeed();
        if (shaft.hasSource()
                && shaftSpeed != 0.0F
                && conveyedDirection != 0
                && (shaftSpeed > 0.0F) != (conveyedDirection > 0)) {
            this.movementDirection.setValue(1 - this.movementDirection.get().ordinal());
            conveyedDirection *= -1;
        }

        shaft.update(this.worldPosition, conveyedDirection, efficiency);
    }

    public @Nullable PoweredShaftBlockEntity getPrimaryShaft() {
        if (this.level == null) {
            return null;
        }
        BlockState state = this.getBlockState();
        if (state.getBlock() instanceof MkViiSteamEngineBlock) {
            BlockPos controllerPos = MkViiSteamEngineBlock.controllerPos(state, this.worldPosition);
            if (!controllerPos.equals(this.worldPosition)) {
                if (this.level.getBlockEntity(controllerPos) instanceof SteamEngineGradeBlockEntity controller) {
                    return controller.getShaft();
                }
                return null;
            }
        }
        return this.getShaft();
    }

    private @Nullable PoweredShaftBlockEntity getOwnedShaft() {
        if (this.isMkViiAuxiliary()) {
            return null;
        }
        PoweredShaftBlockEntity shaft = this.getPrimaryShaft();
        return shaft != null && shaft.isPoweredBy(this.worldPosition) ? shaft : null;
    }

    private void deactivateOwnedShaft() {
        PoweredShaftBlockEntity shaft = this.getOwnedShaft();
        if (shaft != null && (shaft.engineEfficiency != 0.0F || shaft.movementDirection != 0)) {
            shaft.update(this.worldPosition, 0, 0.0F);
        }
    }

    private boolean isMkViiAuxiliary() {
        return MkViiSteamEngineBlock.isAuxiliary(this.getBlockState());
    }

    @Override
    public void remove() {
        PoweredShaftBlockEntity shaft = this.getOwnedShaft();
        if (shaft != null) {
            shaft.remove(this.worldPosition);
        }
        super.remove();
    }

    @Override
    public void applyFlightControl(FlightControlChannel channel, int value, boolean emergencyLatched) {
        if (channel != FlightControlChannel.ENGINE_THROTTLE || this.isMkViiAuxiliary()) return;
        this.flightControlOverride = true;
        this.flightControlCommand = emergencyLatched ? 0 : Math.clamp(value, -15, 15);
        this.setChanged();
        this.sendData();
    }

    @Override
    public void clearFlightControl(FlightControlChannel channel) {
        if (channel != FlightControlChannel.ENGINE_THROTTLE || this.isMkViiAuxiliary()) return;
        this.flightControlOverride = false;
        this.flightControlCommand = 0;
        this.setChanged();
        this.sendData();
    }

    @Override
    public boolean isValid() {
        Level level = this.getLevel();
        if (level == null) {
            return false;
        }
        BlockState state = this.getBlockState();
        if (state.getBlock() instanceof LeviathanSteamEngineBlock leviathan) {
            return leviathan.isAssemblyComplete(level, this.getBlockPos(), state);
        }
        if (state.getBlock() instanceof MkViiSteamEngineBlock mkVii) {
            return mkVii.isAssemblyComplete(level, this.getBlockPos(), state);
        }
        Direction direction = SteamEngineBlock.getConnectedDirection(state).getOpposite();
        return level.getBlockState(this.getBlockPos().relative(direction)).getBlock()
                instanceof BoilerGradeBlock;
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
        SteamEngineGradeConfiguration.RefreshResult refresh = this.configuration.refresh(
                this.tier(),
                force
        );
        if (!refresh.evaluated()
                || this.level == null
                || this.level.isClientSide
                || (!force && !refresh.changed())) {
            return;
        }

        PoweredShaftBlockEntity shaft = this.getOwnedShaft();
        if (shaft != null) {
            int direction = shaft.movementDirection;
            float efficiency = shaft.engineEfficiency;
            shaft.remove(this.worldPosition);
            shaft.update(this.worldPosition, direction, efficiency);
        }
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
