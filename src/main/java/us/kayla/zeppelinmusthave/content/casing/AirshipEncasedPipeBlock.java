package us.kayla.zeppelinmusthave.content.casing;

import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

import java.util.function.Supplier;

/** Create-native fluid pipe preserving connectivity and flow data while encased. */
public final class AirshipEncasedPipeBlock extends EncasedPipeBlock {
    public AirshipEncasedPipeBlock(
            BlockBehaviour.Properties properties,
            Supplier<Block> casing
    ) {
        super(properties, casing);
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.AIRSHIP_ENCASED_FLUID_PIPE.get();
    }
}
