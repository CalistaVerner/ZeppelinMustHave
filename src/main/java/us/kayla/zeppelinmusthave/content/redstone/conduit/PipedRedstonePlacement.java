package us.kayla.zeppelinmusthave.content.redstone.conduit;

import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Predicate;

/** Create-style placement assist for extending conduit runs. */
final class PipedRedstonePlacement {
    private static final int HELPER_ID = PlacementHelpers.register(new Helper());

    private PipedRedstonePlacement() {
    }

    static IPlacementHelper helper() {
        return PlacementHelpers.get(HELPER_ID);
    }

    private static final class Helper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof PipedRedstoneBlock;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.getBlock() instanceof PipedRedstoneBlock;
        }

        @Override
        public PlacementOffset getOffset(
                Player player,
                Level level,
                BlockState state,
                BlockPos pos,
                BlockHitResult hitResult
        ) {
            List<Direction> directions = IPlacementHelper.orderedByDistance(
                    pos,
                    hitResult.getLocation(),
                    direction -> PipedRedstoneBlock.hasPort(state, direction)
            );

            for (Direction direction : directions) {
                int range = AllConfigs.server().equipment.placementAssistRange.get();
                if (player != null) {
                    AttributeInstance reach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
                    if (reach != null
                            && reach.hasModifier(ExtendoGripItem.singleRangeAttributeModifier.id())) {
                        range += 4;
                    }
                }

                BlockPos cursor = pos;
                BlockState cursorState = state;
                int attached = 0;

                while (attached < range && cursorState.getBlock() instanceof PipedRedstoneBlock) {
                    if (!PipedRedstoneBlock.hasPort(cursorState, direction)) {
                        break;
                    }

                    BlockPos nextPos = cursor.relative(direction);
                    BlockState nextState = level.getBlockState(nextPos);
                    if (nextState.getBlock() instanceof PipedRedstoneBlock
                            && PipedRedstoneBlock.hasPort(nextState, direction.getOpposite())) {
                        cursor = nextPos;
                        cursorState = nextState;
                        attached++;
                        continue;
                    }

                    if (!nextState.canBeReplaced()) {
                        break;
                    }

                    Direction extensionDirection = direction;
                    return PlacementOffset.success(nextPos, placedState -> {
                        BlockState result = placedState;
                        for (Direction candidate : Direction.values()) {
                            result = result.setValue(
                                    PipedRedstoneBlock.PROPERTY_BY_DIRECTION.get(candidate),
                                    false
                            );
                        }
                        return result
                                .setValue(
                                        PipedRedstoneBlock.PROPERTY_BY_DIRECTION.get(extensionDirection),
                                        true
                                )
                                .setValue(
                                        PipedRedstoneBlock.PROPERTY_BY_DIRECTION.get(extensionDirection.getOpposite()),
                                        true
                                );
                    });
                }
            }
            return PlacementOffset.fail();
        }
    }
}
