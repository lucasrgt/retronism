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
    private int facing,age,total;
    public RefineryClickProof(Minecraft client,File directory) throws Exception {
        root=directory; input=new RefineryWindowInput(root);
        int x=((int)client.thePlayer.posX>>4)*16,z=((int)client.thePlayer.posZ>>4)*16;
        fixture=new RefineryFixture(client.theWorld,x,z);
        rotations=Integer.parseInt(System.getenv("REFINERY_NETWORK_ROTATIONS"));
    }
    public boolean tick(Minecraft client) throws Exception {
        if(age==0) {
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
        if(age==3) { assertAim(client); input.rightClick(client,facing,total,"no formation with coal"); }
        if(age==8) {
            assertRaw(false);
            fixture.world.setBlockWithNotify(fixture.wx(0,0,facing),100,fixture.wz(0,0,facing),Block.dirt.blockID);
            input.selectWrench(facing,total);
        }
        if(age==12) {
            check(client.thePlayer.inventory.currentItem==1 && client.thePlayer.inventory.getCurrentItem().itemID==Retronism_Registry.wrench.shiftedIndex,"native hotbar key equips Retronism wrench");
            assertAim(client); input.rightClick(client,facing,total,"no formation with wrong construction block");
        }
        if(age==17) {
            assertRaw(true);
            fixture.world.setBlockWithNotify(fixture.wx(0,0,facing),100,fixture.wz(0,0,facing),Structure.original(0,0,0));
        }
        if(age==20) { assertRaw(false); assertAim(client); snapshot(client,"before"); }
        if(age==21) { assertAim(client); input.rightClick(client,facing,total,"raw blocks become refinery"); }
        if(age==26) {
            RefineryTile master=fixture.tile(2,1,2,facing);
            check(master!=null && Structure.intact(master),"native right click formed and linked the entire multiblock");
            for(int y=0;y<3;y++) for(int z=0;z<3;z++) for(int x=0;x<5;x++)
                check(fixture.world.getBlockId(fixture.wx(x,z,facing),100+y,fixture.wz(x,z,facing))==Structure.PART,"all 45 raw/air cells converted after native click");
            snapshot(client,"after");
            System.out.println("RETRONISM_NATIVE_FORMATION_PASS facing="+facing+" rawComponents=34 formedCells=45 key=2 mouse=right");
        }
        total++;
        if(++age==29) {
            if(++facing==rotations) return true;
            age=0;
        }
        return false;
    }
    public int ticks() { return total; }
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
