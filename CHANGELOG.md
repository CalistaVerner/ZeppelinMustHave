# Changelog

## 0.15.0 — Industrial Powertrain

### Added

- Added the Omni Speed Controller: a six-port Create kinetic transmission with automatic input-face detection and one configurable absolute output speed.
- Three-block-wide Steam Engine MK VII with three synchronized banks, nine internal crankshafts, a central reduction gearbox, and one powered output shaft.
- Transactional placement validation for the full MK VII footprint: three Industrial Boiler MK III backing cells, two side-body cells, a three-cell service row, and one central shaft position.
- MK VII profile, mechanical-crafting recipe, loot table, models, localization, Zeppelin Parts catalog coverage, Ponder registration, and GameTests.

### Changed

- Split the Flight Control Network router into vessel resolution, addressed-network state, vessel state, telemetry aggregation, and timeout policy components.
- Added bounded cleanup for expired FCN addresses, system telemetry, and inactive vessel caches.
- Split steam-engine rendering into frame coordination, cylinder mechanics, flagship mechanisms, and shared model transforms.
- Split steam-power Ponder registration into independent boiler and engine scene modules.
- Split graded steam-engine runtime logic into block-entity state, shaft control, efficiency policy, and assembly validation.
- Isolated GameTest runtime files from source control and added permanent crash/runtime ignore rules.
- Raised the mod version to `0.15.0`.

## 0.14.1 — Curios Aviator Goggles

### Fixed

- Added seamless topology models for tiered fluid pipes and removed internal Create collars between same-grade segments.

### Added

- Optional Curios API integration for `aeronautics:aviators_goggles`.
- Aviator's Goggles can be equipped in the Curios `head` slot.
- Curios-equipped goggles enable Create/Aeronautics goggle overlays and render with the native Aeronautics armor texture.
- Curios remains optional; Zeppelin Must Have loads normally when it is absent.

## 0.14.0 — Engine Grade

### Changed

- Reworked Sovereign Steam Engine MK V into a cleaner cast-crankcase design with machined-steel internals, brass bearing hardware, copper pressure components, and narrower five-cylinder mechanism geometry.
- Reworked Leviathan Steam Engine MK VI into a cohesive naval T-frame power unit with four-cylinder side banks, a stepped output-bearing housing, and two-bank eight-cylinder mechanism.
- Replaced the navy/cyan energy-device palette of MK V and MK VI with engine-grade gunmetal, blackened steel, nickel steel, brass, bronze, copper, and restrained heat accents.
- Raised the mod version to `0.14.0`.

## 0.12.0 — Flight Control Network

### Added

- Vessel-local, server-authoritative Flight Control Network scoped to one Sable sub-level.
- Flight Computer with Aeronautics/Sable telemetry, center-of-mass reporting, system-state aggregation, source arbitration, heartbeat failover, and seven independent command lanes.
- Seven-position Engine Telegraph with FCN command publication, directional bipolar analog output, and direct graded Steam Engine throttle/reversal integration.
- Persisted vessel-wide Emergency Cutoff keyed by Sable vessel UUID, with manual reset and redstone alarm output.
- Named/frequency-addressed Control Transmitter and Control Receiver blocks with mandatory physical redstone power.
- Direct FCN actuator support for Airship Burners, Vertical Thrusters, and graded Steam Engines.
- Grand Steam Engine MK IV: 8192 SU, four-cylinder 90° phasing, custom flagship housing, contra-rotating flywheels, working valve gear, and a live centrifugal governor.
- Sovereign Steam Engine MK V: 12288 SU, five-cylinder 72° phasing, six boiler-load units, rotating pressure core, crown rotor, three valve-gear banks, and a dedicated capital-class model set.
- Leviathan T-Frame Steam Engine MK VI: 20480 SU, eight cylinders in two lateral banks, ten boiler-load units, four occupied cells, free-space validation, Industrial Boiler MK III requirement, a forward shaft nose, and a dedicated synchronized mechanism set.
- Bipolar analog protocol for signed commands and automatic/manual/safety authority levels.
- Protocol GameTests, recipes, loot tables, models, localizations, mining tags, and Zeppelin Parts catalog entries.
- Complete Ponder coverage for all 34 Zeppelin Parts, including FCN, envelope, fluid-pipe, boiler, engine, service, redstone, and upgrade scenes.
- Fail-fast validation of all 13 packaged Ponder NBT templates plus automated overview and specialized coverage GameTests.
- Full operator and integration specification in [`docs/FLIGHT_CONTROL_NETWORK.md`](docs/FLIGHT_CONTROL_NETWORK.md).
- Dedicated Engineering advancement tree with nine fabrication categories, thirteen commissioning milestones, and the complete-catalog Master Shipwright challenge.
- Server-authoritative advancement triggers for Zeppelin Part crafting, burner ignition and upgrades, flight telemetry, altitude hold, FCN commissioning, ballast loading, protected signals, steam power, vertical thrust, and physical mooring.
- Advancement definitions, four-language localization, and release-gate GameTests documented in [`docs/ADVANCEMENTS.md`](docs/ADVANCEMENTS.md).

### Safety

- Emergency shutdown now forces burner output, vertical thrust, engine efficiency, and automatic altitude-control output to zero.
- Stale sources expire after 40 ticks; computer heartbeat and output frames expire after 20 ticks.
- Emergency reset clears prior output frames so stale commands cannot resume automatically.

### Internal

- Flight Computer routes commands only; Aeronautics, Sable, and Create remain authoritative for physical simulation.
- Added deterministic primary-computer election and deterministic same-tick source tie-breaking.

Full cumulative notes for upgrading from `0.9.0`: [`docs/PATCH_NOTES_0.9.0_TO_0.11.0.md`](docs/PATCH_NOTES_0.9.0_TO_0.11.0.md).

## 0.11.0 — Zeppelin Parts Coverage

### Added

- Canonical `ZeppelinPartCatalog` covering all 22 public item entries and 19 block parts.
- Eight public Zeppelin Part categories: flight control, lift, steam power, protected redstone, ballast, mooring, propulsion, and upgrades.
- Root and category item/block tags under `#zeppelin_must_have:zeppelin_parts`.
- Localized subsystem and role tooltips for every part in English, Russian, Italian, and Polish.
- Direct Ponder access from all three burner upgrade items.
- Fail-fast common-setup validation for uncatalogued or stale registry entries.
- Five Zeppelin Parts coverage GameTests.
- Complete public manifest and extension checklist in `docs/ZEPPELIN_PARTS.md`.

### Fixed

- Added Copper, Brass, and Industrial Steam Engines to `#minecraft:mineable/pickaxe`.
- Added all three graded Steam Engines to `#minecraft:needs_stone_tool`.
- Replaced duplicate creative-tab and Ponder membership lists with the canonical catalog.
- Made `#zeppelin_must_have:airship_upgrades` delegate to the Zeppelin Parts upgrade category.

### Internal

- Removed the obsolete `ZmhCreativeContents` list.
- Preserved all registry IDs, recipes, loot tables, NBT keys, and save compatibility.

## 0.10.0 — Airship Service Systems

- Completed Ballast Tank dynamic mass integration.
- Completed physical Mooring Winch integration with Create Simulated rope physics.
- Completed Vertical Thruster integration with Aeronautics and Sable propulsion.
- Added service-system Ponder scenes, profiles, diagnostics, and GameTests.
