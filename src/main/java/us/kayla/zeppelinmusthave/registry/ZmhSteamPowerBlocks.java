package us.kayla.zeppelinmusthave.registry;

import net.minecraft.world.item.BlockItem;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeBlock;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeItem;
import us.kayla.zeppelinmusthave.content.boiler.BoilerGradeTier;
import us.kayla.zeppelinmusthave.content.steam.LeviathanSteamEngineBlock;
import us.kayla.zeppelinmusthave.content.steam.MkViiSteamEngineBlock;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeBlock;
import us.kayla.zeppelinmusthave.content.steam.SteamEngineGradeTier;

/** Boiler and steam-engine block families. */
final class ZmhSteamPowerBlocks {
    static final RegisteredBlock<BoilerGradeBlock, BoilerGradeItem> COPPER_BOILER =
            boiler("copper_boiler_base", BoilerGradeTier.COPPER);
    static final RegisteredBlock<BoilerGradeBlock, BoilerGradeItem> BRASS_BOILER =
            boiler("brass_boiler_base", BoilerGradeTier.BRASS);
    static final RegisteredBlock<BoilerGradeBlock, BoilerGradeItem> INDUSTRIAL_BOILER =
            boiler("industrial_boiler_base", BoilerGradeTier.INDUSTRIAL);

    static final RegisteredBlock<SteamEngineGradeBlock, BlockItem> COPPER_ENGINE =
            engine("copper_steam_engine", SteamEngineGradeTier.COPPER);
    static final RegisteredBlock<SteamEngineGradeBlock, BlockItem> BRASS_ENGINE =
            engine("brass_steam_engine", SteamEngineGradeTier.BRASS);
    static final RegisteredBlock<SteamEngineGradeBlock, BlockItem> INDUSTRIAL_ENGINE =
            engine("industrial_steam_engine", SteamEngineGradeTier.INDUSTRIAL);
    static final RegisteredBlock<SteamEngineGradeBlock, BlockItem> GRAND_ENGINE =
            engine("grand_steam_engine", SteamEngineGradeTier.GRAND);
    static final RegisteredBlock<SteamEngineGradeBlock, BlockItem> SOVEREIGN_ENGINE =
            engine("sovereign_steam_engine", SteamEngineGradeTier.SOVEREIGN);
    static final RegisteredBlock<LeviathanSteamEngineBlock, BlockItem> LEVIATHAN_ENGINE =
            ZmhBlockRegistrar.register(
                    "leviathan_steam_engine",
                    () -> new LeviathanSteamEngineBlock(ZmhBlockProperties.steamEngine())
            );
    static final RegisteredBlock<MkViiSteamEngineBlock, BlockItem> MK_VII_ENGINE =
            ZmhBlockRegistrar.register(
                    "mk_vii_steam_engine",
                    () -> new MkViiSteamEngineBlock(ZmhBlockProperties.steamEngine())
            );

    private ZmhSteamPowerBlocks() {
    }

    private static RegisteredBlock<BoilerGradeBlock, BoilerGradeItem> boiler(
            String name,
            BoilerGradeTier tier
    ) {
        return ZmhBlockRegistrar.register(
                name,
                () -> new BoilerGradeBlock(ZmhBlockProperties.boiler(), tier),
                BoilerGradeItem::new
        );
    }

    private static RegisteredBlock<SteamEngineGradeBlock, BlockItem> engine(
            String name,
            SteamEngineGradeTier tier
    ) {
        return ZmhBlockRegistrar.register(
                name,
                () -> new SteamEngineGradeBlock(ZmhBlockProperties.steamEngine(), tier)
        );
    }
}
