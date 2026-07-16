package us.kayla.zeppelinmusthave.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlockEntity;
import us.kayla.zeppelinmusthave.content.helm.AirshipHelmBlockEntity;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlockEntity;

public final class ZmhBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ZeppelinMustHave.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AirshipHelmBlockEntity>> AIRSHIP_HELM =
            BLOCK_ENTITY_TYPES.register(
                    "airship_helm",
                    () -> BlockEntityType.Builder.of(
                            ZmhBlockEntityTypes::createAirshipHelm,
                            ZmhBlocks.AIRSHIP_HELM.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AirshipBurnerBlockEntity>> AIRSHIP_BURNER =
            BLOCK_ENTITY_TYPES.register(
                    "airship_burner",
                    () -> BlockEntityType.Builder.of(
                            ZmhBlockEntityTypes::createAirshipBurner,
                            ZmhBlocks.AIRSHIP_BURNER.get(),
                            ZmhBlocks.FORCED_DRAFT_AIRSHIP_BURNER.get(),
                            ZmhBlocks.INDUSTRIAL_AIRSHIP_BURNER.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PipedRedstoneNativeLeverBlockEntity>> PIPED_REDSTONE_NATIVE_LEVER =
            BLOCK_ENTITY_TYPES.register(
                    "piped_redstone_native_lever",
                    () -> BlockEntityType.Builder.of(
                            ZmhBlockEntityTypes::createPipedRedstoneNativeLever,
                            ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER.get()
                    ).build(null)
            );

    private ZmhBlockEntityTypes() {
    }

    private static AirshipHelmBlockEntity createAirshipHelm(BlockPos pos, BlockState state) {
        return new AirshipHelmBlockEntity(AIRSHIP_HELM.get(), pos, state);
    }

    private static AirshipBurnerBlockEntity createAirshipBurner(BlockPos pos, BlockState state) {
        return new AirshipBurnerBlockEntity(AIRSHIP_BURNER.get(), pos, state);
    }

    private static PipedRedstoneNativeLeverBlockEntity createPipedRedstoneNativeLever(
            BlockPos pos,
            BlockState state
    ) {
        return new PipedRedstoneNativeLeverBlockEntity(
                PIPED_REDSTONE_NATIVE_LEVER.get(),
                pos,
                state
        );
    }

    static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
