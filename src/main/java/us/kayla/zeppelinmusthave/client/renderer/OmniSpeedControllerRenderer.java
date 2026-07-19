package us.kayla.zeppelinmusthave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import us.kayla.zeppelinmusthave.content.kinetics.OmniSpeedControllerBlockEntity;

/**
 * Renders every port with Create's native half-shaft partial and kinetic
 * rotation transform. Using partialFacing is important for the vertical ports:
 * it aligns the model first, then rotates it around the target world axis.
 */
public final class OmniSpeedControllerRenderer
        extends SafeBlockEntityRenderer<OmniSpeedControllerBlockEntity> {
    private static final double SHAFT_PROTRUSION = 1.0D / 32.0D;
    public OmniSpeedControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(
            OmniSpeedControllerBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        for (Direction face : Direction.values()) {
            Direction.Axis axis = face.getAxis();
            float angle = angleForSpeed(blockEntity, axis, blockEntity.getSpeedForFace(face));
            SuperByteBuffer shaft = CachedBuffers.partialFacing(
                    AllPartialModels.SHAFT_HALF,
                    blockEntity.getBlockState(),
                    face
            );
            poseStack.pushPose();
            poseStack.translate(
                    face.getStepX() * SHAFT_PROTRUSION,
                    face.getStepY() * SHAFT_PROTRUSION,
                    face.getStepZ() * SHAFT_PROTRUSION
            );
            KineticBlockEntityRenderer.kineticRotationTransform(
                    shaft,
                    blockEntity,
                    axis,
                    angle,
                    light
            ).renderInto(poseStack, consumer);
            poseStack.popPose();
        }
    }

    private static float angleForSpeed(
            OmniSpeedControllerBlockEntity blockEntity,
            Direction.Axis axis,
            float speed
    ) {
        float time = AnimationTickHolder.getRenderTime(blockEntity.getLevel());
        float offset = KineticBlockEntityRenderer.getRotationOffsetForPosition(
                blockEntity,
                blockEntity.getBlockPos(),
                axis
        );
        return ((time * speed * 3.0F / 10.0F + offset) % 360.0F)
                / 180.0F * (float) Math.PI;
    }
}
