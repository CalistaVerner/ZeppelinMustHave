package us.kayla.zeppelinmusthave.content.burner;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/** Mutable stratified heat state. Persistence and transfer DTOs live separately. */
public final class AirshipHeatReservoir {
    private int regularTicks;
    private int superheatedTicks;
    private double consumptionRemainder;
    private boolean infinite;
    private AirshipHeatGrade infiniteGrade = AirshipHeatGrade.SUPERHEATED;

    public AirshipHeatInsertionResult insert(
            AirshipHeatSource source,
            int capacity,
            boolean simulate
    ) {
        int safeCapacity = Math.max(1, capacity);
        if (!source.infinite() && this.infinite) {
            return AirshipHeatInsertionResult.REJECTED;
        }

        if (source.infinite()) {
            if (this.infinite && this.infiniteGrade == source.grade()) {
                return AirshipHeatInsertionResult.REJECTED;
            }
            if (!simulate) {
                this.infinite = true;
                this.infiniteGrade = source.grade();
            }
            return new AirshipHeatInsertionResult(true, 0, source.grade(), true);
        }

        int accepted = Math.min(safeCapacity - this.totalTicks(), source.burnTicks());
        if (accepted <= 0) {
            return AirshipHeatInsertionResult.REJECTED;
        }

        if (!simulate) {
            this.infinite = false;
            if (source.grade() == AirshipHeatGrade.SUPERHEATED) {
                this.superheatedTicks += accepted;
            } else {
                this.regularTicks += accepted;
            }
        }
        return new AirshipHeatInsertionResult(true, accepted, source.grade(), false);
    }

    public AirshipHeatConsumptionResult consume(double requestedTicks) {
        AirshipHeatGrade gradeBefore = this.activeGrade();
        if (this.infinite || requestedTicks <= 0.0D || !this.hasHeat()) {
            return AirshipHeatConsumptionResult.unchanged(gradeBefore, this.activeGrade());
        }

        this.consumptionRemainder += requestedTicks;
        int wholeTicks = (int) Math.floor(this.consumptionRemainder);
        if (wholeTicks <= 0) {
            return AirshipHeatConsumptionResult.unchanged(gradeBefore, this.activeGrade());
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
        return new AirshipHeatConsumptionResult(
                consumed > 0,
                requested,
                consumed,
                gradeBefore,
                gradeAfter,
                gradeBefore != gradeAfter,
                !this.hasHeat()
        );
    }

    /** Applies a reloaded capacity while preferentially retaining hotter heat. */
    public boolean clampToCapacity(int capacity) {
        int excess = this.totalTicks() - Math.max(1, capacity);
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

    public AirshipHeatSnapshot snapshot(int capacity) {
        return new AirshipHeatSnapshot(
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
        AirshipHeatReservoirCodec.write(tag, this.persistentState());
    }

    public void read(CompoundTag tag) {
        this.restore(AirshipHeatReservoirCodec.read(tag));
    }

    private AirshipHeatReservoirState persistentState() {
        return new AirshipHeatReservoirState(
                this.regularTicks,
                this.superheatedTicks,
                this.consumptionRemainder,
                this.infinite,
                this.infiniteGrade
        );
    }

    private void restore(AirshipHeatReservoirState state) {
        this.regularTicks = state.regularTicks();
        this.superheatedTicks = state.superheatedTicks();
        this.consumptionRemainder = state.consumptionRemainder();
        this.infinite = state.infinite();
        this.infiniteGrade = state.infiniteGrade();
    }
}
