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

`AirshipBurnerBlockEntity` extends Aeronautics `HotAirBurnerBlockEntity`. This preserves:

- native value behaviour for selected hot-air output;
- airtight-envelope raycast semantics;
- balloon creation and joining;
- lifting-gas type;
- server fill simulation and air-pressure handling;
- Aeronautics particles, sound, renderer, and balloon goggle section.

Zeppelin Must Have adds only stored fuel, profile-based tier scaling, comparator fuel output, and additional diagnostics.

### Data-driven profiles

`AirshipBurnerTier` contains stable profile IDs only. It contains no numerical tuning.

`AirshipBurnerProfiles` is a `SimpleJsonResourceReloadListener` registered through `AddReloadListenerEvent`. It loads:

```text
data/<namespace>/airship_burner_profiles/*.json
```

The registry publishes an immutable map and monotonically increasing revision. Existing burner block entities resolve a new profile on the next tick after `/reload`, clamp fuel capacity, update comparator consumers, synchronize clients, and refresh balloon membership.

Missing or invalid profiles fail closed without affecting Create or Aeronautics blocks.

### Profile synchronization

The server's resolved profile is serialized only into the client update tag. It is not persisted as a second source of truth. This guarantees:

- server data packs remain authoritative;
- clients do not need matching data packs;
- Engineer's Goggles show actual server tuning;
- persistent worlds re-resolve current profile data after load.

### Fuel resolution

Fuel acceptance delegates to established APIs in this order:

1. Creative Blaze Cake;
2. Create superheated Blaze Burner fuel data map;
3. Create regular Blaze Burner fuel data map;
4. NeoForge item-stack furnace burn time.

No custom fuel registry competes with Create.

### Create Engineer's Goggles

The subclass first invokes Aeronautics' standard goggle section. It then appends stored fuel, grade, redstone throttle, output, profile ID, capacity, range, and current consumption. Sneaking exposes extended profile diagnostics.

## Ponder

`ZmhPonderPlugin` implements `PonderPlugin` directly. It does not inherit Create's restore hooks or index exclusions.

The plugin registers:

- category: `zeppelin_must_have:zeppelin_systems`;
- scene: `helm/telemetry`;
- scene: `burner/operation`.

Storyboards resolve to matching compressed structure templates under `assets/zeppelin_must_have/ponder`.

Burner scenes update both blockstate and `AirshipBurnerBlockEntity` preview state through `WorldInstructions.modifyBlockEntity`. Preview block entities are explicitly marked virtual, so Ponder cannot join or alter real Aeronautics balloons.

Scene text explains profile-driven behaviour without embedding default tuning numbers.

## Assets

Models use native Create texture resources for casing, fan, fluid-tank, pulley, axis, gearbox, copper, brass, and industrial iron surfaces. Four custom 16×16 textures represent only information unique to this add-on:

- Helm panel;
- altimeter dial;
- ballast indicator;
- burner service panel.

Airship burners use the native Aeronautics `HotAirBurnerRenderer` rather than a parallel JSON flame implementation.

## Recipes

Recipe resources use normal Minecraft shaped crafting and Create Mechanical Crafting. Ingredients are existing Create, Simulated, Aeronautics, and vanilla items. The add-on does not register substitute copies of upstream components.

## Invariants

1. Create, Sable, Simulated, and Aeronautics remain mandatory dependencies.
2. Simulation mutations occur only on the logical server or authoritative Sable/Aeronautics callbacks.
3. Clients render synchronized state and do not author flight or profile data.
4. Registry IDs introduced in `0.1.0` remain save-compatible.
5. Gameplay tuning lives in data-pack profiles, not tier enums or Ponder scenes.
6. Invalid data fails closed locally and does not disable upstream machinery.
7. Create and Aeronautics UI, rendering, fuel, and balloon contracts are reused before new systems are introduced.

## Next functional stages

1. Helm ownership and pilot input protocol.
2. Ballast Tank fluid capability and trim contribution.
3. Vertical Thruster kinetic stress and Aeronautics force integration.
4. Mooring constraints through Sable physics.
5. Functional Altitude Gauge renderer and display-source integration.
6. Automated GameTests for reload, fuel, profile synchronization, sub-level attachment, and balloon association.
