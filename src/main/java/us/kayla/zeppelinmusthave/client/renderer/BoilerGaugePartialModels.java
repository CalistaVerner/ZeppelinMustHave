package us.kayla.zeppelinmusthave.client.renderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeTier;

/** Grade-specific boiler gauge body and animated needle models. */
public final class BoilerGaugePartialModels {
    private static final GaugeModels COPPER = create("copper");
    private static final GaugeModels BRASS = create("brass");
    private static final GaugeModels INDUSTRIAL = create("industrial");

    private BoilerGaugePartialModels() {
    }

    public static void init() {
        register(COPPER);
        register(BRASS);
        register(INDUSTRIAL);
    }

    public static GaugeModels forTier(BoilerGradeTier tier) {
        return switch (tier) {
            case COPPER -> COPPER;
            case BRASS -> BRASS;
            case INDUSTRIAL -> INDUSTRIAL;
        };
    }

    private static GaugeModels create(String tier) {
        String root = "block/boiler_gauge/" + tier + "/";
        return new GaugeModels(
                PartialModel.of(ZeppelinMustHave.id(root + "gauge")),
                PartialModel.of(ZeppelinMustHave.id(root + "dial"))
        );
    }

    private static void register(GaugeModels models) {
        models.gauge().modelLocation();
        models.dial().modelLocation();
    }

    public record GaugeModels(PartialModel gauge, PartialModel dial) {
    }
}
