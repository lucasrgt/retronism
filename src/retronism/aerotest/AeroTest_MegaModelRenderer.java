package retronism.aerotest;

import net.minecraft.src.*;
import aero.modellib.Aero_MeshModel;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_ObjLoader;

/**
 * Static-mesh renderer for {@link AeroTest_MegaModelTile}. Calls
 * renderModel for the body groups, then iterates named groups at rest
 * pose so the MegaCrusher's animated parts (turbines, hopper lid) appear
 * frozen — same visual as stationapi's MegaModelBlockEntityRenderer.
 */
public class AeroTest_MegaModelRenderer extends TileEntitySpecialRenderer {

	public static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/models/MegaCrusher.obj");

	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
		bindTextureByName("/models/retronism_megacrusher.png");
		float brightness = AeroTest_Light.brightnessAbove(te.worldObj, te.xCoord, te.yCoord, te.zCoord);
		org.lwjgl.opengl.GL11.glColor4f(1f, 1f, 1f, 1f);

		// Static body groups via renderModel.
		Aero_MeshRenderer.renderModel(MODEL, x, y, z, 0f, brightness);

		// Named groups (turbines, hopper lid, etc) at rest pose. The
		// MegaCrusher OBJ has all geometry in named groups so renderModel
		// alone draws nothing; iterate them at rest brightness for parity
		// with the stationapi static-mode renderer.
		Aero_MeshModel.NamedGroup[] entries = MODEL.getNamedGroupArray();
		for (int i = 0; i < entries.length; i++) {
			org.lwjgl.opengl.GL11.glPushMatrix();
			org.lwjgl.opengl.GL11.glTranslated(x, y, z);
			org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
			org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_LIGHTING);
			Aero_MeshRenderer.renderGroup(MODEL, entries[i].name, brightness);
			org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_LIGHTING);
			org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
			org.lwjgl.opengl.GL11.glPopMatrix();
		}
	}
}
