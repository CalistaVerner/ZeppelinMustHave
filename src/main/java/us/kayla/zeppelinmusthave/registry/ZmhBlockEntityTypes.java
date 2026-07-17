package us.kayla.zeppelinmusthave.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import us.kayla.zeppelinmusthave.content.ballast.BallastTankBlockEntity;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlockEntity;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeTier;
import us.kayla.zeppelinmusthave.content.burner.AirshipBurnerBlockEntity;
import us.kayla.zeppelinmusthave.content.control.AltitudeGaugeBlockEntity;
import us.kayla.zeppelinmusthave.content.helm.AirshipHelmBlockEntity;
import us.kayla.zeppelinmusthave.content.mooring.MooringWinchBlockEntity;
import us.kayla.zeppelinmusthave.content.redstone.conduit.PipedRedstoneNativeLeverBlockEntity;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeBlockEntity;
import us.kayla.zeppelinmusthave.content.thruster.VerticalThrusterBlockEntity;

import java.util.Arrays;
import java.util.function.Supplier;

public final class ZmhBlockEntityTypes {
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AirshipHelmBlockEntity>> AIRSHIP_HELM =
            register("airship_helm", ZmhBlockEntityTypes::createAirshipHelm, ZmhBlocks.AIRSHIP_HELM);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AirshipBurnerBlockEntity>> AIRSHIP_BURNER =
            register(
                    "airship_burner",
                    ZmhBlockEntityTypes::createAirshipBurner,
                    ZmhBlocks.AIRSHIP_BURNER,
                    ZmhBlocks.FORCED_DRAFT_AIRSHIP_BURNER,
                    ZmhBlocks.INDUSTRIAL_AIRSHIP_BURNER
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BallastTankBlockEntity>> BALLAST_TANK =
            register("ballast_tank", ZmhBlockEntityTypes::createBallastTank, ZmhBlocks.BALLAST_TANK);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MooringWinchBlockEntity>> MOORING_WINCH =
            register("mooring_winch", ZmhBlockEntityTypes::createMooringWinch, ZmhBlocks.MOORING_WINCH);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VerticalThrusterBlockEntity>> VERTICAL_THRUSTER =
            register(
                    "vertical_thruster",
                    ZmhBlockEntityTypes::createVerticalThruster,
                    ZmhBlocks.VERTICAL_THRUSTER
            );

    /* ConnectivityHandler groups tank multiblocks by exact type identity. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerGradeBlockEntity>> COPPER_BOILER =
            registerBoiler("copper_boiler", BoilerGradeTier.COPPER, ZmhBlocks.COPPER_BOILER_BASE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerGradeBlockEntity>> BRASS_BOILER =
            registerBoiler("brass_boiler", BoilerGradeTier.BRASS, ZmhBlocks.BRASS_BOILER_BASE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerGradeBlockEntity>> INDUSTRIAL_BOILER =
            registerBoiler("industrial_boiler", BoilerGradeTier.INDUSTRIAL, ZmhBlocks.INDUSTRIAL_BOILER_BASE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamEngineGradeBlockEntity>> STEAM_ENGINE_GRADE =
            register(
                    "steam_engine_grade",
                    ZmhBlockEntityTypes::createSteamEngineGrade,
                    ZmhBlocks.COPPER_STEAM_ENGINE,
                    ZmhBlocks.BRASS_STEAM_ENGINE,
                    ZmhBlocks.INDUSTRIAL_STEAM_ENGINE
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PipedRedstoneNativeLeverBlockEntity>>
            PIPED_REDSTONE_NATIVE_LEVER = register(
                    "piped_redstone_native_lever",
                    ZmhBlockEntityTypes::createPipedRedstoneNativeLever,
                    ZmhBlocks.PIPED_REDSTONE_NATIVE_LEVER
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltitudeGaugeBlockEntity>> ALTITUDE_GAUGE =
            register("altitude_gauge", ZmhBlockEntityTypes::createAltitudeGauge, ZmhBlocks.ALTITUDE_GAUGE);

    private ZmhBlockEntityTypes() {
    }

    static void bootstrap() {
    }

    public static BlockEntityType<BoilerGradeBlockEntity> forBoilerTier(BoilerGradeTier tier) {
        return switch (tier) {
            case COPPER -> COPPER_BOILER.get();
            case BRASS -> BRASS_BOILER.get();
            case INDUSTRIAL -> INDUSTRIAL_BOILER.get();
        };
    }

    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerGradeBlockEntity>> registerBoiler(
            String name,
            BoilerGradeTier tier,
            Supplier<? extends Block> validBlock
    ) {
        return register(
                name,
                (pos, state) -> new BoilerGradeBlockEntity(forBoilerTier(tier), pos, state),
                validBlock
        );
    }

    @SafeVarargs
    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
            String name,
            BlockEntityType.BlockEntitySupplier<T> factory,
            Supplier<? extends Block>... validBlocks
    ) {
        return ZmhRegistryContext.BLOCK_ENTITY_TYPES.register(
                name,
                () -> BlockEntityType.Builder.of(
                        factory,
                        Arrays.stream(validBlocks)
                                .map(Supplier::get)
                                .toArray(Block[]::new)
                ).build(null)
        );
    }

    private static AirshipHelmBlockEntity createAirshipHelm(BlockPos pos, BlockState state) {
        return new AirshipHelmBlockEntity(AIRSHIP_HELM.get(), pos, state);
    }

    private static AirshipBurnerBlockEntity createAirshipBurner(BlockPos pos, BlockState state) {
        return new AirshipBurnerBlockEntity(AIRSHIP_BURNER.get(), pos, state);
    }

    private static BallastTankBlockEntity createBallastTank(BlockPos pos, BlockState state) {
        return new BallastTankBlockEntity(BALLAST_TANK.get(), pos, state);
    }

    private static MooringWinchBlockEntity createMooringWinch(BlockPos pos, BlockState state) {
        return new MooringWinchBlockEntity(MOORING_WINCH.get(), pos, state);
    }

    private static VerticalThrusterBlockEntity createVerticalThruster(BlockPos pos, BlockState state) {
        return new VerticalThrusterBlockEntity(VERTICAL_THRUSTER.get(), pos, state);
    }

    private static SteamEngineGradeBlockEntity createSteamEngineGrade(BlockPos pos, BlockState state) {
        return new SteamEngineGradeBlockEntity(STEAM_ENGINE_GRADE.get(), pos, state);
    }

    private static PipedRedstoneNativeLeverBlockEntity createPipedRedstoneNativeLever(
            BlockPos pos,
            BlockState state
    ) {
        return new PipedRedstoneNativeLeverBlockEntity(PIPED_REDSTONE_NATIVE_LEVER.get(), pos, state);
    }

    private static AltitudeGaugeBlockEntity createAltitudeGauge(BlockPos pos, BlockState state) {
        return new AltitudeGaugeBlockEntity(ALTITUDE_GAUGE.get(), pos, state);
    }
}
