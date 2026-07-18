package us.kayla.zeppelinmusthave.content.control.fcn;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import us.kayla.zeppelinmusthave.advancement.ZmhAdvancements;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

public final class EngineTelegraphBlock extends HorizontalDirectionalBlock
        implements IBE<EngineTelegraphBlockEntity>, IWrenchable {
    public static final MapCodec<EngineTelegraphBlock> CODEC = simpleCodec(EngineTelegraphBlock::new);

    public EngineTelegraphBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<EngineTelegraphBlockEntity> getBlockEntityClass() {
        return EngineTelegraphBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EngineTelegraphBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.ENGINE_TELEGRAPH.get();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (level.getBlockEntity(pos) instanceof EngineTelegraphBlockEntity telegraph
                && telegraph.configure(stack, player)) {
            return level.isClientSide ? ItemInteractionResult.SUCCESS : ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        return this.onBlockEntityUse(level, pos, telegraph -> {
            telegraph.moveHandle(!player.isShiftKeyDown(), player);
            ZmhAdvancements.activate(player, ZmhAdvancements.ENGINE_ORDER_ISSUED);
            return InteractionResult.CONSUME;
        });
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction side) {
        if (state.getValue(FACING).getOpposite() != side) return 0;
        return level.getBlockEntity(pos) instanceof EngineTelegraphBlockEntity telegraph
                ? FlightControlChannel.ENGINE_THROTTLE.toAnalogSignal(telegraph.order().command())
                : 0;
    }

    @Override
    public int getDirectSignal(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            net.minecraft.core.Direction side
    ) {
        return this.getSignal(state, level, pos, side);
    }

    @Override
    public boolean canConnectRedstone(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            net.minecraft.core.Direction side
    ) {
        return side != null && side.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
