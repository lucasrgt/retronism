package retronism.aerotest;

import net.minecraft.src.Entity;
import net.minecraft.src.Render;
import org.lwjgl.opengl.GL11;
import aero.modellib.Aero_EntityModelRenderer;
import aero.modellib.Aero_ModelSpec;
import aero.modellib.Aero_RenderOptions;

/**
 * Renderer for {@link AeroTest_RobotEntity}. Lerps the chassis tint
 * from white through red as overheat ramps; pulses red↔yellow during
 * meltdown so the visual cycle is unmistakable.
 */
public class AeroTest_RobotEntityRenderer extends Render {

	private static final Aero_ModelSpec MODEL =
		Aero_ModelSpec.mesh("/models/Robot.obj")
			.texture("/models/aerotest_robot.png")
			.animations(AeroTest_RobotEntity.ANIMATION)
			.offset(-0.5f, 0f, -0.5f)
			.build();

	public AeroTest_RobotEntityRenderer() {
		this.shadowSize = 0.4f;
	}

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
		AeroTest_RobotEntity bot = (AeroTest_RobotEntity) entity;
		loadTexture(MODEL.getTexturePath());

		// Lerp white → red as overheat rises. Drop G + B from 1 → ~0.35 so
		// the tint reads as red without over-saturating; never lift R above
		// 1 (would clip and look washed out).
		float overheat = bot.getInterpolatedOverheat(partialTick);
		float baseGB   = 1f - 0.65f * overheat;
		float g = baseGB;
		float b = baseGB;
		// During meltdown, pulse G between baseGB and 1.0 at ~3 Hz so the
		// tint swings between red (1, low, low) and yellow (1, 1, low). B
		// stays low so we never go pink/washed.
		if (bot.isMeltdown()) {
			float t = (bot.ticksExisted + partialTick) * (6f * (float) Math.PI / 20f);
			float pulse = 0.5f + 0.5f * (float) Math.sin(t);
			g = baseGB + (1.0f - baseGB) * pulse;
		}

		Aero_RenderOptions tint = Aero_RenderOptions.tint(1f, g, b);

		// Reset the GL color register too — defensive against any caller
		// that reads it before issuing its own tess.color().
		GL11.glColor4f(1f, 1f, 1f, 1f);

		// Robust brightness sampling: vanilla entity.getEntityBrightness
		// occasionally flickers to 0 at dusk when the eye-height Y lands
		// inside an opaque block (block boundary, half-step, wall brush).
		// Sampling at the column's topmost solid Y gives a sky-lit
		// reference that doesn't flicker.
		int ex = (int) Math.floor(entity.posX);
		int ey = (int) Math.floor(entity.posY);
		int ez = (int) Math.floor(entity.posZ);
		// brightnessAt (entity-relative), not brightnessAbove (column top) —
		// the latter walks to the world's surface, which makes submerged
		// robots glow because they pick up sky brightness through water/ice.
		float brightness = AeroTest_Light.brightnessAt(entity.worldObj, ex, ey, ez);
		Aero_EntityModelRenderer.render(MODEL, bot.animState,
			x, y, z, yaw, brightness, partialTick, tint);
	}
}
