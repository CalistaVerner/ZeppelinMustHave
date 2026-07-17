# Zeppelin Must Have

> **Build a real zeppelin — not just a floating contraption.**

**Zeppelin Must Have** is a technical add-on for **Create Aeronautics** that expands airship construction with proper flight telemetry, scalable hot-air systems, graded pressure vessels, multi-cylinder steam engines, protected redstone routing, and automatic altitude control.

The mod is built for players and servers that want airships to feel like engineered machines: systems have tiers, measurable output, real constraints, Create-style progression, Engineer's Goggles diagnostics, Ponder documentation, and data-pack-driven tuning.

---

## Zeppelin Parts — Complete Coverage

Version `0.11.0` catalogues every public block and item as a Zeppelin Part: **19 functional block parts and 3 burner upgrades**.

The Zeppelin Parts catalog provides:

- a dedicated creative tab;
- subsystem and role tooltips on every item;
- full Ponder category membership, including direct Ponder access from upgrade items;
- public root and category item/block tags for modpack integration;
- complete pickaxe and stone-tool coverage for every metal block;
- automatic registry and tag coverage tests.

The public root tags are `#zeppelin_must_have:zeppelin_parts` for items and blocks. Category tags cover flight control, lift, steam power, protected redstone, ballast, mooring, propulsion, and upgrades.

---

## Main Features

### Airship Helm

The **Airship Helm** is a server-authoritative telemetry station for moving Sable sub-levels.

It reports:

- vessel name and persistent sub-level UUID;
- global position and altitude;
- heading, pitch, and roll;
- linear, vertical, and angular velocity;
- total sub-level mass;
- Aeronautics balloon count and capacity;
- current and target lifting-gas volume;
- balloon fill ratio and raw lift.

Create Engineer's Goggles display normal flight information directly on the block. Sneaking adds advanced diagnostics such as UUID, projected coordinates, angular velocity, and telemetry age.

---

### Three Airship Burner Grades

All Zeppelin Must Have burners extend the native **Create Aeronautics Hot Air Burner** implementation. Aeronautics remains responsible for airtight-envelope discovery, gas filling, pressure, interpolation, particles, sound, and lift.

| Burner | Intended role | Default profile |
|---|---|---:|
| Airship Burner | Basic envelope heating | `1.0×` gas output |
| Forced-Draft Airship Burner | Faster heating and larger envelopes | `2.25×` gas output |
| Industrial Airship Burner | Large zeppelins and heavy lift systems | `4.5×` gas output |

Burners support:

- vanilla and NeoForge furnace fuels;
- Create regular Blaze Burner fuels;
- Create superheated fuels;
- Creative Blaze Cake for infinite preview/creative operation;
- mixed regular and superheated heat reserves;
- redstone-controlled throttling;
- multiple burners connected to one native Aeronautics balloon;
- server-side data-pack profiles;
- detailed Engineer's Goggles diagnostics.

Superheated heat is consumed first. When it runs out, the burner automatically falls back to the regular reserve.

---

### Burner Upgrade Sockets

Each burner provides three persistent upgrade sockets:

- **Thermal** — improves endurance and heat-reservoir capacity;
- **Airflow** — increases output and envelope range;
- **Control** — improves low-power redstone regulation.

Upgrade modules are installed directly on the burner. Sneak-use the Create Wrench to remove the most recently occupied socket. Breaking a burner drops all installed modules.

Upgrade slots, conflicts, supported targets, and numerical effects are loaded from data packs rather than hardcoded into the item classes.

---

### Graded Create Boilers

Zeppelin Must Have adds three pressure-vessel grades implemented as direct descendants of Create's **Fluid Tank**.

| Grade | Active heat | Superheated heat | Maximum per heater column |
|---|---:|---:|---:|
| Copper — Grade I | 2 | 3 | 3 |
| Brass — Grade II | 3 | 5 | 5 |
| Industrial — Grade III | 5 | 8 | 8 |

Same-grade blocks merge through Create's native tank connectivity and behave as one vessel:

- shared fluid storage;
- native pipe capability;
- Create Wrench support;
- Create Steam Whistle support;
- native water and boiler calculations;
- controller-only fluid rendering;
- connected textures with no visible internal block grid;
- grade-specific pressure-gauge frame, scale, and animated dial.

