package us.kayla.zeppelinmusthave.content.control.fcn;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.helm.AirshipFlightSnapshot;
import us.kayla.zeppelinmusthave.integration.AeronauticsFlightStateReader;

import java.util.List;

/** Central server-authoritative controller. It routes commands but never applies physics. */
public final class FlightComputerBlockEntity extends SmartBlockEntity {
    private static final int SAMPLE_INTERVAL_TICKS = 2;

    private final FlightControlConfiguration configuration = new FlightControlConfiguration();
    private AirshipFlightSnapshot flight = AirshipFlightSnapshot.detached(0L);
    private FlightSystemsSnapshot systems = FlightSystemsSnapshot.empty(0L);
    private FlightControlFrame input = FlightControlFrame.zero(0L);
    private FlightControlFrame output = FlightControlFrame.zero(0L);
    private boolean powered;
    private boolean primary;
    private boolean emergencyLatched;
    private int sampleCountdown;

    public FlightComputerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void initialize() {
        super.initialize();
        this.sampleCountdown = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (this.sampleCountdown-- > 0) {
            return;
        }
        this.sampleCountdown = SAMPLE_INTERVAL_TICKS - 1;

        boolean previousPowered = this.powered;
        boolean previousPrimary = this.primary;
        boolean previousEmergency = this.emergencyLatched;
        AirshipFlightSnapshot previousFlight = this.flight;
        FlightControlFrame previousInput = this.input;
        FlightControlFrame previousOutput = this.output;

        this.powered = FlightControlBlockSupport.hasControlPower(this.level, this.worldPosition);
        this.flight = AeronauticsFlightStateReader.read(this.level, this.worldPosition);
        this.systems = FlightControlNetworkManager.sampleSystems(this.level, this.worldPosition);
        this.emergencyLatched = FlightControlNetworkManager.isEmergencyLatched(this.level, this.worldPosition);

        if (!this.powered || this.emergencyLatched || !this.flight.attached()) {
            FlightControlNetworkManager.withdrawComputer(
                    this.level,
                    this.worldPosition,
                    this.configuration.address()
            );
            this.primary = false;
            this.input = FlightControlFrame.zero(this.level.getGameTime());
            this.output = this.emergencyLatched
                    ? this.input.with(FlightControlChannel.EMERGENCY_STOP, 1)
                    : this.input;
        } else {
            this.primary = FlightControlNetworkManager.heartbeatComputer(
                    this.level,
                    this.worldPosition,
                    this.configuration.address()
            );
            this.input = FlightControlNetworkManager.readInputs(
                    this.level,
                    this.worldPosition,
                    this.configuration.address()
            );
            this.output = this.sanitize(this.input);
            if (this.primary) {
                FlightControlNetworkManager.publishComputerOutput(
                        this.level,
                        this.worldPosition,
                        this.configuration.address(),
                        this.output
                );
            }
        }

        boolean changed = previousPowered != this.powered
                || previousPrimary != this.primary
                || previousEmergency != this.emergencyLatched
                || this.flight.materiallyDiffersFrom(previousFlight)
                || this.input.materiallyDiffersFrom(previousInput)
                || this.output.materiallyDiffersFrom(previousOutput)
                || this.level.getGameTime() % 20L == 0L;
        if (changed) {
            this.setChanged();
            this.sendData();
        }
    }

    private FlightControlFrame sanitize(FlightControlFrame frame) {
        if (frame.get(FlightControlChannel.EMERGENCY_STOP) > 0) {
            return FlightControlFrame.zero(frame.sampledAtGameTime())
                    .with(FlightControlChannel.EMERGENCY_STOP, 1);
        }
        return frame.with(FlightControlChannel.EMERGENCY_STOP, 0);
    }

    public boolean configure(ItemStack stack, Player player) {
        FlightControlAddress previous = this.configuration.address();
        if (!FlightControlBlockSupport.configure(this.configuration, stack, player)) {
            return false;
        }
        if (this.level != null && !this.level.isClientSide && !previous.equals(this.configuration.address())) {
            FlightControlNetworkManager.withdrawComputer(this.level, this.worldPosition, previous);
        }
        this.setChanged();
        this.sendData();
        return true;
    }

