package us.kayla.zeppelinmusthave.gametest;

import com.simibubi.create.AllItems;
import dev.eriksonn.aeronautics.index.AeroItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.integration.curios.CuriosCompat;

@GameTestHolder(ZeppelinMustHave.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CuriosCompatibilityGameTests {
    private CuriosCompatibilityGameTests() {
    }

    @GameTest(template = "piped_redstone_empty", setupTicks = 1L, timeoutTicks = 10)
    public static void gogglesUseDedicatedCuriosSlot(GameTestHelper helper) {
        var gogglesTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "goggles"));
        ItemStack engineersGoggles = new ItemStack(AllItems.GOGGLES.get());
        ItemStack aviatorsGoggles = new ItemStack(AeroItems.AVIATORS_GOGGLES.get());

        if (!engineersGoggles.is(gogglesTag)) {
            helper.fail("create:goggles is missing from #curios:goggles");
            return;
        }
        if (!aviatorsGoggles.is(gogglesTag)) {
            helper.fail("aeronautics:aviators_goggles is missing from #curios:goggles");
            return;
        }

        var slot = CuriosApi.getSlot(CuriosCompat.GOGGLES_SLOT, false).orElse(null);
        if (slot == null) {
            helper.fail("Curios goggles slot was not loaded");
            return;
        }
        if (slot.getSize() != 1) {
            helper.fail("Curios goggles slot must contain exactly one item, got " + slot.getSize());
            return;
        }
        if (!slot.getIcon().equals(ZeppelinMustHave.id("slot/empty_goggles_slot"))) {
            helper.fail("Curios goggles slot uses the wrong empty-slot icon: " + slot.getIcon());
            return;
        }
        if (!CuriosApi.getPlayerSlots(false).containsKey(CuriosCompat.GOGGLES_SLOT)) {
            helper.fail("Curios goggles slot was not assigned to players");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "piped_redstone_empty", setupTicks = 1L, timeoutTicks = 20)
    public static void gogglesAreExclusiveAndRejectLegacyHeadSlot(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        SlotContext gogglesSlot = new SlotContext(
                CuriosCompat.GOGGLES_SLOT,
                player,
                0,
                false,
                true
        );
        SlotContext headSlot = new SlotContext("head", player, 0, false, true);

        ItemStack engineersGoggles = new ItemStack(AllItems.GOGGLES.get());
        ItemStack aviatorsGoggles = new ItemStack(AeroItems.AVIATORS_GOGGLES.get());
        var engineersCurio = CuriosApi.getCurio(engineersGoggles).orElse(null);
        var aviatorsCurio = CuriosApi.getCurio(aviatorsGoggles).orElse(null);

        if (engineersCurio == null || aviatorsCurio == null) {
            helper.fail("Supported goggles are missing their Curios capability");
            return;
        }
        if (!engineersCurio.canEquip(gogglesSlot) || !aviatorsCurio.canEquip(gogglesSlot)) {
            helper.fail("A supported goggles item cannot be equipped in the dedicated slot");
            return;
        }
        if (engineersCurio.canEquip(headSlot) || aviatorsCurio.canEquip(headSlot)) {
            helper.fail("Supported goggles still accept the legacy Curios head slot");
            return;
        }

        var handler = CuriosApi.getCuriosInventory(player).orElse(null);
        if (handler == null) {
            helper.fail("Mock player is missing the Curios inventory capability");
            return;
        }
        handler.setEquippedCurio(CuriosCompat.GOGGLES_SLOT, 0, engineersGoggles);
        if (aviatorsCurio.canEquip(gogglesSlot)) {
            helper.fail("Aviator's Goggles can be equipped while Engineer's Goggles are already installed");
            return;
        }
        helper.succeed();
    }
}
