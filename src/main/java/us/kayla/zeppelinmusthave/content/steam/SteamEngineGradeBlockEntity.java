package us.kayla.zeppelinmusthave.content.steam;

import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamJetParticleData;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlock;
import us.kayla.zeppelinmusthave.data.ZmhLang;

import java.util.List;

@SuppressWarnings("unchecked")
public final class SteamEngineGradeBlockEntity extends SteamEngineBlockEntity {
    private SteamEngineGradeProfile activeProfile;
    private long observedProfileRevision = Long.MIN_VALUE;

    public SteamEngineGradeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.activeProfile = SteamEngineGradeProfile.unresolved(profileIdFor(state));
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
        // Retain SmartBlockEntity behaviours and Create's invalidation path.
        // The native method exits before generation because this is not
        // create:steam_engine, so the grade-aware generation path follows it.
        super.tick();

        if (this.level == null || !(this.getBlockState().getBlock() instanceof SteamEngineGradeBlock)) {
            return;
        }

        FluidTankBlockEntity tank = this.getTank();
        PoweredShaftBlockEntity shaft = this.getShaft();
        if (tank == null || shaft == null || !this.isValid()) {
            return;
        }

        BlockState shaftState = shaft.getBlockState();
        Axis targetAxis = Axis.X;
        if (shaftState.getBlock() instanceof IRotate rotate) {
            targetAxis = rotate.getRotationAxis(shaftState);
        }
        boolean verticalTarget = targetAxis == Axis.Y;

        BlockState blockState = this.getBlockState();
        Direction facing = SteamEngineBlock.getFacing(blockState);
        if (facing.getAxis() == Axis.Y) {
            facing = blockState.getValue(SteamEngineBlock.FACING);
        }

        float efficiency = Mth.clamp(
                tank.boiler.getEngineEfficiency(tank.getTotalTankSize()),
                0.0F,
                1.0F
        );
        if (efficiency > 0.0F) {
            this.award(AllAdvancements.STEAM_ENGINE);
        }

        int conveyedSpeedLevel = efficiency == 0.0F
                ? 1
                : verticalTarget
                        ? 1
                        : (int) GeneratingKineticBlockEntity.convertToDirection(1, facing);
        if (targetAxis == Axis.Z) {
            conveyedSpeedLevel *= -1;
        }
        if (this.movementDirection.get() == RotationDirection.COUNTER_CLOCKWISE) {
            conveyedSpeedLevel *= -1;
        }

        float shaftSpeed = shaft.getTheoreticalSpeed();
        if (shaft.hasSource()
                && shaftSpeed != 0.0F
                && conveyedSpeedLevel != 0
                && (shaftSpeed > 0.0F) != (conveyedSpeedLevel > 0)) {
            this.movementDirection.setValue(1 - this.movementDirection.get().ordinal());
            conveyedSpeedLevel *= -1;
        }

        shaft.update(this.worldPosition, conveyedSpeedLevel, efficiency);

