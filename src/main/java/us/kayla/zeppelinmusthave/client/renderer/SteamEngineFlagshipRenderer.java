package us.kayla.zeppelinmusthave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.steam.MkViiSteamEngineBlock;
import us.kayla.zeppelinmusthave.content.steam.MkViiSteamEnginePart;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeTier;

/** Renders flywheels, valve gear, governor and advanced synchronization cores. */
final class SteamEngineFlagshipRenderer {
    private SteamEngineFlagshipRenderer() {
    }

    static boolean supports(SteamEngineGradeTier tier) {
        return tier == SteamEngineGradeTier.GRAND
                || tier == SteamEngineGradeTier.SOVEREIGN
                || tier == SteamEngineGradeTier.LEVIATHAN
                || tier == SteamEngineGradeTier.MK_VII;
    }

    static void render(
            SteamEngineGradeTier tier,
            BlockState blockState,
            Direction facing,
            boolean roll90,
            float angle,
            PoweredShaftBlockEntity shaft,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int light
    ) {
        boolean sovereign = tier == SteamEngineGradeTier.SOVEREIGN;
        boolean leviathan = tier == SteamEngineGradeTier.LEVIATHAN;
        boolean mkVii = tier == SteamEngineGradeTier.MK_VII;
        boolean heavy = leviathan || mkVii;
        boolean controllerMechanism = !mkVii
                || blockState.getValue(MkViiSteamEngineBlock.PART) == MkViiSteamEnginePart.CONTROLLER;
        float speedRatio = shaft == null
                ? 0.0F
                : Mth.clamp(Math.abs(shaft.getSpeed()) / 64.0F, 0.0F, 1.5F);
        float flywheelOffset = mkVii ? 0.38F : heavy ? 0.82F : 0.40F;
        float counterRotationRatio = mkVii ? 1.55F : sovereign ? 1.12F : 0.82F;
        float driveRotation = -(angle + Mth.HALF_PI);

        if (leviathan) {
            renderLeviathanCrankshaft(
                    blockState,
                    facing,
                    roll90,
                    driveRotation,
                    poseStack,
                    vertexConsumer,
                    light
            );
        }

        SteamEngineRenderTransform.partial(
                SteamEngineGradePartialModels.flagshipFlywheel(tier), blockState, facing, roll90
        )
                .translate(-flywheelOffset, 2.0F, 0.0F)
                .center()
                .rotateX(driveRotation)
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);

