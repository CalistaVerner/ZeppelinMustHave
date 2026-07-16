package us.kayla.zeppelinmusthave.content.control;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

/**
 * Directional flight sensor and inline altitude-hold controller.
 *
 * <p>The rear face accepts an analog trim signal. The front face emits either
 * flight telemetry or a corrected burner throttle.</p>
 */
public final class AltitudeGaugeBlock extends HorizontalDirectionalBlock
        implements IBE<AltitudeGaugeBlockEntity>, IWrenchable {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    private final MapCodec<AltitudeGaugeBlock> codec = MapCodec.unit(this);

    public AltitudeGaugeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(POWER, 0)
        );
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return this.codec;
    }

    @Override
    public Class<AltitudeGaugeBlockEntity> getBlockEntityClass() {
        return AltitudeGaugeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AltitudeGaugeBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.ALTITUDE_GAUGE.get();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        return this.onBlockEntityUse(level, pos, blockEntity -> {
            if (player.isShiftKeyDown()) {
                blockEntity.captureOrToggleAltitudeHold(player);
            } else {
                blockEntity.sendStatusTo(player);
            }
            return InteractionResult.CONSUME;
        });
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.isClientSide) {
            this.withBlockEntityDo(level, pos, blockEntity -> blockEntity.cycleMode(context.getPlayer()));
            IWrenchable.playRotateSound(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction side
    ) {
        // Redstone signal directions are reversed by vanilla conventions.
        return state.getValue(FACING).getOpposite() == side
                ? state.getValue(POWER)
                : 0;
    }

    @Override
    public int getDirectSignal(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction side
    ) {
        return this.getSignal(state, level, pos, side);
    }

    @Override
    public boolean canConnectRedstone(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction side
    ) {
        return side != null && side.getAxis() == state.getValue(FACING).getAxis();
    }

    public static int getLightPower(BlockState state) {
        return state.getValue(POWER) > 0 ? 4 : 0;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, POWER);
    }
}
