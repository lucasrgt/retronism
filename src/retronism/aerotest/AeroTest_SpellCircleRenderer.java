package retronism.aerotest;

import net.minecraft.src.*;
import aero.modellib.Aero_MeshModel;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_ObjLoader;

public class AeroTest_SpellCircleRenderer extends TileEntitySpecialRenderer {

	// Reuses Conveyor.obj (1×1 plate at floor level) — same geometry, different
	// uv channels in the .anim.json. Demonstrates that uv_offset + uv_scale
	// compose: U scrolls horizontally, V breathes between 1.0 and 1.5 with an
	// easeInOutSine curve, producing a "magic rune" pulse.
	public static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/models/Conveyor.obj");

	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
		AeroTest_SpellCircleTile tile = (AeroTest_SpellCircleTile) te;
		bindTextureByName("/models/aerotest_motor.png");
		float brightness = tile.worldObj.getLightBrightness(tile.xCoord, tile.yCoord + 1, tile.zCoord);
		org.lwjgl.opengl.GL11.glColor4f(1f, 1f, 1f, 1f);
		Aero_MeshRenderer.renderAnimated(MODEL, tile.animState, x, y, z, brightness, partialTick);
	}
}
