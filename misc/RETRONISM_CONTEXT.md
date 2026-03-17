# RetroNism — Project Context Document

> **Use this file as context when working with Claude in VSCode / Claude Code.**
> It contains all research, decisions, and technical details gathered for the RetroNism mod project.

---

## 1. Project Overview

**RetroNism** is a unified tech mod for Minecraft Beta 1.7.3, inspired by Refined Storage and Mekanism. It is being developed as an addon for the **Mango Pack** — a popular retro modpack maintained by LowMango.

### Goals

- Digital storage network (inspired by Refined Storage / Applied Energistics)
- Processing machines (inspired by Mekanism)
- Unified energy system
- Universal transport pipes (items, fluids, gases — future phases)
- Integration with IC2 (already present in Mango Pack)

### Phases

| Phase | Feature | Status |
|-------|---------|--------|
| 0 | First block + item in-game (proof of concept) | **Current** |
| 1 | Storage network (controller, disk drive, terminal, cables) | Planned |
| 2 | Energy network + cables + basic electric machines | Planned |
| 3 | Fluid system + tanks + pipes | Planned |
| 4 | Gas system + refinery + Mekanism-style machines | Planned |

---

## 2. Toolchain & Stack

| Tool | Version | Purpose |
|------|---------|---------|
| **RetroMCP-Java** | Latest release | Decompile, recompile, build b1.7.3 |
| **Minecraft** | Beta 1.7.3 | Target game version |
| **Forge** | 1.0.6 | Mod API (includes ModLoader) |
| **ModLoader** | Risugami's (bundled with Forge) | Base mod loading |
| **JDK** | 8 (Azul Zulu recommended) | Compilation |
| **IDE** | IntelliJ IDEA / VSCode | Development |

### RetroMCP-Java Workflow

```
1. Download RetroMCP-Java from https://github.com/MCPHackers/RetroMCP-Java
2. Run: setup → choose Beta 1.7.3
3. Inject Forge 1.0.6 into jars/minecraft.jar BEFORE decompiling
4. Run: decompile
5. Open generated project in IDE
6. Create mod classes
7. Run: build → generates .zip in build/ folder
8. Distribute .zip as jar mod for Mango Pack
```

### Forge 1.0.6 Download

- Source: https://master.dl.sourceforge.net/project/minecraftforge/1.0.6/minecraftforge-client-1.0.6.zip
- Shared by LowMango in the Mango Pack Discord

### Installation Order (into minecraft.jar)

1. ModLoader (first)
2. ModLoaderMP (second)
3. Forge 1.0.6 (third)
4. Other APIs if needed (GuiAPI, AudioMod)
5. Mods last

> **Note:** Forge 1.0.6 already includes ModLoader, so you may only need Forge. Test to confirm.

---

## 3. Mango Pack Details

### About

- Created by **LowMango**
- Retro modpack for Minecraft Beta 1.7.3
- Has **Vol1** and **Vol2** versions
- Uses ModLoader + Forge 1.0.6
- Addons are posted in the `#mangopack-addons` Discord channel
- Addons are tagged as "Vol1 Compatible" and/or "Vol2 Compatible"

### Included Mods (known)

