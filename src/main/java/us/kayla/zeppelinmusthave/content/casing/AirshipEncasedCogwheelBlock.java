package us.kayla.zeppelinmusthave.content.casing;

import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

import java.util.function.Supplier;

/** Create-native small or large cogwheel with a Zeppelin Must Have casing. */
public final class AirshipEncasedCogwheelBlock extends EncasedCogwheelBlock {
    public AirshipEncasedCogwheelBlock(
            BlockBehaviour.Properties properties,
            boolean large,
            Supplier<Block> casing
    ) {
        super(properties, large, casing);
    }

    @Override
    public BlockEntityType<? extends SimpleKineticBlockEntity> getBlockEntityType() {
        return isLarge
                ? ZmhBlockEntityTypes.AIRSHIP_ENCASED_LARGE_COGWHEEL.get()
                : ZmhBlockEntityTypes.AIRSHIP_ENCASED_COGWHEEL.get();
    }
}
