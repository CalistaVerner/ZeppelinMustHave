package us.kayla.zeppelinmusthave.content.helm;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

public final class AirshipHelmBlock extends HorizontalDirectionalBlock
        implements IBE<AirshipHelmBlockEntity>, IWrenchable {
    public static final MapCodec<AirshipHelmBlock> CODEC = simpleCodec(AirshipHelmBlock::new);

    public AirshipHelmBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<AirshipHelmBlockEntity> getBlockEntityClass() {
        return AirshipHelmBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AirshipHelmBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.AIRSHIP_HELM.get();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }
}
