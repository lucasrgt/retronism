package refinery;

import net.minecraft.src.*;

public final class Wrench extends Item {
    public Wrench(int id) { super(id); maxStackSize = 1; setIconIndex(96); setItemName("refinery.wrench"); }
    @Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int face) {
        return use(player, world, x, y, z, face);
    }
    public static boolean use(EntityPlayer player, World world, int x, int y, int z, int face) {
        if (!(world.getBlockTileEntity(x,y,z) instanceof RefineryTile) && world.getBlockId(x,y,z) != Structure.original(2,1,2)) return false;
        if (world.multiplayerWorld) return false;
        TileEntity value = world.getBlockTileEntity(x, y, z);
        if (value instanceof RefineryTile) {
            RefineryTile tile = (RefineryTile)value, master = tile.master();
            if (master == null) return false;
            if (player.isSneaking()) Structure.dismantle(tile, Integer.MIN_VALUE, -1, Integer.MIN_VALUE);
            else player.addChatMessage(master.status());
            return true;
        }
        for (int direction = 0; direction < 4; direction++) if (Structure.side(3, direction) == face) {
            boolean formed = Structure.form(world, x, y, z, direction);
            player.addChatMessage(formed ? "Refinery formed (5 x 3 x 3)." : "Refinery recipe does not match; check every block and air cell.");
            return formed;
        }
        return false;
    }
}
