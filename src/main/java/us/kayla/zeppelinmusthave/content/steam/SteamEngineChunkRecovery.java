package us.kayla.zeppelinmusthave.content.steam;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import us.kayla.zeppelinmusthave.ZeppelinMustHave;
import us.kayla.zeppelinmusthave.registry.ZmhBlockEntityTypes;

/** Repairs MK I/MK II block entities and crankshafts damaged by chunk load ordering. */
public final class SteamEngineChunkRecovery {
    private static boolean registered;

    private SteamEngineChunkRecovery() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        NeoForge.EVENT_BUS.addListener(SteamEngineChunkRecovery::onChunkLoad);
    }

    private static void onChunkLoad(ChunkEvent.Load event) {
        if (event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ChunkPos chunkPos = event.getChunk().getPos();
        level.getServer().execute(() -> {
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk != null) {
                repairLoadedChunk(level, chunk);
            }
        });
    }

    /** Public for GameTests; production calls originate from the delayed chunk-load handler. */
    public static void repairLoadedChunk(ServerLevel level, LevelChunk chunk) {
        LevelChunkSection[] sections = chunk.getSections();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int repairedBlockEntities = 0;
        int recoveredShafts = 0;

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section.hasOnlyAir()) {
                continue;
            }

            int minY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;
            for (int localY = 0; localY < 16; localY++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        BlockState state = section.getBlockState(localX, localY, localZ);
                        if (!(state.getBlock() instanceof SteamEngineGradeBlock engineBlock)
                                || !isBasicRecoveryTier(engineBlock.tier())) {
                            continue;
                        }

                        BlockPos pos = new BlockPos(minX + localX, minY + localY, minZ + localZ);
                        BlockEntityRepair repair = getOrRepairBlockEntity(level, chunk, pos, state);
                        if (repair == null) {
                            continue;
                        }
                        if (repair.repaired()) {
                            repairedBlockEntities++;
                        }
                        if (SteamEngineShaftController.recoverAfterLoad(repair.engine())
                                == SteamEngineShaftController.RecoveryResult.REPAIRED) {
                            recoveredShafts++;
                        }
                    }
                }
            }
        }

        if (repairedBlockEntities > 0 || recoveredShafts > 0) {
            ZeppelinMustHave.LOGGER.info(
                    "Recovered {} MK I/MK II block entity(s) and {} shaft connection(s) in chunk {}",
                    repairedBlockEntities,
                    recoveredShafts,
                    chunk.getPos()
            );
        }
    }

    private static BlockEntityRepair getOrRepairBlockEntity(
            ServerLevel level,
            LevelChunk chunk,
            BlockPos pos,
            BlockState state
    ) {
        BlockEntity existing = chunk.getBlockEntity(pos);
        if (existing instanceof SteamEngineGradeBlockEntity engine) {
            return new BlockEntityRepair(engine, false);
        }
        if (existing != null) {
            return null;
        }

        SteamEngineGradeBlockEntity replacement = new SteamEngineGradeBlockEntity(
                ZmhBlockEntityTypes.STEAM_ENGINE_GRADE.get(),
                pos,
                state
        );
        chunk.addAndRegisterBlockEntity(replacement);
        replacement.setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        return new BlockEntityRepair(replacement, true);
    }

    private static boolean isBasicRecoveryTier(SteamEngineGradeTier tier) {
        return tier == SteamEngineGradeTier.COPPER || tier == SteamEngineGradeTier.BRASS;
    }

    private record BlockEntityRepair(SteamEngineGradeBlockEntity engine, boolean repaired) {
    }
}
