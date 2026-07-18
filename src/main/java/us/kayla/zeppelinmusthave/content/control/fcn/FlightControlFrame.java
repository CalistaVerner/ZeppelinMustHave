package us.kayla.zeppelinmusthave.content.control.fcn;

import net.minecraft.nbt.CompoundTag;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Immutable server-authored command frame. */
public final class FlightControlFrame {
    private final EnumMap<FlightControlChannel, Integer> values;
    private final long sampledAtGameTime;

    private FlightControlFrame(Map<FlightControlChannel, Integer> values, long sampledAtGameTime) {
        this.values = new EnumMap<>(FlightControlChannel.class);
        for (FlightControlChannel channel : FlightControlChannel.values()) {
            this.values.put(channel, channel.clamp(values.getOrDefault(channel, 0)));
        }
        this.sampledAtGameTime = sampledAtGameTime;
    }

    public static FlightControlFrame zero(long gameTime) {
        return new FlightControlFrame(Map.of(), gameTime);
    }

    public static FlightControlFrame of(Map<FlightControlChannel, Integer> values, long gameTime) {
        return new FlightControlFrame(values, gameTime);
    }

    public int get(FlightControlChannel channel) {
        return this.values.getOrDefault(channel, 0);
    }

    public long sampledAtGameTime() {
        return this.sampledAtGameTime;
    }

    public Map<FlightControlChannel, Integer> values() {
        return Collections.unmodifiableMap(this.values);
    }

    public FlightControlFrame with(FlightControlChannel channel, int value) {
        EnumMap<FlightControlChannel, Integer> next = new EnumMap<>(this.values);
        next.put(channel, channel.clamp(value));
        return new FlightControlFrame(next, this.sampledAtGameTime);
    }

    public boolean materiallyDiffersFrom(FlightControlFrame other) {
        return other == null || !this.values.equals(other.values);
    }

    public void write(CompoundTag tag) {
        CompoundTag valuesTag = new CompoundTag();
        for (FlightControlChannel channel : FlightControlChannel.values()) {
            valuesTag.putInt(channel.name(), this.get(channel));
        }
        tag.put("Values", valuesTag);
        tag.putLong("SampledAtGameTime", this.sampledAtGameTime);
    }

    public static FlightControlFrame read(CompoundTag tag) {
        CompoundTag valuesTag = tag.getCompound("Values");
        EnumMap<FlightControlChannel, Integer> values = new EnumMap<>(FlightControlChannel.class);
        for (FlightControlChannel channel : FlightControlChannel.values()) {
            values.put(channel, valuesTag.getInt(channel.name()));
        }
        return new FlightControlFrame(values, tag.getLong("SampledAtGameTime"));
    }
}
