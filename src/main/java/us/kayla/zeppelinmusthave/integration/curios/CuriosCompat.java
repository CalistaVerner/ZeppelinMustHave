package us.kayla.zeppelinmusthave.integration.curios;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import dev.eriksonn.aeronautics.index.AeroItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

import java.util.List;

/** Optional Curios integration, loaded only after a successful ModList check. */
public final class CuriosCompat {
    public static final String GOGGLES_SLOT = "goggles";

    private static final ICurioItem EXCLUSIVE_GOGGLES_BEHAVIOUR = new ICurioItem() {
        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            if (!GOGGLES_SLOT.equals(slotContext.identifier()) || slotContext.cosmetic()) {
                return false;
            }
            if (slotContext.entity() == null) {
                return true;
            }

            return CuriosApi.getCuriosInventory(slotContext.entity())
                    .flatMap(handler -> handler.findFirstCurio(CuriosCompat::isSupportedGoggles))
                    .map(existing -> existing.slotContext().identifier().equals(slotContext.identifier())
                            && existing.slotContext().index() == slotContext.index()
                            && ItemStack.matches(existing.stack(), stack))
                    .orElse(true);
        }

        @Override
        public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
            return this.canEquip(slotContext, stack);
        }

        @Override
        public List<Component> getSlotsTooltip(
                List<Component> tooltips,
                Item.TooltipContext context,
                ItemStack stack
        ) {
            return List.of(Component.translatable("curios.tooltip.slot")
                    .append(" ")
                    .append(Component.translatable("curios.identifier.goggles")
                            .withStyle(ChatFormatting.YELLOW))
                    .withStyle(ChatFormatting.GOLD));
        }
    };

    private CuriosCompat() {
    }

    public static void register() {
        CuriosApi.registerCurio(AllItems.GOGGLES.get(), EXCLUSIVE_GOGGLES_BEHAVIOUR);
        CuriosApi.registerCurio(AeroItems.AVIATORS_GOGGLES.get(), EXCLUSIVE_GOGGLES_BEHAVIOUR);

        GogglesItem.addIsWearingPredicate(player -> CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(AeroItems.AVIATORS_GOGGLES.get()))
                .orElse(false));
        ZeppelinMustHave.LOGGER.info(
                "Enabled exclusive Curios goggles slot for create:goggles and aeronautics:aviators_goggles"
        );
    }

    public static boolean isSupportedGoggles(ItemStack stack) {
        return stack.is(AllItems.GOGGLES.get()) || stack.is(AeroItems.AVIATORS_GOGGLES.get());
    }
}
