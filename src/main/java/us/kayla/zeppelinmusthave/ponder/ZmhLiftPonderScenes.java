package us.kayla.zeppelinmusthave.ponder;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import us.kayla.zeppelinmusthave.content.balloon.TieredEnvelopeBlock;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

final class ZmhLiftPonderScenes {
    private static final int PANEL_MIN_X = 2;
    private static final int PANEL_MAX_X = 4;
    private static final int PANEL_MIN_Y = 1;
    private static final int PANEL_MAX_Y = 3;
    private static final int PANEL_Z = 3;

    private ZmhLiftPonderScenes() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ZmhPonderRegistration.items(helper)
                .forComponents(
                        ZmhBlocks.REINFORCED_ENVELOPE_ITEM.get(),
                        ZmhBlocks.INDUSTRIAL_ENVELOPE_ITEM.get()
                )
                .addStoryBoard(
                        "lift/envelopes",
                        ZmhLiftPonderScenes::envelopeGrades,
                        ZmhPonderTags.ZEPPELIN_SYSTEMS
                );
    }

    private static void envelopeGrades(SceneBuilder scene, SceneBuildingUtil util) {
        BlockPos panelCenter = util.grid().at(3, 2, PANEL_Z);

        scene.title("envelope_grades", "Connected MK-Series Airship Envelopes");
        scene.configureBasePlate(0, 0, 7);
        scene.scaleSceneView(0.86F);
        setEnvelopePanel(scene, util, ZmhBlocks.REINFORCED_ENVELOPE.get(), false);
        scene.idle(1);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);
        scene.world().showSection(
                util.select().fromTo(PANEL_MIN_X, PANEL_MIN_Y, PANEL_Z, PANEL_MAX_X, PANEL_MAX_Y, PANEL_Z),
                Direction.DOWN
        );
        scene.idle(20);

        scene.overlay().showOutline(
                PonderPalette.INPUT,
                "reinforced_envelope_panel",
                util.select().fromTo(PANEL_MIN_X, PANEL_MIN_Y, PANEL_Z, PANEL_MAX_X, PANEL_MAX_Y, PANEL_Z),
                90
        );
        scene.overlay().showText(90)
                .text("Matching MK II Reinforced Envelope blocks join into one continuous hull panel; internal ribs disappear")
                .attachKeyFrame()
                .colored(PonderPalette.INPUT)
                .pointAt(Vec3.atCenterOf(panelCenter))
                .placeNearTarget();
        scene.idle(105);

        setEnvelopePanel(scene, util, ZmhBlocks.INDUSTRIAL_ENVELOPE.get(), true);
        scene.effects().indicateSuccess(panelCenter);
        scene.overlay().showOutline(
                PonderPalette.OUTPUT,
                "industrial_envelope_panel",
                util.select().fromTo(PANEL_MIN_X, PANEL_MIN_Y, PANEL_Z, PANEL_MAX_X, PANEL_MAX_Y, PANEL_Z),
                95
        );
        scene.overlay().showText(95)
                .text("MK III Industrial Envelope blocks use the same connected geometry with a cleaner pressure-hull finish")
                .attachKeyFrame()
                .colored(PonderPalette.OUTPUT)
                .pointAt(Vec3.atCenterOf(panelCenter))
                .placeNearTarget();
        scene.idle(110);

        scene.overlay().showText(90)
                .text("Only matching MK series merge; mixed envelope materials retain a visible structural joint")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .pointAt(Vec3.atCenterOf(panelCenter))
                .placeNearTarget();
        scene.idle(105);
    }

    private static void setEnvelopePanel(
            SceneBuilder scene,
            SceneBuildingUtil util,
            Block envelope,
            boolean redraw
    ) {
        for (int x = PANEL_MIN_X; x <= PANEL_MAX_X; x++) {
            for (int y = PANEL_MIN_Y; y <= PANEL_MAX_Y; y++) {
                BlockState state = envelope.defaultBlockState()
                        .setValue(TieredEnvelopeBlock.WEST, x > PANEL_MIN_X)
                        .setValue(TieredEnvelopeBlock.EAST, x < PANEL_MAX_X)
                        .setValue(TieredEnvelopeBlock.DOWN, y > PANEL_MIN_Y)
                        .setValue(TieredEnvelopeBlock.UP, y < PANEL_MAX_Y)
                        .setValue(TieredEnvelopeBlock.NORTH, false)
                        .setValue(TieredEnvelopeBlock.SOUTH, false);
                scene.world().setBlock(util.grid().at(x, y, PANEL_Z), state, redraw);
            }
        }
    }
}
