package us.kayla.zeppelinmusthave.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.balloon.TieredEnvelopeBlock;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCatalog;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartCategory;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartDefinition;
import us.kayla.zeppelinmusthave.content.parts.ZeppelinPartTags;
import us.kayla.zeppelinmusthave.registry.ZmhTags;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZeppelinPartCoverageGameTests {
    private static final String TEMPLATE = "piped_redstone_empty";

    private ZeppelinPartCoverageGameTests() {
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void catalogExactlyCoversModRegistries(GameTestHelper helper) {
        try {
            ZeppelinPartCatalog.validateRegistryCoverage();
        } catch (RuntimeException exception) {
            helper.fail(exception.getMessage());
            return;
        }

        assertInt(helper, "Zeppelin Part item count", 35, ZeppelinPartCatalog.all().size());
        assertInt(helper, "Zeppelin Part block count", 32, ZeppelinPartCatalog.blocks().size());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void rootAndCategoryTagsCoverEveryPart(GameTestHelper helper) {
        for (ZeppelinPartDefinition part : ZeppelinPartCatalog.all()) {
            ItemStack stack = new ItemStack(part.item().get());
            if (!stack.is(ZeppelinPartTags.ALL_ITEMS)) {
                helper.fail(part.id() + " is missing from #zeppelin_must_have:zeppelin_parts");
                return;
            }
            if (!stack.is(ZeppelinPartTags.items(part.category()))) {
                helper.fail(part.id() + " is missing from category tag " + part.category());
                return;
            }

            if (part.block() != null) {
                BlockState state = part.block().get().defaultBlockState();
                if (!state.is(ZeppelinPartTags.ALL_BLOCKS)) {
                    helper.fail(part.id() + " is missing from the Zeppelin Parts block tag");
                    return;
                }
                if (!state.is(ZeppelinPartTags.blocks(part.category()))) {
                    helper.fail(part.id() + " is missing from block category " + part.category());
                    return;
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void everyBlockPartHasCompleteMiningTags(GameTestHelper helper) {
        for (ZeppelinPartDefinition part : ZeppelinPartCatalog.blocks()) {
            BlockState state = part.block().get().defaultBlockState();
            boolean envelope = state.getBlock() instanceof TieredEnvelopeBlock;
            if (envelope) {
                if (!state.is(BlockTags.MINEABLE_WITH_AXE)) {
                    helper.fail(part.id() + " is not mineable with an axe");
                    return;
                }
                continue;
            }

            if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                helper.fail(part.id() + " is not mineable with a pickaxe");
                return;
            }
            if (part.id().equals(ZeppelinMustHave.id("industrial_fluid_pipe"))) {
                if (!state.is(BlockTags.NEEDS_IRON_TOOL)) {
                    helper.fail(part.id() + " is missing the iron-tool requirement");
                    return;
                }
            } else if (!state.is(BlockTags.NEEDS_STONE_TOOL)) {
                helper.fail(part.id() + " is missing the stone-tool requirement");
                return;
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void creativeOrderContainsEveryPartExactlyOnce(GameTestHelper helper) {
        List<ResourceLocation> catalogOrder = ZeppelinPartCatalog.all().stream()
                .map(ZeppelinPartDefinition::id)
                .toList();
        List<ResourceLocation> creativeOrder = ZeppelinPartCatalog.orderedItems().stream()
                .map(supplier -> BuiltInRegistries.ITEM.getKey(supplier.get().asItem()))
                .toList();
        Set<ResourceLocation> unique = new HashSet<>(creativeOrder);

        if (!catalogOrder.equals(creativeOrder)) {
            helper.fail("Creative order differs from the canonical Zeppelin Parts catalog");
            return;
        }
        if (unique.size() != creativeOrder.size()) {
            helper.fail("Creative order contains duplicate Zeppelin Parts");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 1L, timeoutTicks = 10)
    public static void allUpgradePartsRetainCompatibilityTag(GameTestHelper helper) {
        for (ZeppelinPartDefinition part : ZeppelinPartCatalog.category(ZeppelinPartCategory.UPGRADE)) {
            if (!new ItemStack(part.item().get()).is(ZmhTags.Items.AIRSHIP_UPGRADES)) {
                helper.fail(part.id() + " is missing from #zeppelin_must_have:airship_upgrades");
                return;
            }
        }
        helper.succeed();
    }

    private static void assertInt(
            GameTestHelper helper,
            String label,
            int expected,
            int actual
    ) {
        if (expected != actual) {
            helper.fail(label + ": expected " + expected + ", got " + actual);
        }
    }
}
