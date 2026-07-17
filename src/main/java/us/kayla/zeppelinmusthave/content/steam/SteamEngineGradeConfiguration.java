package us.kayla.zeppelinmusthave.content.steam;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

final class SteamEngineGradeConfiguration {
    private SteamEngineGradeProfile profile;
    private long observedRevision = Long.MIN_VALUE;

    SteamEngineGradeConfiguration(ResourceLocation fallbackProfileId) {
        this.profile = SteamEngineGradeProfile.unresolved(fallbackProfileId);
    }

    SteamEngineGradeProfile profile() {
        return this.profile;
    }

    RefreshResult refresh(SteamEngineGradeTier tier, boolean force) {
        long revision = SteamEngineGradeProfiles.INSTANCE.revision();
        if (!force && this.observedRevision == revision) {
            return RefreshResult.NOT_EVALUATED;
        }

        SteamEngineGradeProfile next = SteamEngineGradeProfiles.INSTANCE.resolve(tier);
        boolean changed = !next.equals(this.profile);
        this.profile = next;
        this.observedRevision = revision;
        return new RefreshResult(true, changed);
    }

    void writeClientSnapshot(CompoundTag tag) {
        this.profile.writeClientSnapshot(tag);
    }

    void readClientSnapshot(CompoundTag tag) {
        if (tag.contains("SteamEngineGradeProfileId")) {
            this.profile = SteamEngineGradeProfile.readClientSnapshot(tag);
        }
    }

    record RefreshResult(boolean evaluated, boolean changed) {
        private static final RefreshResult NOT_EVALUATED = new RefreshResult(false, false);
    }
}
