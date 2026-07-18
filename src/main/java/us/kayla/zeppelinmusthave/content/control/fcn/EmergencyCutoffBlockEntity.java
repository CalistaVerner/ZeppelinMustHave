package us.kayla.zeppelinmusthave.content.control.fcn;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Physical vessel-wide emergency trip with a mandatory manual reset. */
public final class EmergencyCutoffBlockEntity extends SmartBlockEntity {
    private boolean latched;

    public EmergencyCutoffBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void tick() {
        super.tick();
        this.synchronizeFromNetwork();
    }

    public void activate(Player player) {
        if (this.level == null || this.level.isClientSide) return;
        boolean accepted = FlightControlNetworkManager.latchEmergency(this.level, this.worldPosition);
        player.displayClientMessage(
                Component.translatable(accepted
                        ? "zeppelin_must_have.fcn.emergency.activated"
                        : "zeppelin_must_have.fcn.emergency.detached"),
                false
        );
        this.synchronizeFromNetwork();
    }

    public void reset(Player player) {
        if (this.level == null || this.level.isClientSide) return;
        boolean accepted = FlightControlNetworkManager.resetEmergency(this.level, this.worldPosition);
        player.displayClientMessage(
                Component.translatable(accepted
                        ? "zeppelin_must_have.fcn.emergency.reset"
                        : "zeppelin_must_have.fcn.emergency.detached"),
                false
        );
        this.synchronizeFromNetwork();
    }

    public boolean isLatched() {
        return this.latched;
    }

    private void synchronizeFromNetwork() {
        if (this.level == null || this.level.isClientSide) return;
        boolean next = FlightControlNetworkManager.isEmergencyLatched(this.level, this.worldPosition);
        if (next == this.latched
                && this.getBlockState().getValue(EmergencyCutoffBlock.LATCHED) == next) {
            return;
        }
        this.latched = next;
        this.level.setBlock(
                this.worldPosition,
                this.getBlockState().setValue(EmergencyCutoffBlock.LATCHED, next),
                Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS
        );
        this.setChanged();
        this.sendData();
    }
}
