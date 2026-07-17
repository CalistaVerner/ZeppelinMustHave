package us.kayla.zeppelinmusthave.content.ballast;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

public final class BallastTankBlock extends Block implements IBE<BallastTankBlockEntity> {
    private final MapCodec<BallastTankBlock> codec = MapCodec.unit(this);

    public BallastTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return this.codec;
    }

    @Override
    public Class<BallastTankBlockEntity> getBlockEntityClass() {
        return BallastTankBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BallastTankBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.BALLAST_TANK.get();
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
        if (FluidUtil.interactWithFluidHandler(
                player,
                hand,
                level,
                pos,
                hitResult.getDirection()
        )) {
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
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
            blockEntity.sendStatusTo(player);
            return InteractionResult.CONSUME;
        });
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BallastTankBlockEntity blockEntity = this.getBlockEntity(level, pos);
        return blockEntity == null ? 0 : blockEntity.getComparatorSignal();
    }
}
