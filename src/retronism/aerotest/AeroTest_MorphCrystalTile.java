package retronism.aerotest;

import net.minecraft.src.TileEntity;
import aero.modellib.Aero_AnimationBundle;
import aero.modellib.Aero_AnimationDefinition;
import aero.modellib.Aero_AnimationLoader;
import aero.modellib.Aero_AnimationState;
import aero.modellib.Aero_MorphState;

public class AeroTest_MorphCrystalTile extends TileEntity {

	public static final int STATE_REST = 1;

	public static final Aero_AnimationBundle BUNDLE =
		Aero_AnimationLoader.load("/models/MorphCrystal.anim.json");

	public static final Aero_AnimationDefinition DEF =
		new Aero_AnimationDefinition().state(STATE_REST, "rest");

	public final Aero_AnimationState animState = DEF.createState(BUNDLE);

	/** Morph weights driven by tick-side logic — the "expanded" target's
	 *  weight oscillates 0→1→0 every 2 seconds via a sine wave. */
	public final Aero_MorphState morphState = new Aero_MorphState();

	private int tick = 0;

	public void updateEntity() {
		animState.tick();
		animState.setState(STATE_REST);
		tick++;
		// 2-second period at 20 tps → 40 ticks. Oscillate 0→1.
		float phase = (tick % 40) / 40f * (float) (2.0 * Math.PI);
		float w = 0.5f + 0.5f * (float) Math.sin(phase);
		morphState.set("expanded", w);
	}
}
