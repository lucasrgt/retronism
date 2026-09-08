package net.minecraft.src;

import refinery.*;

/** Tests call the production API against the actual loaded World; no surrogate world implementation. */
public final class RefineryFixture {
    public final World world;
    public final int ox, oy = 100, oz;
    public RefineryFixture(World value, int x, int z) { world = value; ox = x; oz = z; }
    public void construct(int facing) {
        for (int y = 0; y < 3; y++) for (int z = 0; z < 3; z++) for (int x = 0; x < 5; x++)
            world.setBlockAndMetadataWithNotify(wx(x,z,facing), oy+y, wz(x,z,facing), Structure.original(x,y,z), 0);
    }
    public int wx(int x, int z, int facing) { return ox + Structure.dx(x,z,facing); }
    public int wz(int x, int z, int facing) { return oz + Structure.dz(x,z,facing); }
    public RefineryTile tile(int x, int y, int z, int facing) {
        return (RefineryTile)world.getBlockTileEntity(wx(x,z,facing), oy+y, wz(x,z,facing));
    }
    public RefineryTile form(int facing) {
        check(Retronism_Registry.wrench.onItemUse(new ItemStack(Retronism_Registry.wrench,1),
            ModLoader.getMinecraftInstance().thePlayer,world,wx(2,2,facing),oy+1,wz(2,2,facing),Structure.side(3,facing)),
            "formation through Retronism wrench " + facing);
        RefineryTile master = tile(2,1,2,facing);
        check(Structure.intact(master), "all 45 parts linked " + facing);
        return master;
    }
    public void formationAndPorts() {
        for (int f = 0; f < 4; f++) {
            construct(f);
            world.setBlockWithNotify(wx(0,0,f), oy, wz(0,0,f), Block.dirt.blockID);
            check(!Structure.form(world, wx(2,2,f), oy+1, wz(2,2,f), f), "reject wrong component " + f);
            check(world.getBlockId(wx(1,0,f),oy,wz(1,0,f)) == Structure.original(1,0,0), "failed formation is atomic");
            world.setBlockWithNotify(wx(0,0,f), oy, wz(0,0,f), Structure.original(0,0,0));
            world.setBlockWithNotify(wx(2,1,f), oy+1, wz(2,1,f), Block.stone.blockID);
            check(!Structure.form(world, wx(2,2,f), oy+1, wz(2,2,f), f), "reject obstructed air " + f);
            world.setBlockWithNotify(wx(2,1,f), oy+1, wz(2,1,f), 0);
            RefineryTile master = form(f);
            RefineryRenderProof.portsAndCollisions(this,f);
            int[] kinds = new int[4];
            for (int y=0;y<3;y++) for(int z=0;z<3;z++) for(int x=0;x<5;x++) for(int side=0;side<6;side++) {
                int kind = Ports.kind(tile(x,y,z,f), side);
                if (kind >= 0) kinds[kind]++;
                if (kind != 3) check(Ports.insert(tile(x,y,z,f), side, new ItemStack(Item.redstone,1)) == 0, "wrong energy cell or face");
            }
            for(int count:kinds) check(count==1,"exactly one position-face per port");
            check(Ports.kind(tile(0,0,1,f),Structure.side(4,f))==0,"left port rotates");
            check(Ports.kind(tile(4,0,1,f),Structure.side(5,f))==1,"right port rotates");
            check(Ports.kind(tile(2,0,2,f),Structure.side(3,f))==2,"outlet rotates");
            check(Ports.kind(tile(2,1,0,f),Structure.side(2,f))==3,"energy rotates");
            check(Ports.insert(tile(0,0,1,f),Structure.side(4,f),new ItemStack(Item.sugar,1))==0,"wrong item rejected");
            check(Ports.extract(tile(0,0,1,f),Structure.side(4,f),1)==null,"cannot extract inputs");
            Structure.dismantle(master,Integer.MIN_VALUE,-1,Integer.MIN_VALUE);
            restored(f, -1, -1, -1);
            for(int y=0;y<3;y++) for(int z=-5;z<=5;z++) for(int x=-5;x<=5;x++)
                world.setBlockWithNotify(ox+x,oy+y,oz+z,0);
        }
    }
    public void restored(int facing, int bx, int by, int bz) {
        for (int y=0;y<3;y++) for(int z=0;z<3;z++) for(int x=0;x<5;x++) {
            if (x==bx && y==by && z==bz) continue;
            check(world.getBlockId(wx(x,z,facing),oy+y,wz(x,z,facing))==Structure.original(x,y,z),"restore component/air");
        }
    }
    public void feed(int seeds, int sugar, int charges) {
        check(Ports.insert(tile(0,0,1,0),4,new ItemStack(Item.seeds,seeds))==seeds,"left input inserted");
        check(Ports.insert(tile(4,0,1,0),5,new ItemStack(Item.sugar,sugar))==sugar,"right input inserted");
        for(int i=0;i<charges;i++) check(Ports.receiveEnergy(tile(2,1,0,0),2,160)==160,"energy inserted");
    }
    public void breakMaster() {
        construct(0); RefineryTile master=form(0);
        master.setInventorySlotContents(2,new ItemStack(Retronism_Refinery.FUEL,3));
        int before=fuelItems();
        world.setBlockWithNotify(wx(2,2,0),oy+1,wz(2,2,0),0);
        restored(0,2,1,2);
        check(fuelItems()-before==3,"breaking the master drops its inventory exactly once");
        world.setBlockWithNotify(wx(2,2,0),oy+1,wz(2,2,0),0);
        check(fuelItems()-before==3,"repeated removal does not duplicate drops");
    }
    private int fuelItems() {
        int count=0;
        for(Object value:world.loadedEntityList) if(value instanceof EntityItem) {
            EntityItem item=(EntityItem)value;
            if(!item.isDead && item.item.itemID==Retronism_Refinery.FUEL.shiftedIndex) count+=item.item.stackSize;
        }
        return count;
    }
    public static void check(boolean value, String message) { if(!value) throw new AssertionError(message); }
}
