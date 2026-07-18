package us.kayla.zeppelinmusthave.content.fluid;

import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.StraightPipeBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.HitResult;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

/**
 * Internal transparent form of a graded pipe. It retains the pressure grade,
 * flow graph and source pipe item while using Create's native glass-pipe UI.
 */
public final class TieredGlassFluidPipeBlock extends GlassFluidPipeBlock
        implements SpecialBlockItemRequirement {
    private final FluidPipeTier tier;

    public TieredGlassFluidPipeBlock(Properties properties, FluidPipeTier tier) {
        super(properties);
        this.tier = tier;
    }

    public FluidPipeTier tier() {
        return this.tier;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (tryRemoveBracket(context)) {
            return InteractionResult.SUCCESS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidTransportBehaviour.cacheFlows(level, pos);

        BlockState regular = regularPipe()
                .getAxisState(state.getValue(AXIS))
                .setValue(
                        BlockStateProperties.WATERLOGGED,
                        state.getValue(BlockStateProperties.WATERLOGGED)
                );
        level.setBlock(pos, regular, Block.UPDATE_ALL);
        FluidTransportBehaviour.loadFlows(level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        return ItemRequirement.of(regularPipe().defaultBlockState(), blockEntity);
    }

    @Override
    public ItemStack getCloneItemStack(
            BlockState state,
            HitResult target,
            LevelReader level,
            BlockPos pos,
            Player player
    ) {
        return new ItemStack(regularPipe());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class<StraightPipeBlockEntity> getBlockEntityClass() {
        return (Class) TieredGlassFluidPipeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StraightPipeBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.TIERED_GLASS_FLUID_PIPE.get();
    }

    private TieredFluidPipeBlock regularPipe() {
        return switch (this.tier) {
            case REINFORCED -> ZmhBlocks.REINFORCED_FLUID_PIPE.get();
            case INDUSTRIAL -> ZmhBlocks.INDUSTRIAL_FLUID_PIPE.get();
        };
    }
}
