package us.kayla.zeppelinmusthave.content.ballast;

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

public final class BallastTankProfiles extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "ballast_tank_profiles";
    public static final ResourceLocation DEFAULT_ID = ZeppelinMustHave.id("default");

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final BallastTankProfiles INSTANCE = new BallastTankProfiles();

    private final AtomicLong revision = new AtomicLong();
    private volatile Map<ResourceLocation, BallastTankProfile> profiles = Map.of();

    private BallastTankProfiles() {
        super(GSON, DIRECTORY);
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(BallastTankProfiles::addReloadListener);
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
        Map<ResourceLocation, BallastTankProfile> loaded = new HashMap<>();
        resources.forEach((id, element) -> {
            try {
                loaded.put(id, BallastTankProfile.parse(id, element.getAsJsonObject()));
            } catch (RuntimeException exception) {
                ZeppelinMustHave.LOGGER.error("Invalid ballast tank profile {}", id, exception);
            }
        });
        this.profiles = Map.copyOf(loaded);
        long currentRevision = this.revision.incrementAndGet();
        ZeppelinMustHave.LOGGER.info(
                "Loaded {} ballast tank profile(s), revision {}",
                loaded.size(),
                currentRevision
        );
    }

    public BallastTankProfile resolveDefault() {
        return this.profiles.getOrDefault(DEFAULT_ID, BallastTankProfile.unresolved(DEFAULT_ID));
    }

    public long revision() {
        return this.revision.get();
    }
}
