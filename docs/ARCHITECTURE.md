# Zeppelin Must Have Architecture

## Platform contract

Zeppelin Must Have extends, rather than replaces, the Minecraft 1.21.1 NeoForge airship stack:

```text
Create kinetic systems and Engineer's Goggles
        │
        ▼
Sable interactive sub-levels and Rapier physics
        │
        ▼
Create Simulated assembly and sub-level interaction
        │
        ▼
Create Aeronautics balloons, gas providers, lift, and propulsion
        │
        ▼
Zeppelin Must Have zeppelin-specific equipment
```

The project has no Mixins, access transformers, registry replacements, or copied upstream simulation loops.

## Stable boundaries

```text
Block / Block Entity
        │
        ├── Create public APIs
        │     ├── fuel data maps
        │     ├── Engineer's Goggles
        │     └── Ponder
        │
        ├── Aeronautics public contracts
        │     ├── BlockEntityLiftingGasProvider
        │     ├── BalloonMap
        │     └── HotAirBurnerBlockEntity
        │
        ├── Sable public contracts
        │     ├── containing sub-level discovery
        │     ├── pose projection
        │     └── RigidBodyHandle
        │
        └── Zeppelin Must Have state
              ├── data-pack profiles
              ├── persistent fuel state
              └── synchronized telemetry
```

Upstream API calls are concentrated in `integration` or in subclasses explicitly intended by the upstream API.

## Airship Helm

### Server authority

`AeronauticsFlightStateReader` accepts only a `ServerSubLevel` returned by:

```java
Sable.HELPER.getContaining(level, blockPos)
```

It reads:

- stable sub-level UUID and optional name;
- projected global block position;
- logical pose and Euler attitude;
- linear and angular velocity through `RigidBodyHandle`;
- mass from the Sable mass tracker;
- balloon ownership and buoyancy data from `BalloonMap`.

`AirshipFlightSnapshot` is immutable. The block entity samples every five server ticks, applies material-difference thresholds, and sends the standard block-entity update tag. The client never calculates authoritative flight state.

### Create Engineer's Goggles

`AirshipHelmBlockEntity` implements `IHaveGoggleInformation`.

Normal view shows flight and Aeronautics telemetry. Sneaking adds diagnostic UUID, global position, angular velocity, and sample age. The overlay uses the normal Create tooltip pipeline and the Helm item as its icon.

## Airship Burner family

### Upstream extension

`AirshipBurnerBlockEntity` extends Aeronautics `HotAirBurnerBlockEntity`. Native value behaviour, airtight-envelope discovery, balloon ownership, gas type, pressure, fill simulation, particles, sound, rendering, and the Aeronautics goggle section remain upstream responsibilities.

### Heat-source pipeline

Fuel handling is split into reusable components:

```text
AirshipHeatSources
  ordered adapters over Create and NeoForge fuel APIs
        │
        ▼
AirshipHeatSource
  immutable normalized contribution
        │
        ▼
AirshipHeatReservoir
  regular + superheated layers, persistence, migration, consumption
```

`AirshipHeatSources` resolves Creative Blaze Cake, Create superheated fuel data, Create regular fuel data, and NeoForge furnace burn time. It does not define another fuel registry.

The reservoir allows regular and superheated contributions to coexist. The superheated layer is consumed first; output automatically falls back to regular heat when that layer is exhausted. A finite source cannot replace active infinite creative heat.

0.4.x NBT is migrated into the new reservoir during load.

### Data-driven profiles

`AirshipBurnerTier` contains stable profile IDs only. `AirshipBurnerProfiles` reloads immutable profile data from:

```text
data/<namespace>/airship_burner_profiles/*.json
```

The profile owns capacity, output scaling, consumption, throttle curve, and cast range. The reservoir owns stored energy. `AirshipBurnerMetrics` derives shared values once for status chat, Engineer's Goggles, and diagnostics.

Missing or invalid profiles fail closed without affecting Create or Aeronautics blocks.

### Native multi-source aggregation

Multiple burners are not joined by a Zeppelin Must Have network. Aeronautics already associates providers with a balloon through:

```java
Balloon.getHeaters()
```

`BalloonHeatAggregate` is a read-only snapshot of that native collection. It reports connected providers, active providers, and combined gas output. It never mutates balloon membership or lift simulation.

### Synchronization

The server update tag contains:

- resolved burner profile snapshot;
- heat reservoir state;
- read-only balloon heat aggregate.

Persistent saves store the reservoir but not a copied profile. Clients therefore display actual server tuning without becoming another source of truth.

### Upgrade composition

`AirshipUpgradeSet` stores one ItemStack per typed socket. Upgrade items are markers; their slot, targets, conflicts, exclusivity, and numerical effects come from `AirshipUpgradeDefinitions`, another server resource reload listener.

```text
Base AirshipBurnerProfile
        │
        ▼
AirshipUpgradeModifiers from installed definitions
        │
        ▼
Effective AirshipBurnerProfile
        │
        ├── reservoir capacity clamp
        ├── output and consumption
        ├── throttle curve
        └── Aeronautics cast range
```

Definitions are combined in stable socket order. The block entity observes burner-profile revision, upgrade-definition revision, and local installed-set revision. Any change recomputes the effective profile and synchronizes the client.

Installation uses normal block interaction. Sneak-wrenching removes one module before delegating to Create's normal block-removal behaviour. `onRemove` drops remaining modules.

### Create Engineer's Goggles

The subclass first invokes Aeronautics' standard balloon section, then appends reservoir layers, active heat grade, redstone throttle, individual output, source counts, combined output, profile ID, capacity, range, consumption, installed modules, and aggregated upgrade modifiers. Sneaking exposes the detailed regular/superheated and modifier split.

