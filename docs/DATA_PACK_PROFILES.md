# Airship Burner Data-Pack Profiles

Zeppelin Must Have keeps Airship Burner tuning outside Java code. The three burner blocks contain only stable profile identifiers; numerical behaviour is loaded through the normal Minecraft server resource-reload pipeline.

## Resource location

Profiles are loaded from:

```text
data/<namespace>/airship_burner_profiles/<profile>.json
```

The built-in tiers reference these IDs:

| Burner tier | Profile ID |
|---|---|
| Standard | `zeppelin_must_have:standard` |
| Forced draft | `zeppelin_must_have:forced_draft` |
| Industrial | `zeppelin_must_have:industrial` |

A data pack overrides a built-in profile by supplying the same resource ID at a higher pack priority. `/reload` updates already placed burner block entities on their next server tick, clamps stored fuel to the new capacity, refreshes comparator output, resynchronizes clients, and reconnects or disconnects the Aeronautics balloon provider as required.

## Schema version 1

```json
{
  "schema_version": 1,
  "gas_output_multiplier": 1.0,
  "fuel_use_per_tick_at_full_power": 1.0,
  "fuel_capacity_ticks": 12000,
  "cast_range": 16,
  "superheated_output_multiplier": 1.5,
  "throttle_exponent": 1.0
}
```

| Field | Meaning |
|---|---|
| `schema_version` | Profile format. Current value: `1`. |
| `gas_output_multiplier` | Multiplier applied to the output selected by the native Aeronautics burner value behaviour. `0` disables the profile. |
| `fuel_use_per_tick_at_full_power` | Stored fuel ticks consumed during one game tick at full redstone power. |
| `fuel_capacity_ticks` | Maximum fuel inventory expressed in furnace burn ticks. |
| `cast_range` | Maximum vertical raycast distance used by the Aeronautics airtight-envelope discovery contract. |
| `superheated_output_multiplier` | Additional output multiplier when the accepted Create fuel is classified as superheated. |
| `throttle_exponent` | Shape of the redstone throttle curve. `1.0` is linear; values above `1.0` make low signals gentler. |

## Runtime equations

```text
throttle = (redstone_signal / 15) ^ throttle_exponent

gas_output =
    aeronautics_selected_output
    × gas_output_multiplier
    × superheated_output_multiplier_if_applicable
    × throttle

fuel_use_per_tick =
    fuel_use_per_tick_at_full_power
    × throttle
```

The add-on does not replace Aeronautics balloon construction, airtight-block checks, gas types, air-pressure calculations, fill interpolation, or lift simulation. It supplies an additional `BlockEntityLiftingGasProvider` implementation using the existing public contract.

## Validation and failure behaviour

Profiles are validated during resource reload:

- `fuel_capacity_ticks`: `1..1,728,000`
- `cast_range`: `1..256`
- multipliers and exponents must be finite and non-negative; fields that define curve shape must be greater than zero
- unknown schema versions are rejected

An absent or invalid required profile fails closed: the affected burner produces no lift and accepts no fuel. Other Create and Aeronautics mechanics continue operating normally.

## Client synchronization

The server remains authoritative. The resolved profile snapshot is included in the burner block-entity update tag so that:

- Create Engineer's Goggles display the actual server configuration;
- clients do not need the server's data pack installed locally;
- Ponder and rendering do not calculate gameplay values independently.

## Template

A reusable profile file is available at:

```text
docs/templates/airship_burner_profile.template.json
```
