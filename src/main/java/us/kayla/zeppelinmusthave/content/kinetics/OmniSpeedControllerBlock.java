package us.kayla.zeppelinmusthave.content.kinetics;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

/**
 * Six-port absolute speed controller.
 *
 * <p>Whichever adjacent kinetic component becomes the network source is treated
 * as the input. The remaining five shaft faces transmit the configured target
 * speed through Create's native split-shaft propagation rules.</p>
 */
public final class OmniSpeedControllerBlock extends KineticBlock
        implements IBE<OmniSpeedControllerBlockEntity>, IWrenchable {
    public static final MapCodec<OmniSpeedControllerBlock> CODEC = simpleCodec(OmniSpeedControllerBlock::new);

    public OmniSpeedControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public boolean hasShaftTowards(
            LevelReader level,
            BlockPos pos,
            BlockState state,
            Direction face
    ) {
        return true;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        // The controller exposes all three axes. Y is the stable reference axis
        // required by IRotate for particles and generic kinetic diagnostics.
        return Direction.Axis.Y;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.PUSH_ONLY;
    }

    @Override
    public Class<OmniSpeedControllerBlockEntity> getBlockEntityClass() {
        return OmniSpeedControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends OmniSpeedControllerBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.OMNI_SPEED_CONTROLLER.get();
    }
}
