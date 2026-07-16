package us.kayla.zeppelinmusthave.content.upgrade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class AirshipUpgradeDefinitions extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "airship_upgrades";

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static final AirshipUpgradeDefinitions INSTANCE = new AirshipUpgradeDefinitions();

    private final AtomicLong revision = new AtomicLong();
    private volatile Map<ResourceLocation, AirshipUpgradeDefinition> definitions = Map.of();

    private AirshipUpgradeDefinitions() {
        super(GSON, DIRECTORY);
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(AirshipUpgradeDefinitions::addReloadListener);
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
        Map<ResourceLocation, AirshipUpgradeDefinition> loaded = new HashMap<>();

        resources.forEach((id, element) -> {
            try {
                AirshipUpgradeDefinition definition = AirshipUpgradeDefinition.parse(
                        id,
                        element.getAsJsonObject()
                );
                if (!BuiltInRegistries.ITEM.containsKey(id)) {
                    ZeppelinMustHave.LOGGER.warn(
                            "Airship upgrade definition {} has no registered item and will be ignored",
                            id
                    );
                    return;
                }
                loaded.put(id, definition);
            } catch (RuntimeException exception) {
                ZeppelinMustHave.LOGGER.error("Invalid airship upgrade definition {}", id, exception);
            }
        });

        this.definitions = Map.copyOf(loaded);
        long newRevision = this.revision.incrementAndGet();
        ZeppelinMustHave.LOGGER.info(
                "Loaded {} airship upgrade definition(s), revision {}",
                loaded.size(),
                newRevision
        );
    }

    public Optional<AirshipUpgradeDefinition> resolve(ResourceLocation id) {
        return Optional.ofNullable(this.definitions.get(id));
    }

    public Optional<AirshipUpgradeDefinition> resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return this.resolve(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public boolean isKnownUpgrade(ItemStack stack) {
        return this.resolve(stack).isPresent();
    }

    public long revision() {
        return this.revision.get();
    }

    public Map<ResourceLocation, AirshipUpgradeDefinition> snapshot() {
        return this.definitions;
    }
}
