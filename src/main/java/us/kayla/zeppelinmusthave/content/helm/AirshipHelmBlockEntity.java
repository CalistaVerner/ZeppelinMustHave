package us.kayla.zeppelinmusthave.content.helm;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.integration.AeronauticsFlightStateReader;

import java.util.List;
import java.util.Locale;

public final class AirshipHelmBlockEntity extends SmartBlockEntity {
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
                        decimal(current.balloonFillRatio() * 100.0, 1),
                        decimal(current.balloonLift(), 1)
                ),
                false
        );
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
