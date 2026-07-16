# Airship Upgrade System

Zeppelin Must Have 0.6.0 introduces persistent, data-driven equipment upgrades. The first target is the Airship Burner family; the target/slot contract is designed for later Helm, ballast, propulsion, and mooring modules.

## Interaction

- Right-click an Airship Burner with an upgrade module to install it.
- Each burner has one `thermal`, one `airflow`, and one `control` socket.
- Sneak-use the Create Wrench to remove the most recently occupied socket.
- If no upgrade is installed, sneak-wrenching falls back to normal Create block removal.
- Breaking the burner drops all installed modules.

Upgrade ItemStacks are stored in the burner block entity and synchronized to clients for Engineer's Goggles.

## Definitions

Upgrade definitions are loaded from:

```text
data/<namespace>/airship_upgrades/<upgrade>.json
```

The definition ID must match a registered item ID.

```json
{
  "schema_version": 1,
  "targets": ["airship_burner"],
  "slot": "thermal",
  "exclusive_group": "burner_thermal_path",
  "conflicts": [],
  "modifiers": {
    "gas_output_multiplier": 0.92,
    "fuel_use_multiplier": 0.70,
    "fuel_capacity_multiplier": 1.25,
    "cast_range_add": 0,
    "superheated_output_multiplier": 1.0,
    "throttle_exponent_multiplier": 1.0
  }
}
```

### Compatibility fields

| Field | Meaning |
|---|---|
| `targets` | Equipment families that accept the module. |
| `slot` | The one-item socket occupied by the module. |
| `exclusive_group` | Optional cross-slot exclusivity group. |
| `conflicts` | Explicit incompatible upgrade IDs. |

### Burner modifiers

| Field | Operation |
|---|---|
| `gas_output_multiplier` | Multiplies the base profile's gas-output multiplier. |
| `fuel_use_multiplier` | Multiplies full-power fuel consumption. |
| `fuel_capacity_multiplier` | Multiplies the shared heat-reservoir capacity. |
| `cast_range_add` | Adds blocks to the Aeronautics airtight-envelope raycast range. |
| `superheated_output_multiplier` | Multiplies the base superheated bonus. |
| `throttle_exponent_multiplier` | Multiplies the redstone throttle exponent. |

Installed definitions are combined in stable slot order and applied to the current data-pack burner profile. `/reload` recalculates already placed burners without replacing their stored upgrade ItemStacks.

## Bundled modules

### Heat Recuperator

Slot: `thermal`

- output `0.92x`;
- fuel use `0.70x`;
- reservoir capacity `1.25x`.

This is an endurance module. It does not create free lift: lower consumption is balanced by reduced peak gas output.

### Forced Induction

Slot: `airflow`

- output `1.30x`;
- fuel use `1.22x`;
- range `+8` blocks;
- superheated bonus `1.05x`.

This is a high-demand lift module.

### Precision Regulator

Slot: `control`

- fuel use `0.95x`;
- range `+4` blocks;
- throttle exponent `1.35x`.

This makes low redstone levels less aggressive and gives finer low-power control.

## Authority and compatibility

- Upgrade definitions are server data.
- The client receives installed ItemStacks, the effective profile, and the aggregated modifier snapshot.
- Create fuel data maps and NeoForge furnace fuels remain the source of fuel compatibility.
- Aeronautics remains responsible for balloon ownership, gas filling, pressure, and lift.
- No Mixins, registry replacement, or parallel simulation is introduced.
