# Vertical Thruster

The Vertical Thruster is a Create-powered Aeronautics propeller designed for direct lift and vertical maneuvering.

## Native integration

The block extends Aeronautics `BasePropellerBlock`; its block entity extends `BasePropellerBlockEntity`. It therefore participates in the existing Sable/Aeronautics propulsion pipeline:

```text
Create shaft speed
        │
        ▼
BasePropellerBlockEntity
        │
        ▼
BlockEntitySubLevelPropellerActor
        │
        ▼
Sable ForceGroups.PROPULSION
        │
        ▼
Force at the thruster's physical block position
```

The mod does not move the vessel directly and does not implement another rigid-body solver. Sable applies the force during physics ticks, including torque caused by mounting the thruster away from the center of mass.

## Installation and controls

Normal placement creates an upward-facing unit. Placing while sneaking creates a downward-facing unit. Clicking a vertical face uses that face directly.

Create Wrench behavior is constrained to vertical operation:

- wrench the fan side to reverse the propeller force;
- wrench another side to flip the complete unit between upward and downward orientation.

The block connects to a Create shaft on the rear side and consumes kinetic stress.

## Default profile

```text
data/zeppelin_must_have/vertical_thruster_profiles/default.json
```

```json
{
  "schema_version": 1,
  "thrust_scaling": 1.75,
  "airflow_scaling": 0.12,
  "radius": 1.0,
  "stress_impact": 8.0
}
```

The profile controls:

- thrust scaling passed to Aeronautics;
- airflow scaling;
- effective propeller radius;
- Create stress impact.

Server data packs may rebalance the profile and apply it with `/reload`. Existing block entities receive the new profile and synchronize the effective values to clients.

## Rendering and diagnostics

The static housing is baked as the block model. The hub and blades are a separate partial model animated by the native Aeronautics propeller renderer and Create kinetic angle.

Create Engineer's Goggles display:

- thrust direction;
- current calculated thrust;
- current airflow;
- resolved profile ID while sneaking;
- inherited Create kinetic information.

## Control applications

Vertical Thrusters may be used for:

- direct climb and descent authority;
- assisting hot-air lift during takeoff;
- stabilizing heavily loaded vessels;
- counteracting ballast changes;
- closed-loop vertical control through Create redstone and kinetic systems.