    public void sendStatusTo(Player player) {
        player.displayClientMessage(
                Component.translatable(
                        "zeppelin_must_have.fcn.computer.status",
                        FlightControlBlockSupport.addressComponent(this.configuration.address()),
                        this.powered,
                        this.primary,
                        this.emergencyLatched
                ),
                false
        );
        player.displayClientMessage(
                Component.translatable(
                        "zeppelin_must_have.fcn.computer.flight",
                        decimal(this.flight.worldY()),
                        decimal(this.flight.velocityY()),
                        decimal(this.flight.headingDegrees()),
                        decimal(this.flight.pitchDegrees()),
                        decimal(this.flight.rollDegrees()),
                        decimal(this.flight.speed())
                ),
                false
        );
        player.displayClientMessage(
                Component.translatable(
                        "zeppelin_must_have.fcn.computer.structure",
                        decimal(this.flight.mass()),
                        decimal(this.flight.centerOfMassX()),
                        decimal(this.flight.centerOfMassY()),
                        decimal(this.flight.centerOfMassZ()),
                        decimal(this.flight.balloonFillRatio() * 100.0D),
                        decimal(this.flight.balloonLift())
                ),
                false
        );
        player.displayClientMessage(
                Component.translatable(
                        "zeppelin_must_have.fcn.computer.systems",
                        this.systems.activeEngines(),
                        this.systems.engineCount(),
                        this.systems.activeBurners(),
                        this.systems.burnerCount(),
                        this.systems.activeThrusters(),
                        this.systems.thrusterCount()
                ),
                false
        );
    }

    public FlightControlAddress address() {
        return this.configuration.address();
    }

    public AirshipFlightSnapshot flight() {
        return this.flight;
    }

    public FlightSystemsSnapshot systems() {
        return this.systems;
    }

    public FlightControlFrame output() {
        return this.output;
    }

    public boolean isPowered() {
        return this.powered;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    public boolean isEmergencyLatched() {
        return this.emergencyLatched;
    }

    @Override
    public void remove() {
        if (this.level != null && !this.level.isClientSide) {
            FlightControlNetworkManager.withdrawComputer(
                    this.level,
                    this.worldPosition,
                    this.configuration.address()
            );
        }
        super.remove();
    }

    @Override
    public void onChunkUnloaded() {
        if (this.level != null && !this.level.isClientSide) {
            FlightControlNetworkManager.withdrawComputer(
                    this.level,
                    this.worldPosition,
                    this.configuration.address()
            );
        }
        super.onChunkUnloaded();
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        this.configuration.write(tag);
        if (clientPacket) {
            CompoundTag flightTag = new CompoundTag();
            this.flight.write(flightTag);
            tag.put("Flight", flightTag);
            CompoundTag systemsTag = new CompoundTag();
            this.systems.write(systemsTag);
            tag.put("Systems", systemsTag);
            CompoundTag inputTag = new CompoundTag();
            this.input.write(inputTag);
            tag.put("Input", inputTag);
            CompoundTag outputTag = new CompoundTag();
            this.output.write(outputTag);
            tag.put("Output", outputTag);
            tag.putBoolean("Powered", this.powered);
            tag.putBoolean("Primary", this.primary);
            tag.putBoolean("EmergencyLatched", this.emergencyLatched);
        }
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        this.configuration.read(tag);
        if (clientPacket) {
            if (tag.contains("Flight")) this.flight = AirshipFlightSnapshot.read(tag.getCompound("Flight"));
            if (tag.contains("Systems")) this.systems = FlightSystemsSnapshot.read(tag.getCompound("Systems"));
            if (tag.contains("Input")) this.input = FlightControlFrame.read(tag.getCompound("Input"));
            if (tag.contains("Output")) this.output = FlightControlFrame.read(tag.getCompound("Output"));
            this.powered = tag.getBoolean("Powered");
            this.primary = tag.getBoolean("Primary");
            this.emergencyLatched = tag.getBoolean("EmergencyLatched");
        }
        super.read(tag, registries, clientPacket);
    }

    private static String decimal(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
