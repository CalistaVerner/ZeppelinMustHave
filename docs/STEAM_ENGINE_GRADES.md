# Graded Steam Engines

Zeppelin Must Have adds seven Create-compatible MK-series steam engines for the graded boiler family.

Every engine uses Create's powered-shaft and kinetic-network rules. The grade profile controls stress capacity, boiler load, cylinder count, crank geometry, and steam output intensity. Heat, water supply, boiler size, and Create's efficiency calculation remain authoritative.

## Installation

MK I–V attach directly to a Zeppelin Must Have graded boiler and use the standard Create shaft position two blocks from the engine.

MK VI uses a T-shaped four-cell body and requires an Industrial Boiler MK III.

MK VII uses a three-block-wide body. Its three banks contain nine internal crankshafts, coupled through a central reduction gearbox to one Create-compatible output shaft.

```text
MK I–V:

Graded Boiler ─ Engine ─ clear ─ Create Shaft

MK VI, top view:

                 Left Cylinder Bank
Industrial Boiler ─ Controller ─ Shaft Nose ─ Create Shaft
                 Right Cylinder Bank

MK VII, top view:

Industrial Boiler MK III:   [ B ][ B ][ B ]
Engine body:                 [ L ][ C ][ R ]
Required service clearance:  [ . ][ . ][ . ]
Output row:                   [ . ][ S ][ . ]

L = left bank, C = controller/reduction gearbox, R = right bank,
S = the single powered output shaft.
```

MK VII placement is rejected before any auxiliary block is placed when any of the following is unavailable:

- one of the two side-body cells;
- one of the three service-clearance cells;
- the central output-shaft position;
- one of the three Industrial Boiler MK III backing cells.

Vanilla Create Fluid Tanks intentionally continue using the native Create Steam Engine. Graded engines require `BoilerGradeBlockEntity`, preserving authoritative heat, water, engine-load, and efficiency accounting.

## Bundled grades

| Grade | Stress capacity | Boiler load | Cylinders / internal shafts | Output |
|---|---:|---:|---:|---|
| Copper — MK I | 1024 SU | 1 engine unit | 1 | one shaft |
| Brass — MK II | 2560 SU | 2 engine units | 2 | one shaft |
| Industrial — MK III | 4608 SU | 3 engine units | 3 | one shaft |
| Grand — MK IV | 8192 SU | 4 engine units | 4 | one shaft |
| Sovereign — MK V | 12288 SU | 6 engine units | 5 | one shaft |
| Leviathan — MK VI | 20480 SU | 10 engine units | 8 across two banks | one shaft |
| MK VII | 36864 SU | 18 engine units | 9 across three banks | one central shaft |

All grades retain Create's normal 16–64 RPM efficiency range. Higher grades increase available Stress Units and mechanical smoothness; they do not bypass boiler limitations or increase the maximum shaft speed.

## MK VII drive architecture

Each physical bank contains three crank positions. The three local positions are separated by 120 degrees. Adjacent banks are offset by 40 degrees, giving nine evenly distributed power strokes while preserving a balanced three-crank arrangement inside every block.

The controller is the only section that:

- contributes the 18-unit boiler load;
- publishes engine state to the Flight Control Network;
- accepts throttle, reversal, and emergency-cutoff commands;
- owns the powered output shaft;
- contributes the configured 36864 SU capacity.

The side banks are structural members of the same machine. Removing any bank dismantles the whole assembly, and removing the controller also removes the automatically created central output shaft.

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
zeppelin_must_have:grand
zeppelin_must_have:sovereign
zeppelin_must_have:leviathan
zeppelin_must_have:mk_vii
```

Schema version 1:

```json
{
  "schema_version": 1,
  "stress_capacity": 36864.0,
  "boiler_load_units": 18,
  "cylinder_count": 9,
  "crank_radius": 0.64,
  "connecting_rod_length": 1.44,
  "piston_base_offset": 1.52,
  "cylinder_spread": 0.24,
  "steam_particle_scale": 5.5
}
```

`/reload` updates already placed engines on their next lazy tick. Capacity changes rebuild the output shaft's generated-rotation entry.

## Crafting progression

- **MK I — Copper:** shaped upgrade from Create's native Steam Engine.
- **MK II — Brass:** Mechanical Crafting upgrade from MK I.
- **MK III — Industrial:** five-by-five industrial recipe.
- **MK IV — Grand:** flagship upgrade using Flywheels, Precision Mechanisms, Electron Tubes, Railway Casings, Brass Casings, and Sturdy Sheets.
- **MK V — Sovereign:** capital-class upgrade using Netherite and reinforced components.
- **MK VI — Leviathan:** combines Sovereign machinery with Industrial Boiler MK III components.
- **MK VII:** combines three Leviathan MK VI engines with Industrial Boiler MK III components, Flywheels, Railway Casings, Sturdy Sheets, Netherite Ingots, and Precision Mechanisms.

## Automated verification

`SteamEngineGradeGameTests` verifies:

- profile capacity, boiler load, and cylinder counts;
- stress-registry values;
- MK VI footprint and boiler requirements;
- MK VII rejection of a narrow boiler face;
- MK VII rejection of occupied side-body, service-row, or output positions;
- creation of the complete three-block body;
- exactly nine internal crank positions and one powered output shaft.
