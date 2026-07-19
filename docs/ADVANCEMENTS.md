# Engineering Advancements

Zeppelin Must Have `0.15.0` provides a dedicated advancement tree for fabrication and commissioning milestones.

The tree is server-authoritative. Advancement JSON defines the display hierarchy and rewards, while `ZmhAdvancements` awards criteria only after verified crafting or successful system activation.

## Tree structure

```text
Zeppelin Engineering
├── Fabrication
│   ├── Flight Control Workshop
│   ├── Lighter Than Air
│   ├── Raise the Pressure
│   ├── High-Pressure Plumbing
│   ├── Signal in a Pipe
│   ├── Trim the Ship
│   ├── Make Fast
│   ├── Vertical Authority
│   ├── Performance Package
│   └── Master Shipwright
└── Bring It Online
    ├── Instruments Alive
    ├── Light the Envelope
    ├── Modified for Flight
    ├── Hands Off the Altitude
    ├── Command Network Online
    ├── Ring the Telegraph
    ├── All Stop!
    ├── On the Same Frequency
    ├── Take On Ballast
    ├── Sealed Signal
    ├── Steam on the Main
    ├── Lift from Below
    └── Secure the Airship
```

The packaged tree contains 25 advancement definitions:

- one root;
- nine fabrication categories;
- one complete-catalog challenge;
- one commissioning branch root;
- thirteen commissioning milestones.

## Fabrication milestones

Fabrication uses two complementary server-side paths:

- `PlayerEvent.ItemCraftedEvent` grants immediate progress for player crafting;
- catalog-generated `minecraft:inventory_changed` criteria grant the same persistent progress when a player receives output from Create Mechanical Crafting or another automated production path.

Receiving a newly produced catalogued part awards:

1. the engineering root;
2. the matching fabrication category;
3. that part's persistent criterion in `Master Shipwright`.

The category mapping is derived from `ZeppelinPartCategory`, so newly catalogued categories must also provide a corresponding fabrication advancement.

### Master Shipwright

`Master Shipwright` contains one criterion for every current Zeppelin Part registry path. The criteria are accumulated over time; players do not need to hold every part simultaneously.

The advancement is a challenge and grants `500` experience when all 34 catalogued item entries have been crafted.

`AdvancementGameTests.masterShipwrightMatchesCatalog` fails when the criteria and `ZeppelinPartCatalog` diverge.

## Commissioning milestones

Commissioning advancements are not awarded for merely placing a block. The relevant system must enter a meaningful operational state.

| Advancement | Server-side condition |
|---|---|
| Instruments Alive | Inspect an Airship Helm attached to a Sable vessel |
| Light the Envelope | Successfully insert accepted fuel into an Airship Burner |
| Modified for Flight | Successfully install a burner upgrade module |
| Hands Off the Altitude | Capture a valid target and engage Altitude Hold |
| Command Network Online | Successfully configure a Flight Computer |
| Ring the Telegraph | Issue an Engine Telegraph order |
| All Stop! | Latch the vessel-wide Emergency Cutoff |
| On the Same Frequency | Successfully configure a Control Transmitter or Receiver |
| Take On Ballast | Increase a Ballast Tank's stored fluid through player interaction |
| Sealed Signal | Switch a Piped Redstone Native Lever from off to on |
| Steam on the Main | Transition a graded Steam Engine from inactive to effective output |
| Lift from Below | Transition a Vertical Thruster from zero to physical thrust |
| Secure the Airship | Transition a Mooring Winch rope holder from detached to attached |

Passive machinery transitions award nearby server players within eight blocks. Transient edge flags prevent repeated award scans while a system remains continuously active.

## Runtime architecture

```text
ZmhAdvancements
├── crafting event bridge
├── catalog-to-fabrication routing
├── criterion awards
├── commissioning milestone awards
├── nearby operator awards
└── advancement progress inspection
```

All direct progress changes pass through `ZmhAdvancements`; blocks and block entities only report verified activation events.

Commissioning advancements use `minecraft:impossible` criteria because operational conditions are validated by mod systems rather than generic vanilla predicates. Fabrication advancements use generated `minecraft:inventory_changed` criteria so Create Mechanical Crafting outputs are recognized when the player receives them.

## Data location

```text
data/zeppelin_must_have/advancement/engineering/*.json
```

Localization keys are present in:

- `en_us`;
- `ru_ru`;
- `it_it`;
- `pl_pl`.

Only the Russian localization file contains Cyrillic repository content.

## Validation

`AdvancementGameTests` verifies:

- all 25 advancement definitions load;
- every fabrication category resolves to a root child;
- the Master Shipwright criteria exactly match `ZeppelinPartCatalog`;
- every commissioning milestone is parented to the commissioning branch;
- every commissioning milestone exposes exactly one server-awarded criterion.

The complete GameTest suite currently contains 41 tests.
