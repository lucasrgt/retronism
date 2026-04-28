package retronism.aerotest;

import aero.modellib.Aero_AnimationBundle;
import aero.modellib.Aero_AnimationDefinition;
import aero.modellib.Aero_AnimationEventListener;
import aero.modellib.Aero_AnimationLoader;
import aero.modellib.Aero_AnimationState;
import aero.modellib.Aero_RenderDistanceTileEntity;

/**
 * TileEntity exposing an {@link Aero_AnimationState} driven by tick().
 * Permanently in STATE_SPIN so the rendered mesh keeps looping the
 * "working" clip from MegaCrusher.anim.json — placing the block is
 * enough to see the keyframes play continuously.
 *
 * <p>Routes locator-anchored sound + particle events from the clip to
 * actual Beta MC calls so testers can validate the keyframe-event wiring
 * without needing to grep the server log.
 */
public class AeroTest_AnimatedMegaModelTile extends Aero_RenderDistanceTileEntity {

	public static final int STATE_SPIN = 1;

	public static final Aero_AnimationBundle BUNDLE =
		Aero_AnimationLoader.load("/models/MegaCrusher.anim.json");

	public static final Aero_AnimationDefinition DEF =
		new Aero_AnimationDefinition().state(STATE_SPIN, "working");

	public final Aero_AnimationState animState = DEF.createState(BUNDLE);

	public AeroTest_AnimatedMegaModelTile() {
		animState.setEventListener(new Aero_AnimationEventListener() {
			public void onEvent(String channel, String data, String locator, float time) {
				System.out.println("[aerotest:event] " + channel + "=" + data
					+ " @ t=" + time + "s loc=" + locator);
				if (worldObj == null) return;

				// Resolve the locator's animated pivot so particle/sound
				// events fire from the moving bone, not the block centre.
				double ox = 0.5, oy = 0.5, oz = 0.5;
				if (locator != null) {
					float[] pivot = new float[3];
					if (animState.getAnimatedPivot(locator, 0f, pivot)) {
						ox = pivot[0]; oy = pivot[1]; oz = pivot[2];
					}
				}
				double cx = xCoord + ox, cy = yCoord + oy, cz = zCoord + oz;

				if ("sound".equals(channel)) {
					worldObj.playSoundEffect(cx, cy, cz, data, 0.6f, 1.0f);
				} else if ("particle".equals(channel)) {
					for (int i = 0; i < 5; i++) {
						worldObj.spawnParticle(data,
							cx + (Math.random() - 0.5) * 0.3,
							cy,
							cz + (Math.random() - 0.5) * 0.3,
							0, 0.05, 0);
					}
				}
				// "custom" stays console-only.
			}
		});
	}

	@Override
	protected double getAeroRenderRadius() {
		return 4.0d;
	}

	public void updateEntity() {
		super.updateEntity();
		if (!shouldTickAnimation()) return;
		animState.setState(STATE_SPIN);
		animState.tick();
	}
}
