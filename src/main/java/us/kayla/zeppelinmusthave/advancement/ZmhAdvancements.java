package us.kayla.zeppelinmusthave.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCatalog;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCategory;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartDefinition;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Server-authoritative advancement gateway for Zeppelin Parts fabrication and
 * commissioning milestones.
 */
public final class ZmhAdvancements {
    public static final ResourceLocation ROOT = id("root");
    public static final ResourceLocation MASTER_SHIPWRIGHT = id("master_shipwright");
    public static final ResourceLocation COMMISSIONING = id("commissioning");

    public static final ResourceLocation FLIGHT_TELEMETRY_ONLINE = id("flight_telemetry_online");
    public static final ResourceLocation BURNER_IGNITION = id("burner_ignition");
    public static final ResourceLocation BURNER_UPGRADED = id("burner_upgraded");
    public static final ResourceLocation ALTITUDE_HOLD_ENGAGED = id("altitude_hold_engaged");
    public static final ResourceLocation FLIGHT_CONTROL_ONLINE = id("flight_control_online");
    public static final ResourceLocation ENGINE_ORDER_ISSUED = id("engine_order_issued");
    public static final ResourceLocation EMERGENCY_CUTOFF_LATCHED = id("emergency_cutoff_latched");
    public static final ResourceLocation CONTROL_LINK_CONFIGURED = id("control_link_configured");
    public static final ResourceLocation BALLAST_LOADED = id("ballast_loaded");
    public static final ResourceLocation PROTECTED_SIGNAL_ONLINE = id("protected_signal_online");
    public static final ResourceLocation STEAM_POWER_ONLINE = id("steam_power_online");
    public static final ResourceLocation VERTICAL_THRUST_ONLINE = id("vertical_thrust_online");
    public static final ResourceLocation MOORING_ATTACHED = id("mooring_attached");

    private static final Map<ZeppelinPartCategory, ResourceLocation> FABRICATION =
            fabricationAdvancements();

    private static final List<ResourceLocation> COMMISSIONING_ADVANCEMENTS = List.of(
            FLIGHT_TELEMETRY_ONLINE,
            BURNER_IGNITION,
            BURNER_UPGRADED,
            ALTITUDE_HOLD_ENGAGED,
            FLIGHT_CONTROL_ONLINE,
            ENGINE_ORDER_ISSUED,
            EMERGENCY_CUTOFF_LATCHED,
            CONTROL_LINK_CONFIGURED,
            BALLAST_LOADED,
            PROTECTED_SIGNAL_ONLINE,
            STEAM_POWER_ONLINE,
            VERTICAL_THRUST_ONLINE,
            MOORING_ATTACHED
    );

    private ZmhAdvancements() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ZmhAdvancements::onItemCrafted);
    }

    public static ResourceLocation fabricationAdvancement(ZeppelinPartCategory category) {
        return FABRICATION.get(category);
    }

    public static List<ResourceLocation> commissioningAdvancements() {
        return COMMISSIONING_ADVANCEMENTS;
    }

    private static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            recordCraft(player, event.getCrafting());
        }
    }

    public static void recordCraft(ServerPlayer player, ItemStack crafted) {
        Optional<ZeppelinPartDefinition> resolved = ZeppelinPartCatalog.find(crafted.getItem());
        if (resolved.isEmpty()) {
            return;
        }

        ZeppelinPartDefinition part = resolved.get();
        award(player, ROOT);
        award(player, fabricationAdvancement(part.category()));
        awardCriterion(player, MASTER_SHIPWRIGHT, part.id().getPath());
    }

    public static void activate(Player player, ResourceLocation advancement) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        award(serverPlayer, ROOT);
        award(serverPlayer, COMMISSIONING);
        award(serverPlayer, advancement);
    }

    public static void activateNearby(
            Level level,
            BlockPos pos,
            ResourceLocation advancement,
            double radius
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB area = new AABB(pos).inflate(Math.max(1.0D, radius));
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, area)) {
            activate(player, advancement);
        }
    }

    public static boolean award(ServerPlayer player, ResourceLocation advancementId) {
        AdvancementHolder advancement = resolve(player, advancementId);
        if (advancement == null) {
            return false;
        }

        boolean changed = false;
        var progress = player.getAdvancements().getOrStartProgress(advancement);
        for (String criterion : progress.getRemainingCriteria()) {
            changed |= player.getAdvancements().award(advancement, criterion);
        }
        return changed;
    }

    public static boolean awardCriterion(
            ServerPlayer player,
            ResourceLocation advancementId,
            String criterion
    ) {
        AdvancementHolder advancement = resolve(player, advancementId);
        return advancement != null && player.getAdvancements().award(advancement, criterion);
    }

    public static boolean isComplete(ServerPlayer player, ResourceLocation advancementId) {
        AdvancementHolder advancement = resolve(player, advancementId);
        return advancement != null
                && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public static boolean isCriterionComplete(
            ServerPlayer player,
            ResourceLocation advancementId,
            String criterion
    ) {
        AdvancementHolder advancement = resolve(player, advancementId);
        if (advancement == null) {
            return false;
        }
        var progress = player.getAdvancements().getOrStartProgress(advancement);
        var criterionProgress = progress.getCriterion(criterion);
        return criterionProgress != null && criterionProgress.isDone();
    }

    private static AdvancementHolder resolve(ServerPlayer player, ResourceLocation id) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return null;
        }
        AdvancementHolder advancement = server.getAdvancements().get(id);
        if (advancement == null) {
            ZeppelinMustHave.LOGGER.warn("Missing Zeppelin Must Have advancement {}", id);
        }
        return advancement;
    }

    private static Map<ZeppelinPartCategory, ResourceLocation> fabricationAdvancements() {
        Map<ZeppelinPartCategory, ResourceLocation> result =
                new EnumMap<>(ZeppelinPartCategory.class);
        for (ZeppelinPartCategory category : ZeppelinPartCategory.values()) {
            result.put(category, id("fabricate_" + category.path()));
        }
        return Map.copyOf(result);
    }

    private static ResourceLocation id(String path) {
        return ZeppelinMustHave.id("engineering/" + path);
    }
}
