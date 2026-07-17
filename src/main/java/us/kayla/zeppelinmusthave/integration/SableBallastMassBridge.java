package us.kayla.zeppelinmusthave.integration;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

/** Applies fluid ballast as dynamic Sable mass without replacing block mass. */
public final class SableBallastMassBridge {
    private static final double EPSILON = 1.0E-6D;

    private SableBallastMassBridge() {
    }

    public static Binding reconcile(
            Level level,
            BlockPos pos,
            BlockState state,
            Binding previous,
            double desiredMass
    ) {
        if (!(level instanceof ServerLevel)) {
            return previous;
        }

        ServerSubLevel target = containingSubLevel(level, pos);
        Binding current = previous;

        if (current.subLevel() != null && current.subLevel() != target) {
            remove(current, state, pos);
            current = Binding.EMPTY;
        }

        if (target == null || target.isRemoved()) {
            return Binding.EMPTY;
        }

        double clampedDesired = Math.max(0.0D, desiredMass);
        if (Math.abs(clampedDesired - current.mass()) <= EPSILON) {
            return new Binding(target, clampedDesired);
        }

        try {
            MassTracker tracker = target.getSelfMassTracker();
            // Sable expects the absolute extra mass for this block. It rebuilds
            // base block mass + supplied addition and diffs that against the
            // value already stored in the tracker.
            tracker.addBlockMass(target.getLevel(), state, pos, clampedDesired, null);
            return new Binding(target, clampedDesired);
        } catch (RuntimeException exception) {
            ZeppelinMustHave.LOGGER.debug(
                    "Ballast mass tracker is not ready for {} at {}",
                    target.getUniqueId(),
                    pos,
                    exception
            );
            return current;
        }
    }

    public static Binding release(Binding binding, BlockState state, BlockPos pos) {
        remove(binding, state, pos);
        return Binding.EMPTY;
    }

    private static void remove(Binding binding, BlockState state, BlockPos pos) {
        ServerSubLevel subLevel = binding.subLevel();
        if (subLevel == null || subLevel.isRemoved() || binding.mass() <= EPSILON) {
            return;
        }
        try {
            // Zero restores this position to its normal block mass.
            subLevel.getSelfMassTracker().addBlockMass(
                    subLevel.getLevel(),
                    state,
                    pos,
                    0.0D,
                    null
            );
        } catch (RuntimeException exception) {
            ZeppelinMustHave.LOGGER.debug(
                    "Unable to release ballast mass from {} at {}",
                    subLevel.getUniqueId(),
                    pos,
                    exception
            );
        }
    }

    private static @Nullable ServerSubLevel containingSubLevel(Level level, BlockPos pos) {
        SubLevel containing = Sable.HELPER.getContaining(level, pos);
        return containing instanceof ServerSubLevel serverSubLevel ? serverSubLevel : null;
    }

    public record Binding(@Nullable ServerSubLevel subLevel, double mass) {
        public static final Binding EMPTY = new Binding(null, 0.0D);
    }
}
