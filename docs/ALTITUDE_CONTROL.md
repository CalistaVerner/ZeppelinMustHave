# Altitude Gauge and Closed-Loop Altitude Hold

Zeppelin Must Have 0.8.0 turns the existing Altitude Gauge into a functional flight sensor and inline burner controller.

The system complements Create Aeronautics rather than replacing its physics:

```text
Native Lever / analog trim source
        │
        ▼
rear face of Altitude Gauge
        │
        ├── authoritative Sable altitude
        ├── authoritative vertical velocity
        └── Aeronautics balloon fill
        │
        ▼
front analog output 0..15
        │
        ▼
Piped Redstone
        │
        ▼
Airship Burners
```

Aeronautics remains responsible for balloon gas, pressure, lift, and vessel motion. The gauge only calculates a redstone control signal.

## Orientation

The instrument is directional:

- the dial is the front face;
- the rear face accepts an optional analog trim input;
- the front face emits the selected telemetry or control signal;
- both faces connect directly to vanilla redstone and Piped Redstone.

Place the gauge with its dial facing the direction in which the output line should leave.

## Modes

Use the Create Wrench normally on the gauge to cycle modes.

### Altitude telemetry

Maps global altitude across the dimension build-height range:

```text
minimum build height → 0
middle of build range → approximately 8
maximum build height → 15
```

This mode is useful for altitude alarms, staged burner systems, and automatic lighting or warning panels.

### Vertical-speed telemetry

Maps descent and climb around neutral signal 8:

```text
maximum configured descent → 0
stationary vertical motion → 8
maximum configured climb → 15
```

The full-scale vertical speed is data-pack configurable.

### Balloon-fill telemetry

Maps the current Aeronautics balloon fill ratio directly to `0..15`.

This can drive low-gas alarms, reserve burners, warning lamps, or ComputerCraft redstone inputs without duplicating Aeronautics balloon state.

### Altitude hold

Altitude Hold is an inline controller rather than a fixed-power source.

The rear input is the operator's neutral burner trim. The gauge adds a bounded correction:

```text
correction =
    altitude_error × proportional_gain
    - vertical_speed × damping_gain

output = clamp(trim + correction, 0, 15)
```

This design matters because different airships have different mass, envelope volume, and burner arrangements. The player establishes the approximate neutral signal with a Native Lever; the controller performs only stabilization.

## Arming and disabling

- Sneak-right-click the gauge with an empty hand to enter Altitude Hold and capture the current global altitude.
- Sneak-right-click again to disable the controller.
- While disabled in Altitude Hold mode, the rear trim signal passes to the front unchanged.
- Normal right-click reports current telemetry and signal state.

## Stability controls

The bundled profile includes three anti-oscillation measures:

1. **Deadband** — small altitude errors do not trigger proportional correction.
2. **Vertical damping** — upward motion reduces throttle and downward motion increases it before the altitude error becomes large.
3. **Signal slew limit** — output can change only by a configured number of redstone levels per sample.

## Data-pack profile

Profiles are loaded from:

```text
data/<namespace>/altitude_control_profiles/<profile>.json
```

The built-in controller uses:

```text
zeppelin_must_have:default
```

Schema version 1:

```json
{
  "schema_version": 1,
  "sample_interval_ticks": 2,
  "vertical_speed_full_scale": 6.0,
  "hold_deadband_blocks": 0.35,
  "hold_proportional_gain": 1.25,
  "hold_vertical_damping_gain": 2.0,
  "hold_maximum_correction": 7.0,
  "maximum_signal_step": 2
}
```

| Field | Meaning |
|---|---|
| `sample_interval_ticks` | Server ticks between telemetry samples. |
| `vertical_speed_full_scale` | Absolute vertical speed represented by the ends of the telemetry range. |
| `hold_deadband_blocks` | Altitude error ignored by the proportional term. |
| `hold_proportional_gain` | Signal correction per block of altitude error. |
| `hold_vertical_damping_gain` | Signal correction opposing vertical velocity. |
| `hold_maximum_correction` | Maximum correction above or below trim. |
| `maximum_signal_step` | Maximum output change per sample. |

Invalid or absent profiles fail closed by using a deliberately slow, low-gain fallback.

## Engineer's Goggles

The gauge exposes:

- selected mode;
- current altitude;
- vertical speed;
- rear trim input;
- front output;
- balloon fill in fill mode;
- hold state, target, and altitude error;
- control interaction hints while sneaking.

## Automated verification

`AltitudeControlGameTests` verifies:

- altitude, vertical-speed, and balloon-fill scaling;
- positive correction below target;
- negative correction above target;
- deadband trim pass-through;
- vertical damping against climb and descent;
- signal slew limiting.
