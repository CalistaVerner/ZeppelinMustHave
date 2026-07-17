package us.kayla.zeppelinmusthave.content.boiler;

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

public final class BoilerGradeProfiles extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "boiler_grade_profiles";

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static final BoilerGradeProfiles INSTANCE = new BoilerGradeProfiles();

    private final AtomicLong revision = new AtomicLong();
    private volatile Map<ResourceLocation, BoilerGradeProfile> profiles = Map.of();

    private BoilerGradeProfiles() {
        super(GSON, DIRECTORY);
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(BoilerGradeProfiles::addReloadListener);
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
        Map<ResourceLocation, BoilerGradeProfile> loaded = new HashMap<>();
        resources.forEach((id, element) -> {
            try {
                loaded.put(id, BoilerGradeProfile.parse(id, element.getAsJsonObject()));
            } catch (RuntimeException exception) {
                ZeppelinMustHave.LOGGER.error("Invalid boiler grade profile {}", id, exception);
            }
        });

        for (BoilerGradeTier tier : BoilerGradeTier.values()) {
            if (!loaded.containsKey(tier.profileId())) {
                ZeppelinMustHave.LOGGER.error(
                        "Required boiler grade profile {} is missing; tier {} will fail closed",
                        tier.profileId(),
                        tier.name()
                );
            }
        }

        this.profiles = Map.copyOf(loaded);
        long currentRevision = this.revision.incrementAndGet();
        ZeppelinMustHave.LOGGER.info(
                "Loaded {} boiler grade profile(s), revision {}",
                loaded.size(),
                currentRevision
        );
    }

    public BoilerGradeProfile resolve(BoilerGradeTier tier) {
        return this.profiles.getOrDefault(
                tier.profileId(),
                BoilerGradeProfile.unresolved(tier.profileId())
        );
    }

    public long revision() {
        return this.revision.get();
    }
}
