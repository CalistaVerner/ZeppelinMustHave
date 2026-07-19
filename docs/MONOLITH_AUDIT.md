# Monolith Audit

This audit covers `src/main/java` through the `0.15.0` industrial-powertrain release.

## Classification criteria

A long class is not automatically a monolith. A class is treated as an architectural monolith when several of the following are true:

1. more than 300 physical lines;
2. more than 30 methods with unrelated call paths;
3. ownership of multiple independent mutable state groups;
4. simultaneous responsibility for persistence, presentation, world integration, and domain calculations;
5. more than one external subsystem acting as a source of truth;
6. changes to one feature regularly require editing unrelated sections of the same class.

Storyboard and test classes are evaluated by cohesion rather than line count. One linear Ponder storyboard can be long without owning runtime state. A test class covering unrelated production modules is split even when it has no mutable state.

## Refactored runtime monoliths

| Original class | Before | After | Extracted responsibilities |
|---|---:|---:|---|
| `AirshipBurnerBlockEntity` | 398 | 290 | runtime state, reservoir codec/results, world effects |
| `SteamEngineGradeBlockEntity` | 315 | 210 | profile configuration, presentation, client effects |
| `AltitudeGaugeBlockEntity` | 313 | 256 | runtime state, reload configuration, NBT codec |
| `AirshipHelmBlockEntity` | 300 | 109 | presentation, telemetry cadence, client-state codec |
| `AirshipHeatReservoir` | 296 | 205 | NBT migration codec, persistent state, transfer result records |
| `PipedRedstoneBlock` | 290 | 205 | conduit callback policy and shared waterlogging |
| `PipedRedstoneNativeLeverBlock` | 283 | 186 | support topology, shape policy, signal policy |
| `BallastTankBlockEntity` | 270 | 167 | storage/profile state, mass controller, presentation |
| `PipedRedstoneRepeaterBlock` | 247 | 165 | delay/signal policy and shared waterlogging |

Line counts are a review aid, not the primary success condition. The important result is that each block entity now owns one state aggregate and delegates persistence, presentation, or external integration to a dedicated component.

## 0.15.0 decomposition pass

| Original class | Before | Facade after | Extracted responsibilities |
|---|---:|---:|---|
| `FlightControlNetworkManager` | 541 | 329 | vessel resolution, addressed network state, vessel state, telemetry aggregation, timeout policy |
| `SteamEngineGradeRenderer` | 348 | 167 | crank geometry, flagship mechanisms, shared partial transforms |
| `ZmhSteamPowerPonderScenes` | 417 | 15 | boiler storyboard and engine storyboard |
| `SteamEngineGradeBlockEntity` | 336 | 250 | shaft lifecycle, efficiency policy, assembly validation |

The FCN split also adds bounded cleanup for expired signal maps, stale computer heartbeats, stale telemetry, unused addressed networks, and inactive vessel entries. Persistent emergency state remains authoritative in `FlightControlEmergencySavedData`, so pruning transient cache entries does not lose safety state.

## Current responsibility boundaries

### Airship Burner

```text
AirshipBurnerBlockEntity
├── Aeronautics lifecycle
├── world-side balloon transitions
└── public block-entity facade

AirshipBurnerRuntimeState
├── heat reservoir
├── installed upgrades
├── resolved profile
└── client balloon snapshot

AirshipBurnerWorldEffects
├── world publication
├── LIT blockstate
└── sound effects

AirshipBurnerStateCodec
└── persistent/client NBT
```

### Steam Engine

```text
SteamEngineGradeBlockEntity
├── FCN command state
├── advancement/reporting orchestration
├── profile synchronization
└── Create block-entity lifecycle

SteamEngineShaftController
├── powered-shaft lookup and ownership
├── direction transfer
└── capacity refresh/deactivation

SteamEnginePowerController
└── boiler efficiency, emergency latch and throttle policy

SteamEngineAssemblyValidator
└── single-block, Leviathan and MK VII structural validity

SteamEngineGradeConfiguration / Presentation / ClientEffects
└── reload state, Goggles output and client effects
```

### Flight Control Network

```text
FlightControlNetworkManager
└── synchronized public routing facade

FlightControlVesselResolver
└── Sable sub-level to vessel identity resolution

FlightControlVesselState
├── addressed network ownership
├── emergency state mirror
└── cache lifecycle

FlightControlNetworkState
├── authority arbitration
├── primary-computer election
└── input/output expiry

FlightControlSystemsRegistry
└── short-lived system telemetry aggregation
```

### Altitude Gauge

```text
AltitudeGaugeBlockEntity
├── world redstone I/O
├── sampling orchestration
└── player-triggered world effects

AltitudeGaugeRuntimeState
├── mode
├── hold session
├── trim/output
└── telemetry snapshot

AltitudeGaugeConfiguration
├── reload-aware control profile
└── sample cadence

AltitudeGaugeStateCodec
└── persistent/client NBT
```

### Airship Helm

```text
AirshipHelmBlockEntity
├── lifecycle
└── snapshot ownership

AirshipHelmTelemetrySampler
└── sampling and forced-sync cadence

AirshipHelmPresentation
├── chat status
└── Engineer's Goggles

AirshipHelmStateCodec
└── client snapshot NBT
```

### Ballast Tank

```text
BallastTankBlockEntity
└── lifecycle adapter

BallastTankStorage
├── fluid capability state
├── data-pack profile
├── capacity clamping
└── NBT

BallastTankMassController
└── Sable mass binding lifecycle

BallastTankPresentation
└── chat and Goggles
```

### Piped Redstone blocks

```text
PipedRedstoneBlock
└── Minecraft block contract

PipedRedstoneBlockSupport
├── placement assist
├── topology callbacks
├── wrench mutation
└── signal exposure

PipedRedstoneLeverSupport
├── conduit attachment
├── shape policy
└── isolated signal policy

PipedRedstoneRepeaterLogic
├── delay cycle
├── scheduled propagation
└── directional signal policy

PipedRedstoneWaterlogging
└── shared fluid-state lifecycle
```

## Test-suite decomposition

The previous `PipedRedstoneGameTests` class mixed topology, propagation, devices, waterlogging, and placement-assist tests. It is now split into:

- `PipedRedstoneTopologyGameTests`;
- `PipedRedstoneSignalGameTests`;
- `PipedRedstoneDeviceGameTests`;
- `PipedRedstoneGameTestFixtures`.

## Large files intentionally retained

### `ZmhBurnerPonderScenes`

This is a single ordered storyboard with no mutable production state and only two methods: registration and scene construction. Splitting it by arbitrary line ranges would reduce readability and make keyframe sequencing harder to review.

### Presentation classes

`AirshipBurnerPresentation`, `AirshipHelmPresentation`, and `AltitudeGaugePresentation` are relatively long because localized UI output is verbose. They are intentionally isolated from gameplay and persistence. Their size does not create cross-domain coupling.

## Review guardrails

A future change should trigger a new monolith review when:

- a production class exceeds 300 lines;
- a block entity gains a second independent mutable state aggregate;
- a presentation class begins calculating gameplay values;
- a codec performs world access;
- a helper begins owning lifecycle state;
- a single GameTest class covers more than one production subsystem.
