package us.kayla.zeppelinmusthave.content.control;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.control.fcn.FlightControlNetworkManager;
import us.kayla.zeppelinmusthave.content.helm.AirshipFlightSnapshot;
import us.kayla.zeppelinmusthave.integration.AeronauticsFlightStateReader;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

import java.util.List;

public final class AltitudeGaugeBlockEntity extends SmartBlockEntity
        implements IHaveGoggleInformation {
    private final LerpedFloat clientNeedle = LerpedFloat.linear();
    private final AltitudeGaugeRuntimeState runtimeState = new AltitudeGaugeRuntimeState();
    private final AltitudeGaugeConfiguration configuration = new AltitudeGaugeConfiguration();

    public AltitudeGaugeBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void initialize() {
        super.initialize();
        this.clientNeedle.startWithValue(this.runtimeState.outputSignal());
        this.configuration.resetSampling();
        this.refreshProfile(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null) {
            return;
        }
        if (this.level.isClientSide) {
            this.clientNeedle.chase(this.runtimeState.outputSignal(), 0.22F, Chaser.EXP);
            this.clientNeedle.tickChaser();
            return;
        }

        this.refreshProfile(false);
        if (this.configuration.shouldSample()) {
            this.sampleAndUpdate();
        }
    }

    public void cycleMode(Player player) {
        this.runtimeState.cycleMode();
        this.sampleAndUpdate();
        this.publishState();
        AltitudeGaugePresentation.notifyMode(this, player);
    }

    public void captureOrToggleAltitudeHold(Player player) {
        if (!this.runtimeState.toggleHoldAtCurrentAltitude()) {
            AltitudeGaugePresentation.notifyDetached(player, true);
            return;
        }

        this.sampleAndUpdate();
        this.publishState();
        if (this.level != null) {
            this.level.playSound(
                    null,
                    this.worldPosition,
                    SoundEvents.COMPARATOR_CLICK,
                    SoundSource.BLOCKS,
                    0.35F,
                    this.runtimeState.altitudeHoldEnabled() ? 1.15F : 0.75F
            );
        }
        AltitudeGaugePresentation.notifyHoldState(this, player);
    }

    public void sendStatusTo(Player player) {
        AltitudeGaugePresentation.sendStatusTo(this, player);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return AltitudeGaugePresentation.addToGoggleTooltip(this, tooltip, isPlayerSneaking);
    }

    @Override
    public ItemStack getIcon(boolean isPlayerSneaking) {
        return ZmhBlocks.ALTITUDE_GAUGE_ITEM.get().getDefaultInstance();
    }

    public AltitudeGaugeMode getMode() {
        return this.runtimeState.mode();
    }

    public int getOutputSignal() {
        return this.runtimeState.outputSignal();
    }

    public int getTrimInput() {
        return this.runtimeState.trimInput();
    }

    public float getNeedleSignal(float partialTicks) {
        return this.clientNeedle.getValue(partialTicks);
    }

    public boolean isAltitudeHoldEnabled() {
        return this.runtimeState.altitudeHoldEnabled();
    }

    public double getTargetAltitude() {
        return this.runtimeState.targetAltitude();
    }

    AirshipFlightSnapshot snapshot() {
        return this.runtimeState.snapshot();
    }

    private void sampleAndUpdate() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        AirshipFlightSnapshot previousSnapshot = this.runtimeState.snapshot();
        AirshipFlightSnapshot nextSnapshot = AeronauticsFlightStateReader.read(
                this.level,
                this.worldPosition
        );
        this.runtimeState.setSnapshot(nextSnapshot);

        int previousTrim = this.runtimeState.trimInput();
        this.runtimeState.setTrimInput(this.readTrimInput());

        int desiredOutput = AltitudeGaugeController.desiredOutput(
                this.runtimeState.mode(),
                this.runtimeState.altitudeHoldEnabled(),
                this.runtimeState.trimInput(),
                this.runtimeState.targetAltitude(),
                nextSnapshot,
                this.configuration.profile(),
                this.level.getMinBuildHeight(),
                this.level.getMaxBuildHeight()
        );
        if (FlightControlNetworkManager.isEmergencyLatched(this.level, this.worldPosition)) {
            desiredOutput = 0;
        }
        int nextOutput = AltitudeControlMath.slew(
                this.runtimeState.outputSignal(),
                desiredOutput,
                this.configuration.profile().maximumSignalStep()
        );
        boolean outputChanged = this.applyOutput(nextOutput);
        boolean synchronize = nextSnapshot.materiallyDiffersFrom(previousSnapshot)
                || previousTrim != this.runtimeState.trimInput()
                || outputChanged
                || this.level.getGameTime() % 20L == 0L;

        this.setChanged();
        if (synchronize) {
            this.sendData();
        }
    }

    private int readTrimInput() {
        Direction outputDirection = this.getBlockState().getValue(AltitudeGaugeBlock.FACING);
        Direction inputDirection = outputDirection.getOpposite();
        BlockPos inputPos = this.worldPosition.relative(inputDirection);
        return this.level == null ? 0 : this.level.getSignal(inputPos, inputDirection);
    }

    private boolean applyOutput(int power) {
        if (this.level == null) {
            return false;
        }

        int clamped = Math.clamp(power, 0, 15);
        BlockState state = this.getBlockState();
        if (this.runtimeState.outputSignal() == clamped
                && state.getValue(AltitudeGaugeBlock.POWER) == clamped) {
            return false;
        }

        this.runtimeState.setOutputSignal(clamped);
        BlockState nextState = state.setValue(AltitudeGaugeBlock.POWER, clamped);
        this.level.setBlock(this.worldPosition, nextState, Block.UPDATE_CLIENTS);
        Direction outputDirection = nextState.getValue(AltitudeGaugeBlock.FACING);
        this.level.neighborChanged(
                this.worldPosition.relative(outputDirection),
                nextState.getBlock(),
                this.worldPosition
        );
        return true;
    }

    private void refreshProfile(boolean force) {
        if (this.level != null && !this.level.isClientSide) {
            this.configuration.refresh(force);
        }
    }

    private void publishState() {
        this.setChanged();
        this.sendData();
    }

    @Override
    protected void write(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        AltitudeGaugeStateCodec.write(
                tag,
                this.runtimeState.persistentState(),
                clientPacket
        );
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        this.runtimeState.restore(
                AltitudeGaugeStateCodec.read(
                        tag,
                        this.runtimeState.snapshot(),
                        clientPacket
                )
        );
        if (clientPacket) {
            this.clientNeedle.chase(this.runtimeState.outputSignal(), 0.22F, Chaser.EXP);
        }
        super.read(tag, registries, clientPacket);
    }
}
