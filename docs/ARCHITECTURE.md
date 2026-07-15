# Zeppelin Must Have Architecture

## Platform

Zeppelin Must Have is an add-on for the Minecraft 1.21.1 NeoForge stack formed by Create, Sable, Create Simulated, and Create Aeronautics.

```text
Create kinetic systems
        │
        ▼
Sable interactive sub-levels and Rapier physics
        │
        ▼
Create Simulated assembly and simulated-contraption interaction
        │
        ▼
Create Aeronautics lift and flight machinery
        │
        ▼
Zeppelin Must Have zeppelin control and service systems
```

These dependencies are mandatory in both Gradle and `neoforge.mods.toml`.

## Runtime boundaries

```text
Zeppelin block / block entity
            │
            ▼
Zeppelin subsystem controller
            │
            ├── Create kinetic input and stress
            ├── Simulated assembly/sub-level state
            ├── Aeronautics lift and propulsion state
            ├── Sable physics-tick interaction
            ├── server-authoritative saved state
            └── client telemetry synchronization
```

Upstream API calls remain concentrated in the `integration` package. Aeronautics is not optional; the boundary exists to contain package and API churn between Simulated Project releases.

## Airship Helm

### Containing-vessel discovery

`AeronauticsFlightStateReader` resolves the vessel using:

```java
Sable.HELPER.getContaining(level, blockPos)
```

Only a `ServerSubLevel` is accepted as an attached airship. A Helm placed in the parent world reports a detached state.

### Physics telemetry

For an attached sub-level the reader captures:

- persistent sub-level UUID;
- optional sub-level name;
- projected global Helm position;
- heading, pitch, and roll from `logicalPose().orientation()`;
- global linear velocity from `RigidBodyHandle`;
- global angular velocity from `RigidBodyHandle`;
- mass from the sub-level mass tracker.

### Aeronautics telemetry

`BalloonMap.MAP` is scanned for Aeronautics balloons whose controller positions resolve to the same Sable sub-level. The reader aggregates:

- balloon count;
- enclosed capacity;
- currently filled lifting-gas volume;
- target lifting-gas volume;
- total raw lift.

### Synchronization

`AirshipHelmBlockEntity` samples every five server ticks. `AirshipFlightSnapshot.materiallyDiffersFrom` applies per-field epsilon thresholds to prevent unnecessary packet updates. A forced synchronization occurs once per second.

The client receives telemetry through the standard block entity update tag and never authors flight state.

## Airship burner family

### Integration model

`AirshipBurnerBlockEntity` extends Aeronautics `HotAirBurnerBlockEntity`. This retains the established balloon discovery, flood-fill, gas type, client effects, and `BlockEntityLiftingGasProvider` contract while adding Zeppelin Must Have fuel storage and tier behaviour.

### Tiers

| Tier | Gas output | Fuel use | Capacity | Cast range |
|---|---:|---:|---:|---:|
| Standard | `1.0×` | `1.0 fuel tick/tick` | 12,000 ticks | 16 blocks |
| Forced draft | `2.25×` | `1.6 fuel ticks/tick` | 24,000 ticks | 32 blocks |
| Industrial | `4.5×` | `3.0 fuel ticks/tick` | 48,000 ticks | 64 blocks |

Fuel consumption is multiplied by `redstoneSignal / 15`, so partial redstone input provides proportional throttling.

### Fuel resolution

Fuel is resolved in this order:

1. Creative Blaze Cake — infinite operation.
2. Create superheated Blaze Burner fuel data map.
3. Create regular Blaze Burner fuel data map.
4. Vanilla/NeoForge furnace burn time.

Superheated fuel multiplies gas output by `1.5`.

### Safety and lifecycle

- No fuel or zero redstone signal means zero gas output.
- Fuel state is persisted in NBT.
- The `lit` block state follows actual gas-producing status, not merely redstone power.
- Fuel exhaustion immediately removes the provider from its current balloon.
- Comparator output represents remaining fuel percentage.
- The burner is server-authoritative; the client only receives synchronized fuel state and normal Aeronautics effects.

## Subsystem model

`ZeppelinSubsystem` defines five stable domains:

- `CONTROL` — pilot commands, steering, mode selection, and safety interlocks.
- `BUOYANCY` — burners, ballast, lift demand, trim, and vertical equilibrium.
- `PROPULSION` — thrust producers, vectoring, power availability, and stress demand.
- `NAVIGATION` — altitude, vertical speed, heading, and instrumentation.
- `MOORING` — anchors, winches, docking constraints, and ground handling.

## Implementation state

### Completed

- Mandatory Create/Sable/Simulated/Aeronautics platform.
- Permanent mod identity and Java namespace.
- Airship Helm containing-sub-level discovery.
- Server-side physics and Aeronautics telemetry snapshot.
- Material-change block entity synchronization.
- Three fuel-driven Aeronautics lifting-gas burners.
- Recipes, block drops, localisation, comparator fuel output, and creative-tab registration.
- Successful NeoForge build and datagen-runtime platform initialization.

### Next

1. Helm menu and continuous cockpit HUD.
2. Server-bound pilot input packets.
3. Per-sub-level Helm ownership and conflict resolution.
4. Vertical trim controller combining burners, ballast, and thrusters.
5. Create kinetic stress integration for forced-draft and industrial burners.
6. Production Blockbench models, flame renderer integration, and Ponder scenes.
7. GameTests for attachment, telemetry, fueling, and balloon association.

## Invariants

1. Sable, Simulated, and Aeronautics are mandatory dependencies.
2. Simulation changes execute on the logical server or authoritative Sable physics callback.
3. Client code renders synchronized state but never owns flight state.
4. Registry names introduced in `0.1.0` are persistent save-format identifiers.
5. Create RPM, stress, gas output, and fuel use are explicit parameters rather than hidden block-class constants.
6. Calls into upstream APIs remain concentrated in integration and subsystem boundaries.
