package us.kayla.zeppelinmusthave.content.burner;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/**
 * A single stratified heat reservoir shared by every supported fuel source.
 *
 * <p>Regular and superheated contributions may be inserted in any order. The
 * hotter layer is consumed first; when it is exhausted, operation falls back
 * to regular heat without discarding previously inserted fuel.</p>
 */
public final class AirshipHeatReservoir {
    private int regularTicks;
    private int superheatedTicks;
    private double consumptionRemainder;
    private boolean infinite;
    private AirshipHeatGrade infiniteGrade = AirshipHeatGrade.SUPERHEATED;

    public InsertionResult insert(
            AirshipHeatSource source,
            int capacity,
            boolean simulate
    ) {
        int safeCapacity = Math.max(1, capacity);

        if (!source.infinite() && this.infinite) {
            return InsertionResult.REJECTED;
        }

        if (source.infinite()) {
            if (this.infinite && this.infiniteGrade == source.grade()) {
                return InsertionResult.REJECTED;
            }
            if (!simulate) {
                this.infinite = true;
                this.infiniteGrade = source.grade();
            }
            return new InsertionResult(true, 0, source.grade(), true);
        }

        int available = safeCapacity - this.totalTicks();
        int accepted = Math.min(available, source.burnTicks());
        if (accepted <= 0) {
            return InsertionResult.REJECTED;
        }

        if (!simulate) {
            this.infinite = false;
            if (source.grade() == AirshipHeatGrade.SUPERHEATED) {
                this.superheatedTicks += accepted;
            } else {
                this.regularTicks += accepted;
            }
        }

        return new InsertionResult(true, accepted, source.grade(), false);
    }

    public ConsumptionResult consume(double requestedTicks) {
        AirshipHeatGrade gradeBefore = this.activeGrade();
        if (this.infinite || requestedTicks <= 0.0D || !this.hasHeat()) {
            return ConsumptionResult.unchanged(gradeBefore, this.activeGrade());
        }

        this.consumptionRemainder += requestedTicks;
        int wholeTicks = (int) Math.floor(this.consumptionRemainder);
        if (wholeTicks <= 0) {
            return ConsumptionResult.unchanged(gradeBefore, this.activeGrade());
        }

        this.consumptionRemainder -= wholeTicks;
        int requested = wholeTicks;

        int fromSuperheated = Math.min(this.superheatedTicks, wholeTicks);
        this.superheatedTicks -= fromSuperheated;
        wholeTicks -= fromSuperheated;

        int fromRegular = Math.min(this.regularTicks, wholeTicks);
        this.regularTicks -= fromRegular;

        int consumed = fromSuperheated + fromRegular;
        if (!this.hasHeat()) {
            this.consumptionRemainder = 0.0D;
        }

        AirshipHeatGrade gradeAfter = this.activeGrade();
        return new ConsumptionResult(
                consumed > 0,
                requested,
                consumed,
                gradeBefore,
                gradeAfter,
                gradeBefore != gradeAfter,
                !this.hasHeat()
        );
    }

    /**
     * Applies a reloaded profile capacity while preserving the hotter layer.
     */
    public boolean clampToCapacity(int capacity) {
        int safeCapacity = Math.max(1, capacity);
        int excess = this.totalTicks() - safeCapacity;
        if (excess <= 0) {
            return false;
        }

        int removeRegular = Math.min(this.regularTicks, excess);
        this.regularTicks -= removeRegular;
        excess -= removeRegular;

        if (excess > 0) {
            this.superheatedTicks = Math.max(0, this.superheatedTicks - excess);
        }
        if (!this.hasHeat()) {
            this.consumptionRemainder = 0.0D;
        }
        return true;
    }

    public void configureInfinitePreview(AirshipHeatGrade grade) {
        this.infinite = true;
        this.infiniteGrade = grade == AirshipHeatGrade.NONE
                ? AirshipHeatGrade.REGULAR
                : grade;
    }

    public boolean hasHeat() {
        return this.infinite || this.totalTicks() > 0;
    }

    public boolean isInfinite() {
        return this.infinite;
    }

    public AirshipHeatGrade activeGrade() {
        if (this.infinite) {
            return this.infiniteGrade;
        }
        if (this.superheatedTicks > 0) {
            return AirshipHeatGrade.SUPERHEATED;
        }
        if (this.regularTicks > 0) {
            return AirshipHeatGrade.REGULAR;
        }
        return AirshipHeatGrade.NONE;
    }

