# Internal Refactoring

This document describes the internal code organization of Zeppelin Must Have. The public registry IDs and the existing `ZmhBlocks` source-level facade remain stable.

## Registration pipeline

All vanilla registries are owned by `ZmhRegistryContext`:

```text
ZmhRegistryContext
├── BLOCKS
├── ITEMS
├── BLOCK_ENTITY_TYPES
└── CREATIVE_TABS
```

Block definitions are split by domain:

```text
ZmhAirshipBlocks
├── Helm
├── burner family
├── Ballast Tank
├── Mooring Winch
├── Altitude Gauge
└── Vertical Thruster

ZmhSteamPowerBlocks
├── graded boilers
└── graded steam engines

ZmhRedstoneBlocks
├── conduit family
├── native lever
└── waterproof repeater
```

`ZmhBlockRegistrar` is the single block-and-item registration path. It returns a typed `RegisteredBlock<B, I>` containing both deferred handles. Most blocks use the default `BlockItem` factory; graded boilers supply `BoilerGradeItem::new`.

`ZmhBlocks` is intentionally retained as a stable public facade. Existing code can continue using names such as `ZmhBlocks.COPPER_BOILER_BASE`, but the facade no longer constructs blocks or registers items.

`ZmhCreativeContents` defines creative-tab ordering only. It does not perform registration.

## Block entity registration

`ZmhBlockEntityTypes` uses one generic builder helper for name, factory, and valid-block suppliers. Boiler grades still receive distinct `BlockEntityType` instances because Create's `ConnectivityHandler` groups tanks by exact type identity.

## Runtime responsibilities

### Airship Burner

```text
AirshipBurnerBlockEntity
├── lifecycle and Aeronautics integration
├── fuel insertion and consumption orchestration
├── upgrade installation API
└── synchronization triggers

AirshipBurnerConfiguration
├── data-pack profile resolution
├── upgrade modifier composition
└── revision tracking

AirshipBurnerStateCodec
└── persistent and client-snapshot NBT

AirshipBurnerPresentation
├── player status messages
├── Engineer's Goggles sections
└── diagnostics formatting
```

### Altitude Gauge

```text
AltitudeGaugeBlockEntity
├── sampling lifecycle
├── world/redstone I/O
├── state persistence
└── client synchronization

AltitudeGaugeController
└── pure mode and altitude-hold output policy

AltitudeGaugePresentation
├── interaction messages
└── Engineer's Goggles output
```

### Piped Redstone

```text
PipedRedstoneBlock
├── blockstate transitions
├── vanilla redstone I/O
└── wrench and interaction entry points

PipedRedstoneTopology
└── reciprocal-port and elbow/terminal mutation

PipedRedstonePlacement
└── Create placement-assist traversal

PipedRedstoneNetworkManager
├── rebuild scheduling
├── safe application phase
└── neighbor notification

PipedRedstoneNetworkDiscovery
└── reciprocal-port graph discovery and weakest-link profile

PipedRedstoneSignalSolver
└── strongest-source signal propagation
```

### Airship service systems

```text
BallastTankBlockEntity
├── NeoForge fluid capability
├── data-driven capacity and density
├── comparator / Goggles / renderer synchronization
└── SableBallastMassBridge

MooringWinchBlockEntity
└── Create Simulated RopeWinchBlockEntity contract

VerticalThrusterBlockEntity
├── reload-aware propulsion profile
└── Aeronautics BasePropellerBlockEntity contract
```

`ZmhCapabilities` is the single capability-registration entry point. `ZmhDataReloaders` includes ballast and vertical-thruster profiles, while `VerticalThrusterStressRegistration` owns the Create stress impact supplier.

## Ponder organization

`ZmhPonderScenes` is only the root registration entry point. Storyboards are split into domain modules:

- `ZmhHelmPonderScenes`;
- `ZmhBurnerPonderScenes`;
- `ZmhSteamPowerPonderScenes`;
- `ZmhControlPonderScenes`;
- `ZmhRedstonePonderScenes`;
- `ZmhServicePonderScenes`.

## Bootstrap organization

`ZmhDataReloaders` owns the list of data-driven resource reload listeners. `SteamEngineStressRegistration` owns Create stress-capacity and RPM metadata for the graded engine family. The main mod class therefore coordinates bootstrap phases without containing domain-specific registration tables.

## Compatibility invariants

1. Registry names are unchanged.
2. `ZmhBlocks` public deferred handles are unchanged.
3. Save NBT keys are unchanged.
4. Boiler grades retain distinct block entity types.
5. Data-pack directories and profile IDs are unchanged.
6. Gameplay and renderer behavior must remain covered by the existing GameTests and client runtime checks.
