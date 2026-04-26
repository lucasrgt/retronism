package retronism.aerotest;

import net.minecraft.src.TileEntity;
import aero.modellib.Aero_AnimationBundle;
import aero.modellib.Aero_AnimationDefinition;
import aero.modellib.Aero_AnimationLoader;
import aero.modellib.Aero_AnimationState;

public class AeroTest_MotorTile extends TileEntity {

	public static final int STATE_PULSE = 1;

	public static final Aero_AnimationBundle BUNDLE =
		Aero_AnimationLoader.load("/models/Motor.anim.json");

	public static final Aero_AnimationDefinition DEF =
		new Aero_AnimationDefinition().state(STATE_PULSE, "pulse");

	public final Aero_AnimationState animState = DEF.createState(BUNDLE);

	public void updateEntity() {
		animState.tick();
		animState.setState(STATE_PULSE);
	}
}
