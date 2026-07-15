package us.kayla.zeppelinmusthave.integration;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.simulated_team.simulated.Simulated;
import net.neoforged.fml.ModList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compile-time and runtime boundary for the complete physics stack used by
 * Zeppelin Must Have.
 *
 * <p>The dependency order is:</p>
 * <pre>
 * Create -> Sable -> Create Simulated -> Create Aeronautics -> Zeppelin Must Have
 * </pre>
 */
public final class SimulatedStack {
    public static final String CREATE_MOD_ID = "create";
    public static final String SABLE_MOD_ID = "sable";

    private static final List<String> REQUIRED_MODS = List.of(
            CREATE_MOD_ID,
            SABLE_MOD_ID,
            Simulated.MOD_ID,
            Aeronautics.MOD_ID
    );

    private SimulatedStack() {
    }

    public static Map<String, String> loadedVersions() {
        ModList modList = ModList.get();
        Map<String, String> versions = new LinkedHashMap<>();

        for (String modId : REQUIRED_MODS) {
            String version = modList.getModContainerById(modId)
                    .map(container -> container.getModInfo().getVersion().toString())
                    .orElseThrow(() -> new IllegalStateException(
                            "Required Zeppelin Must Have dependency is not loaded: " + modId
                    ));
            versions.put(modId, version);
        }

        return Map.copyOf(versions);
    }
}
