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

import java.util.List;

/** Seven-position mechanical engine-order telegraph. */
public final class EngineTelegraphBlockEntity extends SmartBlockEntity {
    private final FlightControlConfiguration configuration = new FlightControlConfiguration();
    private EngineTelegraphOrder order = EngineTelegraphOrder.STOP;
    private boolean powered;
    private boolean published;

    public EngineTelegraphBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.configuration.setChannel(FlightControlChannel.ENGINE_THROTTLE);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide) return;

        boolean nextPowered = FlightControlBlockSupport.hasControlPower(this.level, this.worldPosition);
        boolean emergency = FlightControlNetworkManager.isEmergencyLatched(this.level, this.worldPosition);
        if (nextPowered && !emergency) {
            this.published = FlightControlNetworkManager.publishInput(
                    this.level,
                    this.worldPosition,
                    this.configuration.address(),
                    FlightControlChannel.ENGINE_THROTTLE,
                    this.order.command(),
                    FlightControlAuthority.MANUAL
            );
        } else if (this.published) {
            FlightControlNetworkManager.clearInput(
                    this.level,
                    this.worldPosition,
                    this.configuration.address(),
                    FlightControlChannel.ENGINE_THROTTLE
            );
            this.published = false;
        }
        if (this.powered != nextPowered || this.level.getGameTime() % 20L == 0L) {
            this.powered = nextPowered;
            this.setChanged();
            this.sendData();
        }
    }

    public void moveHandle(boolean towardAhead, Player player) {
        EngineTelegraphOrder previous = this.order;
        this.order = towardAhead ? this.order.next() : this.order.previous();
        if (previous != this.order) {
            this.setChanged();
            this.sendData();
            if (this.level != null) {
                net.minecraft.core.Direction facing = this.getBlockState()
                        .getValue(EngineTelegraphBlock.FACING);
                this.level.neighborChanged(
                        this.worldPosition.relative(facing),
                        this.getBlockState().getBlock(),
                        this.worldPosition
                );
            }
        }
        player.displayClientMessage(Component.translatable(this.order.translationKey()), true);
    }

    public boolean configure(ItemStack stack, Player player) {
        FlightControlAddress previous = this.configuration.address();
        if (!FlightControlBlockSupport.configure(this.configuration, stack, player)) return false;
        if (this.level != null && !this.level.isClientSide && !previous.equals(this.configuration.address())) {
            FlightControlNetworkManager.clearInput(
                    this.level,
                    this.worldPosition,
                    previous,
                    FlightControlChannel.ENGINE_THROTTLE
            );
        }
        this.setChanged();
        this.sendData();
        return true;
    }

    public EngineTelegraphOrder order() {
        return this.order;
    }

    public FlightControlAddress address() {
        return this.configuration.address();
    }

    public boolean isPowered() {
        return this.powered;
    }

    @Override
    public void remove() {
        this.clearPublished();
        super.remove();
    }

    @Override
    public void onChunkUnloaded() {
        this.clearPublished();
        super.onChunkUnloaded();
    }

    private void clearPublished() {
        if (this.level != null && !this.level.isClientSide) {
            FlightControlNetworkManager.clearInput(
                    this.level,
                    this.worldPosition,
                    this.configuration.address(),
                    FlightControlChannel.ENGINE_THROTTLE
            );
        }
        this.published = false;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        this.configuration.write(tag);
        tag.putInt("Order", this.order.ordinal());
        if (clientPacket) tag.putBoolean("Powered", this.powered);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        this.configuration.read(tag);
        this.configuration.setChannel(FlightControlChannel.ENGINE_THROTTLE);
        this.order = EngineTelegraphOrder.byIndex(tag.getInt("Order"));
        if (clientPacket) this.powered = tag.getBoolean("Powered");
        super.read(tag, registries, clientPacket);
    }
}
