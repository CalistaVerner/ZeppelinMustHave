package us.kayla.zeppelinmusthave.content.burner;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.datamaps.BlazeBurnerFuel;
import com.simibubi.create.api.registry.CreateDataMaps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Ordered adapter pipeline that normalizes every supported upstream fuel source
 * into one reservoir input.
 *
 * <p>Other mods remain compatible by contributing to Create Blaze Burner data
 * maps or NeoForge furnace burn time. Zeppelin Must Have does not introduce a
 * competing fuel registry.</p>
 */
public final class AirshipHeatSources {
    private static final List<Resolver> RESOLVERS = List.of(
            AirshipHeatSources::resolveCreativeBlazeCake,
            AirshipHeatSources::resolveCreateSuperheated,
            AirshipHeatSources::resolveCreateRegular,
            AirshipHeatSources::resolveFurnaceFuel
    );

    private AirshipHeatSources() {
    }

    public static Optional<AirshipHeatSource> resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        for (Resolver resolver : RESOLVERS) {
            Optional<AirshipHeatSource> source = resolver.resolve(stack);
            if (source.isPresent()) {
                return source;
            }
        }
        return Optional.empty();
    }

    private static Optional<AirshipHeatSource> resolveCreativeBlazeCake(ItemStack stack) {
        if (!AllItems.CREATIVE_BLAZE_CAKE.isIn(stack)) {
            return Optional.empty();
        }
        return Optional.of(AirshipHeatSource.infinite(
                itemId(stack),
                AirshipHeatGrade.SUPERHEATED
        ));
    }

    private static Optional<AirshipHeatSource> resolveCreateSuperheated(ItemStack stack) {
        Holder<Item> holder = stack.getItemHolder();
        BlazeBurnerFuel fuel = holder.getData(CreateDataMaps.SUPERHEATED_BLAZE_BURNER_FUELS);
        if (fuel == null || fuel.burnTime() <= 0) {
            return Optional.empty();
        }
        return Optional.of(AirshipHeatSource.finite(
                itemId(stack),
                AirshipHeatGrade.SUPERHEATED,
                fuel.burnTime()
        ));
    }

    private static Optional<AirshipHeatSource> resolveCreateRegular(ItemStack stack) {
        Holder<Item> holder = stack.getItemHolder();
        BlazeBurnerFuel fuel = holder.getData(CreateDataMaps.REGULAR_BLAZE_BURNER_FUELS);
        if (fuel == null || fuel.burnTime() <= 0) {
            return Optional.empty();
        }
        return Optional.of(AirshipHeatSource.finite(
                itemId(stack),
                AirshipHeatGrade.REGULAR,
                fuel.burnTime()
        ));
    }

    private static Optional<AirshipHeatSource> resolveFurnaceFuel(ItemStack stack) {
        int burnTicks = stack.getBurnTime(null);
        if (burnTicks <= 0) {
            return Optional.empty();
        }
        return Optional.of(AirshipHeatSource.finite(
                itemId(stack),
                AirshipHeatGrade.REGULAR,
                burnTicks
        ));
    }

    private static ResourceLocation itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    @FunctionalInterface
    private interface Resolver {
        Optional<AirshipHeatSource> resolve(ItemStack stack);
    }
}
