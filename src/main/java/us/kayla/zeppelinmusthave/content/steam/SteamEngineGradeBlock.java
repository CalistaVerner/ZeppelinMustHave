package us.kayla.zeppelinmusthave.content.steam;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlock;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

/**
 * A Create-compatible steam engine restricted to graded boilers.
 * Native shaft placement, wrenching, rotation direction and boiler attachment
 * behaviour are inherited from {@link SteamEngineBlock}.
 */
public class SteamEngineGradeBlock extends SteamEngineBlock {
    private final SteamEngineGradeTier tier;
    private final MapCodec<SteamEngineGradeBlock> codec = MapCodec.unit(this);

    public SteamEngineGradeBlock(Properties properties, SteamEngineGradeTier tier) {
        super(properties);
        this.tier = tier;
    }

    public SteamEngineGradeTier tier() {
        return this.tier;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos boilerPos = pos.relative(getConnectedDirection(state).getOpposite());
        return level.getBlockState(boilerPos).getBlock() instanceof BoilerGradeBlock;
    }

    @Override
    public BlockEntityType<? extends SteamEngineBlockEntity> getBlockEntityType() {
        return ZmhBlockEntityTypes.STEAM_ENGINE_GRADE.get();
    }

    @Override
    protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
        return this.codec;
    }
}
