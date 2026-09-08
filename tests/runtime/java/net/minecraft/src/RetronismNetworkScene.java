package net.minecraft.src;

import aero.machineapi.*;
import refinery.*;

/** Only arranges fixtures; every subsequent transfer is performed by production tile ticks. */
public final class RetronismNetworkScene {
    public final RefineryFixture fixture;
    public final int facing;
    public RefineryTile master;
    public TileEntityChest left, right, output;
    public Retronism_TileGenerator generator;
    public final Retronism_TileItemPipe[] pipes = new Retronism_TileItemPipe[6];
    public final Retronism_TileCable[] cables = new Retronism_TileCable[2];
    public RetronismNetworkScene(World world, int x, int z, int rotation, boolean create) {
        fixture = new RefineryFixture(world, x, z); facing = rotation;
        if (create) {
            for (int a=-10;a<=14;a++) for(int b=-10;b<=14;b++) {
                world.setBlockWithNotify(x+a,99,z+b,Block.stone.blockID);
                for(int y=100;y<109;y++) world.setBlockWithNotify(x+a,y,z+b,0);
            }
            for(Object value:world.loadedEntityList) if(value instanceof EntityItem) ((EntityItem)value).setEntityDead();
            fixture.construct(facing); master=fixture.form(facing);
            place(-3,0,1,Block.chest.blockID); place(7,0,1,Block.chest.blockID); place(2,0,5,Block.chest.blockID);
            int id=Retronism_Registry.itemPipeBlock.blockID;
            place(-1,0,1,id); place(-2,0,1,id); place(5,0,1,id); place(6,0,1,id); place(2,0,3,id); place(2,0,4,id);
            place(2,1,-1,Retronism_Registry.cableBlock.blockID); place(2,1,-2,Retronism_Registry.cableBlock.blockID);
            place(2,0,-3,Block.stone.blockID); place(2,1,-3,Retronism_Registry.generatorBlock.blockID);
        }
        master=fixture.tile(2,1,2,facing);
        left=(TileEntityChest)tile(-3,0,1); right=(TileEntityChest)tile(7,0,1); output=(TileEntityChest)tile(2,0,5);
        generator=(Retronism_TileGenerator)tile(2,1,-3);
        pipes[0]=(Retronism_TileItemPipe)tile(-1,0,1); pipes[1]=(Retronism_TileItemPipe)tile(-2,0,1);
        pipes[2]=(Retronism_TileItemPipe)tile(5,0,1); pipes[3]=(Retronism_TileItemPipe)tile(6,0,1);
        pipes[4]=(Retronism_TileItemPipe)tile(2,0,3); pipes[5]=(Retronism_TileItemPipe)tile(2,0,4);
        cables[0]=(Retronism_TileCable)tile(2,1,-1); cables[1]=(Retronism_TileCable)tile(2,1,-2);
        if(create) {
            for(int i=0;i<2;i++) configure(pipes[i],Aero_SideConfig.TYPE_ITEM,4,5);
            for(int i=2;i<4;i++) configure(pipes[i],Aero_SideConfig.TYPE_ITEM,5,4);
            for(int i=4;i<6;i++) configure(pipes[i],Aero_SideConfig.TYPE_ITEM,2,3);
            for(Retronism_TileCable cable:cables) configure(cable,Aero_SideConfig.TYPE_ENERGY,2,3);
            left.setInventorySlotContents(0,new ItemStack(Item.seeds,4));
            right.setInventorySlotContents(0,new ItemStack(Item.sugar,4));
            for(int i=0;i<output.getSizeInventory();i++) output.setInventorySlotContents(i,new ItemStack(Block.cobblestone,64));
            check(pipes[0].receiveItem(new ItemStack(Block.dirt,1),Structure.side(4,facing)),"wrong-item fixture inserted into actual pipe");
        }
    }
    public void configure(Aero_ISideConfigurable target,int type,int input,int output) {
        for(int side=0;side<6;side++) target.setSideMode(side,type,Aero_SideConfig.MODE_NONE);
        target.setSideMode(Structure.side(input,facing),type,Aero_SideConfig.MODE_INPUT);
        target.setSideMode(Structure.side(output,facing),type,Aero_SideConfig.MODE_OUTPUT);
    }
    public void place(int x,int y,int z,int id) { fixture.world.setBlockWithNotify(fixture.wx(x,z,facing),100+y,fixture.wz(x,z,facing),id); }
    public TileEntity tile(int x,int y,int z) { return fixture.world.getBlockTileEntity(fixture.wx(x,z,facing),100+y,fixture.wz(x,z,facing)); }
    public int count(IInventory inventory,int id) {
        int count=0;
        for(int slot=0;slot<inventory.getSizeInventory();slot++) {
            ItemStack stack=inventory.getStackInSlot(slot);
            if(stack!=null && stack.itemID==id) count+=stack.stackSize;
        }
        return count;
    }
    public int items(int id) {
        int value=count(master,id)+count(left,id)+count(right,id)+count(output,id);
        for(Retronism_TileItemPipe pipe:pipes) value+=count(pipe,id);
        return value;
    }
    public int energy() {
        int value=master.energy+generator.storedEnergy;
        for(Retronism_TileCable cable:cables) if(cable!=null) value+=cable.getStoredEnergy();
        return value;
    }
    public void conserve(int recipes) {
        int fuel=items(Retronism_Refinery.FUEL.shiftedIndex);
        check(items(Item.seeds.shiftedIndex)+fuel==recipes,"seed conservation orientation "+facing);
        check(items(Item.sugar.shiftedIndex)+fuel==recipes,"sugar conservation orientation "+facing);
        check(items(Block.dirt.blockID)==1,"rejected item retained exactly once");
        if(cables[0]!=null) check(energy()+fuel*160+master.progress*8==(1600-generator.burnTime)*32,"RN conservation across generator, cables and recipe");
    }
    public void connections() {
        Retronism_BlockCable cable=(Retronism_BlockCable)Retronism_Registry.cableBlock;
        Retronism_BlockItemPipe pipe=(Retronism_BlockItemPipe)Retronism_Registry.itemPipeBlock;
        check(cable.canConnectToSide(fixture.world,cables[0].xCoord,cables[0].yCoord,cables[0].zCoord,Structure.side(3,facing)),"cable visual connects to energy socket");
        check(pipe.canConnectToSide(fixture.world,pipes[0].xCoord,pipes[0].yCoord,pipes[0].zCoord,Structure.side(5,facing)),"left pipe visual connects to inlet");
        // A real cable against the bottom of the energy cell must not connect or transfer.
        place(2,-1,0,Retronism_Registry.cableBlock.blockID);
        Retronism_TileCable wrong=(Retronism_TileCable)tile(2,-1,0);
        check(!cable.canConnectToSide(fixture.world,wrong.xCoord,wrong.yCoord,wrong.zCoord,1),"wrong cell and face reject cable connector");
        place(2,-1,0,Block.stone.blockID);
        for(int y=0;y<3;y++) for(int z=0;z<3;z++) for(int x=0;x<5;x++) for(int side=0;side<6;side++) {
            RefineryTile part=fixture.tile(x,y,z,facing); int kind=Ports.kind(part,side);
            int energy=Aero_SideConfig.get(part.getSideConfig(),side,Aero_SideConfig.TYPE_ENERGY);
            int item=Aero_SideConfig.get(part.getSideConfig(),side,Aero_SideConfig.TYPE_ITEM);
            check((energy==Aero_SideConfig.MODE_INPUT)==(kind==3),"energy position and face rotate");
            check((item==Aero_SideConfig.MODE_INPUT)==(kind==0||kind==1),"item inlets rotate");
            check((item==Aero_SideConfig.MODE_OUTPUT)==(kind==2),"item outlet rotates");
        }
    }
    private static void check(boolean value,String detail) { RefineryFixture.check(value,detail); }
}
