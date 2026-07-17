package us.kayla.zeppelinmusthave.content.burner;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeDefinitions;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeModifiers;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeSet;
import us.kayla.zeppelinmusthave.content.upgrade.AirshipUpgradeTarget;

/** Resolves one burner's data-pack profile and installed-upgrade modifiers. */
final class AirshipBurnerConfiguration {
    private AirshipBurnerProfile activeProfile;
    private AirshipUpgradeModifiers upgradeModifiers = AirshipUpgradeModifiers.IDENTITY;
    private long observedProfileRevision = Long.MIN_VALUE;
    private long observedDefinitionRevision = Long.MIN_VALUE;
    private long observedUpgradeRevision = Long.MIN_VALUE;

    AirshipBurnerConfiguration(ResourceLocation unresolvedProfileId) {
        this.activeProfile = AirshipBurnerProfile.unresolved(unresolvedProfileId);
    }

    AirshipBurnerProfile profile() {
        return this.activeProfile;
    }

    AirshipUpgradeModifiers modifiers() {
        return this.upgradeModifiers;
    }

    RefreshResult refresh(
            AirshipBurnerTier tier,
            AirshipUpgradeSet upgrades,
            boolean force
    ) {
        long profileRevision = AirshipBurnerProfiles.INSTANCE.revision();
        long definitionRevision = AirshipUpgradeDefinitions.INSTANCE.revision();
        long upgradeRevision = upgrades.localRevision();
        if (!force
                && profileRevision == this.observedProfileRevision
                && definitionRevision == this.observedDefinitionRevision
                && upgradeRevision == this.observedUpgradeRevision) {
            return RefreshResult.NOT_EVALUATED;
        }

        AirshipBurnerProfile baseProfile = AirshipBurnerProfiles.INSTANCE.resolve(tier);
        AirshipUpgradeModifiers nextModifiers = upgrades.modifiers(AirshipUpgradeTarget.AIRSHIP_BURNER);
        AirshipBurnerProfile nextProfile = nextModifiers.apply(baseProfile);
        boolean changed = !nextProfile.equals(this.activeProfile)
                || !nextModifiers.equals(this.upgradeModifiers);

        this.activeProfile = nextProfile;
        this.upgradeModifiers = nextModifiers;
        this.observedProfileRevision = profileRevision;
        this.observedDefinitionRevision = definitionRevision;
        this.observedUpgradeRevision = upgradeRevision;
        return new RefreshResult(true, changed);
    }

    void writeClientSnapshot(CompoundTag tag) {
        CompoundTag profileTag = new CompoundTag();
        this.activeProfile.writeClientSnapshot(profileTag);
        tag.put("ResolvedBurnerProfile", profileTag);

        CompoundTag modifierTag = new CompoundTag();
        this.upgradeModifiers.write(modifierTag);
        tag.put("ResolvedUpgradeModifiers", modifierTag);
    }

    void readClientSnapshot(CompoundTag tag) {
        if (tag.contains("ResolvedBurnerProfile")) {
            this.activeProfile = AirshipBurnerProfile.readClientSnapshot(
                    tag.getCompound("ResolvedBurnerProfile")
            );
        }
        if (tag.contains("ResolvedUpgradeModifiers")) {
            this.upgradeModifiers = AirshipUpgradeModifiers.read(
                    tag.getCompound("ResolvedUpgradeModifiers")
            );
        }
    }

    record RefreshResult(boolean evaluated, boolean changed) {
        static final RefreshResult NOT_EVALUATED = new RefreshResult(false, false);
    }
}
