package us.kayla.zeppelinmusthave.client.renderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Shared model-space transform for every graded steam-engine partial. */
final class SteamEngineRenderTransform {
    private SteamEngineRenderTransform() {
    }

    static SuperByteBuffer partial(
            PartialModel model,
            BlockState blockState,
            Direction facing,
            boolean roll90
    ) {
        return CachedBuffers.partial(model, blockState)
                .center()
                .rotateYDegrees(AngleHelper.horizontalAngle(facing))
                .rotateXDegrees(AngleHelper.verticalAngle(facing) + 90.0F)
                .rotateYDegrees(roll90 ? -90.0F : 0.0F)
                .uncenter();
    }
}
