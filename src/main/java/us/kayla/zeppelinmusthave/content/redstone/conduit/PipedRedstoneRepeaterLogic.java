package us.kayla.zeppelinmusthave.content.redstone.conduit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class PipedRedstoneRepeaterLogic {
    private PipedRedstoneRepeaterLogic() {
    }

    static BlockState placementState(BlockState defaultState, BlockPlaceContext context) {
        return PipedRedstoneWaterlogging.applyPlacement(
                defaultState.setValue(PipedRedstoneRepeaterBlock.FACING, context.getClickedFace()),
                PipedRedstoneRepeaterBlock.WATERLOGGED,
                context
        );
    }

    static InteractionResult cycleDelay(
            PipedRedstoneRepeaterBlock block,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player
    ) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }

        BlockState nextState = state.cycle(PipedRedstoneRepeaterBlock.DELAY);
        int redstoneTicks = nextState.getValue(PipedRedstoneRepeaterBlock.DELAY);
        int gameTicks = redstoneTicks * 2;
        if (!level.isClientSide) {
            level.setBlock(pos, nextState, Block.UPDATE_ALL);
            level.playSound(
                    null,
                    pos,
                    SoundEvents.COMPARATOR_CLICK,
                    SoundSource.BLOCKS,
                    0.35F,
                    0.55F + redstoneTicks * 0.1F
            );
            player.displayClientMessage(
                    Component.translatable(
                            "message.zeppelin_must_have.piped_redstone.repeater_delay",
                            redstoneTicks,
                            gameTicks
                    ),
                    true
            );
            schedule(block, level, pos, nextState);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    static void schedule(
            PipedRedstoneRepeaterBlock block,
            Level level,
            BlockPos pos,
            BlockState state
    ) {
        if (level.isClientSide || level.getBlockTicks().hasScheduledTick(pos, block)) {
            return;
        }
        level.scheduleTick(
                pos,
                block,
                state.getValue(PipedRedstoneRepeaterBlock.DELAY) * 2
        );
    }

    static void applyScheduledTick(
            PipedRedstoneRepeaterBlock block,
            BlockState state,
            ServerLevel level,
            BlockPos pos
    ) {
        Direction outputDirection = state.getValue(PipedRedstoneRepeaterBlock.FACING);
        BlockPos inputPos = pos.relative(outputDirection.getOpposite());
        int inputPower = level.getSignal(inputPos, outputDirection.getOpposite());
        if (state.getValue(PipedRedstoneRepeaterBlock.POWER) == inputPower) {
            return;
        }

        level.setBlock(
                pos,
                state.setValue(PipedRedstoneRepeaterBlock.POWER, inputPower),
                Block.UPDATE_CLIENTS
        );
        level.neighborChanged(pos.relative(outputDirection), block, pos);
    }

    static int signal(BlockState state, Direction side) {
        return state.getValue(PipedRedstoneRepeaterBlock.FACING).getOpposite() == side
                ? state.getValue(PipedRedstoneRepeaterBlock.POWER)
                : 0;
    }

    static boolean canConnect(BlockState state, Direction side) {
        if (side == null) {
            return false;
        }
        Direction facing = state.getValue(PipedRedstoneRepeaterBlock.FACING);
        return side == facing || side == facing.getOpposite();
    }
}
