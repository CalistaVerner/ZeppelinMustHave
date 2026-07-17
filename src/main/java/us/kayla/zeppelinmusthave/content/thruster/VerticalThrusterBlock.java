package us.kayla.zeppelinmusthave.content.thruster;

import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

public final class VerticalThrusterBlock extends BasePropellerBlock {
    public VerticalThrusterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends BasePropellerBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.VERTICAL_THRUSTER.get();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        Direction facing;
        if (clicked.getAxis().isVertical()) {
            facing = clicked;
        } else {
            facing = context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
                    ? Direction.DOWN
                    : Direction.UP;
        }
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(REVERSED, false);
    }
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Direction facing = state.getValue(FACING);
        BlockState next = context.getClickedFace() == facing
                ? state.cycle(REVERSED)
                : state.setValue(FACING, facing.getOpposite());
        context.getLevel().setBlock(context.getClickedPos(), next, 3);
        IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
        return InteractionResult.SUCCESS;
    }

}