## Piped Redstone

### Explicit topology

`PipedRedstoneBlock` extends vanilla `PipeBlock` and stores six independent boolean ports plus analog power and waterlogging in blockstate.

Topology is never inferred from adjacency. `PipedRedstoneNetworkManager` traverses only reciprocal enabled ports:

```text
A.east == true
B.west == true
A is west of B
```

Both conditions are required. This permits neighboring and crossing lines without accidental electrical coupling.

The Create Wrench toggles the selected face and mirrors that change to an adjacent Piped Redstone conduit when present.

### Event-driven network solver

Network rebuilds are requested after placement, removal, explicit port changes, or external redstone neighbor updates. One scheduled tick is attached to the canonical lowest-position anchor of the connected component.

The solver performs:

1. reciprocal-port component discovery;
2. weakest-link profile aggregation;
3. temporary removal of conduit output to avoid reading its own previous state;
4. external analog source collection;
5. priority traversal by strongest power and shortest path;
6. blockstate power application and notification of non-conduit neighbors.

Power remains in the vanilla `0..15` range and is not attenuated per conduit. Paths beyond `max_signal_distance` are not energized.

Mixed tiers use:

```text
delay = max(all segment delays)
range = min(all segment ranges)
```

Components larger than 8192 blocks fail closed.

### Profiles

`PipedRedstoneProfiles` is a server resource-reload listener for:

```text
data/<namespace>/piped_redstone_profiles/*.json
```

`PipedRedstoneTier` contains only stable profile IDs. Copper, Brass, and Resonant defaults are data-pack resources rather than Java tuning constants.

### Native conduit control

`PipedRedstoneNativeLeverBlock` extends `FaceAttachedHorizontalDirectionalBlock` and accepts only a `PipedRedstoneBlock` as its supporting block. Placement opens the matching pipe port and toggling requests a rebuild of only that connected conduit component.

The lever emits weak and direct power only when the queried redstone side matches its support direction. It therefore cannot energize adjacent unrelated blocks.

`PipedRedstoneNativeLeverBlockEntity` stores no authoritative signal state. The blockstate remains the source of truth; the block entity owns only a client-side `LerpedFloat` used by `PipedRedstoneNativeLeverRenderer` for smooth handle rotation and indicator interpolation.

### Waterproof repeater

`PipedRedstoneRepeaterBlock` is directional, waterloggable, preserves analog strength, and starts a new conduit-distance segment. Its `BlockStateProperties.DELAY` value mirrors vanilla repeater semantics:

```text
1..4 redstone ticks = 2..8 game ticks
```

Ordinary right-click cycles the setting. The selected position is represented by a separate multipart model indicator.

### Verification

`PipedRedstoneGameTests` runs on the NeoForge GameTest server and verifies isolation, Copper range, waterlogged operation, and the fastest/slowest repeater settings.

## Ponder

`ZmhPonderPlugin` implements `PonderPlugin` directly. It does not inherit Create's restore hooks or index exclusions.

The plugin registers:

- category: `zeppelin_must_have:zeppelin_systems`;
- scene: `helm/telemetry`;
- scene: `burner/operation`;
- scene: `redstone/conduits`.

Storyboards resolve to matching compressed structure templates under `assets/zeppelin_must_have/ponder`.

Burner scenes update both blockstate and `AirshipBurnerBlockEntity` preview state through `WorldInstructions.modifyBlockEntity`. Preview block entities are explicitly marked virtual, so Ponder cannot join or alter real Aeronautics balloons.

The scene places three independent virtual providers beneath one envelope and explains that Aeronautics combines them through the balloon's native heater collection. Scene text contains no profile tuning constants.

## Assets

Models use native Create texture resources for casing, fan, fluid-tank, pulley, axis, gearbox, copper, brass, and industrial iron surfaces.

A single `templates/equipment_display.json` owns GUI, hand, ground, and fixed transforms for every equipment item. Burners use NeoForge's `neoforge:composite` loader and reusable parts:

- common core;
- single, dual, and triple heat-source manifolds;
- shared forced-draft fans;
- industrial auxiliary tanks and piping.

Six custom 16×16 textures represent information unique to this add-on: Helm panel, altimeter, ballast indicator, burner service panel, heat chamber, and heat manifold.

Airship burners use the native Aeronautics `HotAirBurnerRenderer` rather than a parallel JSON flame implementation.

## Recipes

Recipe resources use normal Minecraft shaped crafting and Create Mechanical Crafting. Ingredients are existing Create, Simulated, Aeronautics, and vanilla items. The add-on does not register substitute copies of upstream components.

## Invariants

1. Create, Sable, Simulated, and Aeronautics remain mandatory dependencies.
2. Simulation mutations occur only on the logical server or authoritative Sable/Aeronautics callbacks.
3. Clients render synchronized state and do not author flight or profile data.
4. Registry IDs introduced in `0.1.0` remain save-compatible.
5. Gameplay tuning lives in data-pack burner profiles and upgrade definitions, not tier enums or Ponder scenes.
6. Invalid data fails closed locally and does not disable upstream machinery.
7. Upgrade ItemStacks persist independently from their current data definitions and are never silently converted into other modules.
8. Create and Aeronautics UI, rendering, fuel, wrench, and balloon contracts are reused before new systems are introduced.

## Next functional stages

1. Helm ownership and pilot input protocol.
2. Ballast Tank fluid capability and trim contribution.
3. Vertical Thruster kinetic stress and Aeronautics force integration.
4. Mooring constraints through Sable physics.
5. Functional Altitude Gauge renderer and display-source integration.
6. Automated GameTests for reload, fuel, profile synchronization, sub-level attachment, and balloon association.
