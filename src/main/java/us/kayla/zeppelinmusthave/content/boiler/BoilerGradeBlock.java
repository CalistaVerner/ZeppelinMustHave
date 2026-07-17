package us.kayla.zeppelinmusthave.content.boiler;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

/**
 * Heat-transfer base placed between a Create boiler tank and any registered
 * Create BoilerHeater.
 */
public final class BoilerGradeBlock extends Block
        implements IBE<BoilerGradeBlockEntity>, IWrenchable {
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;

    private final BoilerGradeTier tier;
    private final MapCodec<BoilerGradeBlock> codec = MapCodec.unit(this);

    public BoilerGradeBlock(Properties properties, BoilerGradeTier tier) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return this.codec;
    }

    public BoilerGradeTier tier() {
        return this.tier;
    }

    public float getTransferredHeat(Level level, BlockPos pos, BlockState state) {
        BlockPos sourcePos = pos.below();
        BlockState sourceState = level.getBlockState(sourcePos);
        if (sourceState.getBlock() instanceof BoilerGradeBlock) {
            return BoilerHeater.NO_HEAT;
        }

        float sourceHeat = BoilerHeater.findHeat(level, sourcePos, sourceState);
        BoilerGradeProfile profile = BoilerGradeProfiles.INSTANCE.resolve(this.tier);
        return profile.transfer(sourceHeat);
    }

    public static int getLightPower(BlockState state) {
        return state.getValue(ACTIVE) ? 7 : 0;
    }

    @Override
    protected void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston
    ) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock())) {
            notifyBoilerAbove(level, pos);
        }
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston
    ) {
        this.withBlockEntityDo(level, pos, BoilerGradeBlockEntity::requestImmediateSample);
        notifyBoilerAbove(level, pos);
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())) {
            notifyBoilerAbove(level, pos);
            IBE.onRemove(state, level, pos, newState);
            return;
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
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
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BoilerGradeBlockEntity blockEntity
                ? blockEntity.getComparatorSignal()
                : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public Class<BoilerGradeBlockEntity> getBlockEntityClass() {
        return BoilerGradeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BoilerGradeBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.BOILER_GRADE_BASE.get();
    }

    public static void notifyBoilerAbove(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos.above()) instanceof FluidTankBlockEntity tank) {
            tank.updateBoilerTemperature();
        }
    }
}
