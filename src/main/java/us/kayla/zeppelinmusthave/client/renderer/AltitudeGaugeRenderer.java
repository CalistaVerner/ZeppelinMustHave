package us.kayla.zeppelinmusthave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.control.AltitudeGaugeBlock;
import us.kayla.zeppelinmusthave.content.control.AltitudeGaugeBlockEntity;

/** Renders the physical dial needle from the synchronized analog output. */
public final class AltitudeGaugeRenderer
        extends SafeBlockEntityRenderer<AltitudeGaugeBlockEntity> {
    private static final PartialModel NEEDLE = PartialModel.of(
            ZeppelinMustHave.id("block/altitude_gauge/needle")
    );

    public AltitudeGaugeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(
            AltitudeGaugeBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BlockState state = blockEntity.getBlockState();
        float signal = blockEntity.getNeedleSignal(partialTicks);
        float angleDegrees = -135.0F + Math.clamp(signal / 15.0F, 0.0F, 1.0F) * 270.0F;

        SuperByteBuffer needle = CachedBuffers.partial(NEEDLE, state);
        float facingRadians = AngleHelper.rad(
                AngleHelper.horizontalAngle(state.getValue(AltitudeGaugeBlock.FACING))
        );
        needle.rotateCentered(facingRadians, Direction.UP);

        float needleRadians = AngleHelper.rad(angleDegrees);
        needle.translate(0.5F, 0.5F, 0.025F)
                .rotate(needleRadians, Direction.SOUTH)
                .translate(-0.5F, -0.5F, -0.025F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.solid());
        needle.light(light).renderInto(poseStack, vertexConsumer);
    }
}
