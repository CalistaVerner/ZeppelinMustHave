package us.kayla.zeppelinmusthave.content.control.fcn;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Persistent vessel-wide emergency latch state. Command frames remain transient. */
final class FlightControlEmergencySavedData extends SavedData {
    private static final String DATA_NAME = "zeppelin_must_have_flight_control_emergency";
    private static final String VESSELS_KEY = "LatchedVessels";
    private static final String VESSEL_ID_KEY = "VesselId";
    private static final Factory<FlightControlEmergencySavedData> FACTORY = new Factory<>(
            FlightControlEmergencySavedData::new,
            FlightControlEmergencySavedData::load
    );

    private final Set<UUID> latchedVessels = new HashSet<>();

    private FlightControlEmergencySavedData() {
    }

    static FlightControlEmergencySavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    boolean isLatched(UUID vesselId) {
        return this.latchedVessels.contains(vesselId);
    }

    void setLatched(UUID vesselId, boolean latched) {
        boolean changed = latched
                ? this.latchedVessels.add(vesselId)
                : this.latchedVessels.remove(vesselId);
        if (changed) {
            this.setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag vessels = new ListTag();
        for (UUID vesselId : this.latchedVessels) {
            CompoundTag vessel = new CompoundTag();
            vessel.putUUID(VESSEL_ID_KEY, vesselId);
            vessels.add(vessel);
        }
        tag.put(VESSELS_KEY, vessels);
        return tag;
    }

    private static FlightControlEmergencySavedData load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        FlightControlEmergencySavedData data = new FlightControlEmergencySavedData();
        ListTag vessels = tag.getList(VESSELS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < vessels.size(); index++) {
            CompoundTag vessel = vessels.getCompound(index);
            if (vessel.hasUUID(VESSEL_ID_KEY)) {
                data.latchedVessels.add(vessel.getUUID(VESSEL_ID_KEY));
            }
        }
        return data;
    }
}
