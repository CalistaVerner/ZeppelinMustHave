package us.kayla.zeppelinmusthave.client.renderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

public final class MooringWinchPartialModels {
    public static final PartialModel SHAFT = PartialModel.of(
            ZeppelinMustHave.id("block/mooring_winch/shaft")
    );
    public static final PartialModel ROPE_COIL = PartialModel.of(
            ZeppelinMustHave.id("block/mooring_winch/rope_coil")
    );

    private MooringWinchPartialModels() {
    }

    public static void init() {
        SHAFT.modelLocation();
        ROPE_COIL.modelLocation();
    }
}
