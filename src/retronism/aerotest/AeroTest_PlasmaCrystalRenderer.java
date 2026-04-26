package retronism.aerotest;

import net.minecraft.src.*;
import aero.modellib.Aero_BoneRenderPose;
import aero.modellib.Aero_MeshBlendMode;
import aero.modellib.Aero_MeshModel;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_ObjLoader;
import aero.modellib.Aero_ProceduralPose;
import aero.modellib.Aero_RenderOptions;

public class AeroTest_PlasmaCrystalRenderer extends TileEntitySpecialRenderer {

	public static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/models/Crystal.obj");

	private static final Aero_RenderOptions GLOW = Aero_RenderOptions.builder()
		.tint(0.6f, 0.85f, 1f)
		.alpha(0.85f)
		.blend(Aero_MeshBlendMode.ADDITIVE)
		.build();

	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
		AeroTest_PlasmaCrystalTile tile = (AeroTest_PlasmaCrystalTile) te;
		bindTextureByName("/models/aerotest_crystal.png");
		float brightness = tile.worldObj.getLightBrightness(tile.xCoord, tile.yCoord + 1, tile.zCoord);
		org.lwjgl.opengl.GL11.glColor4f(1f, 1f, 1f, 1f);

		final float spinDeg = (System.currentTimeMillis() / 4L) % 360L;
		Aero_ProceduralPose proceduralSpin = new Aero_ProceduralPose() {
			public void apply(String boneName, Aero_BoneRenderPose pose) {
				if ("crystal".equals(boneName)) pose.rotY += spinDeg;
			}
		};

		Aero_MeshRenderer.renderAnimated(MODEL,
			AeroTest_PlasmaCrystalTile.BUNDLE,
			AeroTest_PlasmaCrystalTile.DEF,
			tile.animState,
			x, y, z, brightness, partialTick,
			GLOW, proceduralSpin);
	}
}
