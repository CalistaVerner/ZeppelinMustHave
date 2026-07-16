package us.kayla.zeppelinmusthave.content.helm;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.data.ZmhLang;
import us.kayla.zeppelinmusthave.integration.AeronauticsFlightStateReader;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

import java.util.List;
import java.util.Locale;

public final class AirshipHelmBlockEntity extends SmartBlockEntity
        implements IHaveGoggleInformation {
    private static final int SAMPLE_INTERVAL_TICKS = 5;
    private static final int FORCED_SYNC_INTERVAL_TICKS = 20;

    private AirshipFlightSnapshot snapshot = AirshipFlightSnapshot.detached(0L);
    private int ticksUntilSample;

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
        this.ticksUntilSample = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        if (this.ticksUntilSample > 0) {
            this.ticksUntilSample--;
            return;
        }
        this.ticksUntilSample = SAMPLE_INTERVAL_TICKS - 1;

        AirshipFlightSnapshot next = AeronauticsFlightStateReader.read(this.level, this.worldPosition);
        boolean changed = next.materiallyDiffersFrom(this.snapshot);
        boolean forcedSync = this.level.getGameTime() % FORCED_SYNC_INTERVAL_TICKS == 0;
        this.snapshot = next;

        if (changed || forcedSync) {
            this.sendData();
        }
    }

    public AirshipFlightSnapshot getSnapshot() {
        return this.snapshot;
    }

    public void sendStatusTo(Player player) {
        AirshipFlightSnapshot current = this.snapshot;
        if (!current.attached()) {
            player.displayClientMessage(
                    Component.translatable("message.zeppelin_must_have.helm.detached")
                            .withStyle(ChatFormatting.RED),
                    false
            );
            return;
        }

        player.displayClientMessage(
                Component.translatable(
                                "message.zeppelin_must_have.helm.identity",
                                current.displayName(),
                                current.subLevelId()
                        )
                        .withStyle(ChatFormatting.GOLD),
                false
        );

        player.displayClientMessage(
                Component.translatable(
                        "message.zeppelin_must_have.helm.motion",
                        decimal(current.worldY(), 1),
                        decimal(current.speed(), 2),
                        signedDecimal(current.velocityY(), 2),
                        decimal(current.headingDegrees(), 1),
                        signedDecimal(current.pitchDegrees(), 1),
                        signedDecimal(current.rollDegrees(), 1)
                ),
                false
        );

        player.displayClientMessage(
                Component.translatable(
                        "message.zeppelin_must_have.helm.physics",
                        decimal(current.mass(), 1),
                        decimal(current.angularVelocityX(), 3),
                        decimal(current.angularVelocityY(), 3),
                        decimal(current.angularVelocityZ(), 3)
                ),
                false
        );

        player.displayClientMessage(
                Component.translatable(
                        "message.zeppelin_must_have.helm.aeronautics",
                        current.balloonCount(),
                        current.balloonCapacity(),
                        decimal(current.balloonFilledVolume(), 1),
                        decimal(current.balloonFillRatio() * 100.0D, 1),
                        decimal(current.balloonLift(), 1)
                ),
                false
        );
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        AirshipFlightSnapshot current = this.snapshot;
        ZmhLang.blockName(this.getBlockState()).text(":").forGoggles(tooltip, 1);

        if (!current.attached()) {
            ZmhLang.translate("goggles.helm.detached")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 2);
            return true;
        }

        ZmhLang.translate(
                        "goggles.helm.airship",
                        Component.literal(current.displayName()).withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        ZmhLang.translate("goggles.helm.flight").forGoggles(tooltip, 1);
        ZmhLang.translate(
                        "goggles.helm.altitude",
                        Component.literal(decimal(current.worldY(), 1) + " m")
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.helm.speed",
                        Component.literal(decimal(current.speed(), 2) + " m/s")
                                .withStyle(ChatFormatting.AQUA),
                        Component.literal(signedDecimal(current.velocityY(), 2) + " m/s")
                                .withStyle(current.velocityY() >= 0.0D
                                        ? ChatFormatting.GREEN
                                        : ChatFormatting.RED)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.helm.attitude",
                        Component.literal(decimal(current.headingDegrees(), 1) + "°")
                                .withStyle(ChatFormatting.GOLD),
                        Component.literal(signedDecimal(current.pitchDegrees(), 1) + "°")
                                .withStyle(ChatFormatting.AQUA),
                        Component.literal(signedDecimal(current.rollDegrees(), 1) + "°")
                                .withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        ZmhLang.emptyLine(tooltip);
        ZmhLang.translate("goggles.helm.aeronautics").forGoggles(tooltip, 1);
        ZmhLang.translate(
                        "goggles.helm.mass",
                        Component.literal(decimal(current.mass(), 1)).withStyle(ChatFormatting.GOLD)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.helm.balloons",
                        Component.literal(Integer.toString(current.balloonCount())).withStyle(ChatFormatting.AQUA),
                        Component.literal(Integer.toString(current.balloonCapacity())).withStyle(ChatFormatting.DARK_AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.helm.fill",
                        Component.literal(decimal(current.balloonFilledVolume(), 1) + " m³")
                                .withStyle(ChatFormatting.AQUA),
                        Component.literal(decimal(current.balloonTargetVolume(), 1) + " m³")
                                .withStyle(ChatFormatting.GOLD),
                        Component.literal(decimal(current.balloonFillRatio() * 100.0D, 1) + "%")
                                .withStyle(ChatFormatting.DARK_AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);
        ZmhLang.translate(
                        "goggles.helm.lift",
                        Component.literal(decimal(current.balloonLift(), 1)).withStyle(ChatFormatting.AQUA)
                )
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        if (isPlayerSneaking) {
            ZmhLang.emptyLine(tooltip);
            ZmhLang.translate("goggles.helm.diagnostics").forGoggles(tooltip, 1);
            ZmhLang.translate(
                            "goggles.helm.sublevel",
                            Component.literal(String.valueOf(current.subLevelId()))
                                    .withStyle(ChatFormatting.DARK_AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
            ZmhLang.translate(
                            "goggles.helm.position",
                            Component.literal(
                                    decimal(current.worldX(), 1) + ", "
                                            + decimal(current.worldY(), 1) + ", "
                                            + decimal(current.worldZ(), 1)
                            ).withStyle(ChatFormatting.GOLD)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
            ZmhLang.translate(
                            "goggles.helm.angular_velocity",
                            Component.literal(
                                    decimal(current.angularVelocityX(), 3) + ", "
                                            + decimal(current.angularVelocityY(), 3) + ", "
                                            + decimal(current.angularVelocityZ(), 3)
                                            + " rad/s"
                            ).withStyle(ChatFormatting.AQUA)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);

            long sampleAge = this.level == null
                    ? 0L
                    : Math.max(0L, this.level.getGameTime() - current.sampledAtGameTime());
            ZmhLang.translate(
                            "goggles.helm.sample_age",
                            Component.literal(sampleAge + " t").withStyle(ChatFormatting.DARK_GRAY)
                    )
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 2);
        }

        return true;
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
            CompoundTag telemetry = new CompoundTag();
            this.snapshot.write(telemetry);
            tag.put("AirshipTelemetry", telemetry);
        }
    }

    @Override
    protected void read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket
    ) {
        super.read(tag, registries, clientPacket);
        if (clientPacket && tag.contains("AirshipTelemetry")) {
            this.snapshot = AirshipFlightSnapshot.read(tag.getCompound("AirshipTelemetry"));
        }
    }

    private static String decimal(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }

    private static String signedDecimal(double value, int precision) {
        return String.format(Locale.ROOT, "%+." + precision + "f", value);
    }
}
