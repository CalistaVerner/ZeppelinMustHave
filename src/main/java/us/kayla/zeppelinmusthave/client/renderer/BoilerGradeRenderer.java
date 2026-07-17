package us.kayla.zeppelinmusthave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlockEntity;

/**
 * Create Fluid Tank renderer with a gauge skin selected from the boiler grade.
 */
public final class BoilerGradeRenderer extends FluidTankRenderer {
    public BoilerGradeRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderAsBoiler(
            FluidTankBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        if (!(blockEntity instanceof BoilerGradeBlockEntity gradedBoiler)) {
            super.renderAsBoiler(blockEntity, partialTicks, poseStack, buffer, light, overlay);
            return;
        }

        BlockState blockState = blockEntity.getBlockState();
        BoilerGaugePartialModels.GaugeModels models =
                BoilerGaugePartialModels.forTier(gradedBoiler.tier());
        VertexConsumer consumer = buffer.getBuffer(RenderType.cutout());

        poseStack.pushPose();
        var transform = TransformStack.of(poseStack);
        transform.translate(blockEntity.getWidth() / 2.0F, 0.5F, blockEntity.getWidth() / 2.0F);

        float dialPivotY = 6.0F / 16.0F;
        float dialPivotZ = 8.0F / 16.0F;
        float progress = blockEntity.boiler.gauge.getValue(partialTicks);

        for (Direction direction : Iterate.horizontalDirections) {
            if (blockEntity.boiler.occludedDirections[direction.get2DDataValue()]) {
                continue;
            }

            poseStack.pushPose();
            float yRotation = -direction.toYRot() - 90.0F;

            CachedBuffers.partial(models.gauge(), blockState)
                    .rotateYDegrees(yRotation)
                    .uncenter()
                    .translate(blockEntity.getWidth() / 2.0F - 6.0F / 16.0F, 0.0F, 0.0F)
                    .light(light)
                    .renderInto(poseStack, consumer);

            CachedBuffers.partial(models.dial(), blockState)
                    .rotateYDegrees(yRotation)
                    .uncenter()
                    .translate(blockEntity.getWidth() / 2.0F - 6.0F / 16.0F, 0.0F, 0.0F)
                    .translate(0.0F, dialPivotY, dialPivotZ)
                    .rotateXDegrees(-145.0F * progress + 90.0F)
                    .translate(0.0F, -dialPivotY, -dialPivotZ)
                    .light(light)
                    .renderInto(poseStack, consumer);

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
