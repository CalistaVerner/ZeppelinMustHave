package us.kayla.zeppelinmusthave.client.renderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

public final class VerticalThrusterPartialModels {
    public static final PartialModel PROPELLER = PartialModel.of(
            ZeppelinMustHave.id("block/vertical_thruster/propeller")
    );

    private VerticalThrusterPartialModels() {
    }

    public static void init() {
        PROPELLER.modelLocation();
    }
}
