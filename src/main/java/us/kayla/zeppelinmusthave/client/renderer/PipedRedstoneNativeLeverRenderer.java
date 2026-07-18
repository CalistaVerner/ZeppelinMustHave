package us.kayla.zeppelinmusthave.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlock;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlockEntity;

public final class PipedRedstoneNativeLeverRenderer
        extends SafeBlockEntityRenderer<PipedRedstoneNativeLeverBlockEntity> {
    private static final PartialModel HANDLE = PartialModel.of(
            ZeppelinMustHave.id("block/piped_redstone_native_lever/handle")
    );

    public PipedRedstoneNativeLeverRenderer(BlockEntityRendererProvider.Context context) {
    }

    /** Forces PartialModel registration before Minecraft bakes the model registry. */
    public static void init() {
        HANDLE.modelLocation();
    }

    @Override
    protected void renderSafe(
            PipedRedstoneNativeLeverBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        BlockState state = blockEntity.getBlockState();
        float progress = blockEntity.getHandlePosition(partialTicks);
        // Request one render layer only. BufferSource invalidates the previously
        // returned builder whenever another RenderType is requested.
        VertexConsumer consumer = buffer.getBuffer(RenderType.cutout());

        float handleRadians = (float) Math.toRadians(90.0F * progress);
        SuperByteBuffer handle = orient(CachedBuffers.partial(HANDLE, state), state);
        handle.translate(0.5F, 1.0F / 16.0F, 0.5F)
                .rotate(handleRadians, Direction.EAST)
                .translate(-0.5F, -1.0F / 16.0F, -0.5F)
                .light(light)
                .renderInto(poseStack, consumer);
    }

    private static SuperByteBuffer orient(SuperByteBuffer buffer, BlockState state) {
        AttachFace face = state.getValue(PipedRedstoneNativeLeverBlock.FACE);
        float xRotation = switch (face) {
            case FLOOR -> 0.0F;
            case WALL -> 90.0F;
            case CEILING -> 180.0F;
        };
        float yRotation = AngleHelper.horizontalAngle(
                state.getValue(PipedRedstoneNativeLeverBlock.FACING)
        );

        buffer.rotateCentered((float) Math.toRadians(yRotation), Direction.UP);
        buffer.rotateCentered((float) Math.toRadians(xRotation), Direction.EAST);
        return buffer;
    }
}
