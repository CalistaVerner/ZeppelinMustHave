# Graded Steam Engines

Zeppelin Must Have adds three Create-compatible steam-engine grades for the graded boiler family.

The engines preserve Create's native powered-shaft, rotation-direction, boiler-efficiency, water-supply, and kinetic-network behaviour. Grade-specific logic changes only:

- stress capacity;
- equivalent boiler load;
- cylinder count and phase;
- crank radius and connecting-rod geometry;
- housing and animated partial models;
- steam-particle intensity.

## Installation

A graded engine must be attached directly to a Zeppelin Must Have graded boiler tank. It uses the normal Create Steam Engine placement rules and drives a standard Create shaft two blocks away.

```text
Graded Boiler Tank ─ Graded Steam Engine ─ air ─ Create Shaft
```

Vanilla Create Fluid Tanks intentionally do not accept graded engines. Create's native `BoilerData` only recognizes `create:steam_engine`; restricting the new engines to `BoilerGradeBlockEntity` keeps water, heat, engine-count, and efficiency calculations authoritative and prevents free generation.

## Bundled grades

| Grade | Stress capacity | Boiler load | Cylinders | Crank phasing |
|---|---:|---:|---:|---|
| Copper — I | 1024 SU | 1 engine unit | 1 | single cylinder |
| Brass — II | 2560 SU | 2 engine units | 2 | 180° opposed |
| Industrial — III | 4608 SU | 3 engine units | 3 | 120° evenly phased |

The actual generated capacity remains multiplied by Create's boiler efficiency. A dry, undersized, or underheated boiler still reduces or stops the engine.

All grades retain Create's native 16–64 RPM efficiency curve. Higher grades increase stress capacity and mechanical smoothness, not the maximum shaft speed.

## Animation

`SteamEngineGradeRenderer` renders one independent piston, linkage, and crank connector per configured cylinder.

For cylinder `i`:

```text
phase_i = 2π × i / cylinder_count
```

The piston position uses the same slider-crank geometry as Create, parameterized by the grade profile:

```text
piston = r × sin(angle)
       - sqrt(L² - r² × cos²(angle))
```

where:

- `r` is `crank_radius`;
- `L` is `connecting_rod_length`;
- `angle` includes the cylinder phase offset.

Brass and Industrial engines use narrower grade-specific partial models so multiple cylinders remain visually distinct inside one block footprint.

## Data-pack profiles

Profiles are loaded from:

```text
data/<namespace>/steam_engine_grade_profiles/*.json
```

Built-in profile IDs:

```text
zeppelin_must_have:copper
zeppelin_must_have:brass
zeppelin_must_have:industrial
```

Schema version 1:

```json
{
  "schema_version": 1,
  "stress_capacity": 2560.0,
  "boiler_load_units": 2,
  "cylinder_count": 2,
  "crank_radius": 0.42,
  "connecting_rod_length": 0.95,
  "piston_base_offset": 1.28,
  "cylinder_spread": 0.34,
  "steam_particle_scale": 1.35
}
```

`/reload` updates already placed engines on their next lazy tick. Capacity changes force the attached powered shaft to rebuild its generated rotation entry.

## Crafting progression

### Grade I — Copper

A shaped upgrade from Create's native Steam Engine using Copper Sheets, a Precision Mechanism, Shafts, and an Andesite Casing.

### Grade II — Brass

Create Mechanical Crafting using the Copper grade, Brass Sheets, an Electron Tube, a Brass Casing, and Precision Mechanisms.

### Grade III — Industrial

A five-by-five Mechanical Crafting recipe using the Brass grade, Sturdy Sheets, Flywheels, Brass Casings, Electron Tubes, Brass Sheets, and Precision Mechanisms.

## Automated verification

`SteamEngineGradeGameTests` verifies:

- increasing cylinder count and capacity across grades;
- stress-registry values supplied by the active profiles;
- weighted boiler load for the Industrial grade.

The complete GameTest suite currently contains 20 required tests.
