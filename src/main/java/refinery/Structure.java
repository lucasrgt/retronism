package refinery;

import net.minecraft.src.*;

/** Exact construction recipe, expressed in local coordinates; one unit is one block. */
public final class Structure {
    public static final String[][] LAYERS = {
        {"SSLSS", "PPPPP", "SSHSS"},
        {"IILII", "II.II", "..H.R"},
        {"II.II", "II.II", "....."}
    };
    public static final String PALETTE = "SLPHIR";
    public static final int PART = 246;
    private Structure() {}
    public static int original(int x, int y, int z) {
        int index = PALETTE.indexOf(LAYERS[y][z].charAt(x));
        return index < 0 ? 0 : 240 + index;
    }
    public static int dx(int x, int z, int facing) {
        switch (facing & 3) { case 1: return -z; case 2: return -x; case 3: return z; default: return x; }
    }
    public static int dz(int x, int z, int facing) {
        switch (facing & 3) { case 1: return x; case 2: return -z; case 3: return -x; default: return z; }
    }
    public static int side(int local, int facing) {
        int[] directions = {3, 4, 2, 5};
        if (local < 2) return local;
        for (int i = 0; i < 4; i++) if (directions[i] == local) return directions[(i + facing) & 3];
        throw new IllegalArgumentException("side");
    }
    public static boolean form(World world, int mx, int my, int mz, int facing) {
        if (world.multiplayerWorld || my < 1 || my > 125) return false;
        int ox = mx - dx(2, 2, facing), oy = my - 1, oz = mz - dz(2, 2, facing);
        for (int y = 0; y < 3; y++) for (int z = 0; z < 3; z++) for (int x = 0; x < 5; x++) {
            int wx = ox + dx(x, z, facing), wz = oz + dz(x, z, facing);
            if (!world.blockExists(wx, oy + y, wz) || world.getBlockId(wx, oy + y, wz) != original(x, y, z)) return false;
        }
        // Air cells are reserved too: each visible piece has a selectable, collidable world cell.
        for (int y = 0; y < 3; y++) for (int z = 0; z < 3; z++) for (int x = 0; x < 5; x++) {
            int wx = ox + dx(x, z, facing), wz = oz + dz(x, z, facing);
            world.setBlockAndMetadata(wx, oy + y, wz, PART, facing);
            RefineryTile tile = (RefineryTile) world.getBlockTileEntity(wx, oy + y, wz);
            tile.configure(mx, my, mz, x, y, z, facing);
        }
        world.markBlocksDirty(ox - 4, oy, oz - 4, ox + 5, oy + 3, oz + 5);
        return true;
    }
    public static boolean intact(RefineryTile master) {
        int ox = master.xCoord - dx(2, 2, master.facing), oz = master.zCoord - dz(2, 2, master.facing);
        for (int y = 0; y < 3; y++) for (int z = 0; z < 3; z++) for (int x = 0; x < 5; x++) {
            int wx = ox + dx(x, z, master.facing), wz = oz + dz(x, z, master.facing);
            if (!master.worldObj.blockExists(wx, master.yCoord - 1 + y, wz)) return false;
            TileEntity value = master.worldObj.getBlockTileEntity(wx, master.yCoord - 1 + y, wz);
            if (!(value instanceof RefineryTile)) return false;
            RefineryTile tile = (RefineryTile) value;
            if (tile.mx != master.xCoord || tile.my != master.yCoord || tile.mz != master.zCoord
                    || tile.lx != x || tile.ly != y || tile.lz != z || tile.facing != master.facing) return false;
        }
        return true;
    }
    public static void dismantle(RefineryTile part, int brokenX, int brokenY, int brokenZ) {
        RefineryTile master = part.master();
        if (master == null || master.dismantling) return;
        master.dismantling = true;
        master.dropInventory();
        int ox = master.xCoord - dx(2, 2, master.facing), oz = master.zCoord - dz(2, 2, master.facing);
        for (int y = 0; y < 3; y++) for (int z = 0; z < 3; z++) for (int x = 0; x < 5; x++) {
            int wx = ox + dx(x, z, master.facing), wy = master.yCoord - 1 + y, wz = oz + dz(x, z, master.facing);
            if (wx == brokenX && wy == brokenY && wz == brokenZ) continue;
            if (master.worldObj.getBlockId(wx, wy, wz) == PART) master.worldObj.setBlockWithNotify(wx, wy, wz, original(x, y, z));
        }
    }
}