    public int regularTicks() {
        return this.regularTicks;
    }

    public int superheatedTicks() {
        return this.superheatedTicks;
    }

    public int totalTicks() {
        return this.regularTicks + this.superheatedTicks;
    }

    public double fillRatio(int capacity) {
        if (this.infinite) {
            return 1.0D;
        }
        return Mth.clamp(this.totalTicks() / (double) Math.max(1, capacity), 0.0D, 1.0D);
    }

    public int comparatorSignal(int capacity) {
        if (this.infinite) {
            return 15;
        }
        if (this.totalTicks() <= 0) {
            return 0;
        }
        return Mth.clamp(
                (int) Math.ceil(15.0D * this.totalTicks() / Math.max(1, capacity)),
                1,
                15
        );
    }

    public Snapshot snapshot(int capacity) {
        return new Snapshot(
                this.regularTicks,
                this.superheatedTicks,
                this.totalTicks(),
                Math.max(1, capacity),
                this.fillRatio(capacity),
                this.infinite,
                this.activeGrade()
        );
    }

    public void write(CompoundTag tag) {
        tag.putInt("RegularHeatTicks", this.regularTicks);
        tag.putInt("SuperheatedHeatTicks", this.superheatedTicks);
        tag.putDouble("HeatConsumptionRemainder", this.consumptionRemainder);
        tag.putBoolean("InfiniteHeat", this.infinite);
        tag.putString("InfiniteHeatGrade", this.infiniteGrade.name());
    }

    public void read(CompoundTag tag) {
        if (tag.contains("RegularHeatTicks") || tag.contains("SuperheatedHeatTicks")) {
            this.regularTicks = Math.max(0, tag.getInt("RegularHeatTicks"));
            this.superheatedTicks = Math.max(0, tag.getInt("SuperheatedHeatTicks"));
            this.consumptionRemainder = Math.max(0.0D, tag.getDouble("HeatConsumptionRemainder"));
            this.infinite = tag.getBoolean("InfiniteHeat");
            this.infiniteGrade = parseGrade(
                    tag.getString("InfiniteHeatGrade"),
                    AirshipHeatGrade.SUPERHEATED
            );
            return;
        }

        // Save migration from Zeppelin Must Have 0.4.x.
        int legacyTicks = Math.max(0, tag.getInt("RemainingFuelTicks"));
        AirshipHeatGrade legacyGrade = parseLegacyGrade(tag.getString("FuelGrade"));
        if (legacyGrade == AirshipHeatGrade.SUPERHEATED) {
            this.superheatedTicks = legacyTicks;
        } else {
            this.regularTicks = legacyTicks;
        }
        this.consumptionRemainder = Math.max(0.0D, tag.getDouble("FuelConsumptionRemainder"));
        this.infinite = tag.getBoolean("CreativeFuel");
        this.infiniteGrade = legacyGrade == AirshipHeatGrade.NONE
                ? AirshipHeatGrade.SUPERHEATED
                : legacyGrade;
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

    public record Snapshot(
            int regularTicks,
            int superheatedTicks,
            int totalTicks,
            int capacityTicks,
            double fillRatio,
            boolean infinite,
            AirshipHeatGrade activeGrade
    ) {
    }

    public record InsertionResult(
            boolean accepted,
            int acceptedTicks,
            AirshipHeatGrade grade,
            boolean infinite
    ) {
        private static final InsertionResult REJECTED = new InsertionResult(
                false,
                0,
                AirshipHeatGrade.NONE,
                false
        );
    }

    public record ConsumptionResult(
            boolean changed,
            int requestedTicks,
            int consumedTicks,
            AirshipHeatGrade gradeBefore,
            AirshipHeatGrade gradeAfter,
            boolean gradeChanged,
            boolean depleted
    ) {
        private static ConsumptionResult unchanged(
                AirshipHeatGrade gradeBefore,
                AirshipHeatGrade gradeAfter
        ) {
            return new ConsumptionResult(
                    false,
                    0,
                    0,
                    gradeBefore,
                    gradeAfter,
                    gradeBefore != gradeAfter,
                    gradeAfter == AirshipHeatGrade.NONE
            );
        }
    }
}
