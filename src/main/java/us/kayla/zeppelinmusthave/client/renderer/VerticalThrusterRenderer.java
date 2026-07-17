package us.kayla.zeppelinmusthave.client.renderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.SimplePropellerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import us.kayla.zeppelinmusthave.content.thruster.VerticalThrusterBlockEntity;

public final class VerticalThrusterRenderer extends SimplePropellerRenderer<VerticalThrusterBlockEntity> {
    public VerticalThrusterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public PartialModel getCurrentModel(VerticalThrusterBlockEntity blockEntity) {
        return VerticalThrusterPartialModels.PROPELLER;
    }

    @Override
    public float getAngle(
            float partialTicks,
            Direction direction,
            VerticalThrusterBlockEntity blockEntity
    ) {
        return super.getAngle(partialTicks, direction, blockEntity)
                + getRotationOffsetForPosition(
                        blockEntity,
                        blockEntity.getBlockPos(),
                        direction.getAxis()
                );
    }
}
