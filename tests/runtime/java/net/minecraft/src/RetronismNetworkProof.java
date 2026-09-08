package net.minecraft.src;

import java.io.File;
import net.minecraft.client.Minecraft;
import refinery.*;

/** Runs the installed Retronism production transport across four rotated real-world assemblies. */
public final class RetronismNetworkProof {
    private final File root;
    private final int x,z;
    private int age,rotation,disconnectedEnergy;
    private RetronismNetworkScene scene;
    private final int rotations = Integer.parseInt(System.getenv("REFINERY_NETWORK_ROTATIONS"));
    public RetronismNetworkProof(Minecraft client,File directory,int ox,int oz) { root=directory; x=ox; z=oz; }
    public boolean tick(Minecraft client) throws Exception {
        if(age==0) {
            scene=new RetronismNetworkScene(client.theWorld,x,z,rotation,true);
            scene.connections();
            client.theWorld.setWorldTime(6000);
        }
        if(age==12) {
            check(scene.count(scene.pipes[0],Block.dirt.blockID)==1 && scene.master.count(0)==0,"wrong item remains in inlet pipe");
            scene.left.setInventorySlotContents(1,scene.pipes[0].decrStackSize(0,1));
            for(int i=0;i<2;i++) {
                scene.pipes[i].setFilterSlot(0,new ItemStack(Item.seeds,1));
                scene.pipes[i].setWhitelist(true);
            }
        }
        if(age==25) {
            check(scene.master.energy==0 && scene.master.progress==0 && scene.master.count(2)==0,"real unpowered network cannot process");
            scene.generator.setInventorySlotContents(0,new ItemStack(Item.coal,1));
        }
        if(age==70) {
            scene.conserve(4);
            int energy=scene.energy(), progress=scene.master.progress;
            check(progress>0,"disk reload happens during a recipe");
            client.theWorld.saveWorld(true,null);
            World old=client.theWorld;
            client.changeWorld1(null);
            client.startWorld(System.getProperty("worldline.legacy.testkit.world"),"Worldline TestKit",173L);
            client.displayGuiScreen(null);
            scene=new RetronismNetworkScene(client.theWorld,x,z,rotation,false);
            check(client.theWorld!=old && scene.master.progress==progress && scene.energy()==energy,"disk reload preserves entire active network");
            scene.connections(); scene.conserve(4);
        }
        if(age==140) {
            scene.conserve(4);
            check(scene.items(Retronism_Refinery.FUEL.shiftedIndex)==4 && scene.count(scene.output,Retronism_Refinery.FUEL.shiftedIndex)==0,"full destination retains all products in machine and pipes");
            for(int i=0;i<scene.output.getSizeInventory();i++) scene.output.setInventorySlotContents(i,null);
        }
        if(age==175) {
            scene.conserve(4);
            check(scene.count(scene.output,Retronism_Refinery.FUEL.shiftedIndex)==4,"four products arrive in output chest through both pipes");
            check(scene.master.count(0)==0 && scene.master.count(1)==0 && scene.master.count(2)==0,"machine drained only through valid ports");
            disconnectedEnergy=scene.master.energy;
            scene.place(2,1,-1,0); scene.cables[0]=null;
            scene.left.setInventorySlotContents(0,new ItemStack(Item.seeds,1));
            scene.right.setInventorySlotContents(0,new ItemStack(Item.sugar,1));
        }
        if(age==235) {
            scene.conserve(5);
            check(scene.count(scene.output,Retronism_Refinery.FUEL.shiftedIndex)==5,"disconnected machine uses only buffered energy; chest="+scene.count(scene.output,Retronism_Refinery.FUEL.shiftedIndex)+" progress="+scene.master.progress);
            check(scene.master.energy==disconnectedEnergy-160,"removed cable prevents all further RN supply");
            scene.place(2,1,-1,Retronism_Registry.cableBlock.blockID);
            scene.cables[0]=(Retronism_TileCable)scene.tile(2,1,-1);
            scene.configure(scene.cables[0],aero.machineapi.Aero_SideConfig.TYPE_ENERGY,2,3);
        }
        if(age==250) {
            check(scene.master.energy>disconnectedEnergy-160,"replacement cable restores supply");
            System.out.println("RETRONISM_NETWORK_PASS orientation="+rotation+" output="+scene.count(scene.output,Retronism_Refinery.FUEL.shiftedIndex));
            if(rotation==rotations-1) {
                // Save an operating north-facing workshop for the player, with spare inputs.
                scene=new RetronismNetworkScene(client.theWorld,x,z,0,true);
                scene.pipes[0].decrStackSize(0,1);
                for(int i=0;i<scene.output.getSizeInventory();i++) scene.output.setInventorySlotContents(i,null);
                scene.left.setInventorySlotContents(0,new ItemStack(Item.seeds,64));
                scene.right.setInventorySlotContents(0,new ItemStack(Item.sugar,64));
                scene.generator.setInventorySlotContents(0,new ItemStack(Item.coal,8));
                client.thePlayer.inventory.setInventorySlotContents(0,new ItemStack(Retronism_Refinery.WRENCH,1));
                client.thePlayer.inventory.setInventorySlotContents(1,new ItemStack(Retronism_Registry.cableBlock,16));
                client.thePlayer.inventory.setInventorySlotContents(2,new ItemStack(Retronism_Registry.itemPipeBlock,16));
                client.thePlayer.inventory.setInventorySlotContents(3,new ItemStack(Item.coal,64));
            } else { rotation++; age=-1; }
        }
        if(age==325) client.gameSettings.hideGUI=true;
        if(age==330) {
            check(scene.count(scene.output,Retronism_Refinery.FUEL.shiftedIndex)>0,"saved demo line produces through actual pipes");
            RefineryRenderProof.aeroCache();
            client.thePlayer.inventory.setInventorySlotContents(0,new ItemStack(Retronism_Refinery.WRENCH,1));
            client.thePlayer.inventory.setInventorySlotContents(1,new ItemStack(Retronism_Registry.cableBlock,16));
            client.thePlayer.inventory.setInventorySlotContents(2,new ItemStack(Retronism_Registry.itemPipeBlock,16));
            client.thePlayer.inventory.setInventorySlotContents(3,new ItemStack(Item.coal,64));
            client.theWorld.saveWorld(true,null);
            System.out.println(ScreenShotHelper.saveScreenshot(root,client.displayWidth,client.displayHeight));
        }
        if(age==335) {
            System.out.println(ScreenShotHelper.saveScreenshot(root,client.displayWidth,client.displayHeight));
            client.gameSettings.hideGUI=false;
            return true;
        }
        if(age<330) client.thePlayer.setPositionAndRotation(x+9,103.5,z+8,135,20);
        else client.thePlayer.setPositionAndRotation(x-4,103.5,z-6,-40,20);
        client.thePlayer.motionX=client.thePlayer.motionY=client.thePlayer.motionZ=0;
        age++; return false;
    }
    private static void check(boolean value,String message) { RefineryFixture.check(value,message); }
}
