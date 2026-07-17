package us.kayla.zeppelinmusthave.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;

/** Owns every vanilla registry used by the mod. */
final class ZmhRegistryContext {
    static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ZeppelinMustHave.MOD_ID);
    static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ZeppelinMustHave.MOD_ID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ZeppelinMustHave.MOD_ID);
    static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ZeppelinMustHave.MOD_ID);

    private ZmhRegistryContext() {
    }

    static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
    }
}
