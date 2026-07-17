package us.kayla.zeppelinmusthave.content.boiler;

import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

/**
 * A graded Create fluid tank. Tank interaction, multiblock connectivity,
 * wrench behaviour, comparator output, boiler detection, and shape state are
 * inherited directly from Create's {@link FluidTankBlock} implementation.
 */
public final class BoilerGradeBlock extends FluidTankBlock {
    private final BoilerGradeTier tier;

    public BoilerGradeBlock(Properties properties, BoilerGradeTier tier) {
        super(properties, false);
        this.tier = tier;
    }

    public BoilerGradeTier tier() {
        return this.tier;
    }

    @Override
    public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.forBoilerTier(this.tier);
    }
}
