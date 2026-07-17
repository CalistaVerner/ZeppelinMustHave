package us.kayla.zeppelinmusthave.content.boiler;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FluidTankItem variant that uses the grade-specific block entity type while
 * batch-placing complete layers of an existing boiler multiblock.
 */
public final class BoilerGradeItem extends FluidTankItem {
    public BoilerGradeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (result.consumesAction()) {
            this.tryGradeMultiPlace(context);
        }
        return result;
    }

    private void tryGradeMultiPlace(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return;
        }

        Direction face = context.getClickedFace();
        if (!face.getAxis().isVertical() || SymmetryWandItem.presentInHotbar(player)) {
            return;
        }
        if (!(this.getBlock() instanceof BoilerGradeBlock boilerBlock)) {
            return;
        }

        Level level = context.getLevel();
        BlockPos placedPos = context.getClickedPos();
        BlockPos placedOnPos = placedPos.relative(face.getOpposite());
        BlockState placedOnState = level.getBlockState(placedOnPos);
        if (!placedOnState.is(this.getBlock())) {
            return;
        }

        FluidTankBlockEntity tankAt = ConnectivityHandler.partAt(
                boilerBlock.getBlockEntityType(),
                level,
                placedOnPos
        );
        if (tankAt == null) {
            return;
        }

        FluidTankBlockEntity controller = tankAt.getControllerBE();
        if (controller == null || controller.getWidth() == 1) {
            return;
        }

        int width = controller.getWidth();
        BlockPos startPos = face == Direction.DOWN
                ? controller.getBlockPos().below()
                : controller.getBlockPos().above(controller.getHeight());
        if (startPos.getY() != placedPos.getY()) {
            return;
        }

        int blocksToPlace = 0;
        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos targetPos = startPos.offset(xOffset, 0, zOffset);
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.is(this.getBlock())) {
                    continue;
                }
                if (!targetState.canBeReplaced()) {
                    return;
                }
                blocksToPlace++;
            }
        }

        ItemStack stack = context.getItemInHand();
        if (!player.isCreative() && stack.getCount() < blocksToPlace) {
            return;
        }

        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos targetPos = startPos.offset(xOffset, 0, zOffset);
                if (level.getBlockState(targetPos).is(this.getBlock())) {
                    continue;
                }

                BlockPlaceContext placement = BlockPlaceContext.at(context, targetPos, face);
                player.getPersistentData().putBoolean("SilenceTankSound", true);
                try {
                    super.place(placement);
                } finally {
                    player.getPersistentData().remove("SilenceTankSound");
                }
            }
        }
    }
}
