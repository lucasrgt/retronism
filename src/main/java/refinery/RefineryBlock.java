package refinery;

import java.util.Random;
import java.util.ArrayList;
import net.minecraft.src.*;

public final class RefineryBlock extends BlockContainer {
    public static int wrenchId;
    public RefineryBlock() { super(Structure.PART, 22, Material.iron); setHardness(3); setBlockName("refinery.formed"); }
    @Override protected TileEntity getBlockEntity() { return new RefineryTile(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return -1; }
    @Override public int idDropped(int metadata, Random random) { return 0; }
    @Override public void getCollidingBoundingBoxes(World world, int x, int y, int z, AxisAlignedBB query, ArrayList boxes) {
        TileEntity value = world.getBlockTileEntity(x, y, z);
        if (!(value instanceof RefineryTile) || !((RefineryTile)value).configured) return;
        for (float[] b : Geometry.boxes((RefineryTile)value)) {
            AxisAlignedBB box = AxisAlignedBB.getBoundingBoxFromPool(x+b[0], y+b[1], z+b[2], x+b[3], y+b[4], z+b[5]);
            if (query.intersectsWith(box)) boxes.add(box);
        }
    }
    @Override public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3D start, Vec3D end) {
        TileEntity value = world.getBlockTileEntity(x, y, z);
        if (!(value instanceof RefineryTile) || !((RefineryTile)value).configured) return null;
        MovingObjectPosition closest = null;
        try {
            for (float[] b : Geometry.boxes((RefineryTile)value)) {
                setBlockBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
                MovingObjectPosition hit = super.collisionRayTrace(world, x, y, z, start, end);
                if (hit != null && (closest == null || hit.hitVec.distanceTo(start) < closest.hitVec.distanceTo(start))) closest = hit;
            }
        } finally { setBlockBounds(0, 0, 0, 1, 1, 1); }
        return closest;
    }
    @Override public void onBlockRemoval(World world, int x, int y, int z) {
        TileEntity value = world.getBlockTileEntity(x, y, z);
        if (!world.multiplayerWorld && value instanceof RefineryTile) {
            RefineryTile tile = (RefineryTile) value;
            RefineryTile master = tile.master();
            if (master != null && !master.dismantling) {
                int original = Structure.original(tile.lx, tile.ly, tile.lz);
                Structure.dismantle(tile, x, y, z);
                if (original != 0) world.entityJoinedWorld(new EntityItem(world, x + .5, y + .5, z + .5, new ItemStack(original, 1, 0)));
            }
        }
        super.onBlockRemoval(world, x, y, z);
    }
    @Override public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
        ItemStack held = player.inventory.getCurrentItem();
        if (held != null && held.itemID == wrenchId) return false;
        TileEntity value = world.getBlockTileEntity(x, y, z);
        if (!(value instanceof RefineryTile) || world.multiplayerWorld) return false;
        RefineryTile part = (RefineryTile)value, master = part.master();
        MovingObjectPosition hit = player.rayTrace(6, 1);
        if (master == null || hit == null || hit.blockX != x || hit.blockY != y || hit.blockZ != z) return false;
        if (held == null) {
            ItemStack output = Ports.extract(part, hit.sideHit, 64);
            if (output != null) player.inventory.setInventorySlotContents(player.inventory.currentItem, output);
        } else {
            Ports.insert(part, hit.sideHit, held);
            if (held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
        }
        player.addChatMessage(master.status()); return true;
    }
}
