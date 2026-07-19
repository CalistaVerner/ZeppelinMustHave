package us.kayla.zeppelinmusthave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

public final class OmniSpeedControllerRenderer
        extends SafeBlockEntityRenderer<OmniSpeedControllerBlockEntity> {
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
            float speed = blockEntity.getSpeedForFace(face);
            float angle = angleForSpeed(blockEntity, face.getAxis(), speed);
            SuperByteBuffer shaft = CachedBuffers.partial(
                    OmniSpeedControllerPartialModels.SHAFT_HALF,
                    blockEntity.getBlockState()
            );
            shaft.rotateCentered(angle, Direction.SOUTH);
            orientFromSouth(shaft, face);
            shaft.light(light).renderInto(poseStack, consumer);
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

    private static void orientFromSouth(SuperByteBuffer buffer, Direction target) {
        switch (target) {
            case SOUTH -> {
            }
            case NORTH -> buffer.rotateCentered((float) Math.PI, Direction.UP);
            case EAST -> buffer.rotateCentered((float) (Math.PI / 2.0D), Direction.UP);
            case WEST -> buffer.rotateCentered((float) (-Math.PI / 2.0D), Direction.UP);
            case UP -> buffer.rotateCentered((float) (-Math.PI / 2.0D), Direction.EAST);
            case DOWN -> buffer.rotateCentered((float) (Math.PI / 2.0D), Direction.EAST);
        }
    }
}
