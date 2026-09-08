# Retronism for Minecraft Beta 1.7.3

The tech mod now includes the 5 x 3 x 3 refinery, rendered by AeroModelLib and
connected to Retronism's RN energy cables and item pipes. The original machine,
generator, fluid, gas and transport code remains in `src/retronism`.

## Play the integrated workshop

Install `dist/retronism-0.2.0-b1.7.3.jar` in a Beta 1.7.3 instance with ModLoader
and Forge 1.0.6 (historical archive `1.0.7-20110907`). AeroModelLib and AeroMachineAPI
are bundled. Replace an older Retronism JAR; this is the mod itself, not a second
refinery addon. The previous standalone refinery JAR uses conflicting block IDs.

Extract `dist/retronism-refinery-world.zip` into the instance's `saves` directory.
The workshop contains a coal generator, two energy cables, six directional item
pipes, two stocked input chests and an output chest. The hotbar includes the
Retronism wrench, spare cables, pipes and coal. This release is qualified for
singleplayer Forge; it does not establish dedicated-server, StationAPI or Mango
Pack compatibility.

The line is:

```text
Seeds chest -- two item pipes --> left inlet
Sugar chest -- two item pipes --> right inlet
Coal generator -- two RN cables --> rear energy socket
Front outlet -- two item pipes --> fuel chest
```

One seed + one sugar makes one biofuel pellet in 20 processing ticks, consuming
8 RN/tick (160 RN total). The machine stores 1600 RN. The generator produces
32 RN/tick while burning; pellets are accepted as 1600-tick furnace/generator fuel.
Redstone items no longer charge this integrated machine. Its recipe transports
items, not fluids.

## Build and connect a refinery

Each row below runs left to right; rows run from back (-Z) to front (+Z).
Dots must be air before formation.

```text
bottom (y=0)      middle (y=1)     top (y=2)
S S L S S         I I L I I        I I . I I
P P P P P         I I . I I        I I . I I
S S H S S         . . H . R        . . . . .
```

S = steel scaffolding, L = light engineering, P = refinery construction pipe,
H = heavy engineering, I = iron sheetmetal, R = redstone engineering.
These are dedicated construction blocks, distinct from the active transport pipes.
Their crafting recipes are registered in `Retronism_Refinery`.

Use the **Retronism wrench** on the front of H at local `(2,1,2)` to form it.
Use the wrench on the formed machine for status; sneak-use it to dismantle.
Breaking a formed part restores the construction and releases inventory once.

| Port | Local cell | Outward face | Accepted transfer |
|---|---|---|---|
| Left inlet | `(0,0,1)` | West | Seeds in |
| Right inlet | `(4,0,1)` | East | Sugar in |
| Front outlet | `(2,0,2)` | South | Biofuel pellets out |
| Rear energy | `(2,1,0)` | North | RN in |

Cells and faces rotate together. Machine faces are fixed by the geometry; configure
the neighboring pipe's sides with the wrench. Set input towards the source and
output towards the destination. Pipe whitelist filters are useful for mixed chests.
Wrong items stay in the pipe, and a full destination retains products upstream.
Removing the power cable stops new RN delivery; existing machine energy can still
finish recipes. The machine, network buffers, directions and filters persist on disk.

Existing production block IDs are 200–214 and 216. Refinery components use 240–245,
with formed parts at 246. Existing items use 756–764; the fuel pellet uses 2257.
These IDs must be free in the target instance.

## Reproduce the proof

Dependencies and revisions are recorded in `dependencies.properties`. Use JDK 21+
for the Worldline runner/compiler and the pinned Java 8 runtime for the game.
Worldline's prepared Forge workspace and legitimate local Minecraft inputs are
required; game JARs and decompiled code are not part of this repository.

```powershell
# In the Worldline checkout, verify its runtime inputs first:
java tools/harness/Gate.java --runtime

# In this repository; defaults point at sibling checkouts:
./tools/prove.ps1 -Obfuscated
```

The delivery gate compiles unchanged pinned libraries, runs the 170 existing unit
tests, audits the common refinery side boundary, reobfuscates the product, and runs
Worldline against the actual installable bytes. `-NetworkOrientations 1` is a focused
diagnostic run; delivery uses all four orientations. Evidence is written to a fresh
`build/proofs/<run>` directory and `build/latest-proof.txt` identifies the last pass.
See `PROOF.md` for the delivered run and qualified claims.

The Windows input proof starts with raw construction blocks. It selects the wrench
using a native hotbar-key event, verifies the real client's crosshair, and sends
right-button press/release messages to the game's own native window. LWJGL and
Minecraft dispatch the click normally; this test never calls the wrench's use
method or the formation method directly. It also rejects the wrong held item and
an incorrect construction block. The valid case runs first on a fresh assembly:
exactly one button press/release must form the machine, and the button must be
released. Negative cases run only after a separate raw rebuild. Before/after images
are saved for all four orientations. Raw-block placement and player positioning are fixture setup; this
does not simulate a player placing each construction block from inventory.
Run `python tools/export_demo.py` after a delivery pass to export the workshop
and the eight `dist/formation-facing-<n>-before/after.png` images.

The legacy shell tools remain for the original monorepo/MCP setup. The new PowerShell
build retains Retronism's flat game-package generation and uses AeroModelLib 3's
package layout directly. The optional Aero showcase companion and VFX demo are kept
as source but excluded from this production JAR; the VFX demo's `aero.particlelib`
dependency is absent from the published checkout, so its registration and hooks
are no longer in the production entrypoint.

## Assets

`assets/refinery.bbmodel` is the native source. `python tools/export_model.py
assets/refinery.bbmodel` regenerates the four material meshes and clipped collision
volumes. The model now has 483 cuboids: centered side/front ports, a recessed rear
energy socket, and clearance cuts through the intersecting perimeter metal.
The four texture byte streams and 16-pixel block scale are preserved; the receipt
records their hashes. `tests/test_refinery_ports.py` checks the mouth clearances,
face centers, runtime port semantics and correspondence with the exported meshes.
`tools/sync_model_geometry.py` transfers native edits while retaining the game's
material palette; its optional `--provenance` receipt preserves materials on cut
fragments. One tilted cuboid uses its enclosing
AABB for collision. See `CREDITS.md` for asset provenance and dependency credits.
