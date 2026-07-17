package us.kayla.zeppelinmusthave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeBlockEntity;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeProfile;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeTier;

/**
 * Grade-aware crank renderer.
 *
 * <p>Copper uses one cylinder, Brass two cylinders at 180 degrees, and
 * Industrial three cylinders at 120 degrees. Profile values also control
 * crank radius, rod length and cylinder spacing.</p>
 */
public final class SteamEngineGradeRenderer extends SafeBlockEntityRenderer<SteamEngineGradeBlockEntity> {
    public SteamEngineGradeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(
            SteamEngineGradeBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        Float targetAngle = blockEntity.getTargetAngle();
        if (targetAngle == null) {
            return;
        }

        BlockState blockState = blockEntity.getBlockState();
        Direction facing = SteamEngineBlock.getFacing(blockState);
        Axis facingAxis = facing.getAxis();
        Axis shaftAxis = Axis.Y;

        PoweredShaftBlockEntity shaft = blockEntity.getShaft();
        if (shaft != null) {
            shaftAxis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        }

        boolean roll90 = facingAxis.isHorizontal() && shaftAxis == Axis.Y
                || facingAxis.isVertical() && shaftAxis == Axis.Z;
        SteamEngineGradeTier tier = blockEntity.tier();
        SteamEngineGradeProfile profile = blockEntity.activeProfile();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.solid());

        for (int cylinder = 0; cylinder < profile.cylinderCount(); cylinder++) {
            float angle = targetAngle + profile.cylinderPhase(cylinder);
            float lateralOffset = profile.cylinderOffset(cylinder);
            renderCylinder(
                    tier,
                    profile,
                    blockState,
                    facing,
                    roll90,
                    angle,
                    lateralOffset,
                    poseStack,
                    vertexConsumer,
                    light
            );
        }
    }

    private static void renderCylinder(
            SteamEngineGradeTier tier,
            SteamEngineGradeProfile profile,
            BlockState blockState,
            Direction facing,
            boolean roll90,
            float angle,
            float lateralOffset,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int light
    ) {
        float radius = profile.crankRadius();
        float rodLength = profile.connectingRodLength();
        float cosine = Mth.cos(angle);
        float sine = Mth.sin(angle);
        float underRoot = Math.max(0.0F, Mth.square(rodLength) - Mth.square(radius) * Mth.square(cosine));
        float piston = radius * sine - Mth.sqrt(underRoot);
        float distance = Mth.sqrt(Mth.square(piston - radius * sine));
        float normalizedDistance = Mth.clamp(distance / rodLength, -1.0F, 1.0F);
        float linkageAngle = (float) Math.acos(normalizedDistance) * (cosine >= 0.0F ? 1.0F : -1.0F);

        transformed(
                SteamEngineGradePartialModels.piston(tier),
                blockState,
                facing,
                roll90
        )
                .translate(lateralOffset, piston + profile.pistonBaseOffset(), 0.0F)
                .light(light)
                .renderInto(poseStack, vertexConsumer);

        transformed(
                SteamEngineGradePartialModels.linkage(tier),
                blockState,
                facing,
                roll90
        )
                .translate(lateralOffset, 0.0F, 0.0F)
                .center()
                .translate(0.0F, 1.0F, 0.0F)
                .uncenter()
                .translate(0.0F, piston + profile.pistonBaseOffset(), 0.0F)
                .translate(0.0F, 4.0F / 16.0F, 8.0F / 16.0F)
                .rotateX(linkageAngle)
                .translate(0.0F, -4.0F / 16.0F, -8.0F / 16.0F)
                .light(light)
                .renderInto(poseStack, vertexConsumer);

        transformed(
                SteamEngineGradePartialModels.connector(tier),
                blockState,
                facing,
                roll90
        )
                .translate(lateralOffset, 2.0F, 0.0F)
                .center()
                .rotateX(-(angle + Mth.HALF_PI))
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);
    }

    private static SuperByteBuffer transformed(
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

    @Override
    public int getViewDistance() {
        return 128;
    }
}
