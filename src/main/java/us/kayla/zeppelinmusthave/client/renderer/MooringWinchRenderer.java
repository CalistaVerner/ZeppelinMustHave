package us.kayla.zeppelinmusthave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.contraptions.pulley.AbstractPulleyRenderer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchRenderer;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.RopeStrandRenderer;
import us.kayla.zeppelinmusthave.content.mooring.MooringWinchBlockEntity;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

/** Uses Zeppelin Must Have partials while preserving Simulated rope rendering. */
public final class MooringWinchRenderer extends RopeWinchRenderer {
    public MooringWinchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderComponents(
            RopeWinchBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        poseStack.pushPose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        BlockState state = blockEntity.getBlockState();

        SuperByteBuffer shaft = CachedBuffers.partial(MooringWinchPartialModels.SHAFT, state);
        SuperByteBuffer ropeCoil = CachedBuffers.partial(MooringWinchPartialModels.ROPE_COIL, state);

        Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
        float angle = KineticBlockEntityRenderer.getAngleForBe(
                blockEntity,
                blockEntity.getBlockPos(),
                axis
        );
        KineticBlockEntityRenderer.kineticRotationTransform(
                shaft,
                blockEntity,
                axis,
                angle,
                light
        );
        transform(shaft, state, true).renderInto(poseStack, consumer);

        if (blockEntity.getRopeHolder().isAttached()
                || (blockEntity.isVirtual() && blockEntity.getRopeHolder().renderAttached)) {
            ropeCoil.light(light);
            Direction facing = state.getValue(FACING);
            float speed;
            if (facing == Direction.DOWN) {
                speed = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                        ? 1.0F
                        : -1.0F;
            } else {
                speed = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE
                        == state.getValue(AXIS_ALONG_FIRST_COORDINATE)
                        ? 1.0F
                        : -1.0F;
            }

            AbstractPulleyRenderer.scrollCoil(
                    ropeCoil,
                    this.getCoilShift(),
                    ((MooringWinchBlockEntity) blockEntity).getClientRopeAngle(partialTicks),
                    speed
            );
            transform(ropeCoil, state, true).renderInto(poseStack, consumer);
        }

        poseStack.popPose();
        RopeStrandRenderer.render(
                blockEntity,
                blockEntity.getRopeHolder(),
                partialTicks,
                poseStack,
                buffer
        );
    }

    private static SuperByteBuffer transform(
            SuperByteBuffer buffer,
            BlockState state,
            boolean axisDirectionMatters
    ) {
        Direction facing = state.getValue(FACING);
        float finalZRotation = axisDirectionMatters
                && (state.getValue(AXIS_ALONG_FIRST_COORDINATE)
                ^ facing.getAxis() == Direction.Axis.Z)
                ? 90.0F
                : 0.0F;
        float yRotation = AngleHelper.horizontalAngle(facing)
                + (state.getValue(AXIS_ALONG_FIRST_COORDINATE)
                || facing.getAxis() != Direction.Axis.Y
                ? 0.0F
                : 90.0F);
        float zRotation = facing == Direction.UP
                ? 270.0F
                : facing == Direction.DOWN ? 90.0F : 0.0F;

        buffer.rotateCentered((float) Math.toRadians(zRotation), Direction.SOUTH);
        buffer.rotateCentered((float) Math.toRadians(yRotation), Direction.UP);
        buffer.rotateCentered((float) Math.toRadians(finalZRotation), Direction.SOUTH);
        return buffer;
    }
}
