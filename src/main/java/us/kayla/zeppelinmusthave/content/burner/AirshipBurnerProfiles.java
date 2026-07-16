package us.kayla.zeppelinmusthave.content.burner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Authoritative server-side catalog of Airship Burner profiles.
 *
 * <p>Profiles are loaded from {@code data/<namespace>/airship_burner_profiles/*.json}.
 * Bundled profiles provide defaults and data packs may replace them by resource priority.</p>
 */
public final class AirshipBurnerProfiles extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "airship_burner_profiles";

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static final AirshipBurnerProfiles INSTANCE = new AirshipBurnerProfiles();

    private final AtomicLong revision = new AtomicLong();
    private volatile Map<ResourceLocation, AirshipBurnerProfile> profiles = Map.of();

    private AirshipBurnerProfiles() {
        super(GSON, DIRECTORY);
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(AirshipBurnerProfiles::addReloadListener);
    }

    private static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resources,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        Map<ResourceLocation, AirshipBurnerProfile> loaded = new HashMap<>();

        resources.forEach((id, element) -> {
            try {
                AirshipBurnerProfile profile = AirshipBurnerProfile.parse(id, element.getAsJsonObject());
                loaded.put(id, profile);
            } catch (RuntimeException exception) {
                ZeppelinMustHave.LOGGER.error("Invalid Airship Burner profile {}", id, exception);
            }
        });

        for (AirshipBurnerTier tier : AirshipBurnerTier.values()) {
            if (!loaded.containsKey(tier.profileId())) {
                ZeppelinMustHave.LOGGER.error(
                        "Required Airship Burner profile {} is missing; tier {} will fail closed",
                        tier.profileId(),
                        tier.name()
                );
            }
        }

        this.profiles = Map.copyOf(loaded);
        long newRevision = this.revision.incrementAndGet();
        ZeppelinMustHave.LOGGER.info(
                "Loaded {} Airship Burner profile(s), revision {}",
                loaded.size(),
                newRevision
        );
    }

    public AirshipBurnerProfile resolve(ResourceLocation id) {
        AirshipBurnerProfile profile = this.profiles.get(id);
        return profile != null ? profile : AirshipBurnerProfile.unresolved(id);
    }

    public AirshipBurnerProfile resolve(AirshipBurnerTier tier) {
        return this.resolve(tier.profileId());
    }

    public long revision() {
        return this.revision.get();
    }

    public Map<ResourceLocation, AirshipBurnerProfile> snapshot() {
        return this.profiles;
    }
}
