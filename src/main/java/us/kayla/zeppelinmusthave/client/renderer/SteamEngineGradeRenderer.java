package us.kayla.zeppelinmusthave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.steam.MkViiSteamEngineBlock;
import us.kayla.zeppelinmusthave.content.steam.MkViiSteamEnginePart;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeBlockEntity;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeProfile;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeTier;

/** Coordinates grade-specific steam-engine rendering without owning mechanism geometry. */
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
        PoweredShaftBlockEntity shaft = blockEntity.getPrimaryShaft();
        Axis shaftAxis = shaft == null ? Axis.Y : KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        boolean roll90 = requiresQuarterRoll(facing.getAxis(), shaftAxis);
        SteamEngineGradeTier tier = blockEntity.tier();
        SteamEngineGradeProfile profile = blockEntity.activeProfile();
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());

        renderCylinderBank(
                tier,
                profile,
                blockState,
                facing,
                roll90,
                targetAngle,
                poseStack,
                consumer,
                light
        );
        if (SteamEngineFlagshipRenderer.supports(tier)) {
            SteamEngineFlagshipRenderer.render(
                    tier,
                    blockState,
                    facing,
                    roll90,
                    targetAngle,
                    shaft,
                    poseStack,
                    consumer,
                    light
            );
        }
    }

    private static void renderCylinderBank(
            SteamEngineGradeTier tier,
            SteamEngineGradeProfile profile,
            BlockState blockState,
            Direction facing,
            boolean roll90,
            float targetAngle,
            PoseStack poseStack,
            VertexConsumer consumer,
            int light
    ) {
        boolean mkVii = tier == SteamEngineGradeTier.MK_VII
                && blockState.getBlock() instanceof MkViiSteamEngineBlock;
        int mkViiBank = mkVii
                ? blockState.getValue(MkViiSteamEngineBlock.PART).bankIndex()
                : 0;
        int renderedCylinderCount = mkVii
                ? MkViiSteamEngineBlock.INTERNAL_SHAFTS_PER_BANK
                : profile.cylinderCount();
        int firstCylinder = mkVii ? mkViiBank * renderedCylinderCount : 0;

        for (int localCylinder = 0; localCylinder < renderedCylinderCount; localCylinder++) {
            int cylinder = firstCylinder + localCylinder;
            float angle = mkVii
                    ? targetAngle
                            + (float) (Math.TAU * localCylinder / renderedCylinderCount)
                            + (float) (Math.TAU * mkViiBank / MkViiSteamEngineBlock.INTERNAL_SHAFT_COUNT)
                    : targetAngle + profile.cylinderPhase(cylinder);
            float lateralOffset = tier == SteamEngineGradeTier.LEVIATHAN
                    ? (cylinder < 4 ? -0.92F : 0.92F)
                    : mkVii
                            ? (localCylinder - 1.0F) * profile.cylinderSpread()
                            : profile.cylinderOffset(cylinder);
            float longitudinalOffset = tier == SteamEngineGradeTier.LEVIATHAN
                    ? (cylinder % 4 - 1.5F) * 0.28F
                    : mkVii ? (localCylinder - 1.0F) * 0.08F : 0.0F;
            SteamEngineCylinderRenderer.render(
                    tier,
                    profile,
                    blockState,
                    facing,
                    roll90,
                    angle,
                    lateralOffset,
                    longitudinalOffset,
                    poseStack,
                    consumer,
                    light
            );
        }
    }

    private static boolean requiresQuarterRoll(Axis facingAxis, Axis shaftAxis) {
        return facingAxis.isHorizontal() && shaftAxis == Axis.Y
                || facingAxis.isVertical() && shaftAxis == Axis.Z;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
