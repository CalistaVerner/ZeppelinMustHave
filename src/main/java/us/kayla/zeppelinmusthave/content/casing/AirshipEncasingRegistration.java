package us.kayla.zeppelinmusthave.content.casing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

/** Registers both casing materials as native Create encasing choices after block registration. */
public final class AirshipEncasingRegistration {
    private static boolean registered;

    private AirshipEncasingRegistration() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        EncasingRegistry.addVariant(AllBlocks.SHAFT.get(), ZmhBlocks.AIRSHIP_BRASS_ENCASED_SHAFT.get());
        EncasingRegistry.addVariant(AllBlocks.SHAFT.get(), ZmhBlocks.AIRSHIP_BRONZE_ENCASED_SHAFT.get());

        EncasingRegistry.addVariant(AllBlocks.COGWHEEL.get(), ZmhBlocks.AIRSHIP_BRASS_ENCASED_COGWHEEL.get());
        EncasingRegistry.addVariant(AllBlocks.COGWHEEL.get(), ZmhBlocks.AIRSHIP_BRONZE_ENCASED_COGWHEEL.get());

        EncasingRegistry.addVariant(
                AllBlocks.LARGE_COGWHEEL.get(),
                ZmhBlocks.AIRSHIP_BRASS_ENCASED_LARGE_COGWHEEL.get()
        );
        EncasingRegistry.addVariant(
                AllBlocks.LARGE_COGWHEEL.get(),
                ZmhBlocks.AIRSHIP_BRONZE_ENCASED_LARGE_COGWHEEL.get()
        );

        EncasingRegistry.addVariant(AllBlocks.FLUID_PIPE.get(), ZmhBlocks.AIRSHIP_BRASS_ENCASED_FLUID_PIPE.get());
        EncasingRegistry.addVariant(AllBlocks.FLUID_PIPE.get(), ZmhBlocks.AIRSHIP_BRONZE_ENCASED_FLUID_PIPE.get());

        ZeppelinMustHave.LOGGER.info(
                "Registered Airship Brass and Bronze casing variants for shafts, cogwheels and fluid pipes"
        );
    }
}
