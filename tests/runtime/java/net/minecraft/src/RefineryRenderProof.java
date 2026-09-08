package net.minecraft.src;

import aero.modellib.model.Aero_MeshModel;
import aero.modellib.model.Aero_ObjLoader;
import java.util.ArrayList;
import org.lwjgl.opengl.GL11;
import refinery.*;

public final class RefineryRenderProof {
    private RefineryRenderProof() {}
    public static void aeroCache() {
        int triangles=0, lists=0;
        float[] bounds={Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,
                        Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};
        for(int i=0;i<4;i++) {
            Aero_MeshModel model=Aero_ObjLoader.load("/refinery/model-"+i+".obj");
            triangles+=model.triangleCount();
            float[] b=model.getBounds();
            for(int a=0;a<3;a++) { bounds[a]=Math.min(bounds[a],b[a]); bounds[a+3]=Math.max(bounds[a+3],b[a+3]); }
            RefineryFixture.check(model.getAtRestListIds()!=null,"AeroModelLib at-rest draw was actually executed");
            for(int id:model.getAtRestListIds()) if(id!=0) {
                RefineryFixture.check(GL11.glIsList(id),"AeroModelLib cached list exists in real GL context"); lists++;
            }
        }
        RefineryFixture.check(triangles==5676,"all 473 cubes reached AeroModelLib");
        RefineryFixture.check(bounds[0]==-2 && bounds[1]==-1 && bounds[2]==-2
            && bounds[3]==3 && bounds[4]==2 && bounds[5]==1,"actual parsed mesh has exact 5x3x3 scale and master origin");
        RefineryFixture.check(lists>0 && lists<=16,"bounded cached brightness buckets");
        System.out.println("REFINERY_AERO_PROOF=triangles:"+triangles+",display-lists:"+lists+",bounds:5x3x3");
    }
    public static void portsAndCollisions(RefineryFixture fixture,int facing) {
        // Independent landmarks taken from the native model, in blocks; no Ports.kind-derived coordinates.
        ray(fixture,facing,new double[]{-1,.5,1.5},new double[]{.125,.5,1.5},0,0,1,4);
        ray(fixture,facing,new double[]{6,.5,1.5},new double[]{4.875,.5,1.5},4,0,1,5);
        ray(fixture,facing,new double[]{2.3,.5,4},new double[]{2.3,.5,2.9375},2,0,2,3);
        ray(fixture,facing,new double[]{2.45,1.05,-1},new double[]{2.45,1.05,.0625},2,1,0,2);
        ArrayList collision=new ArrayList();
        Block block=Block.blocksList[Structure.PART];
        RefineryTile tile=fixture.tile(2,2,1,facing);
        AxisAlignedBB query=AxisAlignedBB.getBoundingBoxFromPool(tile.xCoord+.3,tile.yCoord+.3,tile.zCoord+.3,
            tile.xCoord+.7,tile.yCoord+.7,tile.zCoord+.7);
        block.getCollidingBoundingBoxes(fixture.world,tile.xCoord,tile.yCoord,tile.zCoord,query,collision);
        RefineryFixture.check(collision.isEmpty(),"open upper center does not collide like a full block");
    }
    private static void ray(RefineryFixture fixture,int f,double[] start,double[] end,int x,int y,int z,int side) {
        MovingObjectPosition hit=fixture.world.rayTraceBlocks(point(fixture,f,start),point(fixture,f,end));
        RefineryFixture.check(hit!=null && hit.blockX==fixture.wx(x,z,f) && hit.blockY==fixture.oy+y
            && hit.blockZ==fixture.wz(x,z,f) && hit.sideHit==Structure.side(side,f),"rendered connector ray matches cell/face in orientation "+f);
    }
    private static Vec3D point(RefineryFixture fixture,int f,double[] value) {
        double x=value[0]-.5,z=value[2]-.5;
        for(int i=0;i<f;i++) { double old=x; x=-z; z=old; }
        return Vec3D.createVector(fixture.ox+.5+x,fixture.oy+value[1],fixture.oz+.5+z);
    }
}
