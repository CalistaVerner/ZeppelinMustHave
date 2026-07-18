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
import us.kayla.zeppelinmusthave.content.steam.MkViiSteamEngineBlock;
import us.kayla.zeppelinmusthave.content.steam.MkViiSteamEnginePart;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeBlockEntity;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeProfile;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeTier;

/**
 * Grade-aware crank renderer.
 *
 * <p>Copper uses one cylinder, Brass two cylinders at 180 degrees,
 * Industrial three cylinders at 120 degrees, Grand four cylinders at
 * 90 degrees, Sovereign five cylinders at 72 degrees, and Leviathan eight
 * cylinders in two lateral banks at 45-degree phasing. Flagship tiers add
 * contra-rotating flywheels, animated valve gear, and governors; MK V and
 * MK VI also adds two spatially separated four-cylinder banks, a rotating pressure core, and a crown rotor.</p>
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

        PoweredShaftBlockEntity shaft = blockEntity.getPrimaryShaft();
        if (shaft != null) {
            shaftAxis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        }

        boolean roll90 = facingAxis.isHorizontal() && shaftAxis == Axis.Y
                || facingAxis.isVertical() && shaftAxis == Axis.Z;
        SteamEngineGradeTier tier = blockEntity.tier();
        SteamEngineGradeProfile profile = blockEntity.activeProfile();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.solid());

        int firstCylinder = 0;
        int renderedCylinderCount = profile.cylinderCount();
        int mkViiBank = 0;
        if (tier == SteamEngineGradeTier.MK_VII
                && blockState.getBlock() instanceof MkViiSteamEngineBlock) {
            MkViiSteamEnginePart part = blockState.getValue(MkViiSteamEngineBlock.PART);
            renderedCylinderCount = MkViiSteamEngineBlock.INTERNAL_SHAFTS_PER_BANK;
            mkViiBank = part.bankIndex();
            firstCylinder = mkViiBank * renderedCylinderCount;
        }

        for (int localCylinder = 0; localCylinder < renderedCylinderCount; localCylinder++) {
            int cylinder = firstCylinder + localCylinder;
            float angle = tier == SteamEngineGradeTier.MK_VII
                    ? targetAngle
                            + (float) (Math.TAU * localCylinder / renderedCylinderCount)
                            + (float) (Math.TAU * mkViiBank / MkViiSteamEngineBlock.INTERNAL_SHAFT_COUNT)
                    : targetAngle + profile.cylinderPhase(cylinder);
            float lateralOffset = profile.cylinderOffset(cylinder);
            float longitudinalOffset = 0.0F;
            if (tier == SteamEngineGradeTier.LEVIATHAN) {
                int bankCylinder = cylinder % 4;
                lateralOffset = cylinder < 4 ? -0.92F : 0.92F;
                longitudinalOffset = (bankCylinder - 1.5F) * 0.28F;
            } else if (tier == SteamEngineGradeTier.MK_VII) {
                lateralOffset = (localCylinder - 1.0F) * profile.cylinderSpread();
                longitudinalOffset = (localCylinder - 1.0F) * 0.08F;
            }
            renderCylinder(
                    tier,
                    profile,
                    blockState,
                    facing,
                    roll90,
                    angle,
                    lateralOffset,
                    longitudinalOffset,
                    poseStack,
                    vertexConsumer,
                    light
            );
        }
        if (tier == SteamEngineGradeTier.GRAND
                || tier == SteamEngineGradeTier.SOVEREIGN
                || tier == SteamEngineGradeTier.LEVIATHAN
                || tier == SteamEngineGradeTier.MK_VII) {
            renderFlagshipMechanism(
                    tier,
                    blockState,
                    facing,
                    roll90,
                    targetAngle,
                    shaft,
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
            float longitudinalOffset,
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
                .translate(lateralOffset, piston + profile.pistonBaseOffset(), longitudinalOffset)
                .light(light)
                .renderInto(poseStack, vertexConsumer);

        transformed(
                SteamEngineGradePartialModels.linkage(tier),
                blockState,
                facing,
                roll90
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

        transformed(
                SteamEngineGradePartialModels.connector(tier),
                blockState,
                facing,
                roll90
        )
                .translate(lateralOffset, 2.0F, longitudinalOffset)
                .center()
                .rotateX(-(angle + Mth.HALF_PI))
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);
    }

    private static void renderFlagshipMechanism(
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
        float speedRatio = shaft == null
                ? 0.0F
                : Mth.clamp(Math.abs(shaft.getSpeed()) / 64.0F, 0.0F, 1.5F);
        boolean sovereign = tier == SteamEngineGradeTier.SOVEREIGN;
        boolean leviathan = tier == SteamEngineGradeTier.LEVIATHAN;
        boolean mkVii = tier == SteamEngineGradeTier.MK_VII;
        boolean mkViiController = !mkVii
                || blockState.getValue(MkViiSteamEngineBlock.PART) == MkViiSteamEnginePart.CONTROLLER;
        boolean heavyFlagship = leviathan || mkVii;

        transformed(
                SteamEngineGradePartialModels.flagshipFlywheel(tier),
                blockState,
                facing,
                roll90
        )
                .translate(mkVii ? -0.38F : heavyFlagship ? -0.82F : -0.40F, 2.0F, 0.0F)
                .center()
                .rotateX(-(angle + Mth.HALF_PI))
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);

        transformed(
                SteamEngineGradePartialModels.flagshipCounterFlywheel(tier),
                blockState,
                facing,
                roll90
        )
                .translate(mkVii ? 0.38F : heavyFlagship ? 0.82F : 0.40F, 2.0F, 0.0F)
                .center()
                .rotateX(angle * (mkVii ? 1.55F : leviathan ? 1.35F : sovereign ? 1.12F : 0.82F) + Mth.HALF_PI)
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);

        if (mkViiController) {
            float governorLift = Mth.clamp(speedRatio, 0.0F, 1.0F) * (heavyFlagship ? 0.28F : 0.18F);
            transformed(
                    SteamEngineGradePartialModels.flagshipGovernor(tier),
                    blockState,
                    facing,
                    roll90
            )
                    .translate(0.0F, 0.25F + governorLift, 0.0F)
                    .center()
                    .rotateYDegrees((float) Math.toDegrees(angle * (mkVii ? 5.6F : leviathan ? 4.8F : sovereign ? 3.6F : 2.75F)))
                    .uncenter()
                    .light(light)
                    .renderInto(poseStack, vertexConsumer);
        }

        int valveBanks = mkVii ? 3 : leviathan ? 4 : sovereign ? 3 : 2;
        for (int bank = 0; bank < valveBanks; bank++) {
            float bankPhase = angle + (float) (Math.TAU * bank / valveBanks);
            float valveTravel = Mth.sin(bankPhase * 2.0F) * 0.10F;
            float valveRock = Mth.cos(bankPhase) * 0.16F;
            transformed(
                    SteamEngineGradePartialModels.flagshipValveGear(tier),
                    blockState,
                    facing,
                    roll90
            )
                    .translate(
                            valveBanks == 2
                                    ? (bank == 0 ? -0.28F : 0.28F)
                                    : (bank - (valveBanks - 1) / 2.0F) * (heavyFlagship ? 0.42F : 0.26F),
                            1.08F + valveTravel,
                            0.0F
                    )
                    .center()
                    .rotateX(valveRock)
                    .uncenter()
                    .light(light)
                    .renderInto(poseStack, vertexConsumer);
        }

        if ((sovereign || leviathan || mkVii) && mkViiController) {
            renderAdvancedCore(
                    tier,
                    blockState,
                    facing,
                    roll90,
                    angle,
                    speedRatio,
                    poseStack,
                    vertexConsumer,
                    light
            );
        }
    }

    private static void renderAdvancedCore(
            SteamEngineGradeTier tier,
            BlockState blockState,
            Direction facing,
            boolean roll90,
            float angle,
            float speedRatio,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int light
    ) {
        boolean leviathan = tier == SteamEngineGradeTier.LEVIATHAN;
        boolean mkVii = tier == SteamEngineGradeTier.MK_VII;
        boolean heavyFlagship = leviathan || mkVii;
        transformed(SteamEngineGradePartialModels.flagshipPowerCore(tier), blockState, facing, roll90)
                .translate(0.0F, (heavyFlagship ? 0.82F : 0.72F)
                                + speedRatio * (heavyFlagship ? 0.10F : 0.06F),
                        heavyFlagship ? -0.45F : 0.0F)
                .center()
                .rotateYDegrees((float) Math.toDegrees(-angle * (mkVii ? 3.2F : leviathan ? 2.8F : 1.8F)))
                .uncenter()
                .light(light)
                .renderInto(poseStack, vertexConsumer);

        transformed(SteamEngineGradePartialModels.flagshipCrownRotor(tier), blockState, facing, roll90)
                .translate(0.0F, heavyFlagship ? 0.30F : 0.20F, heavyFlagship ? -0.20F : 0.0F)
                .center()
                .rotateYDegrees((float) Math.toDegrees(angle * (mkVii ? 4.0F : leviathan ? 3.4F : 2.4F)))
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
