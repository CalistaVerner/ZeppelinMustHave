# Changelog

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