- IndustrialCraft 2 (IC2)
- BuildCraft
- Portal Gun
- RetroStorage (buggy, dev not interested in ModLoader support)
- Backpack mod
- Various QoL mods (Rei's Minimap, etc.)

### Key Constraints

- **256 block IDs max** (0-255) — use metadata/damage values to save IDs
- **~32000 item IDs** — items are not a bottleneck
- **Java 8 required** — ModLoader uses reflection incompatible with Java 17+
- **ID conflicts are common** — choose high IDs (200+) and document them
- **Jar mod distribution** — mods go into minecraft.jar or mods/ folder
- **Forge mods → mods/ folder**, base edit mods → minecraft.jar

### Future Direction

LowMango has expressed interest in migrating to **StationAPI** in the future:
> "I think we might try and get some sort of Station API build made in the future. It'd mean multiplayer compat and easier modding."

**Apron 3.0** exists as a bridge that allows ModLoader mods to run under StationAPI. IC2, Portal Gun, and BuildCraft are confirmed working with it.

**Strategy:** Build for ModLoader/Forge now, keep architecture clean for future StationAPI port.

---

## 4. Technical Reference

### ModLoader Mod Structure

```java
package net.minecraft.src;

public class mod_RetroNism extends BaseMod {

    // Block registration — use IDs 200+ to avoid conflicts
    public static final Block storageController = new BlockStorageController(
        200,
        ModLoader.getUniqueSpriteIndex("/terrain.png")
    ).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep);

    public mod_RetroNism() {
        ModLoader.RegisterBlock(storageController);
        ModLoader.AddName(storageController, "Storage Controller");

        // Crafting recipe example
        ModLoader.AddRecipe(
            new ItemStack(storageController, 1),
            new Object[] {
                "IDI",
                "DRD",
                "IDI",
                'I', Item.ingotIron,
                'D', Item.diamond,
                'R', Item.redstone
            }
        );
    }

    public void RegisterTextureOverrides() {
        ModLoader.addOverride(
            "/terrain.png",
            "/retronism/block/controller.png",
            storageController.blockIndexInTexture
        );
    }

    public String Version() {
        return "0.1.0";
    }
}
```

### Block Class Example

```java
package net.minecraft.src;

public class BlockStorageController extends Block {

    public BlockStorageController(int id, int textureIndex) {
        super(id, textureIndex, Material.iron);
    }

    // Override methods as needed:
    // - blockActivated() for GUI opening
    // - onBlockPlacedBy() for orientation
    // - getBlockEntity() for TileEntity binding
}
```

### Key Classes to Reference (from decompiled source)

When you have the RetroMCP workspace set up, examine these classes:

| Class | Purpose |
|-------|---------|
| `Block.java` | Base class for all blocks, registration arrays |
| `Item.java` | Base class for all items |
| `TileEntity.java` | Base for blocks with persistent data/logic |
| `Container.java` | Inventory slot management for GUIs |
| `GuiContainer.java` | GUI rendering for containers |
| `IInventory.java` | Inventory interface |
| `BaseMod.java` | ModLoader base class for mods |
| `ModLoader.java` | Static methods for registration |
| `EntityPlayer.java` | Player class, inventory access |
| `World.java` | World interaction, block get/set |
| `NBTTagCompound.java` | Data serialization for TileEntities |

### Metadata Strategy for Block IDs

To conserve the limited 256 block IDs, use metadata (damage values) for variants:

```
ID 200: Storage Controller (no variants needed)
ID 201: Disk Drive (no variants needed)
ID 202: Terminal / Grid (metadata 0 = terminal, 1 = crafting terminal)
ID 203: Cables (metadata 0-15 = different cable types/colors)
ID 204: Machines (metadata 0 = enrichment, 1 = crusher, 2 = smelter, etc.)
ID 205: Pipes (metadata 0 = item pipe, 1 = fluid pipe, 2 = gas pipe)
ID 206: Generators (metadata 0 = basic, 1 = advanced)
ID 207: Battery (metadata 0 = basic, 1 = advanced)
```

**Target: 10-15 block IDs for the entire mod.**

---

## 5. Existing Prior Art

### RetroStorage

- **Repo:** https://github.com/MartinSVK12/retrostorage
- Digital storage system for b1.7.3, 1.2.5, and BTA 7.2
- Built for **Babric/StationAPI** (NOT ModLoader)
- ModLoader version exists but is very buggy (confirmed by community)
- Dev has no interest in maintaining ModLoader version
- **Use as reference** for storage network logic adapted to b1.7.3 limitations

### Refined Storage 2

- **Repo:** https://github.com/refinedmods/refinedstorage2
- Modern rewrite with clean architecture and separated core API
- MIT license
- **Use as architectural reference** for storage network design

### Applied Energistics 2

- **Repo:** https://github.com/AppliedEnergistics/Applied-Energistics-2
- LGPLv3 license
- More complex (channels, P2P tunnels) — we're going simplified (no channels)

---

## 6. Community Context

### Discord Servers

- **Mango Pack Discord** — LowMango's server, `#mangopack-addons` channel for posting mods
- **Modification Station** — Main community for b1.7.3 modding, more StationAPI-focused

### Key People

| Person | Role |
|--------|------|
| **LowMango** | Mango Pack creator, approves addons |
| **dianaisnthere** | Community member, confirmed Forge 1.0.6 as the toolchain |
| **Dragon** | Confirmed RetroStorage ModLoader version is buggy |
| **Farn** | Active addon creator, has a mod collection repo on GitHub |
| **VasekCZ230** | Active player, interested in tech mods |
| **Modification Station Gas Station** | RetroMCP expert, answers technical questions |
| **Blatherskite** | Also learning to mod with RetroMCP |

### Community Approval

LowMango gave explicit approval for the project:
> "That sounds awesome! If you feel like making anything you are more than welcome to (-:"
> "New features can be downloaded from mangopack-addons which you could post here."

---

## 7. Project Structure (Planned)

```
net/minecraft/src/
├── mod_RetroNism.java              # Main mod class (extends BaseMod)
│
├── retronism/
│   ├── core/
│   │   ├── EnergyNetwork.java      # Energy system
│   │   ├── NetworkNode.java        # Base for networked blocks
│   │   └── RetroNismConfig.java    # ID configuration
│   │
│   ├── storage/
│   │   ├── BlockStorageController.java
│   │   ├── BlockDiskDrive.java
│   │   ├── BlockTerminal.java
│   │   ├── TileStorageController.java
│   │   ├── TileDiskDrive.java
│   │   ├── TileTerminal.java
│   │   ├── ContainerTerminal.java
│   │   ├── GuiTerminal.java
│   │   ├── StorageDisk.java        # Item
│   │   └── StorageNetwork.java     # Core network logic
│   │
│   ├── machines/
│   │   ├── BlockMachine.java       # Single ID, metadata for type
│   │   ├── TileMachineBase.java
│   │   ├── TileEnrichment.java
│   │   ├── TileCrusher.java
│   │   └── TileSmelter.java
│   │
│   └── transport/
│       ├── BlockCable.java         # Single ID, metadata for type
│       ├── BlockPipe.java
│       ├── TileCable.java
│       └── TilePipe.java
```

> **Note:** In ModLoader/Forge 1.0.6, all classes must be in `net.minecraft.src` package or the mod loader won't find them. The sub-package structure above is aspirational — in practice, all classes may need to be in `net.minecraft.src` directly. Verify this once the workspace is set up.

---

## 8. Development Checklist

### Phase 0 — Proof of Concept

- [ ] Install JDK 8 (Azul Zulu)
- [ ] Download RetroMCP-Java
- [ ] Run `setup` → Beta 1.7.3
- [ ] Download Forge 1.0.6
- [ ] Inject Forge into jars/minecraft.jar
- [ ] Run `decompile`
- [ ] Open project in IntelliJ/VSCode
- [ ] Verify vanilla + Forge compiles and runs
- [ ] Create `mod_RetroNism.java`
- [ ] Create `BlockStorageController.java`
- [ ] Create a placeholder texture (16x16 PNG)
- [ ] Register block with ID 200
- [ ] Run `build` → get .zip
- [ ] Test in Mango Pack
- [ ] First block appears in-game!

### Phase 1 — Storage Network (after Phase 0 works)

- [ ] TileEntity for Storage Controller
- [ ] Storage Disk item (multiple tiers: 1K, 4K, 16K, 64K)
- [ ] Disk Drive block + TileEntity
- [ ] Storage network logic (controller discovers connected drives)
- [ ] Terminal block + GUI (search, insert, extract items)
- [ ] Cable block (connects network components)
- [ ] Crafting recipes for all components

---

## 9. Useful Links

| Resource | URL |
|----------|-----|
| RetroMCP-Java | https://github.com/MCPHackers/RetroMCP-Java |
| RetroMCP Wiki | https://github.com/MCPHackers/RetroMCP-Java/wiki |
| Forge 1.0.6 | https://master.dl.sourceforge.net/project/minecraftforge/1.0.6/minecraftforge-client-1.0.6.zip |
| RetroStorage (reference) | https://github.com/MartinSVK12/retrostorage |
| Refined Storage 2 (reference) | https://github.com/refinedmods/refinedstorage2 |
| Farn's Mod Collection | https://github.com/FarnGitHub/Farn_Minecraft_Mod_Collection |
| StationAPI (future reference) | https://github.com/ModificationStation/StationAPI |
| ManyMoreBlocks (ID expansion) | https://modwiki.miraheze.org/wiki/ManyMoreBlocks |
| MC Archive (mod downloads) | https://mcarchive.net |

---

## 10. Notes & Gotchas

1. **Java 8 only** — ModLoader crashes on Java 17+ due to reflection changes
2. **256 block ID limit** — abuse metadata, plan IDs carefully, document everything
3. **No string block IDs** — everything is numeric, conflicts are common
4. **terrain.png atlas** — only 256 texture slots, ModLoader can add more via overrides
5. **TileEntity names** — must be registered with unique string IDs, keep them namespaced (e.g., "RetroNismController")
6. **Forge 1.0.6 is minimal** — no capabilities, no FluidRegistry, no ore dictionary. Everything custom.
7. **Test frequently** — build and test in-game after every change, b1.7.3 errors are often cryptic
8. **Backup worlds** — ID changes can corrupt saves
9. **Reobfuscation** — `build` command handles this automatically in RetroMCP
10. **Package constraint** — ModLoader may require classes in `net.minecraft.src`. Verify.

---

*Last updated: March 6, 2026*
*Author: Lucas (tinoco @ aerocoding.dev)*
