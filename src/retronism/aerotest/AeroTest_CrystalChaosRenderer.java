package retronism.aerotest;

import net.minecraft.src.*;
import aero.modellib.Aero_MeshModel;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_ObjLoader;

public class AeroTest_CrystalChaosRenderer extends TileEntitySpecialRenderer {

	public static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/models/CrystalChaos.obj");

	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
		AeroTest_CrystalChaosTile tile = (AeroTest_CrystalChaosTile) te;
		bindTextureByName("/models/aerotest_crystal_chaos.png");
		float brightness = tile.worldObj.getLightBrightness(tile.xCoord, tile.yCoord + 1, tile.zCoord);
		org.lwjgl.opengl.GL11.glColor4f(1f, 1f, 1f, 1f);
		Aero_MeshRenderer.renderAnimated(MODEL, tile.animState, x, y, z, brightness, partialTick);
	}
}
