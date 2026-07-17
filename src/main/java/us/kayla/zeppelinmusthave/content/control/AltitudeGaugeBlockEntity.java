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
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.helm.AirshipFlightSnapshot;
import us.kayla.zeppelinmusthave.integration.AeronauticsFlightStateReader;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

import java.util.List;

public final class AltitudeGaugeBlockEntity extends SmartBlockEntity
        implements IHaveGoggleInformation {
    private final LerpedFloat clientNeedle = LerpedFloat.linear();
    private AltitudeGaugeMode mode = AltitudeGaugeMode.ALTITUDE;
    private boolean altitudeHoldEnabled;
    private double targetAltitude;
    private int trimInput;
    private int outputSignal;
    private int ticksUntilSample;

    private AirshipFlightSnapshot snapshot = AirshipFlightSnapshot.detached(0L);
    private AltitudeControlProfile activeProfile = AltitudeControlProfile.unresolved(
            AltitudeControlProfiles.DEFAULT_ID
    );
    private long observedProfileRevision = Long.MIN_VALUE;

    public AltitudeGaugeBlockEntity(
            net.minecraft.world.level.block.entity.BlockEntityType<?> type,
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
        this.clientNeedle.startWithValue(this.outputSignal);
        this.ticksUntilSample = 0;
        this.refreshProfile(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null) {
            return;
        }

        if (this.level.isClientSide) {
            this.clientNeedle.chase(this.outputSignal, 0.22F, Chaser.EXP);
            this.clientNeedle.tickChaser();
            return;
        }

        this.refreshProfile(false);
        if (this.ticksUntilSample > 0) {
            this.ticksUntilSample--;
            return;
        }
        this.ticksUntilSample = Math.max(0, this.activeProfile.sampleIntervalTicks() - 1);
        this.sampleAndUpdate();
    }

    public void cycleMode(Player player) {
        this.mode = this.mode.next();
        if (this.mode != AltitudeGaugeMode.ALTITUDE_HOLD) {
            this.altitudeHoldEnabled = false;
        }
        this.sampleAndUpdate();
        this.setChanged();
        this.sendData();
        AltitudeGaugePresentation.notifyMode(this, player);
    }

    public void captureOrToggleAltitudeHold(Player player) {
        if (!this.snapshot.attached()) {
            AltitudeGaugePresentation.notifyDetached(player, true);
            return;
        }

        if (this.mode != AltitudeGaugeMode.ALTITUDE_HOLD) {
            this.mode = AltitudeGaugeMode.ALTITUDE_HOLD;
            this.altitudeHoldEnabled = true;
            this.targetAltitude = this.snapshot.worldY();
        } else if (this.altitudeHoldEnabled) {
            this.altitudeHoldEnabled = false;
        } else {
            this.altitudeHoldEnabled = true;
            this.targetAltitude = this.snapshot.worldY();
        }

        this.sampleAndUpdate();
        this.setChanged();
        this.sendData();

        if (this.level != null) {
            this.level.playSound(
                    null,
                    this.worldPosition,
                    SoundEvents.COMPARATOR_CLICK,
                    SoundSource.BLOCKS,
                    0.35F,
                    this.altitudeHoldEnabled ? 1.15F : 0.75F
            );
        }
        AltitudeGaugePresentation.notifyHoldState(this, player);
    }

    public void sendStatusTo(Player player) {
        AltitudeGaugePresentation.sendStatusTo(this, player);
    }

    @Override
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        return AltitudeGaugePresentation.addToGoggleTooltip(this, tooltip, isPlayerSneaking);
    }

    @Override
    public ItemStack getIcon(boolean isPlayerSneaking) {
        return ZmhBlocks.ALTITUDE_GAUGE_ITEM.get().getDefaultInstance();
    }

    public AltitudeGaugeMode getMode() {
        return this.mode;
    }

    public int getOutputSignal() {
        return this.outputSignal;
    }

    public int getTrimInput() {
        return this.trimInput;
    }

    public float getNeedleSignal(float partialTicks) {
        return this.clientNeedle.getValue(partialTicks);
    }

    public boolean isAltitudeHoldEnabled() {
        return this.altitudeHoldEnabled;
    }

    public double getTargetAltitude() {
        return this.targetAltitude;
    }

    AirshipFlightSnapshot snapshot() {
        return this.snapshot;
    }

    private void sampleAndUpdate() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        AirshipFlightSnapshot nextSnapshot = AeronauticsFlightStateReader.read(
                this.level,
                this.worldPosition
        );
        boolean telemetryChanged = nextSnapshot.materiallyDiffersFrom(this.snapshot);
        this.snapshot = nextSnapshot;

        int previousTrim = this.trimInput;
        this.trimInput = this.readTrimInput();

        int desiredOutput = this.calculateDesiredOutput();
        int nextOutput = AltitudeControlMath.slew(
                this.outputSignal,
                desiredOutput,
                this.activeProfile.maximumSignalStep()
        );
        boolean outputChanged = this.applyOutput(nextOutput);
        boolean periodicSync = this.level.getGameTime() % 20L == 0L;

        this.setChanged();
        if (telemetryChanged
                || previousTrim != this.trimInput
                || outputChanged
                || periodicSync) {
            this.sendData();
        }
    }

    private int calculateDesiredOutput() {
        if (this.level == null) {
            return 0;
        }
        return AltitudeGaugeController.desiredOutput(
                this.mode,
                this.altitudeHoldEnabled,
                this.trimInput,
                this.targetAltitude,
                this.snapshot,
                this.activeProfile,
                this.level.getMinBuildHeight(),
                this.level.getMaxBuildHeight()
        );
    }

    private int readTrimInput() {
        if (this.level == null) {
            return 0;
        }
        Direction outputDirection = this.getBlockState().getValue(AltitudeGaugeBlock.FACING);
        Direction inputDirection = outputDirection.getOpposite();
        BlockPos inputPos = this.worldPosition.relative(inputDirection);
        return this.level.getSignal(inputPos, inputDirection);
    }

    private boolean applyOutput(int power) {
        if (this.level == null) {
            return false;
        }

        int clamped = Math.clamp(power, 0, 15);
        BlockState state = this.getBlockState();
        if (this.outputSignal == clamped
                && state.getValue(AltitudeGaugeBlock.POWER) == clamped) {
            return false;
        }

        this.outputSignal = clamped;
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
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        long revision = AltitudeControlProfiles.INSTANCE.revision();
        if (!force && revision == this.observedProfileRevision) {
            return;
        }
        this.activeProfile = AltitudeControlProfiles.INSTANCE.resolveDefault();
        this.observedProfileRevision = revision;
        this.ticksUntilSample = 0;
    }

    @Override
    protected void write(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        tag.putString("Mode", this.mode.name());
        tag.putBoolean("AltitudeHoldEnabled", this.altitudeHoldEnabled);
        tag.putDouble("TargetAltitude", this.targetAltitude);
        tag.putInt("TrimInput", this.trimInput);
        tag.putInt("OutputSignal", this.outputSignal);

        if (clientPacket) {
            CompoundTag telemetry = new CompoundTag();
            this.snapshot.write(telemetry);
            tag.put("FlightTelemetry", telemetry);
        }

        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        this.mode = AltitudeGaugeMode.parse(tag.getString("Mode"));
        this.altitudeHoldEnabled = tag.getBoolean("AltitudeHoldEnabled");
        this.targetAltitude = tag.getDouble("TargetAltitude");
        this.trimInput = Math.clamp(tag.getInt("TrimInput"), 0, 15);
        this.outputSignal = Math.clamp(tag.getInt("OutputSignal"), 0, 15);
        if (clientPacket) {
            this.clientNeedle.chase(this.outputSignal, 0.22F, Chaser.EXP);
        }

        if (clientPacket && tag.contains("FlightTelemetry")) {
            this.snapshot = AirshipFlightSnapshot.read(tag.getCompound("FlightTelemetry"));
        }

        super.read(tag, registries, clientPacket);
    }

}
