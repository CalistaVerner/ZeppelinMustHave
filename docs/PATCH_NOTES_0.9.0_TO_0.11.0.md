# Zeppelin Must Have 0.11.0

## Cumulative Patch Notes: 0.9.0 → 0.11.0

Versions `0.10.0` and `0.11.0` complete the transition from the original control-system release into a comprehensive engineering package for Create Aeronautics airships.

Highlights:

- completed the Ballast Tank, Mooring Winch, and Vertical Thruster;
- added real Sable mass and center-of-mass changes from stored ballast water;
- added physical mooring through Create Simulated rope constraints;
- added native vertical propulsion through the Aeronautics/Sable propeller pipeline;
- introduced the canonical **Zeppelin Parts** catalog;
- completed creative-tab, tooltip, Ponder, tag, mining, recipe, loot, localization, and GameTest coverage;
- decomposed the remaining runtime monoliths without changing registry IDs or save formats.

---

# 0.10.0 — Airship Service Systems

## Ballast Tank

The Ballast Tank is now a fully functional water-ballast system rather than a registered asset-only block.

### Fluid handling

- bundled capacity: `8000 mB`;
- accepts water-tagged fluids only;
- supports buckets and portable NeoForge fluid containers;
- exposes the standard NeoForge block fluid capability;
- connects to Create pipes and pumps;
- persists fluid contents through NBT;
- synchronizes effective state to clients;
- emits a comparator signal based on fill level;
- renders stored fluid in its front service gauge;
- reports capacity, fill percentage, added mass, and profile ID through Engineer’s Goggles.

### Native Sable mass integration

The tank does not imitate weight with an artificial downward force. Stored fluid contributes directly to the containing Sable sub-level mass tracker:

```text
Ballast Tank fluid
        ↓
BallastTankProfile
        ↓
SableBallastMassBridge
        ↓
ServerSubLevel.getSelfMassTracker()
```

This changes the vessel’s actual physical properties:

- total rigid-body mass;
- center of mass;
- inertia tensor;
- response to lift and propulsion;
- pitch and roll balance;
- acceleration and maneuvering behavior.

Asymmetrically placed ballast tanks can therefore be used for genuine vessel trim and mass distribution.

Dynamic mass is released during removal and chunk unload, preventing duplicate mass after a block entity is loaded again.

### Data-pack profile

```text
data/<namespace>/ballast_tank_profiles/*.json
```

Bundled profile:

```json
{
  "schema_version": 1,
  "capacity_mb": 8000,
  "mass_per_bucket_kg": 1000.0
}
```

---

## Mooring Winch

The Mooring Winch is now a functional specialization of the Create Simulated Rope Winch.

### Implemented behavior

- Create kinetic-network input;
- rope payout and retrieval controlled by shaft direction;
- native Create Simulated rope strands;
- native endpoint attachments;
- current and target line length;
- tension and break-force behavior;
- Sable physics constraints;
- client rope rendering;
- Engineer’s Goggles information for attachment state and commanded line speed.

A moored vessel remains physically simulated. It can move within the available rope length and continues reacting to propulsion, buoyancy, ballast, inertia, and line tension.

### Custom Zeppelin Must Have rendering

The winch retains Create Simulated’s rope physics and animation logic while using dedicated Zeppelin Must Have dynamic models:

```text
models/block/mooring_winch/shaft.json
models/block/mooring_winch/rope_coil.json
```

---

## Vertical Thruster

The Vertical Thruster is now a native Create-powered Aeronautics/Sable propulsion device.

### Propulsion pipeline

```text
Create kinetic speed
        ↓
VerticalThrusterBlockEntity
        ↓
Aeronautics BasePropellerBlockEntity
        ↓
Sable propulsion force group
        ↓
Force applied at the physical block position
```

### Features

- vertical Create shaft connection;
- Create stress consumption;
- upward or downward installation;
- thrust reversal through the Create Wrench;
- native force application at the mounting point;
- naturally generated torque when mounted away from the center of mass;
- separate animated propeller partial model;
- Engineer’s Goggles information for direction, thrust, airflow, and active profile.

The block does not manually move the vessel and does not replace the Sable rigid-body solver. Propulsion is applied by the upstream physics pipeline on physics ticks.

### Data-pack profile

```text
data/<namespace>/vertical_thruster_profiles/*.json
```

Bundled profile:

```json
{
  "schema_version": 1,
  "thrust_scaling": 1.75,
  "airflow_scaling": 0.12,
  "radius": 1.0,
  "stress_impact": 8.0
}
```

---

## Service-system Ponder scenes

Added dedicated scenes:

