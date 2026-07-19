# Flight Control Network

Version `0.12.0` introduces a vessel-local, server-authoritative control bus for large Sable airships. The network does not replace Aeronautics physics. It moves commands and telemetry between existing sensors, controllers, burners, engines, and thrusters.

## System topology

```text
Manual control / automatic sensor
              |
              v
    Control Transmitter or Engine Telegraph
              |
       named network + frequency
              |
              v
        Flight Computer
              |
       authoritative output frame
              |
              v
        Control Receiver
              |
     redstone output or direct actuator
```

Every route is scoped to exactly one loaded Sable `ServerSubLevel`. A transmitter on one vessel cannot address a receiver on another vessel, even when both use the same network name and frequency.

## Addressing

An FCN address consists of:

- a normalized network name, up to 32 characters;
- a frequency from `0` through `15`;
- the containing Sable sub-level UUID, supplied by the server and never editable by the client.

The default address is `primary / 0`.

Use a renamed Name Tag on a configurable FCN block to set the network name. Use any dye to set its 0–15 frequency. Use a Create wrench on a transmitter or receiver to cycle its selected command channel.

## Physical power

The Flight Computer, Engine Telegraph, Control Transmitter, and Control Receiver require a redstone power supply from the block directly below them. A signal arriving at another face does not satisfy the control-power requirement.

The Emergency Cutoff is intentionally independent of normal controller power. Safety shutdown must remain available during a control-bus power failure.

## Flight Computer

Only one live Flight Computer is primary for a given vessel address. The server chooses the primary deterministically from the active block positions. Secondary computers continue sampling telemetry but cannot publish output frames until the primary stops heartbeating.

### Telemetry input

The computer samples:

- world altitude;
- vertical velocity;
- heading, pitch, and roll;
- linear and angular velocity;
- vessel mass;
- local Sable center of mass;
- balloon count, capacity, fill volume, target volume, and lift;
- active and total engine, burner, thruster, and ballast-system counts;
- average ballast fill.

### Output lanes

| Channel | Range | Semantics |
|---|---:|---|
| `LIFT` | 0..15 | Burner/lift command |
| `VERTICAL_THRUST` | -15..15 | Bidirectional vertical thrust |
| `PITCH_TRIM` | -15..15 | Pitch trim command |
| `ROLL_TRIM` | -15..15 | Roll trim command |
| `YAW_TRIM` | -15..15 | Yaw trim command |
| `ENGINE_THROTTLE` | -15..15 | Ahead/astern engine command |
| `EMERGENCY_STOP` | 0/1 | Vessel-wide safety latch |

The first release deliberately performs routing and arbitration only. It does not contain a hidden autopilot or apply force directly to Sable rigid bodies.

## Source arbitration

Competing command sources use the following fixed authority order:

```text
SAFETY > MANUAL > AUTOMATIC
```

At equal authority, the newest server tick wins. A deterministic block-position tie-breaker handles commands published in the same tick.

Input sources expire after 40 ticks. Flight Computer heartbeats and output frames expire after 20 ticks. Expired data is never held indefinitely.

## Engine Telegraph

The physical telegraph publishes `ENGINE_THROTTLE` with manual authority:

| Order | Command |
|---|---:|
| `FULL AHEAD` | +15 |
| `HALF AHEAD` | +10 |
| `SLOW AHEAD` | +5 |
| `STOP` | 0 |
| `SLOW ASTERN` | -5 |
| `HALF ASTERN` | -10 |
| `FULL ASTERN` | -15 |

Use normally to move the handle toward ahead. Sneak-use to move it toward astern.

The front face also emits the selected order as bipolar analog redstone, so the telegraph can operate as a physical seven-position control even without an FCN receiver.

When a receiver faces a graded Steam Engine, the command scales available shaft efficiency while retaining the boiler and Create kinetic constraints. Astern orders reverse the conveyed engine direction.

## Control Transmitter

The transmitter reads analog redstone from the block behind it and publishes one selected FCN lane with automatic authority.

Unsigned lanes preserve the vanilla `0..15` signal. Signed lanes use bipolar analog encoding:

```text
0  = -15
8  = neutral
15 = +15
```

Because 16 redstone levels encode 31 signed command values, round-trip quantization may differ by one command step.

## Control Receiver

The receiver reads the authoritative Flight Computer output for its address and selected lane. It provides:

1. a directional analog redstone output on its front face;
2. a direct command when its front face touches an FCN-aware actuator.

Direct integrations in `0.12.0`:

- `LIFT` -> Airship Burner family;
- `VERTICAL_THRUST` -> Vertical Thruster;
- `ENGINE_THROTTLE` -> Copper, Brass, and Industrial Steam Engines.

Other channels remain available as analog redstone for future rudders, trim surfaces, ballast pumps, or third-party integrations.

## Emergency Cutoff

Activating any Emergency Cutoff on a vessel latches a vessel-wide shutdown. The latch:

- forces burner gas output to zero;
- forces vertical-thruster output to zero;
- forces graded Steam Engine efficiency to zero;
- stops the automatic Altitude Gauge output;
- clears Flight Computer output frames;
- blocks controller restart while latched;
- drives every loaded Emergency Cutoff block on that vessel to redstone level 15.

The latch is persisted in world SavedData by Sable vessel UUID and survives server restarts until a player manually resets an Emergency Cutoff by sneak-using it. Reset clears prior output frames, so controllers must heartbeat and reacquire their commands instead of resuming stale orders.

## Server authority and trust boundary

Clients never select the vessel UUID, publish an output frame, or decide which computer is primary. All routing, timeouts, emergency state, source priority, and sub-level containment checks run on the logical server.

## Compatibility contract

- No direct Sable force is introduced by the Flight Computer.
- Aeronautics remains the owner of balloon and propeller force calculations.
- Create remains the owner of kinetic speed, boiler state, and shaft constraints.
- FCN direct-actuator integration is opt-in through `FlightControlActuator`.
- Existing redstone control continues when no FCN receiver owns an actuator.

## Known first-release boundaries

- Command sources and output frames are runtime state, not a cross-restart command queue. Devices republish after loading; only the emergency latch is persisted.
- Routes require the relevant sub-level and devices to be loaded.
- Pitch, roll, and yaw trim are protocol lanes in `0.12.0`; dedicated control-surface blocks are planned separately.
- Generic signed redstone consumers must understand bipolar encoding and treat level 8 as neutral.
