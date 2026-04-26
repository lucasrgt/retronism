package retronism.aerotest;

import net.minecraft.src.*;
import aero.modellib.Aero_IkChain;
import aero.modellib.Aero_MeshModel;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_ObjLoader;
import aero.modellib.Aero_ProceduralPose;
import aero.modellib.Aero_RenderOptions;

/**
 * Visual showcase for {@link Aero_IkChain} + the CCD solver.
 *
 * <p>The turret has a 3-bone chain — base → arm → tip. Each frame, an
 * IK chain resolves the nearest player's eye position into block-local
 * coordinates (subtracting the block's world origin) and feeds it to the
 * CCD solver. The solver mutates {@code turret_base} and {@code turret_arm}
 * rotations to bring {@code turret_tip}'s pivot close to the target. The
 * tip itself isn't rotated — it's the end-effector marker.
 */
public class AeroTest_TurretIKRenderer extends TileEntitySpecialRenderer {

	public static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/models/Turret.obj");

	private static final String[] CHAIN = {"turret_base", "turret_arm", "turret_tip"};

	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
		final AeroTest_TurretIKTile tile = (AeroTest_TurretIKTile) te;
		bindTextureByName("/models/aerotest_motor.png");
		float brightness = tile.worldObj.getLightBrightness(tile.xCoord, tile.yCoord + 1, tile.zCoord);
		org.lwjgl.opengl.GL11.glColor4f(1f, 1f, 1f, 1f);

		// IK target: nearest player's eye, in block-local pixel space (the
		// frame the FK walker operates in). The lib's outer glTranslate
		// places the block at the world origin, so we subtract block coords
		// and convert blocks → pixels via *16 to match the bundle's pivots.
		Aero_IkChain[] chains = new Aero_IkChain[]{ new Aero_IkChain() {
			public String[] getBoneChain() { return CHAIN; }
			public boolean resolveTargetInto(float[] worldPos) {
				EntityPlayer p = tile.worldObj.getClosestPlayer(
					tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5, 16.0);
				if (p == null) return false;
				worldPos[0] = (float) ((p.posX - tile.xCoord) * 16.0);
				worldPos[1] = (float) ((p.posY + p.getEyeHeight() - tile.yCoord) * 16.0);
				worldPos[2] = (float) ((p.posZ - tile.zCoord) * 16.0);
				return true;
			}
		}};

		Aero_MeshRenderer.renderAnimated(MODEL,
			tile.animState.getBundle(), tile.animState.getDef(), tile.animState,
			x, y, z, brightness, partialTick,
			Aero_RenderOptions.DEFAULT, (Aero_ProceduralPose) null, chains);
	}
}
