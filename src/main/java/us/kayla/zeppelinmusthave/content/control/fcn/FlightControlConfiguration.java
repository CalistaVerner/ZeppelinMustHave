package us.kayla.zeppelinmusthave.content.control.fcn;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Persistent user configuration shared by FCN transmitters and receivers. */
public final class FlightControlConfiguration {
    private FlightControlAddress address = FlightControlAddress.DEFAULT;
    private FlightControlChannel channel = FlightControlChannel.LIFT;

    public FlightControlAddress address() {
        return this.address;
    }

    public FlightControlChannel channel() {
        return this.channel;
    }

    public void setAddress(FlightControlAddress address) {
        this.address = address == null ? FlightControlAddress.DEFAULT : address;
    }

    public void setChannel(FlightControlChannel channel) {
        this.channel = channel == null ? FlightControlChannel.LIFT : channel;
    }

    public void cycleChannel() {
        this.channel = this.channel.next();
    }

    public ConfigurationChange configure(ItemStack stack) {
        if (stack.getItem() instanceof DyeItem dyeItem) {
            int previous = this.address.frequency();
            this.address = this.address.withFrequency(dyeItem.getDyeColor().getId());
            return previous == this.address.frequency()
                    ? ConfigurationChange.UNCHANGED
                    : ConfigurationChange.FREQUENCY;
        }
        if (stack.is(Items.NAME_TAG) && stack.has(DataComponents.CUSTOM_NAME)) {
            String previous = this.address.networkName();
            this.address = this.address.withNetworkName(stack.getHoverName().getString());
            return previous.equals(this.address.networkName())
                    ? ConfigurationChange.UNCHANGED
                    : ConfigurationChange.NETWORK;
        }
        return ConfigurationChange.NONE;
    }

    public void write(CompoundTag tag) {
        CompoundTag addressTag = new CompoundTag();
        this.address.write(addressTag);
        tag.put("Address", addressTag);
        tag.putString("Channel", this.channel.name());
    }

    public void read(CompoundTag tag) {
        if (tag.contains("Address")) {
            this.address = FlightControlAddress.read(tag.getCompound("Address"));
        }
        if (tag.contains("Channel")) {
            try {
                this.channel = FlightControlChannel.valueOf(tag.getString("Channel"));
            } catch (IllegalArgumentException ignored) {
                this.channel = FlightControlChannel.LIFT;
            }
        }
        if (this.channel == FlightControlChannel.EMERGENCY_STOP) {
            this.channel = FlightControlChannel.LIFT;
        }
    }

    public enum ConfigurationChange {
        NONE,
        UNCHANGED,
        NETWORK,
        FREQUENCY
    }
}
