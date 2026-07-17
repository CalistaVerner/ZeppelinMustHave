package us.kayla.zeppelinmusthave.content.burner;

import net.minecraft.nbt.CompoundTag;

final class AirshipHeatReservoirCodec {
    private AirshipHeatReservoirCodec() {
    }

    static void write(CompoundTag tag, AirshipHeatReservoirState state) {
        tag.putInt("RegularHeatTicks", state.regularTicks());
        tag.putInt("SuperheatedHeatTicks", state.superheatedTicks());
        tag.putDouble("HeatConsumptionRemainder", state.consumptionRemainder());
        tag.putBoolean("InfiniteHeat", state.infinite());
        tag.putString("InfiniteHeatGrade", state.infiniteGrade().name());
    }

    static AirshipHeatReservoirState read(CompoundTag tag) {
        if (tag.contains("RegularHeatTicks") || tag.contains("SuperheatedHeatTicks")) {
            return new AirshipHeatReservoirState(
                    tag.getInt("RegularHeatTicks"),
                    tag.getInt("SuperheatedHeatTicks"),
                    tag.getDouble("HeatConsumptionRemainder"),
                    tag.getBoolean("InfiniteHeat"),
                    parseGrade(tag.getString("InfiniteHeatGrade"), AirshipHeatGrade.SUPERHEATED)
            );
        }

        int legacyTicks = Math.max(0, tag.getInt("RemainingFuelTicks"));
        AirshipHeatGrade legacyGrade = parseLegacyGrade(tag.getString("FuelGrade"));
        return new AirshipHeatReservoirState(
                legacyGrade == AirshipHeatGrade.SUPERHEATED ? 0 : legacyTicks,
                legacyGrade == AirshipHeatGrade.SUPERHEATED ? legacyTicks : 0,
                tag.getDouble("FuelConsumptionRemainder"),
                tag.getBoolean("CreativeFuel"),
                legacyGrade == AirshipHeatGrade.NONE
                        ? AirshipHeatGrade.SUPERHEATED
                        : legacyGrade
        );
    }

    private static AirshipHeatGrade parseGrade(String value, AirshipHeatGrade fallback) {
        try {
            return AirshipHeatGrade.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static AirshipHeatGrade parseLegacyGrade(String value) {
        return switch (value) {
            case "SUPERHEATED" -> AirshipHeatGrade.SUPERHEATED;
            case "NORMAL" -> AirshipHeatGrade.REGULAR;
            default -> AirshipHeatGrade.NONE;
        };
    }
}
