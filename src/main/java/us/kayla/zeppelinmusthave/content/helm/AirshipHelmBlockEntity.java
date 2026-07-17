package us.kayla.zeppelinmusthave.content.helm;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.integration.AeronauticsFlightStateReader;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

import java.util.List;

public final class AirshipHelmBlockEntity extends SmartBlockEntity
        implements IHaveGoggleInformation {
    private final AirshipHelmTelemetrySampler sampler = new AirshipHelmTelemetrySampler();
    private AirshipFlightSnapshot snapshot = AirshipFlightSnapshot.detached(0L);

    public AirshipHelmBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void initialize() {
        super.initialize();
        this.sampler.reset();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide || !this.sampler.shouldSample()) {
            return;
        }

        AirshipFlightSnapshot next = AeronauticsFlightStateReader.read(
                this.level,
                this.worldPosition
        );
        boolean synchronize = next.materiallyDiffersFrom(this.snapshot)
                || this.sampler.shouldForceSync(this.level.getGameTime());
        this.snapshot = next;
        if (synchronize) {
            this.sendData();
        }
    }

    public AirshipFlightSnapshot getSnapshot() {
        return this.snapshot;
    }

    public void sendStatusTo(Player player) {
        AirshipHelmPresentation.sendStatusTo(this.snapshot, player);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        long gameTime = this.level == null ? this.snapshot.sampledAtGameTime() : this.level.getGameTime();
        return AirshipHelmPresentation.addToGoggleTooltip(
                this.getBlockState(),
                this.snapshot,
                gameTime,
                tooltip,
                isPlayerSneaking
        );
    }

    @Override
    public ItemStack getIcon(boolean isPlayerSneaking) {
        return ZmhBlocks.AIRSHIP_HELM_ITEM.get().getDefaultInstance();
    }

    @Override
    protected void write(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) {
            AirshipHelmStateCodec.writeClientSnapshot(tag, this.snapshot);
        }
    }

    @Override
    protected void read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) {
            this.snapshot = AirshipHelmStateCodec.readClientSnapshot(tag, this.snapshot);
        }
    }
}
