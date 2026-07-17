# Graded Boiler Bases

Zeppelin Must Have 0.9.0 adds graded heat-exchanger bases for the native Create boiler and Steam Engine system.

They do not replace Fluid Tanks, Steam Engines, Blaze Burners, water input, Stress Units, or Create's boiler-level calculation.

## Installation

Each heated tank column uses this vertical stack:

```text
Create Fluid Tank
        │
Boiler Base Grade I / II / III
        │
registered Create BoilerHeater
```

The heat source may be a Blaze Burner, a passive heater, or an addon block registered through Create's `BoilerHeater` API.

A Boiler Base must be directly below the bottom Fluid Tank and directly above the heat source.

## Transfer equation

Active heat is transformed by the selected data-pack profile:

```text
transferred_heat = clamp(
    round(source_heat × heat_multiplier + additive_heat),
    1,
    maximum_heat_output
)
```

Special Create values retain their semantics:

- `NO_HEAT` remains no heat;
- `PASSIVE_HEAT` remains passive and is never amplified;
- stacking one Boiler Base under another returns `NO_HEAT`.

The resulting value is returned through the public `BoilerHeater.findHeat` path used by Create's `BoilerData`.

## Bundled grades

| Grade | Block | Active Blaze Burner | Superheated Blaze Burner | Maximum output |
|---|---|---:|---:|---:|
| I | Copper Boiler Base | 2 | 3 | 3 |
| II | Brass Boiler Base | 3 | 5 | 5 |
| III | Industrial Boiler Base | 5 | 8 | 8 |

The actual boiler level is still limited by:

- boiler Fluid Tank size;
- supplied water per tick;
- Create's maximum boiler level of 18;
- attached Steam Engine count and efficiency.

A high-grade exchanger does not allow a small or dry boiler to exceed those limits. It reduces the number of actively heated columns required to reach the available size/water ceiling.

## Data-pack profiles

Profiles are loaded from:

```text
data/<namespace>/boiler_grade_profiles/<profile>.json
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
  "heat_multiplier": 2.0,
  "additive_heat": 1.0,
  "maximum_heat_output": 5
}
```

`/reload` updates already placed bases on their next sample and marks the Create Fluid Tank above them for a boiler-temperature refresh.

## Monitoring

Each base has a lightweight block entity that samples the source every five server ticks.

It provides:

- active/inactive light state;
- comparator output proportional to transferred heat;
- right-click status;
- Create Engineer's Goggles information;
- synchronized server profile values for remote clients.

Sneaking with Engineer's Goggles displays the multiplier, additive heat, maximum output, and installation order.

## Crafting progression

### Grade I — Copper

Standard shaped crafting using a Blaze Burner, Copper Sheets, Fluid Pipes, an Andesite Casing, and a Shaft. Produces two bases.

### Grade II — Brass

Create Mechanical Crafting using a Copper Boiler Base, Brass Sheets, Electron Tubes, a Fluid Tank, and Precision Mechanisms. Produces two bases.

### Grade III — Industrial

A five-by-five Mechanical Crafting recipe using the Brass grade, Sturdy Sheets, Fluid Tanks, Electron Tubes, Copper and Brass Sheets, and Precision Mechanisms.

## Automated verification

`BoilerGradeGameTests` validates through the live Create `BoilerHeater.REGISTRY`:

- normal Blaze Burner transfer for all three grades;
- superheated transfer and grade caps;
- passive heat remaining passive;
- stacked bases failing closed.
