package us.kayla.zeppelinmusthave.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlockEntity;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeTier;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlockEntity;
import us.kayla.zeppelinmusthave.content.control.AltitudeGaugeBlockEntity;
import us.kayla.zeppelinmusthave.content.helm.AirshipHelmBlockEntity;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlockEntity;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeBlockEntity;

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

    /*
     * ConnectivityHandler groups multiblocks by exact BlockEntityType identity.
     * One type per grade prevents mixed-grade boilers from merging.
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerGradeBlockEntity>> COPPER_BOILER =
            registerBoilerType("copper_boiler", BoilerGradeTier.COPPER);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerGradeBlockEntity>> BRASS_BOILER =
            registerBoilerType("brass_boiler", BoilerGradeTier.BRASS);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerGradeBlockEntity>> INDUSTRIAL_BOILER =
            registerBoilerType("industrial_boiler", BoilerGradeTier.INDUSTRIAL);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamEngineGradeBlockEntity>> STEAM_ENGINE_GRADE =
            BLOCK_ENTITY_TYPES.register(
                    "steam_engine_grade",
                    () -> BlockEntityType.Builder.of(
                            ZmhBlockEntityTypes::createSteamEngineGrade,
                            ZmhBlocks.COPPER_STEAM_ENGINE.get(),
                            ZmhBlocks.BRASS_STEAM_ENGINE.get(),
                            ZmhBlocks.INDUSTRIAL_STEAM_ENGINE.get()
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltitudeGaugeBlockEntity>> ALTITUDE_GAUGE =
            BLOCK_ENTITY_TYPES.register(
                    "altitude_gauge",
                    () -> BlockEntityType.Builder.of(
                            ZmhBlockEntityTypes::createAltitudeGauge,
                            ZmhBlocks.ALTITUDE_GAUGE.get()
                    ).build(null)
            );

    private ZmhBlockEntityTypes() {
    }

    public static BlockEntityType<BoilerGradeBlockEntity> forBoilerTier(BoilerGradeTier tier) {
        return switch (tier) {
            case COPPER -> COPPER_BOILER.get();
            case BRASS -> BRASS_BOILER.get();
            case INDUSTRIAL -> INDUSTRIAL_BOILER.get();
        };
    }

    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerGradeBlockEntity>> registerBoilerType(
            String name,
            BoilerGradeTier tier
    ) {
        return BLOCK_ENTITY_TYPES.register(
                name,
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new BoilerGradeBlockEntity(forBoilerTier(tier), pos, state),
                        switch (tier) {
                            case COPPER -> ZmhBlocks.COPPER_BOILER_BASE.get();
                            case BRASS -> ZmhBlocks.BRASS_BOILER_BASE.get();
                            case INDUSTRIAL -> ZmhBlocks.INDUSTRIAL_BOILER_BASE.get();
                        }
                ).build(null)
        );
    }

    private static AirshipHelmBlockEntity createAirshipHelm(BlockPos pos, BlockState state) {
        return new AirshipHelmBlockEntity(AIRSHIP_HELM.get(), pos, state);
    }

    private static AirshipBurnerBlockEntity createAirshipBurner(BlockPos pos, BlockState state) {
        return new AirshipBurnerBlockEntity(AIRSHIP_BURNER.get(), pos, state);
    }

    private static SteamEngineGradeBlockEntity createSteamEngineGrade(BlockPos pos, BlockState state) {
        return new SteamEngineGradeBlockEntity(STEAM_ENGINE_GRADE.get(), pos, state);
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

    private static AltitudeGaugeBlockEntity createAltitudeGauge(BlockPos pos, BlockState state) {
        return new AltitudeGaugeBlockEntity(ALTITUDE_GAUGE.get(), pos, state);
    }

    static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
