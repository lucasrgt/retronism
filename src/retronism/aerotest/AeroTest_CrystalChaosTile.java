package retronism.aerotest;

import net.minecraft.src.TileEntity;
import aero.modellib.Aero_AnimationBundle;
import aero.modellib.Aero_AnimationDefinition;
import aero.modellib.Aero_AnimationLoader;
import aero.modellib.Aero_AnimationState;

public class AeroTest_CrystalChaosTile extends TileEntity {

	public static final int STATE_CHAOS = 1;

	public static final Aero_AnimationBundle BUNDLE =
		Aero_AnimationLoader.load("/models/CrystalChaos.anim.json");

	public static final Aero_AnimationDefinition DEF =
		new Aero_AnimationDefinition().state(STATE_CHAOS, "chaos");

	public final Aero_AnimationState animState = DEF.createState(BUNDLE);

	public void updateEntity() {
		animState.tick();
		animState.setState(STATE_CHAOS);
	}
}
