package us.kayla.zeppelinmusthave.content.burner;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.datamaps.BlazeBurnerFuel;
import com.simibubi.create.api.registry.CreateDataMaps;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public final class AirshipBurnerBlockEntity extends HotAirBurnerBlockEntity {
    private FuelGrade fuelGrade = FuelGrade.NONE;
    private int remainingFuelTicks;
    private double fuelConsumptionRemainder;
    private boolean creativeFuel;
    private BlockPos castPositionBridge;

    public AirshipBurnerBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.updateLitState();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        if (this.creativeFuel) {
            this.updateLitState();
            return;
        }

        if (this.remainingFuelTicks <= 0 || this.signalStrength <= 0) {
            this.updateLitState();
            return;
        }

        AirshipBurnerTier tier = this.getTier();
        this.fuelConsumptionRemainder += tier.fuelUsePerTickAtFullPower()
                * (this.signalStrength / 15.0);

        int wholeTicks = (int) Math.floor(this.fuelConsumptionRemainder);
        if (wholeTicks <= 0) {
            return;
        }

        this.fuelConsumptionRemainder -= wholeTicks;
        this.remainingFuelTicks = Math.max(0, this.remainingFuelTicks - wholeTicks);

        if (this.remainingFuelTicks == 0) {
            this.fuelGrade = FuelGrade.NONE;
            this.removeFromBalloon();
            this.level.playSound(
                    null,
                    this.worldPosition,
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    0.35F,
                    0.8F
            );
            this.syncFuelState();
        } else if (this.level.getGameTime() % 20L == 0L) {
            this.setChanged();
        }
    }

    @Override
    public void updateSignal() {
        super.updateSignal();
        this.updateLitState();
    }

    public boolean tryInsertFuel(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return false;
        }

        if (AllItems.CREATIVE_BLAZE_CAKE.isIn(stack)) {
            if (this.creativeFuel) {
                return false;
            }
            if (!simulate) {
                this.creativeFuel = true;
                this.fuelGrade = FuelGrade.SUPERHEATED;
                this.remainingFuelTicks = this.getTier().fuelCapacityTicks();
                this.onFuelInserted(true);
            }
            return true;
        }

        FuelOffer offer = FuelOffer.from(stack);
        if (offer.grade == FuelGrade.NONE || offer.burnTicks <= 0) {
            return false;
        }
        if (offer.grade.ordinal() < this.fuelGrade.ordinal()) {
            return false;
        }

        int capacity = this.getTier().fuelCapacityTicks();
        int available = capacity - this.remainingFuelTicks;
        if (available <= 0) {
            return false;
        }

        if (!simulate) {
            this.creativeFuel = false;
            this.fuelGrade = offer.grade;
            this.remainingFuelTicks += Math.min(available, offer.burnTicks);
            this.onFuelInserted(offer.grade == FuelGrade.SUPERHEATED);
        }
        return true;
    }

    private void onFuelInserted(boolean superheated) {
        if (this.level == null) {
            return;
        }

        this.level.playSound(
                null,
                this.worldPosition,
                superheated ? SoundEvents.BLAZE_SHOOT : SoundEvents.FIRECHARGE_USE,
                SoundSource.BLOCKS,
                0.35F,
                superheated ? 1.15F : 0.9F
        );

        if (!this.level.isClientSide && !this.isVirtual() && this.canOutputGas()) {
            this.tickBalloonLogic();
        }
        this.syncFuelState();
    }

    @Override
    public double getGasOutput() {
        if (!this.canOutputGas()) {
            return 0.0;
        }
        return super.getGasOutput()
                * this.getTier().gasOutputMultiplier()
                * this.fuelGrade.outputMultiplier;
    }

    @Override
    public boolean canOutputGas() {
        return this.signalStrength > 0
                && (this.creativeFuel || this.remainingFuelTicks > 0)
                && !this.isRemoved();
    }

    @Override
    public void doRaycast() {
        BlockPos pos = this.getBlockPos();
        int range = this.getTier().castRange();
        this.castPositionBridge = this.getRaycastedPosition(
                this.level,
                Vec3.upFromBottomCenterOf(pos, 1.0),
                Vec3.upFromBottomCenterOf(pos, 1.0 + range)
        );
    }

    @Override
    public BlockPos getCastPosition() {
        return this.castPositionBridge;
    }

    public int getFuelComparatorSignal() {
        if (this.creativeFuel) {
            return 15;
        }
        int capacity = this.getTier().fuelCapacityTicks();
        if (capacity <= 0 || this.remainingFuelTicks <= 0) {
            return 0;
        }
        return Math.clamp(
                (int) Math.ceil(15.0 * this.remainingFuelTicks / capacity),
                1,
                15
        );
    }

    public void sendStatusTo(Player player) {
        AirshipBurnerTier tier = this.getTier();
        player.displayClientMessage(
                Component.translatable(
                                "message.zeppelin_must_have.burner.status",
                                Component.translatable(this.getBlockState().getBlock().getDescriptionId()),
                                this.creativeFuel
                                        ? Component.translatable("message.zeppelin_must_have.burner.infinite")
                                        : Component.literal(formatSeconds(this.remainingFuelTicks)),
                                Component.translatable(this.fuelGrade.translationKey),
                                decimal(this.getGasOutput(), 1),
                                tier.castRange()
                        )
                        .withStyle(this.canOutputGas() ? ChatFormatting.GOLD : ChatFormatting.GRAY),
                false
        );
    }

    @Override
    public void write(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        tag.putString("FuelGrade", this.fuelGrade.name());
        tag.putInt("RemainingFuelTicks", this.remainingFuelTicks);
        tag.putDouble("FuelConsumptionRemainder", this.fuelConsumptionRemainder);
        tag.putBoolean("CreativeFuel", this.creativeFuel);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        this.fuelGrade = parseFuelGrade(tag.getString("FuelGrade"));
        this.remainingFuelTicks = Math.max(0, tag.getInt("RemainingFuelTicks"));
        this.fuelConsumptionRemainder = Math.max(0.0, tag.getDouble("FuelConsumptionRemainder"));
        this.creativeFuel = tag.getBoolean("CreativeFuel");
        super.read(tag, registries, clientPacket);
    }

    private AirshipBurnerTier getTier() {
        if (this.getBlockState().getBlock() instanceof AirshipBurnerBlock burner) {
            return burner.tier();
        }
        return AirshipBurnerTier.STANDARD;
    }

    private void syncFuelState() {
        this.updateLitState();
        this.setChanged();
        this.sendData();
        if (this.level != null) {
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    private void updateLitState() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        BlockState state = this.getBlockState();
        if (!state.hasProperty(AirshipBurnerBlock.LIT)) {
            return;
        }

        boolean lit = this.canOutputGas();
        if (state.getValue(AirshipBurnerBlock.LIT) != lit) {
            this.level.setBlock(this.worldPosition, state.setValue(AirshipBurnerBlock.LIT, lit), 3);
        }
    }

    private static FuelGrade parseFuelGrade(String value) {
        try {
            return FuelGrade.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return FuelGrade.NONE;
        }
    }

    private static String formatSeconds(int ticks) {
        return String.format(Locale.ROOT, "%.1f s", ticks / 20.0);
    }

    private static String decimal(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }

    private enum FuelGrade {
        NONE(0.0, "message.zeppelin_must_have.burner.fuel.none"),
        NORMAL(1.0, "message.zeppelin_must_have.burner.fuel.normal"),
        SUPERHEATED(1.5, "message.zeppelin_must_have.burner.fuel.superheated");

        private final double outputMultiplier;
        private final String translationKey;

        FuelGrade(double outputMultiplier, String translationKey) {
            this.outputMultiplier = outputMultiplier;
            this.translationKey = translationKey;
        }
    }

    private record FuelOffer(FuelGrade grade, int burnTicks) {
        private static FuelOffer from(ItemStack stack) {
            Holder<Item> holder = stack.getItem().builtInRegistryHolder();
            BlazeBurnerFuel superheated = holder.getData(CreateDataMaps.SUPERHEATED_BLAZE_BURNER_FUELS);
            if (superheated != null) {
                return new FuelOffer(FuelGrade.SUPERHEATED, superheated.burnTime());
            }

            BlazeBurnerFuel regular = holder.getData(CreateDataMaps.REGULAR_BLAZE_BURNER_FUELS);
            if (regular != null) {
                return new FuelOffer(FuelGrade.NORMAL, regular.burnTime());
            }

            int burnTime = stack.getBurnTime(null);
            if (burnTime > 0) {
                return new FuelOffer(FuelGrade.NORMAL, burnTime);
            }

            return new FuelOffer(FuelGrade.NONE, 0);
        }
    }
}
