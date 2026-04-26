package retronism.aerotest;

import net.minecraft.src.Entity;
import net.minecraft.src.Render;
import org.lwjgl.opengl.GL11;
import aero.modellib.Aero_EntityModelRenderer;
import aero.modellib.Aero_ModelSpec;

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
		GL11.glColor4f(1f, 1f, 1f, 1f);
		Aero_EntityModelRenderer.render(MODEL, bot.animState,
			entity, x, y, z, yaw, partialTick);
	}
}
