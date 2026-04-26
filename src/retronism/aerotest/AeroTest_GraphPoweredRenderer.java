package retronism.aerotest;

import net.minecraft.src.*;
import aero.modellib.Aero_MeshModel;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_ObjLoader;

public class AeroTest_GraphPoweredRenderer extends TileEntitySpecialRenderer {

	// Reuses the existing motor mesh — single bone "core" — so the showcase
	// doesn't need a new asset. The Blend1D node lerps between slow and
	// fast spin clips driven by redstone power.
	public static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/models/Motor.obj");

	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
		AeroTest_GraphPoweredTile tile = (AeroTest_GraphPoweredTile) te;
		bindTextureByName("/models/aerotest_motor.png");
		float brightness = tile.worldObj.getLightBrightness(tile.xCoord, tile.yCoord + 1, tile.zCoord);
		org.lwjgl.opengl.GL11.glColor4f(1f, 1f, 1f, 1f);

		Aero_MeshRenderer.renderAnimated(MODEL, tile.graph,
			AeroTest_GraphPoweredTile.BUNDLE,
			x, y, z, brightness, partialTick);
	}
}
