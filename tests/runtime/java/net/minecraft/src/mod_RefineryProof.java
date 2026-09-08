package net.minecraft.src;

import java.io.*;
import java.util.Properties;
import net.minecraft.client.Minecraft;
import refinery.*;

/** Opt-in fixture only. This class is excluded from the product jar. */
public final class mod_RefineryProof extends BaseMod {
    private int ticks;
    private RefineryFixture fixture;
    private RefineryTile master;
    private boolean done;
    private RetronismNetworkProof network;
    private RefineryClickProof clickProof;
    private boolean clickComplete;
    private final File root;
    public mod_RefineryProof() {
        String path = System.getenv("REFINERY_PROOF_ROOT");
        root = path == null ? null : new File(path);
        if(root != null) {
            ModLoader.SetInGameHook(this,true,true);
            ModLoader.SetInGUIHook(this,true,false);
        }
    }
    @Override public String Version() { return "refinery-proof-v1"; }
    @Override public boolean OnTickInGUI(Minecraft client, GuiScreen screen) {
        if(done || root==null) return false;
        if(screen instanceof GuiIngameMenu && client.theWorld!=null) {
            // Beta pauses on window focus loss; restore this test client's focus without a click.
            System.out.println("REFINERY_PROOF_RESUME_PAUSE ticks="+ticks+" nativeComplete="+clickComplete);
            try { new RefineryWindowInput(root).focus(client); }
            catch(Exception error) { record("FAILED",error.toString()); done=true; }
        }
        return !done;
    }
    @Override public boolean OnTickInGame(Minecraft client) {
        if(done || root==null || client.theWorld==null) return !done;
        try {
            if(!clickComplete) {
                if(clickProof==null) clickProof=new RefineryClickProof(client,root);
                clickComplete=clickProof.tick(client);
                if(!clickComplete) return true;
            }
            step(client);
        }
        catch(Throwable error) {
            error.printStackTrace();
            record("FAILED", error.toString()); done=true;
        }
        return !done;
    }
    private void step(Minecraft client) throws Exception {
        if(ticks==0) {
            int x=((int)client.thePlayer.posX >> 4)*16, z=((int)client.thePlayer.posZ >> 4)*16;
            fixture=new RefineryFixture(client.theWorld,x,z);
            for(int a=-8;a<18;a++) for(int b=-8;b<18;b++) {
                client.theWorld.setBlockWithNotify(x+a,99,z+b,Block.stone.blockID);
                for(int y=100;y<110;y++) client.theWorld.setBlockWithNotify(x+a,y,z+b,0);
            }
            fixture.formationAndPorts();
            fixture.breakMaster();
            for(Object value:client.theWorld.loadedEntityList) if(value instanceof EntityItem) ((EntityItem)value).setEntityDead();
            for(int a=-8;a<18;a++) for(int b=-8;b<18;b++) for(int y=100;y<110;y++)
                client.theWorld.setBlockWithNotify(x+a,y,z+b,0);
            fixture.construct(0); master=fixture.form(0); fixture.feed(3,3,0);
            client.theWorld.setWorldTime(6000);
            client.gameSettings.renderDistance=1; client.gameSettings.thirdPersonView=false;
            client.thePlayer.setPositionAndRotation(x+9,104,z+10,135,18);
            client.thePlayer.motionX=client.thePlayer.motionY=client.thePlayer.motionZ=0;
            System.out.println("REFINERY_PROOF=structure-and-ports-pass");
        }
        if(ticks==5) {
            RefineryFixture.check(master.energy==0 && master.progress==0 && master.count(0)==3 && master.count(2)==0,"unpowered machine cannot process");
            fixture.feed(1,1,1);
        }
        if(ticks==15) {
            RefineryFixture.check(master.progress==10 && master.energy==80,"ten actual world ticks consume eighty energy");
            NBTTagCompound tag=new NBTTagCompound(); master.writeToNBT(tag);
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            CompressedStreamTools.writeGzippedCompoundToOutputStream(tag,bytes);
            NBTTagCompound decoded=CompressedStreamTools.func_1138_a(new ByteArrayInputStream(bytes.toByteArray()));
            RefineryTile restored=(RefineryTile)TileEntity.createAndLoadEntity(decoded);
            client.theWorld.removeBlockTileEntity(master.xCoord,master.yCoord,master.zCoord);
            client.theWorld.setBlockTileEntity(restored.xCoord,restored.yCoord,restored.zCoord,restored);
            master=restored;
            RefineryFixture.check(master.progress==10 && master.energy==80 && master.count(0)==4 && Structure.intact(master),"compressed NBT roundtrip");
            client.theWorld.saveWorld(true,null);
            int oldX=fixture.ox, oldZ=fixture.oz;
            World oldWorld=client.theWorld;
            client.changeWorld1(null);
            client.startWorld(System.getProperty("worldline.legacy.testkit.world"),"Worldline TestKit",173L);
            client.displayGuiScreen(null);
            RefineryFixture.check(client.theWorld!=oldWorld,"fresh world instance after disk reload");
            fixture=new RefineryFixture(client.theWorld,oldX,oldZ);
            master=fixture.tile(2,1,2,0);
            RefineryFixture.check(master!=restored && master.progress==10 && master.energy==80 && master.count(0)==4 && Structure.intact(master),"actual saved world reload preserves machine and all parts");
        }
        if(ticks==25) {
            RefineryFixture.check(master.progress==0 && master.energy==0 && master.count(0)==3 && master.count(1)==3 && master.count(2)==1,"recipe consumes exactly two items and 160 energy");
            ItemStack output=Ports.extract(fixture.tile(2,0,2,0),3,1);
            RefineryFixture.check(output!=null && output.itemID==Retronism_Refinery.FUEL.shiftedIndex && master.count(2)==0,"extract actual fuel at front port");
            master.setInventorySlotContents(2,new ItemStack(Retronism_Refinery.FUEL,64));
            Ports.receiveEnergy(fixture.tile(2,1,0,0),2,160);
        }
        if(ticks==30) {
            RefineryFixture.check(master.energy==160 && master.progress==0 && master.count(0)==3,"blocked output consumes nothing");
            RefineryRenderProof.aeroCache();
            System.out.println(ScreenShotHelper.saveScreenshot(root,client.displayWidth,client.displayHeight));
            record("RUNNING","structure,ports,unpowered,processing,nbt,blocked-output");
        }
        if(ticks==35) {
            int before=items(client.theWorld,Retronism_Refinery.FUEL.shiftedIndex);
            client.theWorld.setBlockWithNotify(fixture.wx(0,1,0),fixture.oy,fixture.wz(0,1,0),0);
            fixture.restored(0,0,0,1);
            RefineryFixture.check(items(client.theWorld,Retronism_Refinery.FUEL.shiftedIndex)-before==64,"inventory drops exactly once");
            RefineryFixture.check(!Structure.intact(master),"teardown invalidates master");
            // Final display remains a real usable formed machine with one completed output.
            fixture.construct(0); master=fixture.form(0); fixture.feed(2,2,2);
        }
        if(ticks==60) {
            RefineryFixture.check(master.count(2)>=1,"rebuilt machine processes normally");
            System.out.println(ScreenShotHelper.saveScreenshot(root,client.displayWidth,client.displayHeight));
            client.thePlayer.inventory.setInventorySlotContents(0,new ItemStack(Retronism_Refinery.WRENCH,1));
            client.thePlayer.inventory.setInventorySlotContents(1,new ItemStack(Item.seeds,64));
            client.thePlayer.inventory.setInventorySlotContents(2,new ItemStack(Item.sugar,64));
            client.thePlayer.inventory.setInventorySlotContents(3,new ItemStack(Item.redstone,64));
            client.theWorld.saveWorld(true,null);
            network = new RetronismNetworkProof(client, root, fixture.ox, fixture.oz);
        }
        if (ticks > 60) {
            if (network.tick(client)) {
                record("PASS","structure,ports,unpowered,processing,nbt,disk-reload,blocked-output,teardown,no-duplication,render,retronism-generator,cables,item-pipes,wrong-items,network-reload,cable-removal,network-orientations="+System.getenv("REFINERY_NETWORK_ROTATIONS"));
                done=true;
            }
            ticks++; return;
        }
        client.thePlayer.setPositionAndRotation(fixture.ox+7,103,fixture.oz+7,135,14);
        client.thePlayer.motionX=client.thePlayer.motionY=client.thePlayer.motionZ=0;
        ticks++;
    }
    private static int items(World world,int id) {
        int count=0;
        for(Object value:world.loadedEntityList) if(value instanceof EntityItem) {
            EntityItem entity=(EntityItem)value;
            if(entity.item.itemID==id && !entity.isDead) count+=entity.item.stackSize;
        }
        return count;
    }
    private void record(String status,String detail) {
        try {
            root.mkdirs(); Properties values=new Properties();
            values.setProperty("status",status); values.setProperty("detail",detail); values.setProperty("ticks",Integer.toString(ticks));
            values.setProperty("nativeFormation",Boolean.toString(clickComplete));
            values.setProperty("nativeFormationTicks",Integer.toString(clickProof==null?0:clickProof.ticks()));
            FileOutputStream output=new FileOutputStream(new File(root,"runtime.properties")); values.store(output,"Actual Beta 1.7.3 runtime evidence"); output.close();
        } catch(IOException error) { throw new IllegalStateException(error); }
    }
}
