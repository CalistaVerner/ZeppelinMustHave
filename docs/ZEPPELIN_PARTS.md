# Zeppelin Parts Coverage

Version `0.11.0` introduces a canonical Zeppelin Parts catalog covering every public block and item in Zeppelin Must Have.

The catalog is the source of truth for:

- creative-tab ordering;
- item tooltips;
- Ponder category membership;
- public item and block tags;
- registry coverage validation;
- GameTest coverage gates.

Adding a registry entry without adding a matching Zeppelin Part now fails during common setup and in GameTest.

## Complete catalog

| Category | Zeppelin Parts |
|---|---|
| Flight Control | Airship Helm, Altitude Gauge |
| Lift and Buoyancy | Airship Burner, Forced-Draft Airship Burner, Industrial Airship Burner |
| Steam Power | Copper Boiler Base, Brass Boiler Base, Industrial Boiler Base, Copper Steam Engine, Brass Compound Steam Engine, Industrial Triple-Expansion Engine |
| Protected Redstone | Copper Piped Redstone, Brass Piped Redstone, Resonant Piped Redstone, Native Lever, Waterproof Repeater |
| Ballast and Mass Trim | Ballast Tank |
| Mooring | Mooring Winch |
| Propulsion | Vertical Thruster |
| Burner Upgrades | Heat Recuperator, Forced Induction, Precision Regulator |

Total coverage:

```text
19 block parts
 3 standalone upgrade parts
22 item entries
```

## Java catalog

```text
ZeppelinPartCatalog
├── 22 ordered ZeppelinPartDefinition entries
├── registry-ID index
├── category index
├── creative ordering
└── fail-fast registry validation
```

Each definition contains:

- stable registry ID;
- item supplier;
- optional block supplier;
- subsystem category;
- derived localization key.

The catalog validates that every block and item registered under `zeppelin_must_have` has exactly one definition. It also checks that every definition points to the expected registry object.

## Public tags

### Root tags

```text
#zeppelin_must_have:zeppelin_parts        (items)
#zeppelin_must_have:zeppelin_parts        (blocks)
```

The item tag contains all 22 entries. The block tag contains all 19 block parts.

### Category tags

```text
#zeppelin_must_have:zeppelin_parts/flight_control
#zeppelin_must_have:zeppelin_parts/lift
#zeppelin_must_have:zeppelin_parts/steam_power
#zeppelin_must_have:zeppelin_parts/redstone_control
#zeppelin_must_have:zeppelin_parts/ballast
#zeppelin_must_have:zeppelin_parts/mooring
#zeppelin_must_have:zeppelin_parts/propulsion
#zeppelin_must_have:zeppelin_parts/upgrade        (items only)
```

The compatibility tag:

```text
#zeppelin_must_have:airship_upgrades
```

now delegates to `#zeppelin_must_have:zeppelin_parts/upgrade`, eliminating a duplicate upgrade list.

## Tool and mining coverage

Every block part is included in:

```text
#minecraft:mineable/pickaxe
#minecraft:needs_stone_tool
```

The audit fixed missing coverage for:

- Copper Steam Engine;
- Brass Compound Steam Engine;
- Industrial Triple-Expansion Steam Engine.

These blocks already required a correct tool in their block properties, but previously lacked the matching mining tags.

## Item tooltips

Every Zeppelin Part displays:

1. its Zeppelin Part category;
2. a concise description of its gameplay role;
3. its registry ID when advanced tooltips are enabled.

Tooltip descriptions and category names are fully localized in:

- English;
- Russian;
- Italian;
- Polish.

The creative tab is titled **Zeppelin Parts** and derives its contents directly from the catalog.

## Ponder coverage

The Ponder category retains the stable ID:

```text
zeppelin_must_have:zeppelin_systems
```

Its display title is now **Zeppelin Parts**, and category membership is generated from the canonical catalog.

All three upgrade items directly open the burner operation storyboard, in addition to appearing inside that scene:

- Heat Recuperator Upgrade;
- Forced Induction Upgrade;
- Precision Regulator Upgrade.

Every block part is assigned to its subsystem storyboard.

## Coverage GameTests

`ZeppelinPartCoverageGameTests` verifies:

1. exact registry coverage: `22` items and `19` blocks;
2. root item and block tags;
3. category item and block tags;
4. pickaxe and stone-tool coverage for every block;
5. creative order contains every part exactly once;
6. all upgrade parts retain the legacy compatibility tag.

These tests are intentionally independent of individual mechanics tests. A part can therefore be functionally correct while still failing the release gate if it is missing documentation, category, or mining coverage.

## Adding a new Zeppelin Part

A new part must include all applicable entries below:

1. registry definition and public facade handle;
2. `ZeppelinPartCatalog` definition;
3. blockstate and block model;
4. item model;
5. recipe;
6. loot table for blocks;
7. English, Russian, Italian, and Polish names and part description;
8. root and category tags;
9. mining tags for metal blocks;
10. Ponder storyboard assignment;
11. mechanic-specific GameTest coverage.

The common-setup validator and Zeppelin Parts GameTests prevent incomplete catalog coverage from shipping silently.
