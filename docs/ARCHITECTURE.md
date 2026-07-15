# Zeppelin Must Have Architecture

## Objective

Zeppelin Must Have is not a generic airship abstraction. It is an add-on for the current Minecraft 1.21.1 NeoForge stack formed by Create, Sable, Create Simulated, and Create Aeronautics.

## Required platform

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

These dependencies are required in both Gradle and `neoforge.mods.toml`. `SimulatedStack` imports the Simulated and Aeronautics entrypoints at compile time and verifies all four upstream mod IDs during initialization.

## Subsystem model

`ZeppelinSubsystem` defines five stable domains:

- `CONTROL` — pilot commands, steering, mode selection, and safety interlocks.
- `BUOYANCY` — ballast state, lift demand, trim, and vertical equilibrium.
- `PROPULSION` — thrust producers, vectoring, power availability, and stress demand.
- `NAVIGATION` — altitude, vertical speed, heading, and instrumentation.
- `MOORING` — anchors, winches, docking constraints, and ground handling.

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

The integration package isolates upstream API calls from gameplay blocks. This is not intended to make Aeronautics optional; it limits API churn when the Simulated Project changes internal packages between releases.

## Implementation sequence

### Phase 1 — Platform foundation

- NeoForge 1.21.1 and Java 21 build.
- Mandatory Create, Sable, Simulated, and Aeronautics dependencies.
- Permanent mod identity and Java namespace.
- Base blocks, item forms, creative tab, models, and localisation.
- Compile-time integration boundary and dependency verification.

### Phase 2 — Aeronautics-aware instrumentation

- Directional Airship Helm block.
- Altitude Gauge block entity and client renderer.
- Detection of the containing Simulated/Sable sub-level.
- Aeronautics flight-state telemetry.
- Server-authoritative telemetry snapshot and packet codecs.

### Phase 3 — Create kinetic machinery

- Mooring Winch as a Create kinetic block.
- Vertical Thruster integrated with Aeronautics force production.
- RPM-to-thrust conversion and stress impact.
- Rotation direction, shaft connectivity, and failure behaviour.

### Phase 4 — Buoyancy and trim

- Ballast Tank fluid or mass storage.
- Integration with Aeronautics lifting-gas and buoyancy calculations.
- Centre-of-mass and trim contribution model.
- Safety limits, venting, and failure states.

### Phase 5 — Zeppelin control system

- Per-sub-level subsystem graph.
- Helm command aggregation.
- Vertical trim controller.
- Mooring constraints through Sable physics.
- Persistence across assembly/disassembly and world reloads.
- Ponder scenes and in-game diagnostics.

## Invariants

1. Sable, Simulated, and Aeronautics are mandatory, never soft integrations.
2. Simulation changes execute on the logical server or the authoritative Sable physics callback.
3. Client code renders synchronized state but never owns authoritative flight state.
4. Registry names introduced in `0.1.0` are persistent save-format identifiers.
5. Create RPM and stress are converted through explicit configuration; no hidden multipliers live in block classes.
6. Calls into upstream APIs are concentrated in the integration package rather than distributed through every block.
