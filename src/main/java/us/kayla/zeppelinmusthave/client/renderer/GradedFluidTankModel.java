package us.kayla.zeppelinmusthave.client.renderer;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Create FluidTankModel equivalent using grade-specific connected textures.
 */
public final class GradedFluidTankModel extends CTModel {
    private static final net.neoforged.neoforge.client.model.data.ModelProperty<CullData> CULL_PROPERTY =
            new net.neoforged.neoforge.client.model.data.ModelProperty<>();

    public GradedFluidTankModel(
            BakedModel originalModel,
            CTSpriteShiftEntry side,
            CTSpriteShiftEntry top,
            CTSpriteShiftEntry inner
    ) {
        super(originalModel, new FluidTankCTBehaviour(side, top, inner));
    }

    @Override
    protected ModelData.Builder gatherModelData(
            ModelData.Builder builder,
            BlockAndTintGetter world,
            BlockPos pos,
            BlockState state,
            ModelData blockEntityData
    ) {
        super.gatherModelData(builder, world, pos, state, blockEntityData);
        CullData cullData = new CullData();
        for (Direction direction : Iterate.horizontalDirections) {
            cullData.setCulled(
                    direction,
                    ConnectivityHandler.isConnected(world, pos, pos.relative(direction))
            );
        }
        return builder.with(CULL_PROPERTY, cullData);
    }

    @Override
    public List<BakedQuad> getQuads(
            BlockState state,
            Direction side,
            RandomSource random,
            ModelData modelData,
            RenderType renderType
    ) {
        if (side != null) {
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new ArrayList<>();
        for (Direction direction : Iterate.directions) {
            if (modelData.has(CULL_PROPERTY)
                    && modelData.get(CULL_PROPERTY).isCulled(direction)) {
                continue;
            }
            quads.addAll(super.getQuads(state, direction, random, modelData, renderType));
        }
        quads.addAll(super.getQuads(state, null, random, modelData, renderType));
        return quads;
    }

    private static final class CullData {
        private final boolean[] culledFaces = new boolean[4];

        private CullData() {
            Arrays.fill(this.culledFaces, false);
        }

        private void setCulled(Direction face, boolean culled) {
            if (!face.getAxis().isVertical()) {
                this.culledFaces[face.get2DDataValue()] = culled;
            }
        }

        private boolean isCulled(Direction face) {
            return !face.getAxis().isVertical() && this.culledFaces[face.get2DDataValue()];
        }
    }
}
