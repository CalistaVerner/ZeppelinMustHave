package us.kayla.zeppelinmusthave.content.control.fcn;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Resolves world positions to their owning Sable vessel without leaking Sable types into routing state. */
final class FlightControlVesselResolver {
    private FlightControlVesselResolver() {
    }

    @Nullable
    static ResolvedVessel resolve(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        SubLevel containing = Sable.HELPER.getContaining(level, pos);
        if (!(containing instanceof ServerSubLevel serverSubLevel)) {
            return null;
        }
        return new ResolvedVessel(
                serverLevel.getServer(),
                serverSubLevel.getUniqueId(),
                serverSubLevel.getName()
        );
    }

    record ResolvedVessel(MinecraftServer server, UUID vesselId, String vesselName) {
    }
}
