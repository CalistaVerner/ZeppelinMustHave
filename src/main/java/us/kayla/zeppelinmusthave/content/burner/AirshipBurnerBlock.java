package us.kayla.zeppelinmusthave.content.burner;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlock;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

public final class AirshipBurnerBlock extends HotAirBurnerBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private final AirshipBurnerTier tier;

    public AirshipBurnerBlock(Properties properties, AirshipBurnerTier tier) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
    }

    public AirshipBurnerTier tier() {
        return this.tier;
    }

    public static int getLightPower(BlockState state) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    public BlockEntityType<? extends HotAirBurnerBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.AIRSHIP_BURNER.get();
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
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AirshipBurnerBlockEntity burner
                && burner.tryInsertFuel(stack, true)) {
            if (!level.isClientSide && burner.tryInsertFuel(stack, false) && !player.isCreative()) {
                ItemStack container = stack.hasCraftingRemainingItem()
                        ? stack.getCraftingRemainingItem()
                        : ItemStack.EMPTY;
                stack.shrink(1);

                if (!container.isEmpty()
                        && !player.getInventory().add(container)) {
                    player.drop(container, false);
                }
            }
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

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AirshipBurnerBlockEntity burner) {
            burner.sendStatusTo(player);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof AirshipBurnerBlockEntity burner
                ? burner.getFuelComparatorSignal()
                : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }
}