        if (this.level.isClientSide) {
            this.spawnGradeParticles(shaft, efficiency);
        }
    }

    @Override
    public boolean isValid() {
        Direction direction = SteamEngineBlock.getConnectedDirection(this.getBlockState()).getOpposite();
        Level level = this.getLevel();
        return level != null
                && level.getBlockState(this.getBlockPos().relative(direction)).getBlock() instanceof BoilerGradeBlock;
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public Float getTargetAngle() {
        BlockState blockState = this.getBlockState();
        if (!(blockState.getBlock() instanceof SteamEngineGradeBlock)) {
            return null;
        }

        Direction facing = SteamEngineBlock.getFacing(blockState);
        PoweredShaftBlockEntity shaft = this.getShaft();
        if (shaft == null) {
            return null;
        }

        Axis facingAxis = facing.getAxis();
        Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        float angle = KineticBlockEntityRenderer.getAngleForBe(shaft, shaft.getBlockPos(), axis);
        if (axis == facingAxis) {
            return null;
        }
        if (axis.isHorizontal()
                && (facingAxis == Axis.X ^ facing.getAxisDirection() == AxisDirection.POSITIVE)) {
            angle *= -1.0F;
        }
        if (axis == Axis.X && facing == Direction.DOWN) {
            angle *= -1.0F;
        }
        return angle;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean handled = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(CommonComponents.EMPTY);

        ZmhLang.translate(
                        "goggles.steam_engine_grade.grade",
                        Component.translatable(this.getBlockState().getBlock().getDescriptionId())
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        ZmhLang.translate(
                        "goggles.steam_engine_grade.capacity",
                        Component.literal(Integer.toString((int) Math.round(this.activeProfile.stressCapacity())))
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        ZmhLang.translate(
                        "goggles.steam_engine_grade.load",
                        Component.literal(Integer.toString(this.activeProfile.boilerLoadUnits()))
                                .withStyle(ChatFormatting.YELLOW)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        ZmhLang.translate(
                        "goggles.steam_engine_grade.cylinders",
                        Component.literal(Integer.toString(this.activeProfile.cylinderCount()))
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        if (isPlayerSneaking) {
            ZmhLang.translate(
                            "goggles.steam_engine_grade.profile_id",
                            Component.literal(this.activeProfile.id().toString())
                                    .withStyle(ChatFormatting.DARK_AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
        }

        return true;
    }

    public SteamEngineGradeProfile activeProfile() {
        return this.activeProfile;
    }

    public SteamEngineGradeTier tier() {
        return tierFor(this.getBlockState());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket && tag.contains("SteamEngineGradeProfileId")) {
            this.activeProfile = SteamEngineGradeProfile.readClientSnapshot(tag);
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
        long revision = SteamEngineGradeProfiles.INSTANCE.revision();
        if (!force && this.observedProfileRevision == revision) {
            return;
        }

        SteamEngineGradeProfile nextProfile = SteamEngineGradeProfiles.INSTANCE.resolve(this.tier());
        boolean changed = !nextProfile.equals(this.activeProfile);
        this.activeProfile = nextProfile;
        this.observedProfileRevision = revision;

        if (this.level == null || this.level.isClientSide || (!force && !changed)) {
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

    private void spawnGradeParticles(PoweredShaftBlockEntity shaft, float efficiency) {
        if (efficiency <= 0.0F || shaft.getSpeed() == 0.0F) {
            return;
        }

        float speedRatio = Mth.clamp(Math.abs(shaft.getSpeed()) / 64.0F, 0.0F, 1.0F);
        float chance = 0.035F
                * this.activeProfile.steamParticleScale()
                * this.activeProfile.cylinderCount()
                * efficiency
                * speedRatio;
        if (this.level.random.nextFloat() >= chance) {
            return;
        }

        Direction facing = SteamEngineBlock.getFacing(this.getBlockState());
        Vec3 offset = VecHelper.rotate(
                new Vec3(0.0D, 0.0D, 1.0D)
                        .add(VecHelper.offsetRandomly(Vec3.ZERO, this.level.random, 1.0F)
                                .multiply(1.0D, 1.0D, 0.0D)
                                .normalize()
                                .scale(0.5D)),
                AngleHelper.verticalAngle(facing),
                Axis.X
        );
        offset = VecHelper.rotate(offset, AngleHelper.horizontalAngle(facing), Axis.Y);
        Vec3 position = offset.scale(0.5D).add(Vec3.atCenterOf(this.worldPosition));
        Vec3 motion = offset.subtract(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.75D));
        int particleStrength = Math.max(1, Math.round(this.activeProfile.steamParticleScale()));
        this.level.addParticle(
                new SteamJetParticleData(particleStrength),
                position.x,
                position.y,
                position.z,
                motion.x,
                motion.y,
                motion.z
        );
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