```text
service/ballast_tank
service/mooring_winch
service/vertical_thruster
```

They explain:

- converting stored fluid into Sable dynamic mass;
- kinetic rope deployment and physical mooring;
- converting Create rotation into Aeronautics propulsion.

---

# 0.11.0 — Zeppelin Parts Coverage

## Canonical Zeppelin Parts catalog

Version `0.11.0` introduces `ZeppelinPartCatalog` as the single source of truth for every public block and item.

Complete catalog coverage:

```text
19 functional block parts
 3 burner upgrade items
22 item entries
```

The catalog now drives:

- creative-tab order;
- subsystem classification;
- item tooltips;
- Ponder membership;
- public item and block tags;
- registry validation;
- automated coverage GameTests.

Registering a new Zeppelin Must Have block or item without adding it to the catalog now fails common setup with a precise coverage error.

---

## Zeppelin Parts creative tab

The creative tab is now titled:

```text
Zeppelin Parts
```

The duplicate `ZmhCreativeContents` list has been removed. Creative ordering is generated directly from `ZeppelinPartCatalog`, preventing registered items from being omitted from the tab.

---

## Zeppelin Parts categories

All components are classified by engineering subsystem:

| Category | Components |
|---|---|
| Flight Control | Airship Helm, Altitude Gauge |
| Lift and Buoyancy | Airship Burner, Forced-Draft Burner, Industrial Burner |
| Steam Power | three Boiler grades and three Steam Engine grades |
| Protected Redstone | three conduit grades, Native Lever, Waterproof Repeater |
| Ballast and Mass Trim | Ballast Tank |
| Mooring | Mooring Winch |
| Propulsion | Vertical Thruster |
| Burner Upgrades | Heat Recuperator, Forced Induction, Precision Regulator |

---

## Tooltips for all parts

Every Zeppelin Part now displays:

1. Zeppelin Part identity;
2. engineering subsystem category;
3. a concise gameplay-role description;
4. registry ID when advanced tooltips are enabled.

Example:

```text
Zeppelin Part · Steam Power
Three-cylinder Grade III triple-expansion steam engine.
zeppelin_must_have:industrial_steam_engine
```

Full tooltip localization is included for:

- English;
- Russian;
- Italian;
- Polish.

All language files contain the same `241` keys.

---

## Public item and block tags

Added root tags:

```text
#zeppelin_must_have:zeppelin_parts
```

The root tag exists independently in the item and block registries.

### Category tags

```text
#zeppelin_must_have:zeppelin_parts/flight_control
#zeppelin_must_have:zeppelin_parts/lift
#zeppelin_must_have:zeppelin_parts/steam_power
#zeppelin_must_have:zeppelin_parts/redstone_control
#zeppelin_must_have:zeppelin_parts/ballast
#zeppelin_must_have:zeppelin_parts/mooring
#zeppelin_must_have:zeppelin_parts/propulsion
#zeppelin_must_have:zeppelin_parts/upgrade
```

The compatibility tag:

```text
#zeppelin_must_have:airship_upgrades
```

now delegates to the Zeppelin Parts upgrade category instead of maintaining a duplicate item list.

---

## Mining-tag fixes

The three graded Steam Engines previously required the correct tool in their block properties but were missing from Minecraft mining tags.

Fixed for:

- Copper Steam Engine;
- Brass Compound Steam Engine;
- Industrial Triple-Expansion Steam Engine.

All `19` block parts are now included in:

```text
#minecraft:mineable/pickaxe
#minecraft:needs_stone_tool
```

---

## Complete Ponder coverage

The Ponder category display title is now:

```text
Zeppelin Parts
```

Its stable internal ID remains unchanged:

```text
zeppelin_must_have:zeppelin_systems
```

All 22 item entries are added to the category through the canonical catalog.

Direct burner-storyboard access was added for:

- Heat Recuperator Upgrade;
- Forced Induction Upgrade;
- Precision Regulator Upgrade.

Every block part is associated with its appropriate subsystem storyboard.

---

# Architecture improvements

## Runtime monolith decomposition

Large runtime classes were separated by responsibility without changing gameplay behavior.

| Class | Before | After |
|---|---:|---:|
| `AirshipBurnerBlockEntity` | 398 lines | 290 lines |
| `SteamEngineGradeBlockEntity` | 315 | 210 |
| `AltitudeGaugeBlockEntity` | 313 | 256 |
| `AirshipHelmBlockEntity` | 300 | 109 |
| `AirshipHeatReservoir` | 296 | 205 |
| `PipedRedstoneBlock` | 290 | 205 |
| `PipedRedstoneNativeLeverBlock` | 283 | 186 |
| `BallastTankBlockEntity` | 270 | 167 |
| `PipedRedstoneRepeaterBlock` | 247 | 165 |

