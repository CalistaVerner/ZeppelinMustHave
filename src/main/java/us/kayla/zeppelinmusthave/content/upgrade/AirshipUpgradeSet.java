package us.kayla.zeppelinmusthave.content.upgrade;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistent one-item-per-slot upgrade inventory.
 */
public final class AirshipUpgradeSet {
    private final EnumMap<AirshipUpgradeSlot, ItemStack> installed =
            new EnumMap<>(AirshipUpgradeSlot.class);
    private long localRevision;

    public InstallResult install(
            ItemStack offered,
            AirshipUpgradeTarget target,
            boolean simulate
    ) {
        Optional<AirshipUpgradeDefinition> resolved =
                AirshipUpgradeDefinitions.INSTANCE.resolve(offered);
        if (resolved.isEmpty()) {
            return InstallResult.failure(InstallStatus.MISSING_DEFINITION, null);
        }

        AirshipUpgradeDefinition definition = resolved.get();
        if (!definition.supports(target)) {
            return InstallResult.failure(InstallStatus.WRONG_TARGET, definition);
        }
        if (!this.get(definition.slot()).isEmpty()) {
            return InstallResult.failure(InstallStatus.SLOT_OCCUPIED, definition);
        }

        for (AirshipUpgradeDefinition active : this.activeDefinitions(target)) {
            if (definition.conflictsWith(active)) {
                return InstallResult.failure(InstallStatus.CONFLICT, definition);
            }
        }

        if (!simulate) {
            this.installed.put(definition.slot(), offered.copyWithCount(1));
            this.localRevision++;
        }
        return InstallResult.success(definition);
    }

    public ItemStack removeLast() {
        AirshipUpgradeSlot[] slots = AirshipUpgradeSlot.values();
        for (int index = slots.length - 1; index >= 0; index--) {
            AirshipUpgradeSlot slot = slots[index];
            ItemStack stack = this.installed.remove(slot);
            if (stack != null && !stack.isEmpty()) {
                this.localRevision++;
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> removeAll() {
        List<ItemStack> removed = this.stacks();
        if (!removed.isEmpty()) {
            this.installed.clear();
            this.localRevision++;
        }
        return removed;
    }

    public ItemStack get(AirshipUpgradeSlot slot) {
        return this.installed.getOrDefault(slot, ItemStack.EMPTY);
    }

    public List<ItemStack> stacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (AirshipUpgradeSlot slot : AirshipUpgradeSlot.values()) {
            ItemStack stack = this.get(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        return List.copyOf(stacks);
    }

    public Map<AirshipUpgradeSlot, ItemStack> slotSnapshot() {
        EnumMap<AirshipUpgradeSlot, ItemStack> snapshot =
                new EnumMap<>(AirshipUpgradeSlot.class);
        for (AirshipUpgradeSlot slot : AirshipUpgradeSlot.values()) {
            ItemStack stack = this.get(slot);
            if (!stack.isEmpty()) {
                snapshot.put(slot, stack.copy());
            }
        }
        return Collections.unmodifiableMap(snapshot);
    }

    public List<AirshipUpgradeDefinition> activeDefinitions(AirshipUpgradeTarget target) {
        List<AirshipUpgradeDefinition> definitions = new ArrayList<>();
        for (AirshipUpgradeSlot slot : AirshipUpgradeSlot.values()) {
            ItemStack stack = this.get(slot);
            AirshipUpgradeDefinitions.INSTANCE.resolve(stack)
                    .filter(definition -> definition.supports(target))
                    .ifPresent(definitions::add);
        }
        return List.copyOf(definitions);
    }

    public AirshipUpgradeModifiers modifiers(AirshipUpgradeTarget target) {
        AirshipUpgradeModifiers modifiers = AirshipUpgradeModifiers.IDENTITY;
        for (AirshipUpgradeDefinition definition : this.activeDefinitions(target)) {
            modifiers = modifiers.combine(definition.modifiers());
        }
        return modifiers;
    }

    public int installedCount() {
        return this.installed.size();
    }

    public long localRevision() {
        return this.localRevision;
    }

    public void write(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (AirshipUpgradeSlot slot : AirshipUpgradeSlot.values()) {
            ItemStack stack = this.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putString("Slot", slot.serializedName());
            entry.put("Item", stack.saveOptional(registries));
            list.add(entry);
        }
        tag.put("AirshipUpgrades", list);
    }

    public void read(CompoundTag tag, HolderLookup.Provider registries) {
        this.installed.clear();
        ListTag list = tag.getList("AirshipUpgrades", Tag.TAG_COMPOUND);
        for (Tag rawEntry : list) {
            if (!(rawEntry instanceof CompoundTag entry)) {
                continue;
            }
            try {
                AirshipUpgradeSlot slot = AirshipUpgradeSlot.parse(entry.getString("Slot"));
                ItemStack stack = ItemStack.parseOptional(registries, entry.getCompound("Item"));
                if (!stack.isEmpty()) {
                    this.installed.put(slot, stack.copyWithCount(1));
                }
            } catch (RuntimeException ignored) {
                // Invalid or removed slots stay absent; installed items are never guessed.
            }
        }
        this.localRevision++;
    }

    public enum InstallStatus {
        INSTALLED,
        MISSING_DEFINITION,
        WRONG_TARGET,
        SLOT_OCCUPIED,
        CONFLICT
    }

    public record InstallResult(
            InstallStatus status,
            AirshipUpgradeDefinition definition
    ) {
        public boolean installed() {
            return this.status == InstallStatus.INSTALLED;
        }

        private static InstallResult success(AirshipUpgradeDefinition definition) {
            return new InstallResult(InstallStatus.INSTALLED, definition);
        }

        private static InstallResult failure(
                InstallStatus status,
                AirshipUpgradeDefinition definition
        ) {
            return new InstallResult(status, definition);
        }
    }
}
