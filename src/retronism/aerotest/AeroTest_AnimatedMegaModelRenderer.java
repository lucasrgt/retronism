package retronism.aerotest;

import net.minecraft.src.*;
import aero.modellib.Aero_MeshModel;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_ObjLoader;
import aero.modellib.Aero_RenderDistance;
import aero.modellib.Aero_RenderLod;

/**
 * Animated renderer for {@link AeroTest_AnimatedMegaModelTile}. Uses
 * lodRelative to switch between renderAnimated (close), renderModelAtRest
 * (medium → display list cache fast path), and cull (far).
 */
public class AeroTest_AnimatedMegaModelRenderer extends TileEntitySpecialRenderer {

	public static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/models/MegaCrusher.obj");

	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
		AeroTest_AnimatedMegaModelTile tile = (AeroTest_AnimatedMegaModelTile) te;
		Aero_RenderLod lod = Aero_RenderDistance.lodRelative(
			x, y, z, 4d, retronism.mod_RetronismAeroTest.demoAnimatedLodDistance());
		if (!lod.shouldRender()) return;

		bindTextureByName("/models/retronism_megacrusher.png");
		float brightness = AeroTest_Light.brightnessAbove(tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord);
		org.lwjgl.opengl.GL11.glColor4f(1f, 1f, 1f, 1f);
		if (lod.shouldAnimate()) {
			Aero_MeshRenderer.renderAnimated(MODEL, tile.animState, x, y, z, brightness, partialTick);
		} else {
			Aero_MeshRenderer.renderModelAtRest(MODEL, x, y, z, 0f, brightness);
		}
	}
}
