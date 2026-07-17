package us.kayla.zeppelinmusthave.content.helm;

import net.minecraft.nbt.CompoundTag;

final class AirshipHelmStateCodec {
    private static final String TELEMETRY_KEY = "AirshipTelemetry";

    private AirshipHelmStateCodec() {
    }

    static void writeClientSnapshot(CompoundTag tag, AirshipFlightSnapshot snapshot) {
        CompoundTag telemetry = new CompoundTag();
        snapshot.write(telemetry);
        tag.put(TELEMETRY_KEY, telemetry);
    }

    static AirshipFlightSnapshot readClientSnapshot(
            CompoundTag tag,
            AirshipFlightSnapshot fallback
    ) {
        return tag.contains(TELEMETRY_KEY)
                ? AirshipFlightSnapshot.read(tag.getCompound(TELEMETRY_KEY))
                : fallback;
    }
}
