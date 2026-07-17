package us.kayla.zeppelinmusthave.content.ballast;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

import java.util.List;

public final class BallastTankBlockEntity extends SmartBlockEntity
        implements IHaveGoggleInformation {
    private final BallastTankStorage storage;
    private final BallastTankMassController massController = new BallastTankMassController();

    public BallastTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.storage = new BallastTankStorage(this::onTankContentsChanged);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ZmhBlockEntityTypes.BALLAST_TANK.get(),
                (blockEntity, context) -> blockEntity.fluidHandler()
        );
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void initialize() {
        super.initialize();
        this.refreshProfile(true);
        this.reconcileMass();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.refreshProfile(false);
        this.reconcileMass();
    }

    @Override
    public void remove() {
        this.releaseDynamicMass();
        super.remove();
    }

    @Override
    public void destroy() {
        this.releaseDynamicMass();
        super.destroy();
    }

    @Override
    public void onChunkUnloaded() {
        this.releaseDynamicMass();
        super.onChunkUnloaded();
    }

    public IFluidHandler fluidHandler() {
        return this.storage.handler();
    }

    public FluidTank tank() {
        return this.storage.tank();
    }

    public BallastTankProfile activeProfile() {
        return this.storage.profile();
    }

    public double getBallastMassKg() {
        return this.storage.ballastMassKg();
    }

    public double getFillRatio() {
        return this.storage.fillRatio();
    }

    public int getComparatorSignal() {
        return this.storage.comparatorSignal();
    }

    public void sendStatusTo(Player player) {
        BallastTankPresentation.sendStatusTo(this.storage, player);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return BallastTankPresentation.addToGoggleTooltip(
                this.getBlockState(),
                this.storage,
                tooltip,
                isPlayerSneaking
        );
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        this.storage.write(tag, registries, clientPacket);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        this.storage.read(tag, registries, clientPacket);
        super.read(tag, registries, clientPacket);
    }

    private void onTankContentsChanged() {
        this.setChanged();
        if (this.level == null) {
            return;
        }

        this.level.updateNeighbourForOutputSignal(
                this.worldPosition,
                this.getBlockState().getBlock()
        );
        if (!this.level.isClientSide) {
            this.reconcileMass();
            this.sendData();
        }
    }

    private void refreshProfile(boolean force) {
        if (this.level == null || this.level.isClientSide || !this.storage.refreshProfile(force)) {
            return;
        }
        this.setChanged();
        this.sendData();
    }

    private void reconcileMass() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.massController.reconcile(
                this.level,
                this.worldPosition,
                this.getBlockState(),
                this.storage.ballastMassKg()
        );
    }

    private void releaseDynamicMass() {
        this.massController.release(this.getBlockState(), this.worldPosition);
    }
}
