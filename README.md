# Zeppelin Must Have

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-3C8527?style=flat-square)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.235-EF7B22?style=flat-square)](https://neoforged.net/)
[![Create](https://img.shields.io/badge/Create-6.0.10-5C8D89?style=flat-square)](https://github.com/Creators-of-Create/Create)
[![Aeronautics](https://img.shields.io/badge/Create_Aeronautics-1.3.0-7696D2?style=flat-square)](https://modrinth.com/mod/create-aeronautics)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square)](https://adoptium.net/)
[![Build](https://img.shields.io/github/actions/workflow/status/WorldOfKayla/ZeppelinMustHave/build.yml?branch=main&style=flat-square&label=build)](https://github.com/WorldOfKayla/ZeppelinMustHave/actions)

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
Zeppelin Must Have 0.4.0
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

### Fuel integration

Burners accept:

- vanilla and NeoForge furnace fuels;
- regular Create Blaze Burner fuels;
- superheated Create Blaze Burner fuels;
- Creative Blaze Cake for infinite operation.

Fuel classification uses current Create data maps. Superheated output, capacity, consumption, range, and throttle curve come from the resolved server profile.

### Engineer's Goggles

Burner goggles preserve the standard Aeronautics balloon section and append:

- stored fuel and heat grade;
- redstone strength and calculated throttle;
- current gas output;
- profile ID;
- configured capacity and envelope range;
- current fuel-consumption rate.

Extended profile diagnostics appear while sneaking. The resolved profile is synchronized from the server, so clients do not need the server data pack installed.

## Ponder

The mod registers its own isolated `PonderPlugin` and the category **Zeppelin Systems**.

Implemented scenes:

- **Airship Helm Telemetry** — Sable sub-level detection, physics telemetry, Aeronautics balloon aggregation, and empty-hand inspection;
- **Airship Burner Operation** — fuel insertion, redstone throttling, airtight envelopes, tier progression, and soul-fire appearance.

Scene structures are stored at:

```text
assets/zeppelin_must_have/ponder/helm/telemetry.nbt
assets/zeppelin_must_have/ponder/burner/operation.nbt
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
- four custom 16×16 functional textures only: Helm panel, altimeter, ballast indicator, and burner service panel;
- native Aeronautics hot-air burner renderer for flame and redstone indication;
- full block geometry inherited by item models with GUI, hand, ground, and fixed transforms.

## Crafting progression

All eight registered blocks have recipes and block drops.

| Equipment | Production path |
|---|---|
| Airship Helm | Simulated steering wheel, gimbal and altitude sensors, plus Create precision mechanisms and brass casing |
| Airship Burner | Create Blaze Burner upgraded with copper sheets and andesite alloy |
| Forced-Draft Burner | Airship Burner, Encased Fans, brass sheets, and precision mechanisms |
| Industrial Burner | 5×5 Create Mechanical Crafting recipe using the forced-draft tier, fluid tanks, fans, sturdy sheets, brass sheets, and precision mechanisms |
| Ballast Tank | Create fluid tanks, fluid valve, smart pipe, copper sheets, and precision mechanism |
| Mooring Winch | Create Rope Pulley, large cogwheels, shafts, brass casing, and chain |
| Altitude Gauge | Simulated altitude sensor, Create speedometer, precision mechanisms, brass sheets, and compass |
| Vertical Thruster | Aeronautics propeller and gyroscopic bearing with Create Encased Fans and brass casing |

The Ballast Tank, Mooring Winch, Altitude Gauge, and Vertical Thruster currently have production assets and recipes; their functional block entities are subsequent implementation stages.

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
| Zeppelin Must Have | `0.4.0` |

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
