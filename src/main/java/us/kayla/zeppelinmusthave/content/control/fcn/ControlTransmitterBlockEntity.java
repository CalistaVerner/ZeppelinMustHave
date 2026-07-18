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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Bridges a local analog signal into one vessel-local FCN command lane. */
public final class ControlTransmitterBlockEntity extends SmartBlockEntity {
    private final FlightControlConfiguration configuration = new FlightControlConfiguration();
    private boolean powered;
    private int inputSignal;
    private int command;
    private boolean published;

    public ControlTransmitterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide) return;

        Direction facing = this.getBlockState().getValue(ControlTransmitterBlock.FACING);
        Direction inputDirection = facing.getOpposite();
        BlockPos inputPos = this.worldPosition.relative(inputDirection);
        int nextSignal = this.level.getSignal(inputPos, inputDirection);
        int nextCommand = this.configuration.channel().fromAnalogSignal(nextSignal);
        boolean nextPowered = FlightControlBlockSupport.hasControlPower(this.level, this.worldPosition);
        boolean emergency = FlightControlNetworkManager.isEmergencyLatched(this.level, this.worldPosition);

        if (nextPowered && !emergency) {
            this.published = FlightControlNetworkManager.publishInput(
                    this.level,
                    this.worldPosition,
                    this.configuration.address(),
                    this.configuration.channel(),
                    nextCommand,
                    FlightControlAuthority.AUTOMATIC
            );
        } else if (this.published) {
            this.clearPublished(this.configuration.address(), this.configuration.channel());
        }

        if (this.powered != nextPowered || this.inputSignal != nextSignal || this.command != nextCommand) {
            this.powered = nextPowered;
            this.inputSignal = nextSignal;
            this.command = nextCommand;
            this.setChanged();
            this.sendData();
        }
    }

    public boolean configure(ItemStack stack, Player player) {
        FlightControlAddress previousAddress = this.configuration.address();
        FlightControlChannel previousChannel = this.configuration.channel();
        if (!FlightControlBlockSupport.configure(this.configuration, stack, player)) return false;
        if (this.level != null && !this.level.isClientSide) {
            this.clearPublished(previousAddress, previousChannel);
        }
        this.setChanged();
        this.sendData();
        return true;
    }

    public void cycleChannel(Player player) {
        FlightControlAddress previousAddress = this.configuration.address();
        FlightControlChannel previousChannel = this.configuration.channel();
        if (this.level != null && !this.level.isClientSide) {
            this.clearPublished(previousAddress, previousChannel);
        }
        this.configuration.cycleChannel();
        player.displayClientMessage(Component.translatable(this.configuration.channel().translationKey()), true);
        this.setChanged();
        this.sendData();
    }

    public void sendStatusTo(Player player) {
        player.displayClientMessage(
                Component.translatable(
                        "zeppelin_must_have.fcn.transmitter.status",
                        FlightControlBlockSupport.addressComponent(this.configuration.address()),
                        Component.translatable(this.configuration.channel().translationKey()),
                        this.powered,
                        this.inputSignal,
                        this.command
                ),
                false
        );
    }

    public FlightControlConfiguration configuration() {
        return this.configuration;
    }

    @Override
    public void remove() {
        this.clearPublished(this.configuration.address(), this.configuration.channel());
        super.remove();
    }

    @Override
    public void onChunkUnloaded() {
        this.clearPublished(this.configuration.address(), this.configuration.channel());
        super.onChunkUnloaded();
    }

    private void clearPublished(FlightControlAddress address, FlightControlChannel channel) {
        if (this.level != null && !this.level.isClientSide) {
            FlightControlNetworkManager.clearInput(this.level, this.worldPosition, address, channel);
        }
        this.published = false;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        this.configuration.write(tag);
        if (clientPacket) {
            tag.putBoolean("Powered", this.powered);
            tag.putInt("InputSignal", this.inputSignal);
            tag.putInt("Command", this.command);
        }
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        this.configuration.read(tag);
        if (clientPacket) {
            this.powered = tag.getBoolean("Powered");
            this.inputSignal = tag.getInt("InputSignal");
            this.command = tag.getInt("Command");
        }
        super.read(tag, registries, clientPacket);
    }
}
