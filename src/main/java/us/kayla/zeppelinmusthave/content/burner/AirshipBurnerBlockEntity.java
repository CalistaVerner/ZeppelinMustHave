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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.data.ZmhLang;

import java.util.List;
import java.util.Locale;

public final class AirshipBurnerBlockEntity extends HotAirBurnerBlockEntity {
    private FuelGrade fuelGrade = FuelGrade.NONE;
    private int remainingFuelTicks;
    private double fuelConsumptionRemainder;
    private boolean creativeFuel;
    private BlockPos castPositionBridge;

    private AirshipBurnerProfile activeProfile;
    private long observedProfileRevision = Long.MIN_VALUE;

    public AirshipBurnerBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
        this.activeProfile = AirshipBurnerProfile.unresolved(profileIdFor(state));
    }

    @Override
    public void initialize() {
        super.initialize();
        this.refreshProfile(true);
        this.updateLitState();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        this.refreshProfile(false);

        if (this.creativeFuel) {
            this.updateLitState();
            return;
        }

        if (this.remainingFuelTicks <= 0 || this.signalStrength <= 0) {
            this.updateLitState();
            return;
        }

        double throttle = this.activeProfile.throttleForSignal(this.signalStrength);
        this.fuelConsumptionRemainder += this.activeProfile.fuelUsePerTickAtFullPower() * throttle;

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
            this.sendData();
        }
    }

    @Override
    public void updateSignal() {
        super.updateSignal();
        this.updateLitState();
    }

    public boolean tryInsertFuel(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !this.profileAllowsOperation()) {
            return false;
        }

        if (AllItems.CREATIVE_BLAZE_CAKE.isIn(stack)) {
            if (this.creativeFuel) {
                return false;
            }
            if (!simulate) {
                this.creativeFuel = true;
                this.fuelGrade = FuelGrade.SUPERHEATED;
                this.remainingFuelTicks = this.activeProfile.fuelCapacityTicks();
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

        int available = this.activeProfile.fuelCapacityTicks() - this.remainingFuelTicks;
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
            return 0.0D;
        }

        double linearThrottle = this.signalStrength / 15.0D;
        if (linearThrottle <= 0.0D) {
            return 0.0D;
        }

        double configuredBaseOutput = super.getGasOutput() / linearThrottle;
        double profiledThrottle = this.activeProfile.throttleForSignal(this.signalStrength);
        return configuredBaseOutput
                * profiledThrottle
                * this.activeProfile.outputMultiplier(this.fuelGrade == FuelGrade.SUPERHEATED);
    }

    @Override
    public boolean canOutputGas() {
        boolean hasFuel = this.creativeFuel || this.remainingFuelTicks > 0;
        boolean profileAvailable = this.isVirtual() || this.profileAllowsOperation();
        return this.signalStrength > 0 && hasFuel && profileAvailable && !this.isRemoved();
    }

    @Override
    public void doRaycast() {
        BlockPos pos = this.getBlockPos();
        int range = this.activeProfile.castRange();
        this.castPositionBridge = this.getRaycastedPosition(
                this.level,
                Vec3.upFromBottomCenterOf(pos, 1.0D),
                Vec3.upFromBottomCenterOf(pos, 1.0D + range)
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
        int capacity = this.activeProfile.fuelCapacityTicks();
        if (capacity <= 0 || this.remainingFuelTicks <= 0) {
            return 0;
        }
        return Mth.clamp(
                (int) Math.ceil(15.0D * this.remainingFuelTicks / capacity),
                1,
                15
        );
    }

    public void sendStatusTo(Player player) {
        player.displayClientMessage(
                Component.translatable(
                                "message.zeppelin_must_have.burner.status",
                                Component.translatable(this.getBlockState().getBlock().getDescriptionId()),
                                this.creativeFuel
                                        ? Component.translatable("message.zeppelin_must_have.burner.infinite")
                                        : Component.literal(formatSeconds(this.remainingFuelTicks)),
                                Component.translatable(this.fuelGrade.translationKey),
                                decimal(this.getGasOutput(), 1),
                                this.activeProfile.castRange()
                        )
                        .withStyle(this.canOutputGas() ? ChatFormatting.GOLD : ChatFormatting.GRAY),
                false
        );
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean upstreamInformation = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (!upstreamInformation) {
            ZmhLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip, 1);
        } else {
            ZmhLang.emptyLine(tooltip);
        }

        ZmhLang.translate("goggles.burner.fuel_system").forGoggles(tooltip, 1);

        Component fuelValue = this.creativeFuel
                ? ZmhLang.translate("goggles.value.infinite").style(ChatFormatting.LIGHT_PURPLE).component()
                : Component.literal(formatSeconds(this.remainingFuelTicks)).withStyle(ChatFormatting.GOLD);
        ZmhLang.translate("goggles.burner.fuel", fuelValue)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        Component grade = ZmhLang.translate(this.fuelGrade.goggleTranslationKey)
                .style(this.fuelGrade == FuelGrade.SUPERHEATED ? ChatFormatting.GOLD : ChatFormatting.GRAY)
                .component();
        ZmhLang.translate("goggles.burner.grade", grade)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        double throttlePercent = this.activeProfile.throttleForSignal(this.signalStrength) * 100.0D;
        ZmhLang.translate(
                        "goggles.burner.throttle",
                        Component.literal(this.signalStrength + " / 15").withStyle(ChatFormatting.RED),
                        Component.literal(decimal(throttlePercent, 0) + "%").withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        ZmhLang.translate(
                        "goggles.burner.output",
                        Component.literal(decimal(this.getGasOutput(), 2) + " m³").withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        if (isPlayerSneaking) {
            ZmhLang.emptyLine(tooltip);
            ZmhLang.translate("goggles.burner.profile").forGoggles(tooltip, 1);
            ZmhLang.translate(
                            "goggles.burner.profile_id",
                            Component.literal(this.activeProfile.id().toString()).withStyle(ChatFormatting.DARK_AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
            ZmhLang.translate(
                            "goggles.burner.capacity",
                            Component.literal(formatSeconds(this.activeProfile.fuelCapacityTicks()))
                                    .withStyle(ChatFormatting.GOLD)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
            ZmhLang.translate(
                            "goggles.burner.range",
                            Component.literal(Integer.toString(this.activeProfile.castRange()))
                                    .withStyle(ChatFormatting.GOLD)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
            ZmhLang.translate(
                            "goggles.burner.fuel_rate",
                            Component.literal(decimal(
                                    this.activeProfile.fuelUsePerTickAtFullPower()
                                            * this.activeProfile.throttleForSignal(this.signalStrength),
                                    2
                            )).withStyle(ChatFormatting.GOLD)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
        }

        return true;
    }

    @Override
    public ItemStack getIcon(boolean isPlayerSneaking) {
        return new ItemStack(this.getBlockState().getBlock().asItem());
    }

    /**
     * Configures a virtual block entity for a Ponder scene without touching
     * server mechanics or profile data.
     */
    public void configurePonderPreview(int signal, boolean superheated) {
        this.markVirtual();
        this.setSignalStrength(Mth.clamp(signal, 0, 15));
        this.powered = signal > 0;
        this.creativeFuel = true;
        this.fuelGrade = superheated ? FuelGrade.SUPERHEATED : FuelGrade.NORMAL;
        this.remainingFuelTicks = Math.max(1, this.activeProfile.fuelCapacityTicks());
    }

    public AirshipBurnerProfile getActiveProfile() {
        return this.activeProfile;
    }

    public int getRemainingFuelTicks() {
        return this.remainingFuelTicks;
    }

    public double getFuelRatio() {
        if (this.creativeFuel) {
            return 1.0D;
        }
        return this.activeProfile.fuelCapacityTicks() <= 0
                ? 0.0D
                : Mth.clamp(
                        this.remainingFuelTicks / (double) this.activeProfile.fuelCapacityTicks(),
                        0.0D,
                        1.0D
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

        if (clientPacket) {
            CompoundTag profileTag = new CompoundTag();
            this.activeProfile.writeClientSnapshot(profileTag);
            tag.put("ResolvedBurnerProfile", profileTag);
        }

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
        this.fuelConsumptionRemainder = Math.max(0.0D, tag.getDouble("FuelConsumptionRemainder"));
        this.creativeFuel = tag.getBoolean("CreativeFuel");

        if (clientPacket && tag.contains("ResolvedBurnerProfile")) {
            this.activeProfile = AirshipBurnerProfile.readClientSnapshot(
                    tag.getCompound("ResolvedBurnerProfile")
            );
        }

        super.read(tag, registries, clientPacket);
    }

    private void refreshProfile(boolean forceSync) {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        long currentRevision = AirshipBurnerProfiles.INSTANCE.revision();
        if (!forceSync && currentRevision == this.observedProfileRevision) {
            return;
        }

        AirshipBurnerProfile next = AirshipBurnerProfiles.INSTANCE.resolve(this.getTier());
        boolean changed = !next.equals(this.activeProfile);
        this.activeProfile = next;
        this.observedProfileRevision = currentRevision;
        this.remainingFuelTicks = next.clampFuelTicks(this.remainingFuelTicks);

        if (!changed && !forceSync) {
            return;
        }

        if (!this.canOutputGas()) {
            this.removeFromBalloon();
        } else if (!this.isVirtual()) {
            this.tickBalloonLogic();
        }

        this.updateLitState();
        this.setChanged();
        this.sendData();
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
    }

    private AirshipBurnerTier getTier() {
        if (this.getBlockState().getBlock() instanceof AirshipBurnerBlock burner) {
            return burner.tier();
        }
        return AirshipBurnerTier.STANDARD;
    }

    private boolean profileAllowsOperation() {
        return this.activeProfile.gasOutputMultiplier() > 0.0D;
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

    private static net.minecraft.resources.ResourceLocation profileIdFor(BlockState state) {
        if (state.getBlock() instanceof AirshipBurnerBlock burner) {
            return burner.tier().profileId();
        }
        return AirshipBurnerTier.STANDARD.profileId();
    }

    private static FuelGrade parseFuelGrade(String value) {
        try {
            return FuelGrade.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return FuelGrade.NONE;
        }
    }

    private static String formatSeconds(int ticks) {
        return String.format(Locale.ROOT, "%.1f s", ticks / 20.0D);
    }

    private static String decimal(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }

    private enum FuelGrade {
        NONE(
                "message.zeppelin_must_have.burner.fuel.none",
                "goggles.burner.grade.none"
        ),
        NORMAL(
                "message.zeppelin_must_have.burner.fuel.normal",
                "goggles.burner.grade.normal"
        ),
        SUPERHEATED(
                "message.zeppelin_must_have.burner.fuel.superheated",
                "goggles.burner.grade.superheated"
        );

        private final String translationKey;
        private final String goggleTranslationKey;

        FuelGrade(String translationKey, String goggleTranslationKey) {
            this.translationKey = translationKey;
            this.goggleTranslationKey = goggleTranslationKey;
        }
    }

    private record FuelOffer(FuelGrade grade, int burnTicks) {
        private static FuelOffer from(ItemStack stack) {
            Holder<Item> holder = stack.getItemHolder();
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
