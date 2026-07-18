package us.kayla.zeppelinmusthave.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCatalog;
import us.kayla.zeppelinmusthave.ponder.ZmhPonderManifest;

import java.util.LinkedHashSet;
import java.util.Set;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PonderCoverageGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private PonderCoverageGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void everyZeppelinPartHasPonderCoverage(GameTestHelper helper) {
        Set<ResourceLocation> expected = new LinkedHashSet<>();
        ZeppelinPartCatalog.all().forEach(part -> expected.add(part.id()));
        Set<ResourceLocation> actual = new LinkedHashSet<>(ZmhPonderManifest.allPartIdSet());

        if (!expected.equals(actual)) {
            Set<ResourceLocation> missing = new LinkedHashSet<>(expected);
            missing.removeAll(actual);
            Set<ResourceLocation> stale = new LinkedHashSet<>(actual);
            stale.removeAll(expected);
            helper.fail("Ponder coverage mismatch; missing=" + missing + ", stale=" + stale);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void everyZeppelinPartHasSpecializedPonderCoverage(GameTestHelper helper) {
        Set<ResourceLocation> expected = new LinkedHashSet<>();
        ZeppelinPartCatalog.all().forEach(part -> expected.add(part.id()));
        Set<ResourceLocation> actual = new LinkedHashSet<>(ZmhPonderManifest.specificPartIdSet());

        if (!expected.equals(actual)) {
            Set<ResourceLocation> missing = new LinkedHashSet<>(expected);
            missing.removeAll(actual);
            Set<ResourceLocation> stale = new LinkedHashSet<>(actual);
            stale.removeAll(expected);
            helper.fail("Specialized Ponder coverage mismatch; missing=" + missing + ", stale=" + stale);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void everyStoryboardTemplateIsPresentAndValid(GameTestHelper helper) {
        try {
            ZmhPonderManifest.validatePackagedTemplates();
        } catch (RuntimeException exception) {
            helper.fail(exception.getMessage());
            return;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 20)
    public static void storyboardManifestHasNoDuplicatePaths(GameTestHelper helper) {
        Set<String> unique = new LinkedHashSet<>(ZmhPonderManifest.storyboards());
        if (unique.size() != ZmhPonderManifest.storyboards().size()) {
            helper.fail("Ponder storyboard manifest contains duplicate paths");
            return;
        }
        helper.succeed();
    }
    @GameTest(template = TEMPLATE)
    public static void everyPonderLocalizationKeyExistsInAllLocales(GameTestHelper helper) {
        try {
            ZmhPonderManifest.validateLanguageFiles();
            helper.succeed();
        } catch (RuntimeException exception) {
            helper.fail(exception.getMessage());
        }
    }

}
