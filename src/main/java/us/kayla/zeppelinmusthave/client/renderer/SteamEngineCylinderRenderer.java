package us.kayla.zeppelinmusthave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeProfile;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeTier;

/** Renders one parameterized slider-crank assembly. */
final class SteamEngineCylinderRenderer {
    private SteamEngineCylinderRenderer() {
    }

    static void render(
            SteamEngineGradeTier tier,
            SteamEngineGradeProfile profile,
            BlockState blockState,
            Direction facing,
            boolean roll90,
            float angle,
            float lateralOffset,
            float longitudinalOffset,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int light
    ) {
        float radius = profile.crankRadius();
        float rodLength = profile.connectingRodLength();
        float cosine = Mth.cos(angle);
        float sine = Mth.sin(angle);
        float underRoot = Math.max(
                0.0F,
                Mth.square(rodLength) - Mth.square(radius) * Mth.square(cosine)
        );
        float piston = radius * sine - Mth.sqrt(underRoot);
        float distance = Mth.sqrt(Mth.square(piston - radius * sine));
        float normalizedDistance = Mth.clamp(distance / rodLength, -1.0F, 1.0F);
        float linkageAngle = (float) Math.acos(normalizedDistance)
                * (cosine >= 0.0F ? 1.0F : -1.0F);

        SteamEngineRenderTransform.partial(
                SteamEngineGradePartialModels.piston(tier), blockState, facing, roll90
        )
                .translate(lateralOffset, piston + profile.pistonBaseOffset(), longitudinalOffset)
                .light(light)
                .renderInto(poseStack, vertexConsumer);

        SteamEngineRenderTransform.partial(
                SteamEngineGradePartialModels.linkage(tier), blockState, facing, roll90
        )
                .translate(lateralOffset, 0.0F, longitudinalOffset)
                .center()
                .translate(0.0F, 1.0F, 0.0F)
                .uncenter()
                .translate(0.0F, piston + profile.pistonBaseOffset(), 0.0F)
                .translate(0.0F, 4.0F / 16.0F, 8.0F / 16.0F)
                .rotateX(linkageAngle)
                .translate(0.0F, -4.0F / 16.0F, -8.0F / 16.0F)
                .light(light)
                .renderInto(poseStack, vertexConsumer);

        SteamEngineRenderTransform.partial(
                SteamEngineGradePartialModels.connector(tier), blockState, facing, roll90
        )
                .translate(lateralOffset, 2.0F, longitudinalOffset)
                .center()
                .rotateX(-(angle + Mth.HALF_PI))
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);
    }

}
