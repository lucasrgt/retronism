package refinery;

import net.minecraft.src.*;

/** Position AND outward face are required; these coordinates coincide with the model's bores. */
public final class Ports {
    private Ports() {}
    public static int kind(RefineryTile part, int face) {
        if (!part.configured || face < 0 || face > 5) return -1;
        if (part.lx == 0 && part.ly == 0 && part.lz == 1 && face == Structure.side(4, part.facing)) return 0;
        if (part.lx == 4 && part.ly == 0 && part.lz == 1 && face == Structure.side(5, part.facing)) return 1;
        if (part.lx == 2 && part.ly == 0 && part.lz == 2 && face == Structure.side(3, part.facing)) return 2;
        if (part.lx == 2 && part.ly == 1 && part.lz == 0 && face == Structure.side(2, part.facing)) return 3;
        return -1;
    }
    public static int insert(RefineryTile part, int face, ItemStack offered) {
        RefineryTile master = part.master(); int slot = kind(part, face);
        if (master == null || master.worldObj.multiplayerWorld || master.dismantling || !Structure.intact(master) || offered == null || offered.stackSize <= 0) return 0;
        if (slot != 0 && slot != 1) return 0;
        int accepted = slot == 0 ? Item.seeds.shiftedIndex : Item.sugar.shiftedIndex;
        if (offered.itemID != accepted) return 0;
        ItemStack stored = master.getStackInSlot(slot);
        int count = Math.min(offered.stackSize, 64 - master.count(slot));
        if (count <= 0) return 0;
        if (stored == null) master.setInventorySlotContents(slot, new ItemStack(accepted, count, 0)); else stored.stackSize += count;
        offered.stackSize -= count; master.onInventoryChanged(); return count;
    }
    public static int receiveEnergy(RefineryTile part, int face, int offered) {
        RefineryTile master = part.master();
        if (offered <= 0 || master == null || master.worldObj.multiplayerWorld || master.dismantling
                || kind(part, face) != 3 || !Structure.intact(master)) return 0;
        int accepted = Math.min(offered, 1600 - master.energy);
        master.energy += accepted;
        if (accepted != 0) master.onInventoryChanged();
        return accepted;
    }
    public static ItemStack extract(RefineryTile part, int face, int amount) {
        RefineryTile master = part.master();
        return master != null && !master.worldObj.multiplayerWorld && !master.dismantling && Structure.intact(master) && kind(part, face) == 2
                ? master.decrStackSize(2, amount) : null;
    }
}