Different boiler grades cannot merge into one controller. Create remains authoritative for tank dimensions, water requirements, attached engines, efficiency, boiler level, comparator output, and generated Stress Units.

---

### Graded Steam Engines

Every boiler grade has a matching Create-compatible steam engine with its own housing, capacity, boiler load, cylinder count, and animation.

| Engine | Capacity | Boiler load | Mechanism |
|---|---:|---:|---|
| Copper Steam Engine | 1024 SU | 1 unit | Single cylinder |
| Brass Compound Steam Engine | 2560 SU | 2 units | Two cylinders, 180° opposed |
| Industrial Triple-Expansion Engine | 4608 SU | 3 units | Three cylinders, 120° phased |

The engines retain Create's normal shaft placement, rotation-direction control, efficiency, overstress behavior, and kinetic-network integration.

Higher-grade engines are more efficient per boiler-load unit, but they do not bypass heat, water, vessel-size, or efficiency limits.

Graded engines must be attached to Zeppelin Must Have graded boilers. Standard Create Fluid Tanks continue using the native Create Steam Engine.

---

### Piped Redstone

**Piped Redstone** provides protected, waterloggable analog redstone routing for airships and dense Create machinery.

Unlike ordinary automatic pipe networks, every conduit has six explicit ports. Two neighboring conduits connect only when both reciprocal ports are enabled.

This allows:

- neighboring redstone lines without accidental merging;
- compact crossings and machinery compartments;
- underwater and waterlogged routing;
- full analog values from `0` to `15`;
- Create Wrench port control;
- Create Placement Assist for extending straight runs;
- tiered propagation speed and distance.

| Tier | Propagation delay | Repeater-free distance |
|---|---:|---:|
| Copper | 4 game ticks | 32 edges |
| Brass | 2 game ticks | 64 edges |
| Resonant | 1 game tick | 128 edges |

Mixed-tier networks use the slowest delay and shortest range present in the connected component.

#### Native Lever

The **Piped Redstone Native Lever** mounts directly onto a conduit face, opens the matching port, and emits power only into that isolated conduit line. The handle uses a smooth animated block-entity renderer and remains functional while waterlogged.

#### Waterproof Repeater

The **Piped Redstone Repeater** preserves analog signal strength and begins a new distance segment. Right-clicking cycles the same four delay settings as a vanilla repeater.

---

### Automatic Altitude Control

The **Altitude Gauge** is a functional directional flight sensor and inline burner controller.

It provides four Create-Wrench-selectable modes:

1. **Altitude telemetry** — maps global altitude to analog redstone;
2. **Vertical-speed telemetry** — reports climb and descent around neutral signal `8`;
3. **Balloon-fill telemetry** — exposes the native Aeronautics fill ratio;
4. **Altitude Hold** — applies altitude correction and vertical-speed damping to a rear trim input.

A typical automatic control chain is:

```text
Piped Redstone Native Lever
        │ base throttle
        ▼
Altitude Gauge rear input
        │ stabilized output
        ▼
Piped Redstone network
        ▼
Airship Burners
```

Sneak-right-click with an empty hand to capture the current altitude and arm Altitude Hold. Sneak-right-click again to disable it. Deadband and output slew limiting reduce oscillation.

---

## Ballast, Mooring, and Vertical Control

### Ballast Tank

The Ballast Tank stores up to `8000 mB` of water and exposes the standard NeoForge fluid capability for buckets, containers, and Create pipes. Stored fluid contributes real dynamic mass to the containing Sable sub-level, changing total vessel mass, center of mass, and inertia. A comparator reports fill level and Engineer's Goggles show the current added mass.

### Mooring Winch

The Mooring Winch extends Create Simulated's native Rope Winch. Create kinetic rotation pays out or retrieves a real rope strand with native endpoint attachments, tension, break force, rendering, and Sable physics constraints. A moored zeppelin remains physically simulated instead of being frozen in place.

### Vertical Thruster

The Vertical Thruster is a Create-powered Aeronautics propeller for direct upward or downward maneuvering. Sable applies the force at the actual mounting position every physics tick, so offset thrusters generate physically correct torque. The Create Wrench reverses thrust or flips the installation direction.

---

## Engineer's Goggles and Ponder

The mod integrates with Create's normal documentation and inspection systems.