No production class remains above `300` lines.

### Extracted responsibility boundaries

Dedicated components now own:

- mutable runtime state;
- data-pack profile lifecycle;
- NBT encoding and legacy migration;
- Engineer’s Goggles and status presentation;
- client-only animation and particle effects;
- Sable mass bindings;
- Piped Redstone placement, topology, waterlogging, and repeater policy;
- world sound, light-state, and update effects.

The public `ZmhBlocks` facade, registry IDs, and persistent save keys remain stable.

---

## GameTest suite decomposition

The previous combined Piped Redstone test class was split into:

```text
PipedRedstoneTopologyGameTests
PipedRedstoneSignalGameTests
PipedRedstoneDeviceGameTests
PipedRedstoneGameTestFixtures
```

Added a separate release gate:

```text
ZeppelinPartCoverageGameTests
```

It validates:

- exact registry-to-catalog equality;
- `22` item parts and `19` block parts;
- root and category tags;
- mining tags;
- duplicate-free creative ordering;
- upgrade compatibility tags.

---

# Fixes and quality improvements

- completed the previously unfinished Ballast Tank, Mooring Winch, and Vertical Thruster;
- fixed missing mining tags for all graded Steam Engines;
- removed duplicated Creative Tab, Ponder, and upgrade membership lists;
- added fail-fast validation for incomplete public-part registration;
- restored exact localization-key parity across all supported languages;
- preserved Piped Redstone interaction behavior after internal decomposition;
- retained migration support for legacy burner fuel NBT;
- prevented duplicate Ballast Tank dynamic-mass application;
- gave the Mooring Winch dedicated shaft and rope-coil partial models;
- added `/reload` support for Ballast Tank and Vertical Thruster profiles;
- made tooltip generation safe during client search-tree creation when the player is `null`.

---

# Save compatibility and migration

## Existing worlds

Updating from `0.9.0` does not require a new world.

Preserved compatibility includes:

- all registry IDs;
- block and item IDs;
- existing NBT keys;
- recipes;
- loot tables;
- the public `ZmhBlocks` facade;
- all pre-existing data-pack directories;
- the stable Ponder category ID.

Existing Airship Helms, Burners, Boilers, Steam Engines, Piped Redstone networks, and Altitude Gauges continue loading under their original IDs.

## Upgrade procedure

1. Stop the client and server.
2. Back up the world.
3. Remove `zeppelin_must_have-0.9.0.jar`.
4. Install `zeppelin_must_have-0.11.0.jar`.
5. Use the same mod version on the server and all clients.
6. Run `/reload` after loading the world when server data packs are present.

## New data-pack directories

```text
data/<namespace>/ballast_tank_profiles/*.json
data/<namespace>/vertical_thruster_profiles/*.json
```

Existing custom Burner, Boiler, Steam Engine, Piped Redstone, Upgrade, and Altitude Control profiles remain compatible.

---

# Requirements

The required platform stack is unchanged:

| Component | Supported version |
|---|---:|
| Minecraft | `1.21.1` |
| Java | `21` |
| NeoForge | `21.1.235` or a compatible 21.1 build |
| Create | `6.0.10` to `< 6.1.0` |
| Sable | `2.0.0` to `< 3.0.0` |
| Create Simulated | `1.3.0` to `< 2.0.0` |
| Create Aeronautics | `1.3.0` to `< 2.0.0` |

Zeppelin Must Have remains required on both the server and clients.

---

# Release validation

```text
Registry coverage:       22/22 items, 19/19 blocks
GameTest:                29/29
Localization parity:     241/241 × 4 languages
Production classes >300: 0
JSON validation:         SUCCESS
Clean build:             SUCCESS
Client runtime:          85 seconds without a crash
Missing models/textures: none
Renderer errors:         none
git diff --check:        SUCCESS
```

Development clients may log optional compatibility-probe warnings for absent Iris and JetBrains annotations classes. These warnings originate from dependencies, do not interrupt loading, and are unrelated to Zeppelin Must Have.

---

# Short release summary

> Zeppelin Must Have 0.11.0 completes the airship service layer introduced after 0.9.0. Ballast Tanks now change real Sable mass and center of mass, Mooring Winches use physical Create Simulated ropes, and Vertical Thrusters apply native Aeronautics propulsion. The new Zeppelin Parts catalog provides complete creative-tab, tooltip, Ponder, tag, mining, recipe, loot, localization, and automated test coverage for all 22 public item entries and 19 functional blocks. Existing registry IDs and saves remain compatible.
