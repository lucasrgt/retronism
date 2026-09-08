package net.minecraft.src;

import java.io.File;
import net.minecraft.client.Minecraft;
import refinery.*;

/** Raw blocks become a multiblock only after native mouse input, never a direct formation call. */
public final class RefineryClickProof {
    private final File root;
    private final RefineryWindowInput input;
    private final RefineryFixture fixture;
    private final int rotations;
    private int facing,age,total,initialClicks,formedAt=-1;
    public RefineryClickProof(Minecraft client,File directory) throws Exception {
        root=directory; input=new RefineryWindowInput(root);
        int x=((int)client.thePlayer.posX>>4)*16,z=((int)client.thePlayer.posZ>>4)*16;
        fixture=new RefineryFixture(client.theWorld,x,z);
        rotations=Integer.parseInt(System.getenv("REFINERY_NETWORK_ROTATIONS"));
    }
    public boolean tick(Minecraft client) throws Exception {
        if(age==0) {
            initialClicks=input.rightClicks(); formedAt=-1;
            for(int x=-10;x<15;x++) for(int z=-10;z<15;z++) {
                fixture.world.setBlockWithNotify(fixture.ox+x,99,fixture.oz+z,Block.stone.blockID);
                for(int y=100;y<109;y++) fixture.world.setBlockWithNotify(fixture.ox+x,y,fixture.oz+z,0);
            }
            for(Object value:fixture.world.loadedEntityList) if(value instanceof EntityItem) ((EntityItem)value).setEntityDead();
            fixture.construct(facing); assertRaw(false);
            client.theWorld.setWorldTime(6000);
            client.gameSettings.thirdPersonView=false; client.gameSettings.hideGUI=false;
            client.thePlayer.inventory.setInventorySlotContents(0,new ItemStack(Item.coal,1));
            client.thePlayer.inventory.setInventorySlotContents(1,new ItemStack(Retronism_Registry.wrench,1));
            client.thePlayer.inventory.currentItem=0;
            input.focus(client);
        }
        holdAim(client);
        if(age==3) input.selectWrench(facing,total);
        if(age==7) {
            assertRaw(false); assertAim(client); assertWrench(client);
            check(input.rightClicks()==initialClicks,"fresh assembly has received no mouse click");
            snapshot(client,"before");
        }
        if(age==8) {
            assertAim(client); input.rightClick(client,facing,total,"first click on fresh assembly forms refinery");
        }
        if(age>=9 && age<=13 && formedAt<0) {
            TileEntity value=fixture.world.getBlockTileEntity(fixture.wx(2,2,facing),101,fixture.wz(2,2,facing));
            if(value instanceof RefineryTile && Structure.intact((RefineryTile)value)) formedAt=total;
        }
        if(age==13) {
            check(input.rightClicks()-initialClicks==1,"formation receives exactly one mouse press-release pulse");
            check(!org.lwjgl.input.Mouse.isButtonDown(1),"right button is released, preventing held-button repeat");
            check(formedAt>=0,"first click forms a fresh assembly without an earlier invalid click");
            RefineryTile master=fixture.tile(2,1,2,facing);
            check(master!=null && Structure.intact(master),"native right click formed and linked the entire multiblock");
            for(int y=0;y<3;y++) for(int z=0;z<3;z++) for(int x=0;x<5;x++)
                check(fixture.world.getBlockId(fixture.wx(x,z,facing),100+y,fixture.wz(x,z,facing))==Structure.PART,"all 45 raw/air cells converted after native click");
            snapshot(client,"after");
            System.out.println("RETRONISM_FIRST_CLICK_PASS facing="+facing+" clicks=1 held=false sentTick="+(total-5)+" formedTick="+formedAt);
            System.out.println("RETRONISM_NATIVE_FORMATION_PASS facing="+facing+" rawComponents=34 formedCells=45 key=2 mouse=right");
        }
        // Negative cases follow a separate raw rebuild; they cannot prepare the successful click.
        if(age==15) { fixture.construct(facing); assertRaw(false); client.thePlayer.inventory.currentItem=0; }
        if(age==18) { assertAim(client); input.rightClick(client,facing,total,"no formation with coal"); }
        if(age==22) {
            assertRaw(false);
            fixture.world.setBlockWithNotify(fixture.wx(0,0,facing),100,fixture.wz(0,0,facing),Block.dirt.blockID);
            input.selectWrench(facing,total);
        }
        if(age==26) { assertWrench(client); assertAim(client); input.rightClick(client,facing,total,"no formation with wrong construction block"); }
        if(age==28) assertRaw(true);
        total++;
        if(++age==29) {
            if(++facing==rotations) return true;
            age=0;
        }
        return false;
    }
    public int ticks() { return total; }
    private void assertWrench(Minecraft client) {
        check(client.thePlayer.inventory.currentItem==1 && client.thePlayer.inventory.getCurrentItem().itemID==Retronism_Registry.wrench.shiftedIndex,"native hotbar key equips Retronism wrench");
    }
    private void holdAim(Minecraft client) {
        double x=2,z=6.1;
        for(int i=0;i<facing;i++) { double old=x; x=-z; z=old; }
        client.thePlayer.setPositionAndRotation(fixture.ox+.5+x,101.62,fixture.oz+.5+z,180+90*facing,1.91f);
        client.thePlayer.prevRotationYaw=client.thePlayer.rotationYaw;
        client.thePlayer.prevRotationPitch=client.thePlayer.rotationPitch;
        client.thePlayer.motionX=client.thePlayer.motionY=client.thePlayer.motionZ=0;
    }
    private void assertAim(Minecraft client) {
        MovingObjectPosition hit=client.objectMouseOver;
        check(hit!=null && hit.typeOfHit==EnumMovingObjectType.TILE && hit.blockX==fixture.wx(2,2,facing)
            && hit.blockY==101 && hit.blockZ==fixture.wz(2,2,facing) && hit.sideHit==Structure.side(3,facing),
            "actual client crosshair targets the trigger's front face");
    }
    private void assertRaw(boolean wrong) {
        for(int y=0;y<3;y++) for(int z=0;z<3;z++) for(int x=0;x<5;x++) {
            int expected=wrong && x==0 && y==0 && z==0 ? Block.dirt.blockID : Structure.original(x,y,z);
            check(fixture.world.getBlockId(fixture.wx(x,z,facing),100+y,fixture.wz(x,z,facing))==expected,"raw construction unchanged before valid native click");
        }
    }
    private void snapshot(Minecraft client,String stage) {
        File directory=new File(root,"formation/facing-"+facing+"/"+stage); directory.mkdirs();
        System.out.println(ScreenShotHelper.saveScreenshot(directory,client.displayWidth,client.displayHeight));
    }
    private static void check(boolean value,String detail) { RefineryFixture.check(value,detail); }
}
