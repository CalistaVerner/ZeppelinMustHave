package us.kayla.zeppelinmusthave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import us.kayla.zeppelinmusthave.content.ballast.BallastTankBlockEntity;

/** Renders the ballast level in the front service gauge. */
public final class BallastTankRenderer extends SafeBlockEntityRenderer<BallastTankBlockEntity> {
    public BallastTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(
            BallastTankBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        FluidStack fluid = blockEntity.tank().getFluid();
        if (fluid.isEmpty()) {
            return;
        }

        float ratio = (float) blockEntity.getFillRatio();
        float xMin = 5.0F / 16.0F;
        float xMax = 11.0F / 16.0F;
        float yMin = 4.0F / 16.0F;
        float yMax = yMin + ratio * 8.0F / 16.0F;
        float zMin = -0.020F;
        float zMax = -0.005F;

        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
                fluid,
                xMin,
                yMin,
                zMin,
                xMax,
                yMax,
                zMax,
                buffer,
                poseStack,
                light,
                false,
                true
        );
    }
}
