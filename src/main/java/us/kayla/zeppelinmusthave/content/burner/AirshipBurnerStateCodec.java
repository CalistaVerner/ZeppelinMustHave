package us.kayla.zeppelinmusthave.content.burner;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSet;
import us.kayla.zeppelinmusthave.integration.BalloonHeatAggregate;

/** Persistent and client-synchronized burner state encoding. */
final class AirshipBurnerStateCodec {
    private static final String NETWORK_KEY = "BalloonHeatAggregate";

    private AirshipBurnerStateCodec() {
    }

    static void write(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket,
            AirshipHeatReservoir reservoir,
            AirshipUpgradeSet upgrades,
            AirshipBurnerConfiguration configuration,
            BalloonHeatAggregate network
    ) {
        reservoir.write(tag);
        upgrades.write(tag, registries);
        if (!clientPacket) {
            return;
        }

        configuration.writeClientSnapshot(tag);
        CompoundTag networkTag = new CompoundTag();
        network.write(networkTag);
        tag.put(NETWORK_KEY, networkTag);
    }

    static BalloonHeatAggregate read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket,
            AirshipHeatReservoir reservoir,
            AirshipUpgradeSet upgrades,
            AirshipBurnerConfiguration configuration,
            BalloonHeatAggregate previousNetwork
    ) {
        reservoir.read(tag);
        upgrades.read(tag, registries);
        if (!clientPacket) {
            return previousNetwork;
        }

        configuration.readClientSnapshot(tag);
        return tag.contains(NETWORK_KEY)
                ? BalloonHeatAggregate.read(tag.getCompound(NETWORK_KEY))
                : previousNetwork;
    }
}
