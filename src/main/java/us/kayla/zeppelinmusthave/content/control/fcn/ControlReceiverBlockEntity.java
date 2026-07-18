package us.kayla.zeppelinmusthave.content.control.fcn;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Receives one FCN lane and exposes it as redstone or a direct actuator command. */
public final class ControlReceiverBlockEntity extends SmartBlockEntity {
    private final FlightControlConfiguration configuration = new FlightControlConfiguration();
    private boolean powered;
    private boolean online;
    private boolean emergencyLatched;
    private int command;
    private int analogOutput;

    public ControlReceiverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide) return;

        boolean nextPowered = FlightControlBlockSupport.hasControlPower(this.level, this.worldPosition);
        FlightControlNetworkManager.NetworkOutput network = nextPowered
                ? FlightControlNetworkManager.readOutput(
                        this.level,
                        this.worldPosition,
                        this.configuration.address()
                )
                : null;
        boolean nextOnline = network != null && network.computerOnline();
        boolean nextEmergency = network != null && network.emergencyLatched();
        int nextCommand = nextOnline && !nextEmergency
                ? network.frame().get(this.configuration.channel())
                : 0;
        int nextAnalog = nextPowered
                ? this.configuration.channel().toAnalogSignal(nextCommand)
                : 0;
        if (nextEmergency) nextAnalog = 0;

        this.applyActuator(nextCommand, nextPowered, nextOnline, nextEmergency);

        boolean changed = this.powered != nextPowered
                || this.online != nextOnline
                || this.emergencyLatched != nextEmergency
                || this.command != nextCommand
                || this.analogOutput != nextAnalog;
        this.powered = nextPowered;
        this.online = nextOnline;
        this.emergencyLatched = nextEmergency;
        this.command = nextCommand;
        if (this.analogOutput != nextAnalog) {
            this.analogOutput = nextAnalog;
            Direction facing = this.getBlockState().getValue(ControlReceiverBlock.FACING);
            this.level.neighborChanged(
                    this.worldPosition.relative(facing),
                    this.getBlockState().getBlock(),
                    this.worldPosition
            );
        }
        if (changed || this.level.getGameTime() % 20L == 0L) {
            this.setChanged();
            this.sendData();
        }
    }

    private void applyActuator(int value, boolean receiverPowered, boolean networkOnline, boolean emergency) {
        if (this.level == null) return;
        Direction facing = this.getBlockState().getValue(ControlReceiverBlock.FACING);
        BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(facing));
        if (target instanceof FlightControlActuator actuator) {
            if (receiverPowered && (networkOnline || emergency)) {
                actuator.applyFlightControl(this.configuration.channel(), value, emergency);
            } else {
                actuator.clearFlightControl(this.configuration.channel());
            }
        }
    }

    public boolean configure(ItemStack stack, Player player) {
        FlightControlChannel previousChannel = this.configuration.channel();
        if (!FlightControlBlockSupport.configure(this.configuration, stack, player)) return false;
        this.clearActuator(previousChannel);
        this.setChanged();
        this.sendData();
        return true;
    }

    public void cycleChannel(Player player) {
        this.clearActuator();
        this.configuration.cycleChannel();
        player.displayClientMessage(Component.translatable(this.configuration.channel().translationKey()), true);
        this.setChanged();
        this.sendData();
    }

    public void sendStatusTo(Player player) {
        player.displayClientMessage(
                Component.translatable(
                        "zeppelin_must_have.fcn.receiver.status",
                        FlightControlBlockSupport.addressComponent(this.configuration.address()),
                        Component.translatable(this.configuration.channel().translationKey()),
                        this.powered,
                        this.online,
                        this.command,
                        this.analogOutput
                ),
                false
        );
    }

    public int analogOutput() {
        return this.analogOutput;
    }

    public FlightControlConfiguration configuration() {
        return this.configuration;
    }

    @Override
    public void remove() {
        this.clearActuator();
        super.remove();
    }

    @Override
    public void onChunkUnloaded() {
        this.clearActuator();
        super.onChunkUnloaded();
    }

    private void clearActuator() {
        this.clearActuator(this.configuration.channel());
    }

    private void clearActuator(FlightControlChannel channel) {
        if (this.level == null) return;
        Direction facing = this.getBlockState().getValue(ControlReceiverBlock.FACING);
        BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(facing));
        if (target instanceof FlightControlActuator actuator) {
            actuator.clearFlightControl(channel);
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        this.configuration.write(tag);
        if (clientPacket) {
            tag.putBoolean("Powered", this.powered);
            tag.putBoolean("Online", this.online);
            tag.putBoolean("EmergencyLatched", this.emergencyLatched);
            tag.putInt("Command", this.command);
            tag.putInt("AnalogOutput", this.analogOutput);
        }
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        this.configuration.read(tag);
        if (clientPacket) {
            this.powered = tag.getBoolean("Powered");
            this.online = tag.getBoolean("Online");
            this.emergencyLatched = tag.getBoolean("EmergencyLatched");
            this.command = tag.getInt("Command");
            this.analogOutput = tag.getInt("AnalogOutput");
        }
        super.read(tag, registries, clientPacket);
    }
}
