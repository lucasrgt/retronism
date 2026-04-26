package retronism.aerotest;

import net.minecraft.src.*;
import aero.modellib.Aero_IkChain;
import aero.modellib.Aero_MeshModel;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_MorphTarget;
import aero.modellib.Aero_ObjLoader;
import aero.modellib.Aero_ProceduralPose;
import aero.modellib.Aero_RenderOptions;

public class AeroTest_MorphCrystalRenderer extends TileEntitySpecialRenderer {

	public static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/models/MorphCrystal.obj");

	// Attach morph variants once at class load. The bundle lists every
	// variant's resource path under "morph_targets"; attachAllFromBundle
	// loads each OBJ and computes per-vertex deltas at boot.
	static {
		Aero_MorphTarget.attachAllFromBundle(MODEL, AeroTest_MorphCrystalTile.BUNDLE);
	}

	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
		AeroTest_MorphCrystalTile tile = (AeroTest_MorphCrystalTile) te;
		bindTextureByName("/models/aerotest_motor.png");
		float brightness = tile.worldObj.getLightBrightness(tile.xCoord, tile.yCoord + 1, tile.zCoord);
		org.lwjgl.opengl.GL11.glColor4f(1f, 1f, 1f, 1f);

		Aero_MeshRenderer.renderAnimated(MODEL,
			tile.animState.getBundle(), tile.animState.getDef(), tile.animState,
			x, y, z, brightness, partialTick,
			Aero_RenderOptions.DEFAULT, (Aero_ProceduralPose) null,
			(Aero_IkChain[]) null, tile.morphState);
	}
}