Engineer’s Goggles provide live information for:

- Helm flight telemetry;
- burner fuel, throttle, output, upgrades, and balloon network state;
- graded boiler profiles;
- graded steam-engine capacity and mechanism data;
- Altitude Gauge sensor/controller state.

The **Zeppelin Parts** Ponder category includes scenes for:

- Airship Helm telemetry;
- burner operation and upgrade sockets;
- graded boilers;
- graded steam engines;
- Piped Redstone networks;
- automatic altitude control;
- Ballast Tank mass trim;
- Mooring Winch rope physics;
- Vertical Thruster propulsion.

---

## Data-Pack Configuration

Most numerical tuning is server-data-driven and can be reloaded with `/reload`.

Supported directories:

```text
data/<namespace>/airship_burner_profiles/*.json
data/<namespace>/airship_upgrades/*.json
data/<namespace>/boiler_grade_profiles/*.json
data/<namespace>/steam_engine_grade_profiles/*.json
data/<namespace>/piped_redstone_profiles/*.json
data/<namespace>/altitude_control_profiles/*.json
data/<namespace>/ballast_tank_profiles/*.json
data/<namespace>/vertical_thruster_profiles/*.json
```

Server packs can rebalance burner output, fuel use, capacity, upgrade effects, boiler heat conversion, engine capacity, cylinder geometry, conduit delay/range, and altitude-controller behavior without rebuilding the mod.

Clients receive resolved runtime snapshots for the information required by renderers and Engineer's Goggles.

---

## Required Dependencies

Zeppelin Must Have is an add-on, not a standalone mod. Install every required dependency on both the client and server.

| Component | Supported version |
|---|---:|
| Minecraft | `1.21.1` |
| Java | `21` |
| NeoForge | `21.1.235` or newer compatible 21.1 build |
| Create | `6.0.10` to `< 6.1.0` |
| Sable | `2.0.0` to `< 3.0.0` |
| Create Simulated | `1.3.0` to `< 2.0.0` |
| Create Aeronautics | `1.3.0` to `< 2.0.0` |

The current Zeppelin Must Have release is **0.11.0**.

---

## Installation

1. Install **Java 21**.
2. Install **NeoForge for Minecraft 1.21.1**.
3. Install Create, Sable, Create Simulated, and Create Aeronautics using compatible versions.
4. Place `zeppelin_must_have-0.11.0.jar` into the `mods` folder.
5. Install the same mod and dependency versions on the server and every connecting client.

---

## Compatibility Philosophy

Zeppelin Must Have extends upstream systems through public APIs whenever possible.

The mod does not introduce:

- Mixins;
- access transformers;
- replacement Create or Aeronautics blocks;
- a parallel balloon simulation;
- a parallel gas, pressure, or lift system;
- a replacement kinetic network.

Create, Sable, Create Simulated, and Create Aeronautics remain authoritative for the systems they own.

---

## Current Development Status

Fully functional systems in version `0.11.0`:

- Airship Helm telemetry;
- three Airship Burner grades;
- layered regular/superheated heat storage;
- burner upgrade sockets;
- graded Create boilers;
- graded animated steam engines;
- Piped Redstone, Native Lever, and Waterproof Repeater;
- Altitude Gauge telemetry and Altitude Hold;
- Ballast Tank fluid storage and real Sable mass trim;
- Mooring Winch with native Create Simulated rope physics;
- Vertical Thruster with native Aeronautics/Sable propulsion;
- Engineer's Goggles integration;
- Ponder scenes;
- data-pack profiles;
- multiplayer synchronization.

---

## Multiplayer

The mod is designed for dedicated servers and Create Aeronautics multiplayer worlds.

Authoritative flight telemetry, control output, fuel consumption, profiles, redstone-network solving, and boiler/engine operation run on the logical server. Clients render synchronized state.

The mod must be installed on both sides.

---

## Source, Issues, and Documentation

- Source repository: [GitHub — ZeppelinMustHave](https://github.com/CalistaVerner/ZeppelinMustHave)
- Issue tracker: [GitHub Issues](https://github.com/CalistaVerner/ZeppelinMustHave/issues)
- Detailed technical documentation is included in the repository under `docs/`.

---

## Author and License

**Author:** `us.Kayla`

**License:** All Rights Reserved
