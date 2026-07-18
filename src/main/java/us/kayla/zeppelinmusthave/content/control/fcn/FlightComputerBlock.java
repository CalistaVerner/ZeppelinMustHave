package us.kayla.zeppelinmusthave.content.control.fcn;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ItemInteractionResult;
import org.jetbrains.annotations.Nullable;
import us.kayla.zeppelinmusthave.advancement.ZmhAdvancements;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

public final class FlightComputerBlock extends HorizontalDirectionalBlock
        implements IBE<FlightComputerBlockEntity>, IWrenchable {
    public static final MapCodec<FlightComputerBlock> CODEC = simpleCodec(FlightComputerBlock::new);

    public FlightComputerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<FlightComputerBlockEntity> getBlockEntityClass() {
        return FlightComputerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FlightComputerBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.FLIGHT_COMPUTER.get();
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
        if (level.getBlockEntity(pos) instanceof FlightComputerBlockEntity computer
                && computer.configure(stack, player)) {
            if (!level.isClientSide) {
                ZmhAdvancements.activate(player, ZmhAdvancements.FLIGHT_CONTROL_ONLINE);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
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
        return this.onBlockEntityUse(level, pos, computer -> {
            computer.sendStatusTo(player);
            return InteractionResult.CONSUME;
        });
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
