package us.kayla.zeppelinmusthave.content.control.fcn;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
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

public final class ControlReceiverBlock extends HorizontalDirectionalBlock
        implements IBE<ControlReceiverBlockEntity>, IWrenchable {
    public static final MapCodec<ControlReceiverBlock> CODEC = simpleCodec(ControlReceiverBlock::new);

    public ControlReceiverBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<ControlReceiverBlockEntity> getBlockEntityClass() {
        return ControlReceiverBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ControlReceiverBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.CONTROL_RECEIVER.get();
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
        if (level.getBlockEntity(pos) instanceof ControlReceiverBlockEntity receiver
                && receiver.configure(stack, player)) {
            if (!level.isClientSide) {
                ZmhAdvancements.activate(player, ZmhAdvancements.CONTROL_LINK_CONFIGURED);
            }
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
        return this.onBlockEntityUse(level, pos, receiver -> {
            receiver.sendStatusTo(player);
            return InteractionResult.CONSUME;
        });
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Player player = context.getPlayer();
        if (!context.getLevel().isClientSide && player != null) {
            this.withBlockEntityDo(
                    context.getLevel(),
                    context.getClickedPos(),
                    receiver -> receiver.cycleChannel(player)
            );
            IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        if (state.getValue(FACING).getOpposite() != side) return 0;
        return level.getBlockEntity(pos) instanceof ControlReceiverBlockEntity receiver
                ? receiver.analogOutput()
                : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return this.getSignal(state, level, pos, side);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return side != null && side.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
