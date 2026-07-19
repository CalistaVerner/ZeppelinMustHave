package us.kayla.zeppelinmusthave.client.renderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

public final class OmniSpeedControllerPartialModels {
    public static final PartialModel SHAFT_HALF = PartialModel.of(
            ZeppelinMustHave.id("block/omni_speed_controller/shaft_half")
    );

    private OmniSpeedControllerPartialModels() {
    }

    public static void init() {
    }
}
