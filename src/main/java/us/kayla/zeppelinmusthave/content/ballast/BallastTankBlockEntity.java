package us.kayla.zeppelinmusthave.content.ballast;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import us.kayla.zeppelinmusthave.data.ZmhLang;
import us.kayla.zeppelinmusthave.integration.SableBallastMassBridge;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

import java.util.List;
import java.util.Locale;

public final class BallastTankBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    private static final String FLUID_NBT_KEY = "BallastFluid";

    private final FluidTank tank = new FluidTank(8_000, stack -> stack.is(FluidTags.WATER)) {
        @Override
        protected void onContentsChanged() {
            BallastTankBlockEntity.this.onTankContentsChanged();
        }
    };

    private BallastTankProfile activeProfile = BallastTankProfile.unresolved(BallastTankProfiles.DEFAULT_ID);
    private long observedProfileRevision = Long.MIN_VALUE;
    private SableBallastMassBridge.Binding massBinding = SableBallastMassBridge.Binding.EMPTY;
    private boolean loading;

    public BallastTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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
        // Remove the contribution even when only this plot chunk unloads; otherwise
        // loading the block entity again would apply the same fluid mass twice.
        this.releaseDynamicMass();
        super.onChunkUnloaded();
    }

    public IFluidHandler fluidHandler() {
        return this.tank;
    }

    public FluidTank tank() {
        return this.tank;
    }

    public BallastTankProfile activeProfile() {
        return this.activeProfile;
    }

    public double getBallastMassKg() {
        return this.activeProfile.massForAmount(this.tank.getFluidAmount());
    }

    public double getFillRatio() {
        return this.tank.getCapacity() <= 0
                ? 0.0D
                : this.tank.getFluidAmount() / (double) this.tank.getCapacity();
    }

    public int getComparatorSignal() {
        if (this.tank.isEmpty()) {
            return 0;
        }
        return Math.clamp((int) Math.floor(this.getFillRatio() * 14.0D) + 1, 0, 15);
    }

    public void sendStatusTo(Player player) {
        player.displayClientMessage(
                Component.translatable(
                        "message.zeppelin_must_have.ballast_tank.status",
                        this.tank.getFluidAmount(),
                        this.tank.getCapacity(),
                        decimal(this.getBallastMassKg(), 1)
                ).withStyle(ChatFormatting.AQUA),
                false
        );
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        ZmhLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip, 1);
        ZmhLang.translate(
                        "goggles.ballast_tank.fluid",
                        Component.literal(this.tank.getFluidAmount() + " / " + this.tank.getCapacity() + " mB")
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.ballast_tank.mass",
                        Component.literal(decimal(this.getBallastMassKg(), 1) + " kg")
                                .withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.ballast_tank.fill",
                        Component.literal(decimal(this.getFillRatio() * 100.0D, 1) + "%")
                                .withStyle(ChatFormatting.GREEN)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        if (isPlayerSneaking) {
            ZmhLang.translate(
                            "goggles.ballast_tank.profile",
                            Component.literal(this.activeProfile.id().toString())
                                    .withStyle(ChatFormatting.DARK_AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
        }
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        CompoundTag fluidTag = new CompoundTag();
        this.tank.writeToNBT(registries, fluidTag);
        tag.put(FLUID_NBT_KEY, fluidTag);
        if (clientPacket) {
            this.activeProfile.writeClientSnapshot(tag);
        }
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        this.loading = true;
        try {
            if (tag.contains(FLUID_NBT_KEY)) {
                this.tank.readFromNBT(registries, tag.getCompound(FLUID_NBT_KEY));
            }
            if (clientPacket && tag.contains("BallastProfileId")) {
                this.activeProfile = BallastTankProfile.readClientSnapshot(
                        tag,
                        BallastTankProfiles.DEFAULT_ID
                );
                this.applyProfileCapacity();
            }
        } finally {
            this.loading = false;
        }
        super.read(tag, registries, clientPacket);
    }

    private void onTankContentsChanged() {
        if (this.loading) {
            return;
        }
        this.setChanged();
        if (this.level != null) {
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
            if (!this.level.isClientSide) {
                this.reconcileMass();
                this.sendData();
            }
        }
    }

    private void refreshProfile(boolean force) {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        long revision = BallastTankProfiles.INSTANCE.revision();
        if (!force && revision == this.observedProfileRevision) {
            return;
        }
        this.activeProfile = BallastTankProfiles.INSTANCE.resolveDefault();
        this.observedProfileRevision = revision;
        this.applyProfileCapacity();
        this.setChanged();
        this.sendData();
    }

    private void applyProfileCapacity() {
        int capacity = this.activeProfile.capacityMb();
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

    private void reconcileMass() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.massBinding = SableBallastMassBridge.reconcile(
                this.level,
                this.worldPosition,
                this.getBlockState(),
                this.massBinding,
                this.getBallastMassKg()
        );
    }

    private void releaseDynamicMass() {
        this.massBinding = SableBallastMassBridge.release(
                this.massBinding,
                this.getBlockState(),
                this.worldPosition
        );
    }

    private static String decimal(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }
}
