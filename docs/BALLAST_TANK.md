# Ballast Tank

The Ballast Tank is a functional water storage block that changes the physical mass and center of mass of the Sable sub-level containing it.

## Fluid contract

- NeoForge block fluid capability;
- accepts water-tagged fluids only;
- supports buckets, portable fluid containers, and Create fluid pipes;
- bundled capacity: `8000 mB`;
- comparator output: `0..15` based on fill level;
- server-authoritative NBT persistence and client synchronization.

## Sable mass integration

Stored fluid is not represented by a parallel force or artificial downward impulse. The block entity contributes its current ballast mass directly to:

```java
ServerSubLevel.getSelfMassTracker()
```

The contribution is applied with `MassTracker.addBlockMass(...)`, so the normal Sable mass pipeline recalculates:

- total rigid-body mass;
- center of mass;
- inertia tensor;
- gyroscopic response;
- force response of the complete vessel.

The contribution is delta-updated when the tank contents change and is removed during block removal or chunk unload. Reloading the block entity reapplies the current fluid contribution exactly once.

A Ballast Tank outside a moving Sable sub-level behaves as a normal water tank without modifying world mass.

## Default profile

```text
data/zeppelin_must_have/ballast_tank_profiles/default.json
```

```json
{
  "schema_version": 1,
  "capacity_mb": 8000,
  "mass_per_bucket_kg": 1000.0
}
```

`mass_per_bucket_kg` defines the dynamic mass contributed by `1000 mB` of stored fluid. Server data packs may change capacity and density, then apply the profile with `/reload`.

## Diagnostics

Create Engineer's Goggles display:

- stored fluid and capacity;
- current fill percentage;
- added Sable mass;
- resolved profile ID while sneaking.

Empty-hand interaction prints the current amount and added mass. The front service gauge renders the stored fluid level.

## Automation

A typical ballast installation uses:

```text
Water source / drain
        │
Create pump and pipes
        ▼
Ballast Tank group
        │
        ▼
Changed vessel mass and center of mass
```

Multiple tanks can be placed at different positions to trim pitch and roll through real mass distribution rather than a custom attitude-control simulation.
