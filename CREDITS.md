# Asset and dependency credits

The refinery reference and original refinery, iron and copper textures originate from
Immersive Engineering by BluSunrize and contributors. The reference was adapted locally
to validate an addon-style Beta 1.7.3 machine pipeline. This project is not an official
Immersive Engineering port. The additional 48 x 48 detail texture and cuboid adaptation
were created in the accompanying Blockbench workflow.

The original source model's SHA-256 and every texture's unchanged byte hash are recorded
in `src/main/resources/refinery/model-receipt.json`. Assets retain their original rights;
this local proof does not grant permission for unrelated redistribution.

AeroModelLib by lucasrgt supplies all mesh loading and rendering. Its MIT license is
bundled as `AEROMODELLIB-LICENSE.md`; the exact dependency revision is pinned in
`dependencies.properties`.

Worldline supplies the external extension SDK, TestKit runner, real Forge client
provider, controlled ticks and profiler artifacts. Its probe and runtime instrumentation
are test dependencies and are excluded from the installable mod.

Minecraft is by Mojang. No Minecraft classes, original game assets, historical loader
archives or decompiled sources are included in the installable mod or tracked here.
