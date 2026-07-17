# Graded Fluid-Tank Boilers

Zeppelin Must Have provides three graded boilers implemented as direct subclasses of Create's `FluidTankBlock` and `FluidTankBlockEntity`.

They are not heat-exchanger blocks placed under a separate tank. Every boiler block is itself a fluid tank, participates in Create's native tank multiblock, stores fluid, renders its contents, accepts Engineer's Goggles and the Create Wrench, and becomes a Create boiler when Steam Engines or Steam Whistles are attached.

The save-compatible block registry IDs remain:

```text
zeppelin_must_have:copper_boiler_base
zeppelin_must_have:brass_boiler_base
zeppelin_must_have:industrial_boiler_base
```

The `_base` suffix is retained to avoid breaking existing recipes, tags, commands, and inventories. The displayed names are Copper Boiler, Brass Boiler, and Industrial Boiler.

## Construction

A boiler is assembled exactly like a Create Fluid Tank:

```text
matching graded boiler blocks
matching graded boiler blocks
        в”‚
registered Create BoilerHeater
```

Rules:

- blocks of the same grade merge through Create's `ConnectivityHandler`;
- vertical stacks and valid square tank footprints use Create's normal controller, width, height, and capacity logic;
- Copper, Brass, and Industrial blocks never merge with one another;
- ordinary Create Fluid Tanks do not merge with graded boilers;
- the heat source is placed directly below each block in the bottom layer;
- any block registered through Create's `BoilerHeater` API can be used;
- Steam Engines and Steam Whistles attach to the graded tank exactly as they do to a Create Fluid Tank.

Grade isolation is enforced structurally. Each grade has its own `BlockEntityType`, because Create identifies compatible multiblock parts by exact block-entity type.

## Native Create behaviour retained

The implementation delegates the following systems to Create without copying them:

- multiblock discovery, controller election, split, and rebuild;
- per-block fluid capacity and shared tank storage;
- fluid capability forwarding from child blocks to the controller;
- water-input sampling;
- Steam Engine and Steam Whistle discovery;
- passive and active boiler states;
- boiler level, size limit, water limit, and level-18 ceiling;
- engine efficiency and generated Stress Units;

Graded Steam Engines are counted as weighted engine units through their `boiler_load_units` profile value before Create calculates efficiency. Copper, Brass, and Industrial engines therefore consume one, two, and three boiler units respectively.
- comparator output;
- Engineer's Goggles base tooltip;
- Create Wrench window toggling and dismantling;
- fluid and boiler-gauge rendering;
- blockstate transitions for top, middle, bottom, and window shapes.

Zeppelin Must Have overrides only `BoilerData.updateTemperature`. Every heater column is read through `BoilerHeater.findHeat`, then transformed by the selected grade profile.

## Transfer equation

For active heat:

```text
transferred_heat = clamp(
    round(source_heat Г— heat_multiplier + additive_heat),
    1,
    maximum_heat_output
)
```

Create's sentinel values preserve their meaning:

- `NO_HEAT` remains no heat;
- `PASSIVE_HEAT` remains passive and is never amplified;
- active heat from all heated bottom-layer columns is summed normally;
- the final usable boiler level is still constrained by Create's size and water calculations.

## Bundled grades

| Grade | Block | Kindled Blaze Burner | Superheated Blaze Burner | Maximum per column |
|---|---|---:|---:|---:|
| I | Copper Boiler | 2 | 3 | 3 |
| II | Brass Boiler | 3 | 5 | 5 |
| III | Industrial Boiler | 5 | 8 | 8 |

A higher grade reduces the number of actively heated columns needed to reach the size/water ceiling. It does not allow a small, dry, or engine-starved boiler to bypass Create's limits.

## Data-pack profiles

Profiles are loaded from:

```text
data/<namespace>/boiler_grade_profiles/<profile>.json
```

Built-in IDs:

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

`/reload` updates already placed controllers. The block entity observes the profile revision, marks its inherited Create boiler data for temperature recalculation, and synchronizes the resolved server profile to clients.

Every multiblock renders as one continuous pressure vessel: grade-specific connected textures remove internal block frames and retain rims only around the external perimeter.

## Fluid capability and rendering

NeoForge's block fluid capability is registered separately for all three grade-specific block-entity types. Child blocks expose the inherited Create forwarding handler, so pipes can interact with any section of the multiblock.

The client registers Create's `FluidTankRenderer` for every grade and wraps the baked blockstate models with `FluidTankModel.standard`. This preserves controller-only fluid rendering and internal-face culling while allowing grade-specific casing textures.

The boiler pressure gauge is also grade-aware. Copper, Brass, and Industrial vessels use separate gauge-face and animated dial textures selected by `BoilerGradeTier`.

## Crafting progression

### Grade I вЂ” Copper Boiler

Shaped crafting upgrades one Create Fluid Tank using Copper Sheets, a Fluid Pipe, and an Andesite Casing. Output: one Copper Boiler.

### Grade II вЂ” Brass Boiler

Create Mechanical Crafting upgrades one Copper Boiler using Brass Sheets, an Electron Tube, a Brass Casing, and Precision Mechanisms. Output: one Brass Boiler.

### Grade III вЂ” Industrial Boiler

A five-by-five Create Mechanical Crafting recipe upgrades one Brass Boiler using Sturdy Sheets, additional Fluid Tanks, Electron Tubes, Copper and Brass Sheets, and Precision Mechanisms. Output: one Industrial Boiler.

## Automated verification

`BoilerGradeGameTests` verifies the live implementation:

- equal-grade blocks form one vertical Create tank multiblock;
- controller height and shared fluid capacity expand correctly;
- different grades retain separate controllers and block-entity types;
- Grade I transforms kindled Blaze Burner heat to 2;
- Grade III transforms superheated Blaze Burner heat to 8;
- passive heat remains passive and contributes no active heat.
