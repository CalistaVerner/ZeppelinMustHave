# Mooring Winch

The Mooring Winch is a zeppelin-specific specialization of the native Create Simulated Rope Winch.

It does not create a decorative line or a separate mooring simulation. The block inherits Create Simulated's rope strand, endpoint attachment, tension, break force, length control, rendering, and Sable physics constraints.

## Kinetic operation

The block is a Create kinetic consumer. Shaft rotation controls rope movement through the inherited Rope Winch behavior:

```text
Create kinetic source
        │
        ▼
Mooring Winch drum
        │
        ▼
Create Simulated rope strand
        │
        ▼
Physical attachment / tension constraint
```

Changing the shaft direction changes the extension direction. Stopping the kinetic network holds the current commanded rope length.

The exact rope attachment interaction and rope materials follow the installed Create Simulated version, preserving compatibility with its normal strand and endpoint system.

## Physics integration

Once attached, the line participates in Simulated/Sable physics rather than teleporting or freezing the vessel. The native rope system owns:

- current and target line length;
- rope segments and rendering;
- endpoint attachments;
- tension calculation;
- break-force behavior;
- physical constraints between connected bodies or anchors.

This allows a moored zeppelin to move within the line's permitted length and react to tension, propulsion, lift, and mass changes.

## Block orientation

The Mooring Winch inherits the full directional and shaft-axis state of the Create Simulated Rope Winch. It may be mounted on floors, ceilings, walls, or moving Sable sub-levels.

## Diagnostics

Create Engineer's Goggles display:

- whether a rope strand is attached;
- current commanded line speed;
- inherited Create kinetic information.

The client renderer reuses Create Simulated's animated shaft, rope coil, and rope-strand renderer.

## Intended use

A complete mooring setup normally consists of:

1. a powered Mooring Winch on the zeppelin;
2. a Create Simulated rope strand;
3. a valid endpoint or world attachment;
4. a controllable Create kinetic source for paying out and retrieving the line.
