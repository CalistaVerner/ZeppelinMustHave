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
        │
        ▼
Create Simulated 1.3.0
        │
        ▼
Create Aeronautics 1.3.0
        │
        ▼
Zeppelin Must Have 0.3.0
```

Create, Sable, Create Simulated, and Create Aeronautics are mandatory compile-time and runtime dependencies.

## Implemented in 0.3.0

### Airship Helm

`zeppelin_must_have:airship_helm` is now a functional, server-authoritative telemetry block.

The Helm:

- detects its containing Sable `ServerSubLevel` using `Sable.HELPER.getContaining`;
- reads the persistent sub-level UUID and optional vessel name;
- reads global position and logical pose;
- reads linear and angular velocity through `RigidBodyHandle`;
- reads current sub-level mass;
- aggregates all Create Aeronautics balloons belonging to the same sub-level;
- exposes balloon count, capacity, filled volume, target volume, fill ratio, and total lift;
- samples every five ticks and synchronizes materially changed state to clients;
- prints the current telemetry when right-clicked with an empty hand.

The Helm is currently read-only. Pilot commands, menus, packet-driven controls, autopilot modes, and force requests are subsequent stages.

### Airship burner family

The new burners implement Aeronautics `BlockEntityLiftingGasProvider` through the existing Hot Air Burner block entity architecture. Their output enters real `ServerBalloon` simulation and therefore appears automatically in Helm telemetry.

| Block | Registry ID | Gas multiplier | Full-power fuel use | Envelope range |
|---|---|---:|---:|---:|
| Airship Burner | `airship_burner` | `1.0×` | `1.0×` | 16 blocks |
| Forced-Draft Airship Burner | `forced_draft_airship_burner` | `2.25×` | `1.6×` | 32 blocks |
| Industrial Airship Burner | `industrial_airship_burner` | `4.5×` | `3.0×` | 64 blocks |

Burner behaviour:

- accepts vanilla furnace fuel;
- accepts Create regular and superheated Blaze Burner fuels through Create data maps;
- supports Creative Blaze Cake as infinite fuel;
- consumes fuel proportionally to redstone signal strength;
- emits lifting gas only when fueled and redstone-powered;
- supports the Aeronautics fire and soul-fire burner variants;
- reports remaining fuel, fuel grade, current gas output, and cast range on empty-hand interaction;
- exposes remaining fuel through comparator output;
- saves fuel state in block entity NBT;
- immediately disconnects from its balloon when fuel is exhausted.

The current burner bodies are placeholder block models. Functional flame particles, sound, light emission, fuel logic, and Aeronautics gas simulation are implemented; dedicated production models are still required.

## Production Minecraft models and textures

Version `0.3.0` replaces the temporary `cube_all` assets with custom multi-element Minecraft models.

Implemented model assets:

- Airship Helm with a wood-and-steel console, brass framing, wheel spokes, levers, and pressure instrument;
- three visually distinct burner tiers with separate combustion chambers, chimneys, forced-draft fans, and industrial piping;
- Ballast Tank with pressure gauge, liquid sight glass, reinforcement bands, and top valve;
- Mooring Winch with wooden deck base, rope drum, supports, gearbox, and crank;
- Altitude Gauge with dedicated 64×64 dial texture and three-dimensional needle;
- Vertical Thruster with ducted-fan texture, structural supports, grille, and thrust hub.

The resource pack contains 17 original pixel textures for brass, copper, oxidized metal, steel, wood, rope, fan blades, grilles, gauges, liquid level, and animated normal/soul flames. Burner blockstates compose the base body with a lit flame overlay selected by the Aeronautics burner variant. Item models inherit the full block geometry and include dedicated GUI, hand, ground, and fixed display transforms.

## Complete equipment scope

| Subsystem | Equipment | Status |
|---|---|---|
| Control | Airship Helm | Telemetry implemented; control output pending |
| Buoyancy | Airship burner family | Fuel and Aeronautics gas output implemented |
| Buoyancy | Ballast Tank | Registry shell |
| Propulsion | Vertical Thruster | Registry shell |
| Navigation | Altitude Gauge | Registry shell |
| Mooring | Mooring Winch | Registry shell |

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
| Zeppelin Must Have | `0.3.0` |

## Development

### Requirements

- JDK 21
- IntelliJ IDEA 2024.3 or newer
- Git

Open the repository root as a Gradle project. NeoForge run configurations are generated during Gradle synchronization.

```powershell
# Full verification and mod JAR
gradlew.bat clean build

# Development client with the complete required stack
gradlew.bat runClient

# Dedicated development server
gradlew.bat runServer

# Data-generation runtime
gradlew.bat runData
```

Linux and macOS use `./gradlew`.

## Source layout

```text
src/main/java/us/kayla/zeppelinmusthave/
├── ZeppelinMustHave.java
├── content/
│   ├── burner/
│   │   ├── AirshipBurnerBlock.java
│   │   ├── AirshipBurnerBlockEntity.java
│   │   └── AirshipBurnerTier.java
│   └── helm/
│       ├── AirshipFlightSnapshot.java
│       ├── AirshipHelmBlock.java
│       └── AirshipHelmBlockEntity.java
├── integration/
│   ├── AeronauticsFlightStateReader.java
│   └── SimulatedStack.java
├── registry/
│   ├── ZmhBlockEntityTypes.java
│   ├── ZmhBlocks.java
│   ├── ZmhCreativeTabs.java
│   └── ZmhRegistries.java
└── zeppelin/
    └── ZeppelinSubsystem.java
```

## Identity

| Field | Value |
|---|---|
| Repository | `ZeppelinMustHave` |
| Gradle project | `ZeppelinMustHave` |
| Mod ID | `zeppelin_must_have` |
| Java package | `us.kayla.zeppelinmusthave` |
| Maven group | `us.kayla.zeppelinmusthave` |
| Author | `us.Kayla` |

## License

**All Rights Reserved.** The original NeoForge MDK template notice remains available in `TEMPLATE_LICENSE.txt`.
