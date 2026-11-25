# CBC: Trajectory

An artillery fire-control addon for [Create Big Cannons](https://modrinth.com/mod/create-big-cannons).
Spot a target with a rangefinder, write it into a spotter notebook, and let a calculation table work
out the elevation and bearing for your cannon.

> **Status:** early development. Nothing is released yet, and items, blocks and behaviour may change.

## Planned gameplay

1. **Rangefinder.** Zoom like a spyglass and read the distance and coordinates of the block you are
   looking at. Sneak and release to record the target.
2. **Spotter notebook.** Keeps the recorded targets. Measurements will become imperfect later on, so
   a good spotter matters.
3. **Calculation table.** Link it to a cannon mount, then hand it the notebook. It reads the cannon
   (barrel length, charges, projectile) and gives the low and high arc solutions, including the
   Sequenced Gearshift values to dial them in.

## How it works

The solver is a pure-Java replica of CBC's own projectile physics: per-tick gravity and drag, taken
from CBC's datapack values and dimension multipliers. It is unit-tested against real in-game shots
and lands within one block of them. Research notes (in Turkish) are in
[`docs/research/faz1-cbc-balistik.md`](docs/research/faz1-cbc-balistik.md).

## Roadmap

- [x] **Phase 0: Setup.** NeoForge 1.21.1 project with Create, CBC and Ritchie's Projectile Library.
- [x] **Phase 1: Research.** Where CBC's ballistics live, datapack vs config, how mount angles work.
- [x] **Phase 2: Solver.** Trajectory model and fire solver with JUnit tests, verified in game.
- [ ] **Phase 3: Items and blocks.** Rangefinder, spotter notebook, calculation table. In progress:
  the items work, and the table's fire solution is next.
- [ ] **Phase 4: Spotter flow.** Measurement error and recording into the notebook.
- [ ] **Phase 5: Table interface.** Map, solution display, corrections.
- [ ] **Phase 6: Polish.** Ponder scenes, sounds, config, multiplayer testing.

## Requirements

| | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x |
| Create | 6.0.7 – 6.0.x |
| Create Big Cannons | 5.11.7+ |
| Ritchie's Projectile Library | 2.1.2+ |

## Building from source

You need JDK 21. Gradle downloads everything else, including the dependencies from the Modrinth Maven.

```bash
./gradlew build          # compile and run the unit tests
./gradlew test           # unit tests only (solver and trajectory model)
./gradlew runClient      # start a development client with the mod loaded
```

The built jar ends up in `build/libs/`.

## License

All rights reserved for now. A license will be chosen before the first release.