        SteamEngineRenderTransform.partial(
                SteamEngineGradePartialModels.flagshipCounterFlywheel(tier), blockState, facing, roll90
        )
                .translate(flywheelOffset, 2.0F, 0.0F)
                .center()
                .rotateX(leviathan ? driveRotation : angle * counterRotationRatio + Mth.HALF_PI)
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);

        if (controllerMechanism) {
            renderGovernor(
                    tier,
                    blockState,
                    facing,
                    roll90,
                    angle,
                    speedRatio,
                    heavy ? 0.28F : 0.18F,
                    mkVii ? 5.6F : leviathan ? 4.8F : sovereign ? 3.6F : 2.75F,
                    poseStack,
                    vertexConsumer,
                    light
            );
        }

        int valveBanks = mkVii ? 3 : leviathan ? 4 : sovereign ? 3 : 2;
        renderValveGear(
                tier,
                blockState,
                facing,
                roll90,
                angle,
                valveBanks,
                heavy ? 0.42F : 0.26F,
                poseStack,
                vertexConsumer,
                light
        );

        if ((sovereign || leviathan || mkVii) && controllerMechanism) {
            renderAdvancedCore(
                    tier,
                    blockState,
                    facing,
                    roll90,
                    angle,
                    speedRatio,
                    heavy,
                    leviathan,
                    mkVii,
                    poseStack,
                    vertexConsumer,
                    light
            );
        }
    }

    private static void renderLeviathanCrankshaft(
            BlockState blockState,
            Direction facing,
            boolean roll90,
            float driveRotation,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int light
    ) {
        SteamEngineRenderTransform.partial(
                SteamEngineGradePartialModels.leviathanMainCrankshaft(),
                blockState,
                facing,
                roll90
        )
                .translate(0.0F, 2.0F, 0.0F)
                .center()
                .rotateX(driveRotation)
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);
    }

    private static void renderGovernor(
            SteamEngineGradeTier tier,
            BlockState blockState,
            Direction facing,
            boolean roll90,
            float angle,
            float speedRatio,
            float governorTravel,
            float governorRotationRatio,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int light
    ) {
        float governorLift = Mth.clamp(speedRatio, 0.0F, 1.0F) * governorTravel;
        SteamEngineRenderTransform.partial(
                SteamEngineGradePartialModels.flagshipGovernor(tier), blockState, facing, roll90
        )
                .translate(0.0F, 0.25F + governorLift, 0.0F)
                .center()
                .rotateYDegrees((float) Math.toDegrees(angle * governorRotationRatio))
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);
    }

    private static void renderValveGear(
            SteamEngineGradeTier tier,
            BlockState blockState,
            Direction facing,
            boolean roll90,
            float angle,
            int valveBanks,
            float valveSpacing,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int light
    ) {
        for (int bank = 0; bank < valveBanks; bank++) {
            float bankPhase = angle + (float) (Math.TAU * bank / valveBanks);
            float travelAmplitude = tier == SteamEngineGradeTier.LEVIATHAN
                    ? 0.16F
                    : tier == SteamEngineGradeTier.MK_VII ? 0.13F : 0.10F;
            float rockAmplitude = tier == SteamEngineGradeTier.LEVIATHAN
                    ? 0.24F
                    : tier == SteamEngineGradeTier.MK_VII ? 0.20F : 0.16F;
            float longitudinalAmplitude = tier == SteamEngineGradeTier.LEVIATHAN ? 0.08F : 0.04F;
            float valveTravel = Mth.sin(bankPhase * 2.0F) * travelAmplitude;
            float valveRock = Mth.cos(bankPhase) * rockAmplitude;
            float longitudinalTravel = Mth.sin(bankPhase) * longitudinalAmplitude;
            float lateral = valveBanks == 2
                    ? (bank == 0 ? -0.28F : 0.28F)
                    : (bank - (valveBanks - 1) / 2.0F) * valveSpacing;
            SteamEngineRenderTransform.partial(
                    SteamEngineGradePartialModels.flagshipValveGear(tier), blockState, facing, roll90
            )
                    .translate(lateral, 1.08F + valveTravel, longitudinalTravel)
                    .center()
                    .rotateX(valveRock)
                    .uncenter()
                    .light(light)
                    .renderInto(poseStack, vertexConsumer);
        }
    }

    private static void renderAdvancedCore(
            SteamEngineGradeTier tier,
            BlockState blockState,
            Direction facing,
            boolean roll90,
            float angle,
            float speedRatio,
            boolean heavy,
            boolean leviathan,
            boolean mkVii,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int light
    ) {
        SteamEngineRenderTransform.partial(
                SteamEngineGradePartialModels.flagshipPowerCore(tier), blockState, facing, roll90
        )
                .translate(
                        0.0F,
                        (heavy ? 0.82F : 0.72F) + speedRatio * (heavy ? 0.10F : 0.06F),
                        heavy ? -0.45F : 0.0F
                )
                .center()
                .rotateYDegrees((float) Math.toDegrees(
                        -angle * (mkVii ? 3.2F : leviathan ? 2.8F : 1.8F)
                ))
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);

        SteamEngineRenderTransform.partial(
                SteamEngineGradePartialModels.flagshipCrownRotor(tier), blockState, facing, roll90
        )
                .translate(0.0F, heavy ? 0.30F : 0.20F, heavy ? -0.20F : 0.0F)
                .center()
                .rotateYDegrees((float) Math.toDegrees(
                        angle * (mkVii ? 4.0F : leviathan ? 3.4F : 2.4F)
                ))
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);
    }
}
