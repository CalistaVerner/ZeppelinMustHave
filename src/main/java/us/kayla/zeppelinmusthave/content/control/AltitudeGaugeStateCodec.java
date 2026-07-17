package us.kayla.zeppelinmusthave.content.control;

import net.minecraft.nbt.CompoundTag;
import us.kayla.zeppelinmusthave.content.helm.AirshipFlightSnapshot;

final class AltitudeGaugeStateCodec {
    private static final String TELEMETRY_KEY = "FlightTelemetry";

    private AltitudeGaugeStateCodec() {
    }

    static void write(
            CompoundTag tag,
            AltitudeGaugeRuntimeState.PersistentState state,
            boolean clientPacket
    ) {
        tag.putString("Mode", state.mode().name());
        tag.putBoolean("AltitudeHoldEnabled", state.altitudeHoldEnabled());
        tag.putDouble("TargetAltitude", state.targetAltitude());
        tag.putInt("TrimInput", state.trimInput());
        tag.putInt("OutputSignal", state.outputSignal());
        if (clientPacket) {
            CompoundTag telemetry = new CompoundTag();
            state.snapshot().write(telemetry);
            tag.put(TELEMETRY_KEY, telemetry);
        }
    }

    static AltitudeGaugeRuntimeState.PersistentState read(
            CompoundTag tag,
            AirshipFlightSnapshot previousSnapshot,
            boolean clientPacket
    ) {
        AirshipFlightSnapshot snapshot = clientPacket && tag.contains(TELEMETRY_KEY)
                ? AirshipFlightSnapshot.read(tag.getCompound(TELEMETRY_KEY))
                : previousSnapshot;
        return new AltitudeGaugeRuntimeState.PersistentState(
                AltitudeGaugeMode.parse(tag.getString("Mode")),
                tag.getBoolean("AltitudeHoldEnabled"),
                tag.getDouble("TargetAltitude"),
                tag.getInt("TrimInput"),
                tag.getInt("OutputSignal"),
                snapshot
        );
    }
}
