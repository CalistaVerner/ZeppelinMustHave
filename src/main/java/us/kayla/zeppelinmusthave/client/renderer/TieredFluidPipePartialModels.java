package us.kayla.zeppelinmusthave.client.renderer;

import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.Direction;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.fluid.FluidPipeTier;

import java.util.EnumMap;
import java.util.Map;

/** Tier-specific copies of Create's native pipe attachment geometry. */
public final class TieredFluidPipePartialModels {
    private static final Map<FluidPipeTier, Map<ComponentPartials, Map<Direction, PartialModel>>> MODELS =
            new EnumMap<>(FluidPipeTier.class);

    static {
        for (FluidPipeTier tier : FluidPipeTier.values()) {
            Map<ComponentPartials, Map<Direction, PartialModel>> byPart = new EnumMap<>(ComponentPartials.class);
            for (ComponentPartials part : ComponentPartials.values()) {
                Map<Direction, PartialModel> byDirection = new EnumMap<>(Direction.class);
                for (Direction direction : Direction.values()) {
                    String path = "block/" + tierPath(tier) + "_fluid_pipe/attachment/"
                            + part.name().toLowerCase() + "/" + direction.getName();
                    byDirection.put(direction, PartialModel.of(ZeppelinMustHave.id(path)));
                }
                byPart.put(part, byDirection);
            }
            MODELS.put(tier, byPart);
        }
    }

    private TieredFluidPipePartialModels() {
    }

    public static void init() {
    }

    public static PartialModel get(FluidPipeTier tier, ComponentPartials part, Direction direction) {
        return MODELS.get(tier).get(part).get(direction);
    }

    private static String tierPath(FluidPipeTier tier) {
        return tier == FluidPipeTier.REINFORCED ? "reinforced" : "industrial";
    }
}
