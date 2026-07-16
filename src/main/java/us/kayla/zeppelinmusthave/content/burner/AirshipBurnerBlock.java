package us.kayla.zeppelinmusthave.content.burner;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlock;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeItem;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSet;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;
import us.kayla.zeppelinmusthave.registry.ZmhTags;

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
        if ((stack.getItem() instanceof AirshipUpgradeItem
                || stack.is(ZmhTags.Items.AIRSHIP_UPGRADES))
                && blockEntity instanceof AirshipBurnerBlockEntity burner) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            AirshipUpgradeSet.InstallResult result = burner.tryInstallUpgrade(stack, false);
            if (result.installed()) {
                Component upgradeName = stack.getHoverName().copy();
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                IWrenchable.playRotateSound(level, pos);
                player.displayClientMessage(
                        Component.translatable(
                                "message.zeppelin_must_have.upgrade.installed",
                                Component.translatable(
                                        result.definition().slot().translationKey()
                                ),
                                upgradeName
                        ),
                        false
                );
            } else {
                player.displayClientMessage(
                        Component.translatable(installFailureKey(result.status())),
                        false
                );
            }
            return ItemInteractionResult.SUCCESS;
        }

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
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (!level.isClientSide
                && level.getBlockEntity(pos) instanceof AirshipBurnerBlockEntity burner) {
            ItemStack removed = burner.removeLastUpgrade();
            if (!removed.isEmpty()) {
                if (player == null || !player.getInventory().add(removed)) {
                    Block.popResource(level, pos, removed);
                }
                IWrenchable.playRemoveSound(level, pos);
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.zeppelin_must_have.upgrade.removed",
                                    removed.getHoverName()
                            ),
                            false
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.onSneakWrenched(state, context);
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())
                && !level.isClientSide
                && level.getBlockEntity(pos) instanceof AirshipBurnerBlockEntity burner) {
            burner.extractAllUpgrades()
                    .forEach(stack -> Block.popResource(level, pos, stack));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
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

    private static String installFailureKey(AirshipUpgradeSet.InstallStatus status) {
        return switch (status) {
            case MISSING_DEFINITION -> "message.zeppelin_must_have.upgrade.missing_definition";
            case WRONG_TARGET -> "message.zeppelin_must_have.upgrade.wrong_target";
            case SLOT_OCCUPIED -> "message.zeppelin_must_have.upgrade.slot_occupied";
            case CONFLICT -> "message.zeppelin_must_have.upgrade.conflict";
            case INSTALLED -> "message.zeppelin_must_have.upgrade.installed";
        };
    }
}
