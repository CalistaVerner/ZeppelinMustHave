package us.kayla.zeppelinmusthave.content.control.fcn;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import us.kayla.zeppelinmusthave.advancement.ZmhAdvancements;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

public final class EmergencyCutoffBlock extends Block
        implements IBE<EmergencyCutoffBlockEntity>, IWrenchable {
    public static final BooleanProperty LATCHED = BooleanProperty.create("latched");
    public static final MapCodec<EmergencyCutoffBlock> CODEC = simpleCodec(EmergencyCutoffBlock::new);

    public EmergencyCutoffBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LATCHED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public Class<EmergencyCutoffBlockEntity> getBlockEntityClass() {
        return EmergencyCutoffBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EmergencyCutoffBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.EMERGENCY_CUTOFF.get();
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
        return this.onBlockEntityUse(level, pos, cutoff -> {
            if (cutoff.isLatched() && player.isShiftKeyDown()) {
                cutoff.reset(player);
            } else if (!cutoff.isLatched()) {
                cutoff.activate(player);
                ZmhAdvancements.activate(player, ZmhAdvancements.EMERGENCY_CUTOFF_LATCHED);
            } else {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "zeppelin_must_have.fcn.emergency.manual_reset"
                        ),
                        false
                );
            }
            return InteractionResult.CONSUME;
        });
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return state.getValue(LATCHED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return this.getSignal(state, level, pos, side);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LATCHED);
    }
}
