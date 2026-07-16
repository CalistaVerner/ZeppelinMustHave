# Piped Redstone

Piped Redstone is an isolated, water-resistant redstone transport system for airships and dense Create machinery.

It is intentionally not a replacement for redstone dust. Dust remains useful for exposed local logic. Piped Redstone is intended for protected trunks, vertical routing, machinery spaces, ballast compartments, and parallel control channels where accidental coupling is unacceptable.

## Explicit ports

Every conduit block has six independent ports:

```text
north / east / south / west / up / down
```

Ports are stored directly in blockstate. Adjacency does not create a connection.

- Use the Create Wrench on a face to open or close that exact port.
- If the adjacent block is another Piped Redstone conduit, its reciprocal port is changed at the same time.
- Two conduits may occupy adjacent blocks and remain electrically independent.
- A line can cross another line in neighboring blocks without merging unless matching ports are deliberately opened.

Placement creates a straight pair of ports along the clicked face axis. Further branches are added with the wrench.

## Shaft-style placement assist

When the player holds any Piped Redstone conduit and uses it on an existing conduit, the Create Placement Assist extends the far end of the straight connected run.

- The helper follows only reciprocal enabled ports along the selected axis.
- The new conduit inherits the same pair of axis ports.
- The held conduit may be a different tier, allowing an in-place transition from Copper to Brass or Resonant.
- The normal Create placement-assist range and Extendo Grip bonus are respected.
- Shift-use bypasses the helper and keeps normal block interaction available.

This behaves like Create Shaft extension while preserving Piped Redstone's explicit topology: the helper never creates a side branch or joins a neighboring parallel line.

## Signal model

The system carries the full vanilla analog value from `0` through `15`.

Signal strength does not decrease with every conduit. Instead, every connected component has a maximum repeater-free path distance.

```text
source power = 0..15
power at every reachable conduit = source power
power beyond max_signal_distance = 0
```

When several sources feed the same network, each conduit receives the strongest reachable analog value. Equal-strength paths prefer the shorter route.

Network reconstruction is event-driven. It runs after:

- an external redstone neighbor changes;
- a conduit is placed or removed;
- a port is opened or closed;
- a scheduled propagation update completes.

A safety limit disables components larger than 8192 conduit blocks instead of allowing an unbounded graph traversal.

## Tiers

Default values are loaded from data packs:

| Tier | Profile | Network delay | Repeater-free distance |
|---|---|---:|---:|
| Copper | `zeppelin_must_have:copper` | 4 game ticks | 32 edges |
| Brass | `zeppelin_must_have:brass` | 2 game ticks | 64 edges |
| Resonant | `zeppelin_must_have:resonant` | 1 game tick | 128 edges |

Profiles are located at:

```text
data/<namespace>/piped_redstone_profiles/<profile>.json
```

Schema version 1:

```json
{
  "schema_version": 1,
  "propagation_delay_ticks": 4,
  "max_signal_distance": 32
}
```

### Mixed-tier networks

A connected component operates at the weakest values present in that component:

```text
network delay = maximum delay among all conduits
network range = minimum range among all conduits
```

A short Copper segment therefore limits an otherwise Resonant trunk. This makes upgrading the entire main line meaningful and prevents cheap low-tier segments from receiving high-tier performance for free.

## Water and liquids

Conduits and repeaters are solid blocks rather than dust.

- They can be waterlogged.
- Water does not remove the block or interrupt the signal.
- Flowing liquids adjacent to the conduit do not wash it away.
- The conduit schedules normal water-fluid ticks while waterlogged.

## Piped Redstone Native Lever

The Native Lever is a conduit-mounted control source rather than a general-purpose vanilla lever.

- It can be placed only on a Piped Redstone conduit.
- Placement automatically opens the supporting conduit port.
- Its `0/15` signal is emitted only toward that supporting port.
- Adjacent blocks on the other five sides are not powered.
- The block is waterloggable and remains operational in flooded machinery spaces.
- Create Engineer's Goggles show its state and current conduit output.

The brass-and-copper base remains in the normal block model. The handle and indicator are block-entity partial models: `LerpedFloat` moves the handle smoothly through a 76-degree arc while the indicator interpolates from dark to active red.

Recipe components:

- Copper Piped Redstone;
- vanilla Lever;
- Brass Sheet;
- two Electron Tubes.

## Piped Redstone Repeater

The repeater is directional and waterproof.

It performs two operations:

1. preserves the complete analog value;
2. starts a new conduit-distance segment.

Like a vanilla repeater, ordinary right-click cycles its delay:

| Setting | Redstone ticks | Game ticks |
|---:|---:|---:|
| 1 | 1 | 2 |
| 2 | 2 | 4 |
| 3 | 3 | 6 |
| 4 | 4 | 8 |

The selected delay is stored in `BlockStateProperties.DELAY` and shown by the movable indicator on the repeater model.

## Crafting progression

### Copper Piped Redstone

A basic shaped recipe using:

- Copper Sheets;
- Create Fluid Pipes;
- redstone dust.

Produces eight conduits.

### Brass Piped Redstone

Requires Create Mechanical Crafting and upgrades Copper conduits with:

- Brass Sheets;
- Electron Tubes;
- Precision Mechanisms.

Produces eight conduits.

### Resonant Piped Redstone

A five-by-five Mechanical Crafting recipe requiring:

- Brass Piped Redstone;
- Sturdy Sheets;
- Polished Rose Quartz;
- Electron Tubes;
- Precision Mechanisms;
- Brass Sheets;
- redstone dust.

Produces eight conduits.

### Piped Redstone Native Lever

The Native Lever is a conduit-mounted control source rather than a general-purpose vanilla lever.

- It can be placed only on a Piped Redstone conduit.
- Placement automatically opens the supporting conduit port.
- Its `0/15` signal is emitted only toward that supporting port.
- Adjacent blocks on the other five sides are not powered.
- The block is waterloggable and remains operational in flooded machinery spaces.
- Create Engineer's Goggles show its state and current conduit output.

The brass-and-copper base remains in the normal block model. The handle and indicator are block-entity partial models: `LerpedFloat` moves the handle smoothly through a 76-degree arc while the indicator interpolates from dark to active red.

Recipe components:

- Copper Piped Redstone;
- vanilla Lever;
- Brass Sheet;
- two Electron Tubes.

## Piped Redstone Repeater

Uses Copper Piped Redstone, Brass Sheets, Electron Tubes, and a Comparator.

## Automated verification

`PipedRedstoneGameTests` verifies on a real NeoForge GameTest server:

- adjacent parallel lines remain isolated;
- a Copper line powers edge 32 and stops at edge 33;
- waterlogged conduits remain powered;
- delay setting 1 activates after 2 game ticks;
- delay setting 4 activates after 8 game ticks.
