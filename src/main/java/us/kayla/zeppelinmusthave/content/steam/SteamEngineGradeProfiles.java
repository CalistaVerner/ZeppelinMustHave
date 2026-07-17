package us.kayla.zeppelinmusthave.content.steam;

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

public final class SteamEngineGradeProfiles extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "steam_engine_grade_profiles";

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static final SteamEngineGradeProfiles INSTANCE = new SteamEngineGradeProfiles();

    private final AtomicLong revision = new AtomicLong();
    private volatile Map<ResourceLocation, SteamEngineGradeProfile> profiles = Map.of();

    private SteamEngineGradeProfiles() {
        super(GSON, DIRECTORY);
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SteamEngineGradeProfiles::addReloadListener);
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
        Map<ResourceLocation, SteamEngineGradeProfile> loaded = new HashMap<>();
        resources.forEach((id, element) -> {
            try {
                loaded.put(id, SteamEngineGradeProfile.parse(id, element.getAsJsonObject()));
            } catch (RuntimeException exception) {
                ZeppelinMustHave.LOGGER.error("Invalid steam engine grade profile {}", id, exception);
            }
        });

        for (SteamEngineGradeTier tier : SteamEngineGradeTier.values()) {
            if (!loaded.containsKey(tier.profileId())) {
                ZeppelinMustHave.LOGGER.error(
                        "Required steam engine grade profile {} is missing; tier {} will fail closed",
                        tier.profileId(),
                        tier.name()
                );
            }
        }

        this.profiles = Map.copyOf(loaded);
        long currentRevision = this.revision.incrementAndGet();
        ZeppelinMustHave.LOGGER.info(
                "Loaded {} steam engine grade profile(s), revision {}",
                loaded.size(),
                currentRevision
        );
    }

    public SteamEngineGradeProfile resolve(SteamEngineGradeTier tier) {
        return this.profiles.getOrDefault(
                tier.profileId(),
                SteamEngineGradeProfile.unresolved(tier.profileId())
        );
    }

    public long revision() {
        return this.revision.get();
    }
}
