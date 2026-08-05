package us.kayla.zeppelinmusthave.client.renderer;

import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.content.fluids.PipeAttachmentModel;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogCTBehaviour;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import com.simibubi.create.foundation.data.CreateRegistrate;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

/** Client-only CT, model-wrapper and Flywheel registration for airship casing families. */
public final class AirshipCasingClient {
    private static boolean modelsRegistered;
    private static boolean visualizersRegistered;

    private AirshipCasingClient() {
    }

    public static void registerModels() {
        if (modelsRegistered) {
            return;
        }
        modelsRegistered = true;
        AirshipCasingSpriteShifts.init();

        registerMaterial(
                ZmhBlocks.AIRSHIP_BRASS_CASING.get(),
                ZmhBlocks.AIRSHIP_BRASS_ENCASED_SHAFT.get(),
                ZmhBlocks.AIRSHIP_BRASS_ENCASED_COGWHEEL.get(),
                ZmhBlocks.AIRSHIP_BRASS_ENCASED_LARGE_COGWHEEL.get(),
                ZmhBlocks.AIRSHIP_BRASS_ENCASED_FLUID_PIPE.get(),
                AirshipCasingSpriteShifts.brass()
        );
        registerMaterial(
                ZmhBlocks.AIRSHIP_BRONZE_CASING.get(),
                ZmhBlocks.AIRSHIP_BRONZE_ENCASED_SHAFT.get(),
                ZmhBlocks.AIRSHIP_BRONZE_ENCASED_COGWHEEL.get(),
                ZmhBlocks.AIRSHIP_BRONZE_ENCASED_LARGE_COGWHEEL.get(),
                ZmhBlocks.AIRSHIP_BRONZE_ENCASED_FLUID_PIPE.get(),
                AirshipCasingSpriteShifts.bronze()
        );
    }

    public static void registerVisualizers() {
        if (visualizersRegistered) {
            return;
        }
        visualizersRegistered = true;

        SimpleBlockEntityVisualizer.builder(ZmhBlockEntityTypes.AIRSHIP_ENCASED_SHAFT.get())
                .factory(SingleAxisRotatingVisual::shaft)
                .skipVanillaRender(blockEntity -> true)
                .apply();
        SimpleBlockEntityVisualizer.builder(ZmhBlockEntityTypes.AIRSHIP_ENCASED_COGWHEEL.get())
                .factory(EncasedCogVisual::small)
                .skipVanillaRender(blockEntity -> true)
                .apply();
        SimpleBlockEntityVisualizer.builder(ZmhBlockEntityTypes.AIRSHIP_ENCASED_LARGE_COGWHEEL.get())
                .factory(EncasedCogVisual::large)
                .skipVanillaRender(blockEntity -> true)
                .apply();
    }

    private static void registerMaterial(
            Block casing,
            EncasedShaftBlock shaft,
            EncasedCogwheelBlock cogwheel,
            EncasedCogwheelBlock largeCogwheel,
            EncasedPipeBlock pipe,
            AirshipCasingSpriteShifts.Shifts shifts
    ) {
        CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(shifts.casing())).accept(casing);
        CreateRegistrate.casingConnectivity((block, connectivity) ->
                connectivity.makeCasing(block, shifts.casing())).accept(casing);

        CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(shifts.casing())).accept(shaft);
        CreateRegistrate.casingConnectivity((block, connectivity) -> connectivity.make(
                block,
                shifts.casing(),
                (state, face) -> face.getAxis() != state.getValue(EncasedShaftBlock.AXIS)
        )).accept(shaft);

        CreateRegistrate.connectedTextures(() -> new EncasedCogCTBehaviour(
                shifts.casing(),
                Couple.create(shifts.cogSideVertical(), shifts.cogSideHorizontal())
        )).accept(cogwheel);
        registerCogConnectivity(cogwheel, shifts);

        CreateRegistrate.connectedTextures(() -> new EncasedCogCTBehaviour(shifts.casing())).accept(largeCogwheel);
        registerCogConnectivity(largeCogwheel, shifts);

        CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(shifts.casing())).accept(pipe);
        CreateRegistrate.casingConnectivity((block, connectivity) -> connectivity.make(
                block,
                shifts.casing(),
                (state, face) -> !state.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(face))
        )).accept(pipe);
        CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO).accept(pipe);
    }

    private static void registerCogConnectivity(
            EncasedCogwheelBlock cogwheel,
            AirshipCasingSpriteShifts.Shifts shifts
    ) {
        CreateRegistrate.casingConnectivity((block, connectivity) -> connectivity.make(
                block,
                shifts.casing(),
                (state, face) -> face.getAxis() == state.getValue(EncasedCogwheelBlock.AXIS)
                        && !state.getValue(face.getAxisDirection() == Direction.AxisDirection.POSITIVE
                        ? EncasedCogwheelBlock.TOP_SHAFT
                        : EncasedCogwheelBlock.BOTTOM_SHAFT)
        )).accept(cogwheel);
    }
}
