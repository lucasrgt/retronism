package retronism.aerotest;

import net.minecraft.src.TileEntity;
import aero.modellib.Aero_AnimationBundle;
import aero.modellib.Aero_AnimationDefinition;
import aero.modellib.Aero_AnimationGraph;
import aero.modellib.Aero_AnimationLoader;
import aero.modellib.Aero_AnimationPlayback;
import aero.modellib.Aero_GraphBlend1DNode;
import aero.modellib.Aero_GraphClipNode;
import aero.modellib.Aero_GraphNode;
import aero.modellib.Aero_GraphParams;

/**
 * GraphPowered tile uses {@link Aero_AnimationGraph} with a Blend1D node
 * to smoothly interpolate between a "slow" and "fast" spin clip. The
 * blend factor comes from the redstone power level (0..15) read off the
 * tile's neighbors and pushed into a graph param ahead of every render.
 */
public class AeroTest_GraphPoweredTile extends TileEntity {

	private static final String SPEED_PARAM = "speed";

	public static final Aero_AnimationBundle BUNDLE =
		Aero_AnimationLoader.load("/models/GraphPowered.anim.json");

	private final Aero_AnimationDefinition slowDef =
		new Aero_AnimationDefinition().state(0, "slow");
	private final Aero_AnimationDefinition fastDef =
		new Aero_AnimationDefinition().state(0, "fast");

	private final Aero_AnimationPlayback slowPlayback = slowDef.createPlayback(BUNDLE);
	private final Aero_AnimationPlayback fastPlayback = fastDef.createPlayback(BUNDLE);

	private final Aero_GraphParams params = new Aero_GraphParams();
	public final Aero_AnimationGraph graph;

	private float speed = 0f;
	private boolean powered = false;

	public AeroTest_GraphPoweredTile() {
		slowPlayback.setState(0);
		fastPlayback.setState(0);
		Aero_GraphNode root = new Aero_GraphBlend1DNode(SPEED_PARAM,
			new float[]{0f, 1f},
			new Aero_GraphNode[]{
				new Aero_GraphClipNode(slowPlayback),
				new Aero_GraphClipNode(fastPlayback)
			});
		this.graph = new Aero_AnimationGraph(root, params);
	}

	public void updateEntity() {
		slowPlayback.tick();
		fastPlayback.tick();
		// Ramp speed param toward 1.0 (powered) or 0.0 (unpowered) over
		// ~1 second. Demonstrates Blend1D's smooth interpolation between
		// adjacent thresholds — without the ramp the visual would snap.
		float target = powered ? 1f : 0f;
		speed += (target - speed) * 0.05f;
		params.setFloat(SPEED_PARAM, speed);
	}

	public void updatePower() {
		if (worldObj == null) return;
		powered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}
}
