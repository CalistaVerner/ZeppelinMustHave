package us.kayla.zeppelinmusthave.content.casing;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

import java.util.function.Supplier;

/** Create-native encased shaft backed by Zeppelin Must Have casing materials. */
public final class AirshipEncasedShaftBlock extends EncasedShaftBlock {
    public AirshipEncasedShaftBlock(
            BlockBehaviour.Properties properties,
            Supplier<Block> casing
    ) {
        super(properties, casing);
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.AIRSHIP_ENCASED_SHAFT.get();
    }
}
