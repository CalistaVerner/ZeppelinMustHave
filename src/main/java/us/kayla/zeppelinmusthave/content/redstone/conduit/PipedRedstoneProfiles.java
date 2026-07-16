package us.kayla.zeppelinmusthave.content.redstone.conduit;

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

public final class PipedRedstoneProfiles extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "piped_redstone_profiles";

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static final PipedRedstoneProfiles INSTANCE = new PipedRedstoneProfiles();

    private final AtomicLong revision = new AtomicLong();
    private volatile Map<ResourceLocation, PipedRedstoneProfile> profiles = Map.of();

    private PipedRedstoneProfiles() {
        super(GSON, DIRECTORY);
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PipedRedstoneProfiles::addReloadListener);
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
        Map<ResourceLocation, PipedRedstoneProfile> loaded = new HashMap<>();

        resources.forEach((id, element) -> {
            try {
                loaded.put(id, PipedRedstoneProfile.parse(id, element.getAsJsonObject()));
            } catch (RuntimeException exception) {
                ZeppelinMustHave.LOGGER.error("Invalid Piped Redstone profile {}", id, exception);
            }
        });

        for (PipedRedstoneTier tier : PipedRedstoneTier.values()) {
            if (!loaded.containsKey(tier.profileId())) {
                ZeppelinMustHave.LOGGER.error(
                        "Required Piped Redstone profile {} is missing; tier {} will fail closed",
                        tier.profileId(),
                        tier.name()
                );
            }
        }

        this.profiles = Map.copyOf(loaded);
        long newRevision = this.revision.incrementAndGet();
        ZeppelinMustHave.LOGGER.info(
                "Loaded {} Piped Redstone profile(s), revision {}",
                loaded.size(),
                newRevision
        );
    }

    public PipedRedstoneProfile resolve(PipedRedstoneTier tier) {
        return this.profiles.getOrDefault(
                tier.profileId(),
                PipedRedstoneProfile.unresolved(tier.profileId())
        );
    }

    public long revision() {
        return this.revision.get();
    }
}
