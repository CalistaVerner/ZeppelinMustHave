package us.kayla.zeppelinmusthave.client.renderer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;
import us.kayla.zeppelinmusthave.content.fluid.FluidPipeTier;
import us.kayla.zeppelinmusthave.content.fluid.TieredFluidPipeBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create's native pipe model-data pipeline with tier-specific attachment partials.
 * This preserves Create geometry while preventing copper partials from leaking into
 * reinforced and industrial pipes.
 */
public final class TieredPipeAttachmentModel extends BakedModelWrapperWithData {
    private static final ModelProperty<PipeModelData> PIPE_PROPERTY = new ModelProperty<>();
    private final boolean ambientOcclusion;

    private TieredPipeAttachmentModel(BakedModel template, boolean ambientOcclusion) {
        super(template);
        this.ambientOcclusion = ambientOcclusion;
    }

    public static TieredPipeAttachmentModel withoutAO(BakedModel template) {
        return new TieredPipeAttachmentModel(template, false);
    }

    @Override
    protected ModelData.Builder gatherModelData(
            ModelData.Builder builder,
            BlockAndTintGetter world,
            BlockPos pos,
            BlockState state,
            ModelData blockEntityData
    ) {
        PipeModelData data = new PipeModelData(tierFor(state));
        FluidTransportBehaviour transport = BlockEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
        BracketedBlockEntityBehaviour bracket =
                BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);

        if (transport != null) {
            for (Direction direction : Iterate.directions) {
                data.putAttachment(direction, transport.getRenderedRimAttachment(world, pos, state, direction));
            }
        }
        if (bracket != null) {
            data.putBracket(bracket.getBracket());
        }
        data.setEncased(FluidPipeBlock.shouldDrawCasing(world, pos, state));
        return builder.with(PIPE_PROPERTY, data);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(
            @NotNull BlockState state,
            @NotNull RandomSource random,
            @NotNull ModelData data
    ) {
        List<ChunkRenderTypeSet> sets = new ArrayList<>();
        sets.add(super.getRenderTypes(state, random, data));
        sets.add(AllPartialModels.FLUID_PIPE_CASING.get().getRenderTypes(state, random, data));

        if (data.has(PIPE_PROPERTY)) {
            PipeModelData pipeData = data.get(PIPE_PROPERTY);
            for (Direction direction : Iterate.directions) {
                AttachmentTypes type = pipeData.getAttachment(direction);
                for (ComponentPartials partial : type.partials) {
                    sets.add(TieredFluidPipePartialModels.get(pipeData.tier(), partial, direction)
                            .get().getRenderTypes(state, random, data));
                }
            }
        }
        return ChunkRenderTypeSet.union(sets);
    }

    @Override
    public List<BakedQuad> getQuads(
            BlockState state,
            Direction side,
            RandomSource random,
            ModelData data,
            RenderType renderType
    ) {
        List<BakedQuad> quads = super.getQuads(state, side, random, data, renderType);
        if (!data.has(PIPE_PROPERTY)) {
            return quads;
        }
        PipeModelData pipeData = data.get(PIPE_PROPERTY);
        quads = new ArrayList<>(quads);

        BakedModel bracket = pipeData.getBracket();
        if (bracket != null) {
            quads.addAll(bracket.getQuads(state, side, random, data, renderType));
        }
        for (Direction direction : Iterate.directions) {
            AttachmentTypes type = pipeData.getAttachment(direction);
            for (ComponentPartials partial : type.partials) {
                quads.addAll(TieredFluidPipePartialModels.get(pipeData.tier(), partial, direction)
                        .get().getQuads(state, side, random, data, renderType));
            }
        }
        if (pipeData.isEncased()) {
            quads.addAll(AllPartialModels.FLUID_PIPE_CASING.get()
                    .getQuads(state, side, random, data, renderType));
        }
        return quads;
    }

    @Override
    public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        return ambientOcclusion ? TriState.TRUE : TriState.FALSE;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return ambientOcclusion;
    }

    private static FluidPipeTier tierFor(BlockState state) {
        if (state.getBlock() instanceof TieredFluidPipeBlock pipe) {
            return pipe.tier();
        }
        return FluidPipeTier.REINFORCED;
    }

    private static final class PipeModelData {
        private final FluidPipeTier tier;
        private final AttachmentTypes[] attachments = new AttachmentTypes[6];
        private boolean encased;
        private BakedModel bracket;

        private PipeModelData(FluidPipeTier tier) {
            this.tier = tier;
            Arrays.fill(attachments, AttachmentTypes.NONE);
        }

        private FluidPipeTier tier() {
            return tier;
        }

        private void putBracket(BlockState state) {
            if (state != null) {
                bracket = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
            }
        }

        private BakedModel getBracket() {
            return bracket;
        }

        private void putAttachment(Direction face, AttachmentTypes type) {
            attachments[face.get3DDataValue()] = type;
        }

        private AttachmentTypes getAttachment(Direction face) {
            return attachments[face.get3DDataValue()];
        }

        private void setEncased(boolean encased) {
            this.encased = encased;
        }

        private boolean isEncased() {
            return encased;
        }
    }
}
