package us.kayla.zeppelinmusthave.gametest;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.advancement.ZmhAdvancements;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCatalog;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCategory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AdvancementGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private AdvancementGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void advancementTreeLoads(GameTestHelper helper) {
        List<ResourceLocation> expected = new ArrayList<>();
        expected.add(ZmhAdvancements.ROOT);
        expected.add(ZmhAdvancements.MASTER_SHIPWRIGHT);
        expected.add(ZmhAdvancements.COMMISSIONING);
        for (ZeppelinPartCategory category : ZeppelinPartCategory.values()) {
            expected.add(ZmhAdvancements.fabricationAdvancement(category));
        }
        expected.addAll(ZmhAdvancements.commissioningAdvancements());

        for (ResourceLocation id : expected) {
            if (helper.getLevel().getServer().getAdvancements().get(id) == null) {
                helper.fail("Missing advancement " + id);
                return;
            }
        }
        if (expected.size() != 25) {
            helper.fail("Expected 25 advancement definitions, got " + expected.size());
            return;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void masterShipwrightMatchesCatalog(GameTestHelper helper) {
        AdvancementHolder advancement = helper.getLevel()
                .getServer()
                .getAdvancements()
                .get(ZmhAdvancements.MASTER_SHIPWRIGHT);
        if (advancement == null) {
            helper.fail("Master Shipwright advancement is missing");
            return;
        }

        Set<String> expected = new HashSet<>();
        ZeppelinPartCatalog.all().forEach(part -> expected.add(part.id().getPath()));
        Set<String> actual = advancement.value().criteria().keySet();
        if (!actual.equals(expected)) {
            helper.fail("Master Shipwright criteria differ from ZeppelinPartCatalog");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void fabricationRoutingCoversEveryCatalogCategory(GameTestHelper helper) {
        for (ZeppelinPartCategory category : ZeppelinPartCategory.values()) {
            ResourceLocation id = ZmhAdvancements.fabricationAdvancement(category);
            AdvancementHolder advancement = helper.getLevel()
                    .getServer()
                    .getAdvancements()
                    .get(id);
            if (advancement == null) {
                helper.fail("Missing fabrication advancement for " + category);
                return;
            }
            if (!advancement.value().parent().orElseThrow().equals(ZmhAdvancements.ROOT)) {
                helper.fail("Fabrication advancement is not parented to the engineering root: " + id);
                return;
            }

            Set<String> expected = new HashSet<>();
            ZeppelinPartCatalog.category(category)
                    .forEach(part -> expected.add(part.id().getPath()));
            if (!advancement.value().criteria().keySet().equals(expected)) {
                helper.fail("Fabrication criteria differ from catalog category " + category);
                return;
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void commissioningMilestonesShareCommissioningParent(GameTestHelper helper) {
        for (ResourceLocation id : ZmhAdvancements.commissioningAdvancements()) {
            AdvancementHolder advancement = helper.getLevel()
                    .getServer()
                    .getAdvancements()
                    .get(id);
            if (advancement == null) {
                helper.fail("Missing commissioning advancement " + id);
                return;
            }
            if (!advancement.value().parent().orElseThrow().equals(ZmhAdvancements.COMMISSIONING)) {
                helper.fail("Commissioning milestone has the wrong parent: " + id);
                return;
            }
            if (!advancement.value().criteria().keySet().equals(Set.of("event"))) {
                helper.fail("Commissioning milestone must expose exactly one server-awarded criterion: " + id);
                return;
            }
        }
        helper.succeed();
    }
}
