# Zeppelin Must Have

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-3C8527?style=flat-square)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.235-EF7B22?style=flat-square)](https://neoforged.net/)
[![Create](https://img.shields.io/badge/Create-6.0.10-5C8D89?style=flat-square)](https://github.com/Creators-of-Create/Create)
[![Aeronautics](https://img.shields.io/badge/Create_Aeronautics-1.3.0-7696D2?style=flat-square)](https://modrinth.com/mod/create-aeronautics)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square)](https://adoptium.net/)
[![Build](https://img.shields.io/github/actions/workflow/status/CalistaVerner/ZeppelinMustHave/build.yml?branch=main&style=flat-square&label=build)](https://github.com/CalistaVerner/ZeppelinMustHave/actions)

**Zeppelin Must Have** is a Java add-on for **Create Aeronautics** on Minecraft 1.21.1 NeoForge. It develops the existing Create, Sable, Create Simulated, and Aeronautics systems with zeppelin-specific control, telemetry, buoyancy, propulsion, navigation, and mooring equipment.

**Author:** `us.Kayla`

## Platform

```text
Minecraft 1.21.1 / NeoForge 21.1.235
        │
        ▼
Create 6.0.10
        │
        ▼
Sable 2.0.0
        │
        ▼
Create Simulated 1.3.0
        │
        ▼
Create Aeronautics 1.3.0
        │
        ▼
Zeppelin Must Have 0.9.0
```

All upstream mods are mandatory compile-time and runtime dependencies.

## Compatibility contract

The add-on does **not** replace or patch upstream mechanics:

- no Mixins;
- no access transformers;
- no registry replacement;
- no overwritten Create or Aeronautics blocks;
- no parallel balloon, gas, airtight-envelope, air-pressure, or lift simulation.

Integration uses public contracts:

- `Sable.HELPER.getContaining(...)` for containing sub-level discovery;
- `RigidBodyHandle` for authoritative physics telemetry;
- Aeronautics `BalloonMap` and `BlockEntityLiftingGasProvider` for balloon interaction;
- Create fuel data maps for regular and superheated burner fuel;
- Create `IHaveGoggleInformation` for Engineer's Goggles;
- Ponder `PonderPlugin` for documentation scenes;
- NeoForge server resource reload for data-pack profiles.

## Airship Helm

`zeppelin_must_have:airship_helm` is a server-authoritative telemetry block.

It reports:

- vessel name and persistent Sable sub-level UUID;
- global position and altitude;
- heading, pitch, and roll;
- linear, vertical, and angular velocity;
- sub-level mass;
- Aeronautics balloon count and capacity;
- filled and target lifting-gas volume;
- balloon fill ratio and raw lift.

Telemetry is sampled every five server ticks and synchronized only after material changes or the periodic refresh interval.

### Engineer's Goggles

Looking at the Helm while wearing Create Engineer's Goggles displays normal flight information. Sneaking adds diagnostic information: sub-level UUID, global coordinates, angular velocity, and telemetry age.

## Airship Burner family

All three burners extend the native Aeronautics hot-air burner block entity and retain its value behaviour, gas type, balloon discovery, fill interpolation, air-pressure handling, Ponder rendering, sound, and particle systems.

### Bundled default profiles

| Burner | Profile ID | Gas multiplier | Full-power fuel use | Fuel capacity | Envelope range |
|---|---|---:|---:|---:|---:|
| Airship Burner | `zeppelin_must_have:standard` | `1.0×` | `1.0` tick/tick | 12,000 ticks | 16 blocks |
| Forced-Draft Airship Burner | `zeppelin_must_have:forced_draft` | `2.25×` | `1.6` ticks/tick | 24,000 ticks | 32 blocks |
| Industrial Airship Burner | `zeppelin_must_have:industrial` | `4.5×` | `3.0` ticks/tick | 48,000 ticks | 64 blocks |

These are data-pack defaults, not Java constants. Server packs may replace them and apply changes with `/reload`.

Profile documentation and a reusable template:

```text
docs/DATA_PACK_PROFILES.md
docs/templates/airship_burner_profile.template.json
```

### Unified heat-source system

`AirshipHeatSources` normalizes existing upstream fuel mechanisms into one heat-source contract:

- vanilla and NeoForge furnace fuels;
- regular Create Blaze Burner fuels;
- superheated Create Blaze Burner fuels;
- Creative Blaze Cake for infinite operation.

`AirshipHeatReservoir` stores regular and superheated contributions in separate layers. Both grades may be inserted in any order; superheated heat is consumed first and the burner falls back to the regular reserve automatically. Existing 0.4.x fuel NBT is migrated during block-entity loading.

### Combining multiple burners

The add-on does not create a parallel heat network. `BalloonHeatAggregate` observes the native Aeronautics `Balloon.getHeaters()` collection and reports connected sources, active sources, and combined gas output. Every burner remains an independent `BlockEntityLiftingGasProvider`; Aeronautics remains responsible for combining providers, filling the envelope, pressure, and lift.

Detailed design: `docs/HEAT_SYSTEM.md`.

### Upgrade sockets

Every burner now provides three persistent sockets:

- **Thermal** — Heat Recuperator for endurance and reservoir capacity;
- **Airflow** — Forced Induction for higher output and envelope range;
- **Control** — Precision Regulator for finer low-power redstone control.

Modules install by right-clicking the burner. Sneak-use the Create Wrench to remove the most recently occupied socket; breaking the burner drops all modules. Definitions, compatibility, conflicts, and numerical modifiers are loaded from `data/*/airship_upgrades/*.json`, so server packs can add or rebalance upgrades without Java changes.

Documentation: `docs/UPGRADES.md`.

### Engineer's Goggles

Burner goggles preserve the standard Aeronautics balloon section and append:

- total, regular, and superheated heat reserves;
- active heat grade;
- redstone strength and calculated throttle;
- individual gas output;
- active and connected balloon heat sources;
- combined gas output of the native Aeronautics heater collection;
- profile ID, capacity, range, and current consumption;
- installed upgrade modules and their aggregated effective modifiers.

Extended diagnostics appear while sneaking. The resolved profile and heat-network aggregate are synchronized from the server, so clients do not need the server data pack installed.



## Graded Create Fluid-Tank Boilers

The add-on provides three pressure-vessel grades implemented as direct descendants of Create's Fluid Tank. The graded block is the tank itself:

```text
Copper / Brass / Industrial Boiler blocks
Copper / Brass / Industrial Boiler blocks
        в”‚
Blaze Burner or another registered BoilerHeater
```

Blocks of one grade merge through Create's native tank connectivity, share fluid capacity, render their contents, accept the Create Wrench, and support Steam Engines and Steam Whistles. Different grades have separate block-entity types and cannot merge into one controller.

| Grade | Normal active heat | Superheated heat | Maximum per heater column |
|---|---:|---:|---:|
| Copper вЂ” Grade I | 2 | 3 | 3 |
| Brass вЂ” Grade II | 3 | 5 | 5 |
| Industrial вЂ” Grade III | 5 | 8 | 8 |

Passive heat remains passive and absent heat remains absent. Create remains authoritative for multiblock dimensions, fluid storage, water requirements, attached engines, efficiency, boiler level, comparator output, and generated Stress Units.

EngineerвЂ™s Goggles append the resolved grade profile to Create's native tank/boiler information. Tuning is data-driven through `data/*/boiler_grade_profiles/*.json`.

Same-grade boiler blocks render as one continuous pressure vessel. Connected textures remove the 1×1 block grid and keep structural rims only on the outer perimeter.

The pressure gauge frame, scale, and animated dial are selected from the vessel grade, so the front instrument matches Copper, Brass, or Industrial construction.

The block registry IDs retain the historical `_boiler_base` suffix for save and recipe compatibility. Documentation: `docs/BOILER_GRADES.md`.

## Graded Steam Engines

The graded boiler family now has matching Steam Engines. Every grade inherits Create's normal shaft placement, rotation-direction control, boiler efficiency, and kinetic-network integration, while adding its own stress capacity, housing, and animated crank mechanism.

| Grade | Capacity | Boiler load | Animated mechanism |
|---|---:|---:|---|
| Copper вЂ” I | 1024 SU | 1 unit | one cylinder |
| Brass вЂ” II | 2560 SU | 2 units | two cylinders, 180В° opposed |
| Industrial вЂ” III | 4608 SU | 3 units | three cylinders, 120В° phased |

Higher-grade engines remain bounded by Create's boiler size, heat, water supply, and efficiency. They must be attached to a Zeppelin Must Have graded boiler; vanilla Create Fluid Tanks continue using the native Create Steam Engine.

Numerical capacity, boiler load, crank geometry, cylinder spacing, and steam-particle intensity are data-driven through `data/*/steam_engine_grade_profiles/*.json`.

Documentation: `docs/STEAM_ENGINE_GRADES.md`.

## Piped Redstone

Piped Redstone provides protected analog redstone routing for airships and dense Create machinery.

Reciprocally connected conduit blocks render as one continuous constant-section tube: internal end caps are culled, while closed ports remain visibly sealed and electrically isolated.

Its connections are explicit six-sided ports rather than automatic adjacency. Use the Create Wrench on a selected face to open or close that port. Two conduits may therefore run in neighboring blocks, cross beside one another, or pass through the same machinery compartment without merging unless reciprocal ports are deliberately enabled.

The conduits are solid, waterloggable blocks. Water and flowing liquids do not wash them away or interrupt their signal.

Holding another conduit and using it on an existing straight run activates Create Placement Assist: the far end is extended like a Shaft, the axis ports are preserved, and the normal placement-assist/Extendo Grip range applies.

| Tier | Data-pack profile | Propagation delay | Repeater-free distance |
|---|---|---:|---:|
| Copper | `zeppelin_must_have:copper` | 4 game ticks | 32 edges |
| Brass | `zeppelin_must_have:brass` | 2 game ticks | 64 edges |
| Resonant | `zeppelin_must_have:resonant` | 1 game tick | 128 edges |

The complete analog value `0..15` is preserved without per-block attenuation until the tier distance limit. Mixed-tier components use the greatest delay and shortest distance present in the connected network.

The Piped Redstone Native Lever mounts directly to a conduit face, automatically opens that port, and emits `0/15` only into the attached isolated line. Its physical handle rotates smoothly through a block-entity renderer and it remains usable while waterlogged.

The waterproof Piped Redstone Repeater preserves analog strength and starts a new distance segment. Ordinary right-click cycles the same four delay settings as a vanilla repeater: `1..4` redstone ticks, or `2/4/6/8` game ticks.

Profiles are loaded from `data/*/piped_redstone_profiles/*.json`. Detailed mechanics, recipes, and automated tests are documented in `docs/PIPED_REDSTONE.md`.


## Automatic Altitude Control

The existing Altitude Gauge is now a functional directional flight sensor and inline burner controller.

It provides four Create-Wrench-selectable analog modes:

- **Altitude telemetry** — maps global Sable altitude across the dimension build range;
- **Vertical-speed telemetry** — maps descent and climb around neutral signal `8`;
- **Balloon-fill telemetry** — exposes the native Aeronautics fill ratio as `0..15`;
- **Altitude Hold** — adds proportional altitude correction and vertical-speed damping to a rear trim input.

A practical control chain is:

```text
Piped Redstone Native Lever
        │ base burner trim
        ▼
Altitude Gauge rear input
        │ stabilized output
        ▼
Piped Redstone
        ▼
Airship Burners
```

Sneak-right-click with an empty hand captures the current altitude and arms the controller. Sneak-right-click again disables it; while disabled, trim passes through unchanged. Output slew limiting and a configurable deadband reduce oscillation.

All controller gains, sampling rate, damping, and slew limits are data-pack driven through `data/*/altitude_control_profiles/*.json`.

Documentation: `docs/ALTITUDE_CONTROL.md`.

## Ponder

The mod registers its own isolated `PonderPlugin` and the category **Zeppelin Systems**.

Implemented scenes:

- **Airship Helm Telemetry** — Sable sub-level detection, physics telemetry, Aeronautics balloon aggregation, and empty-hand inspection;
- **Airship Burner Operation** — mixed heat reserves, redstone throttling, airtight envelopes, tier progression, soul-fire appearance, native Aeronautics aggregation, and installation/removal of the three upgrade socket types;
- **Protected Redstone for Airships** — explicit non-merging ports, waterlogging, three conduit tiers, adjustable repeater delay, and weakest-link mixed-tier behaviour;
- **Automatic Altitude Control** — trim input, four sensor/controller modes, target capture, damping, and burner output routing.

Scene structures are stored at:

```text
assets/zeppelin_must_have/ponder/helm/telemetry.nbt
assets/zeppelin_must_have/ponder/burner/operation.nbt
assets/zeppelin_must_have/ponder/redstone/conduits.nbt
assets/zeppelin_must_have/ponder/control/altitude_hold.nbt
```

Ponder preview state is applied through the burner block entity rather than by faking only its blockstate.

## Localizations

Complete key parity is maintained for:

- English — `en_us`
- Russian — `ru_ru`
- Italian — `it_it`
- Polish — `pl_pl`

The language files include block names, chat telemetry, burner status, Engineer's Goggles sections, Ponder scenes, and the Ponder category.

## Create-style assets

The asset pass follows Create's visual grammar:

- native Create andesite, brass, copper, industrial iron, fan, fluid-tank, pulley, axis, and gearbox textures;
- compact block silhouettes and restrained detailing;
- one shared item-display template for GUI, hand, ground, and fixed transforms;
- NeoForge `neoforge:composite` burner models built from reusable core, fan, source-manifold, and auxiliary modules;
- six custom 16×16 functional textures: Helm panel, altimeter, ballast indicator, burner panel, heat chamber, and heat manifold;
- native Aeronautics hot-air burner renderer for flame and redstone indication;
- single, dual, and triple combustion layouts that visibly converge into common collectors.

## Crafting progression

All registered equipment blocks and upgrade modules have production recipes; every block has a loot table.

| Equipment | Production path |
|---|---|
| Airship Helm | Simulated steering wheel, gimbal and altitude sensors, plus Create precision mechanisms and brass casing |
| Airship Burner | Create Blaze Burner upgraded with copper sheets and andesite alloy |
| Forced-Draft Burner | Airship Burner, Encased Fans, brass sheets, and precision mechanisms |
| Industrial Burner | 5×5 Create Mechanical Crafting recipe using the forced-draft tier, fluid tanks, fans, sturdy sheets, brass sheets, and precision mechanisms |
| Copper Boiler — Grade I | Create Fluid Tank upgraded with Copper Sheets, a Fluid Pipe, and Andesite Casing |
| Brass Boiler — Grade II | Mechanical Crafting upgrade from Copper grade with Brass Sheets, Electron Tube, Brass Casing, and Precision Mechanisms |
| Industrial Boiler — Grade III | 5×5 Mechanical Crafting upgrade from Brass grade with Sturdy Sheets, tanks, Electron Tubes, sheets, and mechanisms |
| Copper Steam Engine — Grade I | Create Steam Engine upgraded with Copper Sheets, Shafts, a Precision Mechanism, and Andesite Casing |
| Brass Compound Steam Engine — Grade II | Mechanical Crafting upgrade with Brass Sheets, Electron Tube, Brass Casing, and Precision Mechanisms |
| Industrial Triple-Expansion Steam Engine — Grade III | 5×5 Mechanical Crafting with Sturdy Sheets, Flywheels, Brass Casings, Electron Tubes, and mechanisms |
| Copper Piped Redstone | Copper Sheets, Create Fluid Pipes, and redstone dust; produces eight conduits |
| Brass Piped Redstone | Mechanical Crafting upgrade using Copper conduits, Brass Sheets, Electron Tubes, and Precision Mechanisms |
| Resonant Piped Redstone | 5×5 Mechanical Crafting with Brass conduits, Sturdy Sheets, Polished Rose Quartz, Electron Tubes, and Precision Mechanisms |
| Piped Redstone Native Lever | Copper conduit, vanilla Lever, Brass Sheet, and Electron Tubes |
| Piped Redstone Repeater | Copper conduit, Brass Sheets, Electron Tubes, and a Comparator |
| Ballast Tank | Create fluid tanks, fluid valve, smart pipe, copper sheets, and precision mechanism |
| Mooring Winch | Create Rope Pulley, large cogwheels, shafts, brass casing, and chain |
| Altitude Gauge | Simulated altitude sensor, Create speedometer, precision mechanisms, brass sheets, and compass |
| Vertical Thruster | Aeronautics propeller and gyroscopic bearing with Create Encased Fans and brass casing |

The Ballast Tank, Mooring Winch, and Vertical Thruster currently have production assets and recipes; their functional block entities are subsequent implementation stages. The Altitude Gauge is fully functional as of `0.8.0`.

## Version matrix

| Component | Version |
|---|---:|
| Minecraft | `1.21.1` |
| Java | `21` |
| NeoForge | `21.1.235` |
| Create | `6.0.10` / Maven build `6.0.10-280` |
| Sable | `2.0.0` |
| Create Simulated | `1.3.0` |
| Create Aeronautics | `1.3.0` |
| Ponder | `1.0.82` |
| Flywheel | `1.0.6` |
| Registrate | `MC1.21-1.3.0+67` |
| Zeppelin Must Have | `0.9.0` |

## Development

```powershell
# Compile and package
./gradlew.bat clean build

# Development client
./gradlew.bat runClient

# Dedicated development server
./gradlew.bat runServer

# Data-generation runtime
./gradlew.bat runData

# Automated NeoForge GameTests
./gradlew.bat runGameTestServer
```

Linux and macOS use `./gradlew`.

## Identity

| Field | Value |
|---|---|
| Repository | `ZeppelinMustHave` |
| Mod ID | `zeppelin_must_have` |
| Java package | `us.kayla.zeppelinmusthave` |
| Maven group | `us.kayla.zeppelinmusthave` |
| Author | `us.Kayla` |

## License

**All Rights Reserved.** The original NeoForge MDK template notice remains in `TEMPLATE_LICENSE.txt`.
