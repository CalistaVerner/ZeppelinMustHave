# Zeppelin Must Have

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-3C8527?style=flat-square)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.235-EF7B22?style=flat-square)](https://neoforged.net/)
[![Create](https://img.shields.io/badge/Create-6.0.10-5C8D89?style=flat-square)](https://github.com/Creators-of-Create/Create)
[![Aeronautics](https://img.shields.io/badge/Create_Aeronautics-1.3.0-7696D2?style=flat-square)](https://modrinth.com/mod/create-aeronautics)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square)](https://adoptium.net/)
[![Build](https://img.shields.io/github/actions/workflow/status/WorldOfKayla/ZeppelinMustHave/build.yml?branch=main&style=flat-square&label=build)](https://github.com/WorldOfKayla/ZeppelinMustHave/actions)

**Zeppelin Must Have** is a first-class add-on for **Create Aeronautics** on Minecraft 1.21.1 NeoForge. It adds the control, buoyancy, propulsion, navigation, and mooring equipment required for practical zeppelins.

**Author:** `us.Kayla`

## Required stack

```text
Minecraft 1.21.1 / NeoForge
        │
        ▼
Create 6.0.10
        │
        ▼
Sable 2.0.0
interactive sub-levels + Rapier physics pipeline
        │
        ▼
Create Simulated 1.3.0
assembly, redstone, interaction with simulated contraptions
        │
        ▼
Create Aeronautics 1.3.0
lift, propellers, airborne contraptions
        │
        ▼
Zeppelin Must Have 0.1.0
zeppelin-specific systems
```

`Create Simulated` is the core module of the Simulated Project. `Create Aeronautics` is the flight module built on top of it. Both are mandatory compile-time and runtime dependencies of this repository; Sable is mandatory as their moving-block physics layer.

## Design scope

| Subsystem | Initial equipment | Intended responsibility |
|---|---|---|
| Control | Airship Helm | Pilot input, steering demand, subsystem status |
| Buoyancy | Ballast Tank | Controlled mass, lift balance, and vertical trim |
| Propulsion | Vertical Thruster | Low-speed vertical positioning and manoeuvring |
| Navigation | Altitude Gauge | Altitude, vertical speed, and flight instrumentation |
| Mooring | Mooring Winch | Docking, anchoring, and ground handling |

The first revision establishes permanent registry IDs, localisations, loadable placeholder models, dependency metadata, CI, and subsystem boundaries. Functional block entities, kinetic behaviour, networking, Ponder scenes, and Sable/Aeronautics contraption integration are implemented in subsequent stages.

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
| Zeppelin Must Have | `0.1.0` |

## Development

### Requirements

- JDK 21
- IntelliJ IDEA 2024.3 or newer
- Git

Open the repository root as a Gradle project. NeoForge run configurations are generated during Gradle synchronization.

```powershell
# Full verification and mod JAR
gradlew.bat build

# Development client with Create, Sable, Simulated, and Aeronautics
gradlew.bat runClient

# Dedicated development server
gradlew.bat runServer

# Data generation using resources from all required mods
gradlew.bat runData
```

Linux and macOS use `./gradlew`.

## Source layout

```text
src/main/java/us/kayla/zeppelinmusthave/
├── ZeppelinMustHave.java
├── integration/
│   └── SimulatedStack.java
├── registry/
│   ├── ZmhBlocks.java
│   ├── ZmhCreativeTabs.java
│   └── ZmhRegistries.java
└── zeppelin/
    └── ZeppelinSubsystem.java
```

`SimulatedStack` is the explicit integration boundary. It imports the public `Create Simulated` and `Create Aeronautics` entrypoints at compile time and validates the complete required stack at runtime.

## Identity

| Field | Value |
|---|---|
| Repository | `ZeppelinMustHave` |
| Gradle project | `ZeppelinMustHave` |
| Mod ID | `zeppelin_must_have` |
| Java package | `us.kayla.zeppelinmusthave` |
| Maven group | `us.kayla.zeppelinmusthave` |
| Author | `us.Kayla` |

## Dependency policy

The following runtime dependencies are mandatory:

- Create `[6.0.10,6.1.0)`
- Sable `[2.0.0,3.0.0)`
- Create Simulated `[1.3.0,2.0.0)`
- Create Aeronautics `[1.3.0,2.0.0)`

All dependency versions are centralized in `gradle.properties`. Gradle resolves the public NeoForge artifacts from the Create and RyanHCode Maven repositories.

## License

**All Rights Reserved.** The original NeoForge MDK template notice remains available in `TEMPLATE_LICENSE.txt`.
