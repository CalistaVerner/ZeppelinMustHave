package us.kayla.zeppelinmusthave.content.steam;

import com.simibubi.create.api.stress.BlockStressValues;
import net.minecraft.world.level.block.Block;
import us.kayla.zeppelinmusthave.registry.ZmhBlocks;

import java.util.Map;
import java.util.function.Supplier;

/** Registers Create stress capacity and generated-RPM metadata for graded engines. */
public final class SteamEngineStressRegistration {
    private static final Map<SteamEngineGradeTier, Supplier<? extends Block>> ENGINES = Map.of(
            SteamEngineGradeTier.COPPER, ZmhBlocks.COPPER_STEAM_ENGINE,
            SteamEngineGradeTier.BRASS, ZmhBlocks.BRASS_STEAM_ENGINE,
            SteamEngineGradeTier.INDUSTRIAL, ZmhBlocks.INDUSTRIAL_STEAM_ENGINE,
            SteamEngineGradeTier.GRAND, ZmhBlocks.GRAND_STEAM_ENGINE,
            SteamEngineGradeTier.SOVEREIGN, ZmhBlocks.SOVEREIGN_STEAM_ENGINE,
            SteamEngineGradeTier.LEVIATHAN, ZmhBlocks.LEVIATHAN_STEAM_ENGINE,
            SteamEngineGradeTier.MK_VII, ZmhBlocks.MK_VII_STEAM_ENGINE
    );

    private SteamEngineStressRegistration() {
    }

    public static void registerAll() {
        ENGINES.forEach(SteamEngineStressRegistration::register);
    }

    private static void register(
            SteamEngineGradeTier tier,
            Supplier<? extends Block> blockSupplier
    ) {
        Block block = blockSupplier.get();
        BlockStressValues.CAPACITIES.register(
                block,
                () -> SteamEngineGradeProfiles.INSTANCE.resolve(tier).stressCapacity()
        );
        BlockStressValues.RPM.register(block, new BlockStressValues.GeneratedRpm(64, true));
    }
}
