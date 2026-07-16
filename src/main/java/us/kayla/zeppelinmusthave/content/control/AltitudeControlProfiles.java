package us.kayla.zeppelinmusthave.content.control;

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

public final class AltitudeControlProfiles extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "altitude_control_profiles";
    public static final ResourceLocation DEFAULT_ID = ZeppelinMustHave.id("default");

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static final AltitudeControlProfiles INSTANCE = new AltitudeControlProfiles();

    private final AtomicLong revision = new AtomicLong();
    private volatile Map<ResourceLocation, AltitudeControlProfile> profiles = Map.of();

    private AltitudeControlProfiles() {
        super(GSON, DIRECTORY);
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(AltitudeControlProfiles::addReloadListener);
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
        Map<ResourceLocation, AltitudeControlProfile> loaded = new HashMap<>();
        resources.forEach((id, element) -> {
            try {
                loaded.put(id, AltitudeControlProfile.parse(id, element.getAsJsonObject()));
            } catch (RuntimeException exception) {
                ZeppelinMustHave.LOGGER.error("Invalid altitude control profile {}", id, exception);
            }
        });

        if (!loaded.containsKey(DEFAULT_ID)) {
            ZeppelinMustHave.LOGGER.error(
                    "Required altitude control profile {} is missing; control gauges will fail closed",
                    DEFAULT_ID
            );
        }

        this.profiles = Map.copyOf(loaded);
        long currentRevision = this.revision.incrementAndGet();
        ZeppelinMustHave.LOGGER.info(
                "Loaded {} altitude control profile(s), revision {}",
                loaded.size(),
                currentRevision
        );
    }

    public AltitudeControlProfile resolveDefault() {
        return this.profiles.getOrDefault(
                DEFAULT_ID,
                AltitudeControlProfile.unresolved(DEFAULT_ID)
        );
    }

    public long revision() {
        return this.revision.get();
    }
}
