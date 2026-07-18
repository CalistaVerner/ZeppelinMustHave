package us.kayla.zeppelinmusthave.content.control.fcn;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

final class FlightControlBlockSupport {
    private FlightControlBlockSupport() {
    }

    static boolean hasControlPower(Level level, BlockPos pos) {
        BlockPos supply = pos.below();
        return level.getSignal(supply, Direction.UP) > 0;
    }

    static boolean configure(
            FlightControlConfiguration configuration,
            ItemStack stack,
            Player player
    ) {
        FlightControlConfiguration.ConfigurationChange change = configuration.configure(stack);
        if (change == FlightControlConfiguration.ConfigurationChange.NONE) {
            return false;
        }
        if (change == FlightControlConfiguration.ConfigurationChange.FREQUENCY) {
            player.displayClientMessage(
                    Component.translatable(
                            "zeppelin_must_have.fcn.message.frequency",
                            configuration.address().frequency()
                    ),
                    true
            );
        } else if (change == FlightControlConfiguration.ConfigurationChange.NETWORK) {
            player.displayClientMessage(
                    Component.translatable(
                            "zeppelin_must_have.fcn.message.network",
                            configuration.address().networkName()
                    ),
                    true
            );
        }
        return true;
    }

    static Component addressComponent(FlightControlAddress address) {
        return Component.literal(address.networkName() + " / " + address.frequency());
    }
}
