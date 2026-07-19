package us.kayla.zeppelinmusthave.content.kinetics;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * One-input, five-output split-shaft transmission.
 *
 * <p>The configured value is an output limit, not an independent motor speed.
 * Every output is clamped to the absolute speed received from the active input,
 * so the controller can reduce or reverse rotation but can never amplify it.</p>
 */
public final class OmniSpeedControllerBlockEntity extends SplitShaftBlockEntity {
    public static final int DEFAULT_SPEED = 16;
    private static final float MINIMUM_INPUT_SPEED = 1.0E-4F;

    public KineticScrollValueBehaviour targetSpeed;

    public OmniSpeedControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        int maximumSpeed = AllConfigs.server().kinetics.maxRotationSpeed.get();
        this.targetSpeed = new KineticScrollValueBehaviour(
                Component.translatable("zeppelin_must_have.omni_speed_controller.target_speed"),
                this,
                new OmniValueBoxTransform()
        );
        this.targetSpeed.between(-maximumSpeed, maximumSpeed);
        this.targetSpeed.value = DEFAULT_SPEED;
        this.targetSpeed.withCallback(ignored -> this.rebuildTransmission());
        behaviours.add(this.targetSpeed);
    }

    private void rebuildTransmission() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (this.hasNetwork()) {
            this.getOrCreateNetwork().remove(this);
        }
        RotationPropagator.handleRemoved(this.level, this.worldPosition, this);
        this.removeSource();
        this.attachKinetics();
        this.setChanged();
        this.sendData();
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (this.isVirtual() || !this.hasSource()) {
            return 1.0F;
        }
        if (face == this.getSourceFacing()) {
            return 1.0F;
        }

        return calculateOutputModifier(this.getTheoreticalSpeed(), this.getTargetSpeed());
    }

    /**
     * Computes the split-shaft ratio while enforcing conservation of RPM
     * magnitude. The configured sign still controls output direction.
     */
    public static float calculateOutputModifier(float inputSpeed, int targetSpeed) {
        if (Math.abs(inputSpeed) < MINIMUM_INPUT_SPEED) {
            return 0.0F;
        }
        return calculateLimitedOutputSpeed(inputSpeed, targetSpeed) / inputSpeed;
    }

    /**
     * Returns the requested signed output, limited to the available input RPM.
     */
    public static float calculateLimitedOutputSpeed(float inputSpeed, int targetSpeed) {
        float availableSpeed = Math.abs(inputSpeed);
        if (availableSpeed < MINIMUM_INPUT_SPEED || targetSpeed == 0) {
            return 0.0F;
        }

        float limitedMagnitude = Math.min(availableSpeed, Math.abs((float) targetSpeed));
        return Math.copySign(limitedMagnitude, targetSpeed);
    }

    public int getTargetSpeed() {
        return this.targetSpeed == null ? DEFAULT_SPEED : this.targetSpeed.getValue();
    }

    public float getEffectiveOutputSpeed() {
        return calculateLimitedOutputSpeed(this.getSpeed(), this.getTargetSpeed());
    }

    public float getSpeedForFace(Direction face) {
        if (!this.hasSource()) {
            return 0.0F;
        }
        return face == this.getSourceFacing() ? this.getSpeed() : this.getEffectiveOutputSpeed();
    }

    public Direction getInputFace() {
        return this.hasSource() ? this.getSourceFacing() : null;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.translatable("zeppelin_must_have.goggles.omni_speed_controller")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                        "zeppelin_must_have.goggles.omni_speed_controller.target",
                        this.getTargetSpeed()
                )
                .withStyle(ChatFormatting.AQUA));
        Direction inputFace = this.getInputFace();
        tooltip.add(Component.translatable(
                        inputFace == null
                                ? "zeppelin_must_have.goggles.omni_speed_controller.no_input"
                                : "zeppelin_must_have.goggles.omni_speed_controller.input",
                        inputFace == null ? "" : inputFace.getName()
                )
                .withStyle(inputFace == null ? ChatFormatting.GOLD : ChatFormatting.GREEN));
        return true;
    }

    private static final class OmniValueBoxTransform extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8.0F, 8.0F, 15.5F);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return true;
        }

        @Override
        public float getScale() {
            return 0.5F;
        }
    }
}
