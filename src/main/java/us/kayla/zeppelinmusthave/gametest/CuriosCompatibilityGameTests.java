package us.kayla.zeppelinmusthave.gametest;

import dev.eriksonn.aeronautics.index.AeroItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CuriosCompatibilityGameTests {
    private CuriosCompatibilityGameTests() {
    }

    @GameTest(template = "piped_redstone_empty", setupTicks = 1L, timeoutTicks = 10)
    public static void aviatorsGogglesUseCuriosHeadTag(GameTestHelper helper) {
        var headTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "head"));
        if (!new ItemStack(AeroItems.AVIATORS_GOGGLES.get()).is(headTag)) {
            helper.fail("aeronautics:aviators_goggles is missing from #curios:head");
            return;
        }
        helper.succeed();
    }
}
