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
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlock;

import java.util.List;

@SuppressWarnings("unchecked")
public final class SteamEngineGradeBlockEntity extends SteamEngineBlockEntity {
    private final SteamEngineGradeConfiguration configuration;

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

        FluidTankBlockEntity tank = this.getTank();
        PoweredShaftBlockEntity shaft = this.getShaft();
        if (tank == null || shaft == null || !this.isValid()) {
            return;
        }

        float efficiency = this.updatePoweredShaft(tank, shaft);
        if (this.level.isClientSide) {
            SteamEngineGradeClientEffects.spawnParticles(
                    this,
                    shaft,
                    efficiency,
                    this.configuration.profile()
            );
        }
    }

    private float updatePoweredShaft(
            FluidTankBlockEntity tank,
            PoweredShaftBlockEntity shaft
    ) {
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

        float efficiency = Mth.clamp(
                tank.boiler.getEngineEfficiency(tank.getTotalTankSize()),
                0.0F,
                1.0F
        );
        if (efficiency > 0.0F) {
            this.award(AllAdvancements.STEAM_ENGINE);
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

        float shaftSpeed = shaft.getTheoreticalSpeed();
        if (shaft.hasSource()
                && shaftSpeed != 0.0F
                && conveyedDirection != 0
                && (shaftSpeed > 0.0F) != (conveyedDirection > 0)) {
            this.movementDirection.setValue(1 - this.movementDirection.get().ordinal());
            conveyedDirection *= -1;
        }

        shaft.update(this.worldPosition, conveyedDirection, efficiency);
        return efficiency;
    }

    @Override
    public boolean isValid() {
        Direction direction = SteamEngineBlock.getConnectedDirection(this.getBlockState()).getOpposite();
        Level level = this.getLevel();
        return level != null
                && level.getBlockState(this.getBlockPos().relative(direction)).getBlock()
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
        }
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) {
            this.configuration.writeClientSnapshot(tag);
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

        PoweredShaftBlockEntity shaft = this.getShaft();
        if (shaft != null && shaft.isPoweredBy(this.worldPosition)) {
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
