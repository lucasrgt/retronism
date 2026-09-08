# Retronism refinery delivery proof

Validated on 2026-09-08 with Minecraft Beta 1.7.3, the real Forge 1.0.6 client,
Java 8 and Worldline's qualified `ForgeTestRuntimeProvider`. Library and Worldline
revisions are pinned in `dependencies.properties`.

- Delivery run: `build/proofs/99da10b85ecc4f4f8792d7effd88f6b3`.
- Command: `./tools/prove.ps1 -Obfuscated`.
- Existing JUnit suite: **170 tests passed**.
- Common refinery side audit: **zero violations** (`B173-SIDE-001`).
- Worldline external contract: **1 passed, 0 failures**, 74.954 seconds.
- In-game fixture: **1265 ticks** across native formation and machine/network scenarios, four orientations.
- Product: `dist/retronism-0.2.0-b1.7.3.jar`, 250 owned/dependency classes.
- Product SHA-256: `c5955d25a2592775ffc36fa71377f07eca0dd122bddd5bb570a8944a61fb29c4`.
- Export checked every delivered JAR entry against the bundle installed in the
  passing game process: **all bytes equal**. No Minecraft or fixture classes ship.

## Native click formation

`RefineryClickProof` places the raw construction fixture, positions the actual
player and leaves the first hotbar slot selected with coal. For each orientation:

1. A Windows key-2 press/release travels through LWJGL and Minecraft's keyboard
   loop; the test observes that the held item becomes the Retronism wrench.
2. With a fresh complete assembly and **no prior mouse click**, the test reads the
   actual crosshair target and captures the raw blocks. It sends exactly one
   right-button press/release. All 45 cells become the linked refinery.
3. A click counter must still equal one, the right button must be released, and
   the fixture records the first tick at which formation is observed. All four
   orientations formed on the next tick: 8 -> 9, 37 -> 38, 66 -> 67, 95 -> 96.
4. Only after the valid case, a separate raw rebuild tests that clicking with coal
   does nothing and that the wrench rejects a wrong construction block atomically.
   Those negative clicks cannot prime the earlier successful formation.

This formation test makes **no direct call** to `Structure.form`, `onItemUse`,
`PlayerController.sendPlaceBlock` or `Minecraft.clickMouse`. Its test-only Windows
adapter invokes the pinned LWJGL native `WindowsDisplay.sendMessage(JJJJ)J` function
against that client's own HWND. The route is Windows window procedure -> LWJGL
mouse queue -> Minecraft input loop -> player controller -> held wrench.
`window-input.log` records **12 right-click pulses** and **8 hotbar selections**.
The LWJGL JAR SHA-256 is
`833e721817f70d1445eec13d8ce5a86e12af70efac5014356302e2bbc68b3fe2`.

The native-input scenario takes 116 ticks and runs before the existing 1149-tick
machine/network scenario. Raw-block placement, inventory provisioning and player
position are fixture setup; individual player-driven placement of the 34 components
is outside this proof. This qualifies synthetic native window input on Windows,
not a physical mouse device or another operating system. The later machine/network
fixtures retain their direct setup helpers and are separate from this input proof.

The opt-in fixture also dismisses Beta's automatic `GuiIngameMenu` on focus loss
and restores its own window focus without sending a mouse click. It logs every
such recovery. A two-orientation diagnostic run reproduced this pause at tick 515
and completed after recovery; earlier interrupted runs remain separate evidence.

Before/after images live under `formation/facing-<n>/before|after/screenshots` in
the run and export to `dist/formation-facing-<n>-before|after.png`.

## Qualified behavior

The original invalid/valid formation cases, required air, all 45 formed cells,
connector landmark raycasts, open-space collision, exact recipe consumption,
blocked machine output, mid-recipe save/reload and one-time teardown drops pass.
Formation now also exercises the production Retronism wrench.

For each of the four orientations, the fixture builds a real coal generator,
two directed energy cables, six item pipes and three vanilla chests. Production
tile ticks perform every transfer. The test checks:

1. Physical connector rendering and side contracts agree with each rotated port.
2. A wrong item remains in the inlet pipe. The existing whitelist prevents that
   rejected item from being drawn again from the mixed source chest.
3. An unpowered network does not process. Coal subsequently powers the actual
   generator, which sends RN through both cables to the refinery.
4. Seed, sugar and product counts are conserved across chests, pipe buffers and
   master inventory. Generated RN equals buffered RN plus recipe consumption
   while the network is intact.
5. The active network is saved, closed and reopened during a recipe. Inventory,
   energy, progress, filters, directions and port behavior survive the disk reload.
6. A full destination chest retains all four products upstream. Clearing it allows
   all four products to travel through both output pipes into the chest.
7. Removing the last power cable stops new supply. One additional recipe consumes
   exactly 160 RN of the machine's existing buffer; replacing the cable restores
   charging. Each orientation finishes with five pellets in the output chest.

The final saved workshop is a fresh north-facing assembly with stocked chests and
coal. It produces into the destination chest before export. The front and rear
PNGs are untouched screenshots from its actual GL context. AeroModelLib rendered
5676 triangles using 16 cached display lists, with exact 5 x 3 x 3 mesh bounds.

## Scope

This qualifies the refinery with Retronism's regular RN cables and item pipes in
singleplayer. The Mega Pipe insertion path uses the same guarded refinery transfer
adapter and its existing unit tests pass, but no complete Mega Pipe runtime line
is claimed. Fluid/gas recipes, other mods' transports, dedicated servers and
StationAPI are not covered. A tilted cuboid has an enclosing-AABB collision
approximation. Breaking a cable discards that cable's stored RN; intact-network
energy accounting is checked before this intentional removal.

`runtime.properties`, `worldline-junit.xml`, `world/forge-f01/client.log`, the
profiler capture and screenshots reside in the delivery run. `dist/proof-receipt.json`
records the exported world's hash and its match to the tested product. Failed
diagnostic runs remain separately identified in `build/proofs` and are not delivery
evidence.
