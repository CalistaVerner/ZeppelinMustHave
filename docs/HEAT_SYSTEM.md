# Heat Sources and Aeronautics Aggregation

Zeppelin Must Have 0.5.0 separates three responsibilities that were previously concentrated in `AirshipBurnerBlockEntity`:

```text
ItemStack
   │
   ▼
AirshipHeatSources
upstream fuel adapters
   │
   ▼
AirshipHeatReservoir
stored regular + superheated heat
   │
   ▼
AirshipBurnerProfile
throttle, consumption, capacity, range
   │
   ▼
Aeronautics BlockEntityLiftingGasProvider
individual gas output
   │
   ▼
Balloon.getHeaters()
native combined output of all connected providers
```

## Source resolution

`AirshipHeatSources` is an ordered adapter pipeline. It accepts existing upstream extension mechanisms instead of defining another fuel registry:

1. Creative Blaze Cake;
2. Create superheated Blaze Burner fuel data map;
3. Create regular Blaze Burner fuel data map;
4. NeoForge furnace burn time.

A mod that contributes fuel through Create or NeoForge is automatically compatible with Airship Burners.

Every accepted stack becomes an immutable `AirshipHeatSource` containing:

- source item ID;
- semantic heat grade;
- finite burn time or infinite status.

## Stratified reservoir

`AirshipHeatReservoir` combines all accepted items into one storage system while preserving heat quality.

It has two finite layers:

```text
Superheated reserve  ← consumed first
Regular reserve      ← consumed after the hot layer is exhausted
```

This means regular fuel can be inserted while a superheated charge is active, and superheated fuel can be inserted into a regular reserve. Neither contribution is discarded. The burner automatically changes output grade when the superheated layer reaches zero.

Creative heat is represented as a separate infinite state and cannot be accidentally replaced by finite fuel.

### Persistence migration

The reservoir reads both the current NBT fields and the 0.4.x fields:

```text
FuelGrade
RemainingFuelTicks
FuelConsumptionRemainder
CreativeFuel
```

Existing worlds migrate into the appropriate regular or superheated layer during block-entity loading.

## Data-pack profile

Numerical tuning remains in `AirshipBurnerProfile`:

- capacity;
- fuel consumption;
- output multiplier;
- superheated multiplier;
- redstone throttle curve;
- envelope cast range.

The reservoir owns stored energy. The profile owns operating limits. The block entity coordinates both without duplicating their calculations.

`AirshipBurnerMetrics` calculates the values used by chat status and Engineer's Goggles once per request:

- resolved throttle;
- individual gas output;
- current consumption;
- reservoir snapshot;
- balloon heat aggregate.

## Combining multiple burners

Zeppelin Must Have does not create a second heat network.

Create Aeronautics already associates every `BlockEntityLiftingGasProvider` with a balloon and stores the providers in:

```java
Balloon.getHeaters()
```

`BalloonHeatAggregate` is a read-only view over that collection. It reports:

- connected provider count;
- active provider count;
- combined gas output.

The aggregate is synchronized with the normal burner block-entity update packet and shown through Engineer's Goggles. No provider ownership, balloon fill, lift, or pressure mechanics are replaced.

## Ponder representation

The burner Ponder scene creates three virtual burner block entities under one envelope. Each source keeps its own signal and reservoir preview. The scene then explains that Aeronautics combines them through the balloon's native heater collection.

Virtual preview block entities cannot join or mutate real balloons.

## Model composition

Burner models use NeoForge's `neoforge:composite` loader, also used by Create.

Reusable geometry:

```text
airship_burner/parts/
├── materials.json
├── core.json
├── heat_source_single.json
├── heat_source_dual.json
├── heat_source_triple.json
├── draft_fans.json
└── industrial_auxiliary.json
```

Tier composition:

```text
Standard
  core + single source

Forced Draft
  core + draft fans + dual source/manifold

Industrial
  core + draft fans + triple source/manifold + auxiliary tanks
```

The geometry deliberately shows multiple combustion chambers converging into a common collector before feeding the envelope.
