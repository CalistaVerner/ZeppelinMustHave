package us.kayla.zeppelinmusthave.content.ballast;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** Owns fluid, profile capacity, persistence, and derived storage metrics. */
final class BallastTankStorage {
    private static final String FLUID_NBT_KEY = "BallastFluid";

    private final Runnable contentsChanged;
    private final FluidTank tank;
    private BallastTankProfile profile = BallastTankProfile.unresolved(BallastTankProfiles.DEFAULT_ID);
    private long observedProfileRevision = Long.MIN_VALUE;
    private boolean loading;

    BallastTankStorage(Runnable contentsChanged) {
        this.contentsChanged = contentsChanged;
        this.tank = new FluidTank(8_000, stack -> stack.is(FluidTags.WATER)) {
            @Override
            protected void onContentsChanged() {
                if (!BallastTankStorage.this.loading) {
                    BallastTankStorage.this.contentsChanged.run();
                }
            }
        };
    }

    IFluidHandler handler() {
        return this.tank;
    }

    FluidTank tank() {
        return this.tank;
    }

    BallastTankProfile profile() {
        return this.profile;
    }

    double ballastMassKg() {
        return this.profile.massForAmount(this.tank.getFluidAmount());
    }

    double fillRatio() {
        return this.tank.getCapacity() <= 0
                ? 0.0D
                : this.tank.getFluidAmount() / (double) this.tank.getCapacity();
    }

    int comparatorSignal() {
        if (this.tank.isEmpty()) {
            return 0;
        }
        return Math.clamp((int) Math.floor(this.fillRatio() * 14.0D) + 1, 0, 15);
    }

    boolean refreshProfile(boolean force) {
        long revision = BallastTankProfiles.INSTANCE.revision();
        if (!force && revision == this.observedProfileRevision) {
            return false;
        }
        this.profile = BallastTankProfiles.INSTANCE.resolveDefault();
        this.observedProfileRevision = revision;
        this.applyProfileCapacity();
        return true;
    }

    void write(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        CompoundTag fluidTag = new CompoundTag();
        this.tank.writeToNBT(registries, fluidTag);
        tag.put(FLUID_NBT_KEY, fluidTag);
        if (clientPacket) {
            this.profile.writeClientSnapshot(tag);
        }
    }

    void read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        this.loading = true;
        try {
            if (tag.contains(FLUID_NBT_KEY)) {
                this.tank.readFromNBT(registries, tag.getCompound(FLUID_NBT_KEY));
            }
            if (clientPacket && tag.contains("BallastProfileId")) {
                this.profile = BallastTankProfile.readClientSnapshot(
                        tag,
                        BallastTankProfiles.DEFAULT_ID
                );
                this.applyProfileCapacity();
            }
        } finally {
            this.loading = false;
        }
    }

    private void applyProfileCapacity() {
        int capacity = this.profile.capacityMb();
        this.tank.setCapacity(capacity);
        FluidStack fluid = this.tank.getFluid();
        if (!fluid.isEmpty() && fluid.getAmount() > capacity) {
            this.loading = true;
            try {
                this.tank.setFluid(fluid.copyWithAmount(capacity));
            } finally {
                this.loading = false;
            }
        }
    }
}
