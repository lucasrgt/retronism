package retronism.aerotest;

import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import aero.modellib.Aero_AnimationPlayback;
import aero.modellib.Aero_AnimationPredicate;
import aero.modellib.Aero_AnimationSpec;
import aero.modellib.Aero_AnimationState;
import aero.modellib.Aero_AnimationStateRouter;

import java.util.Random;

/**
 * Walking robot mob with a 5-phase thermal cycle:
 *
 * <pre>
 *   NORMAL (8s)        — idle / walk clips, white tint
 *   OVERHEATING (4s)   — overheat 0 → 1, walks faster, tint white → red,
 *                        animation snaps to *_hot at level=1
 *   OVERHEAT (4s)      — overheat = 1, hot clips, full red tint
 *   MELTDOWN (4s)      — meltdown clip plays: head flies off, arms spin
 *                        on Z, legs shake. Tint pulses red↔yellow.
 *   COOLING (4s)       — overheat 1 → 0, animation back to idle/walk, tint
 *                        fades to white
 *   → loop
 * </pre>
 *
 * Beta 1.7.3 / RetroMCP port of the StationAPI showcase. Extends raw
 * {@link Entity} (not EntityCreature) so vanilla AI doesn't conflict
 * with the manual phase-driven motion.
 */
public class AeroTest_RobotEntity extends Entity {

	public static final int STATE_IDLE     = 0;
	public static final int STATE_WALK     = 1;
	public static final int STATE_IDLE_HOT = 2;
	public static final int STATE_WALK_HOT = 3;
	public static final int STATE_MELTDOWN = 4;

	private static final int PHASE_NORMAL      = 0;
	private static final int PHASE_OVERHEATING = 1;
	private static final int PHASE_OVERHEAT    = 2;
	private static final int PHASE_MELTDOWN    = 3;
	private static final int PHASE_COOLING     = 4;
	private static final int PHASE_COUNT       = 5;

	private static final int PHASE_NORMAL_TICKS      = 160; // 8s
	private static final int PHASE_OVERHEATING_TICKS = 80;  // 4s
	private static final int PHASE_OVERHEAT_TICKS    = 80;  // 4s
	private static final int PHASE_MELTDOWN_TICKS    = 80;  // 4s
	private static final int PHASE_COOLING_TICKS     = 80;  // 4s

	public static final Aero_AnimationSpec ANIMATION =
		Aero_AnimationSpec.builder("/models/Robot.anim.json")
			.state(STATE_IDLE,     "idle")
			.state(STATE_WALK,     "walk")
			.state(STATE_IDLE_HOT, "idle_hot")
			.state(STATE_WALK_HOT, "walk_hot")
			.state(STATE_MELTDOWN, "meltdown")
			.build();

	public final Aero_AnimationState animState = ANIMATION.createState();

	private final Random rng = new Random();
	private float walkYaw;
	private int turnCooldown;
	private boolean walking;

	private int phase = PHASE_NORMAL;
	private int phaseTimer = PHASE_NORMAL_TICKS;
	public float overheatLevel;
	public float prevOverheatLevel;

	public AeroTest_RobotEntity(World world) {
		super(world);
		setSize(0.6f, 1.25f);
		this.yOffset = 0.0f;
	}

	protected void entityInit() {
	}

	public void onUpdate() {
		super.onUpdate();

		if (!worldObj.multiplayerWorld) {
			tickPhase();
			tickWander();
		}

		prevOverheatLevel = overheatLevel;
		overheatLevel = computeOverheatLevel();

		// "is walking" derived from horizontal position delta — both client
		// and server set prevPosX/Z in Entity.onUpdate(), so this stays in
		// sync without an extra packet.
		double dx = posX - prevPosX;
		double dz = posZ - prevPosZ;
		final boolean isMoving = dx * dx + dz * dz > 1.0e-4;

		final int   localPhase    = phase;
		final float localOverheat = overheatLevel;
		Aero_AnimationStateRouter router = new Aero_AnimationStateRouter()
			.when(new Aero_AnimationPredicate() {
				public boolean test(Aero_AnimationPlayback p) { return localPhase == PHASE_MELTDOWN; }
			}, STATE_MELTDOWN)
			.when(new Aero_AnimationPredicate() {
				public boolean test(Aero_AnimationPlayback p) {
					boolean hot = localPhase == PHASE_OVERHEAT
					           || (localPhase == PHASE_OVERHEATING && localOverheat >= 0.5f);
					return hot && isMoving;
				}
			}, STATE_WALK_HOT)
			.when(new Aero_AnimationPredicate() {
				public boolean test(Aero_AnimationPlayback p) {
					boolean hot = localPhase == PHASE_OVERHEAT
					           || (localPhase == PHASE_OVERHEATING && localOverheat >= 0.5f);
					return hot;
				}
			}, STATE_IDLE_HOT)
			.when(new Aero_AnimationPredicate() {
				public boolean test(Aero_AnimationPlayback p) { return isMoving; }
			}, STATE_WALK)
			.otherwise(STATE_IDLE)
			.withTransition(6);
		router.applyTo(animState);
		animState.tick();

		// Yaw shortest-path normalisation so the renderer's lerp doesn't flick.
		while (rotationYaw - prevRotationYaw < -180f) prevRotationYaw -= 360f;
		while (rotationYaw - prevRotationYaw >= 180f) prevRotationYaw += 360f;
	}

	private void tickPhase() {
		if (phaseTimer-- > 0) return;
		phase = (phase + 1) % PHASE_COUNT;
		switch (phase) {
			case PHASE_NORMAL:      phaseTimer = PHASE_NORMAL_TICKS; break;
			case PHASE_OVERHEATING: phaseTimer = PHASE_OVERHEATING_TICKS; break;
			case PHASE_OVERHEAT:    phaseTimer = PHASE_OVERHEAT_TICKS; break;
			case PHASE_MELTDOWN:    phaseTimer = PHASE_MELTDOWN_TICKS; break;
			case PHASE_COOLING:     phaseTimer = PHASE_COOLING_TICKS; break;
		}
	}

	private void tickWander() {
		if (turnCooldown-- <= 0) {
			walking      = rng.nextFloat() < 0.7f;
			walkYaw      = rng.nextFloat() * 360f;
			turnCooldown = walking ? 60 + rng.nextInt(40)
			                       : 40 + rng.nextInt(40);
		}

		float speedMul;
		switch (phase) {
			case PHASE_OVERHEATING: speedMul = 1f + overheatLevel; break;
			case PHASE_OVERHEAT:    speedMul = 2.0f; break;
			case PHASE_MELTDOWN:    speedMul = 0f; break;
			case PHASE_COOLING:     speedMul = 0.5f + overheatLevel * 0.5f; break;
			default:                speedMul = 1.0f;
		}

		if (walking) {
			float yawRad = walkYaw * 0.017453292f;
			float speed  = 0.05f * speedMul;
			motionX = -((double) speed) * (double) Math.sin(yawRad);
			motionZ =  ((double) speed) * (double) Math.cos(yawRad);
		} else {
			motionX = 0;
			motionZ = 0;
		}
		// Vanilla mob gravity + terminal cap.
		motionY -= 0.08;
		if (motionY < -3.92) motionY = -3.92;
		moveEntity(motionX, motionY, motionZ);
		if (onGround && motionY < 0) motionY = 0;

		prevRotationYaw = rotationYaw;
		if (walking) rotationYaw += angleDelta(rotationYaw, walkYaw) * 0.2f;
	}

	private float computeOverheatLevel() {
		switch (phase) {
			case PHASE_NORMAL:      return 0f;
			case PHASE_OVERHEATING: return 1f - phaseTimer / (float) PHASE_OVERHEATING_TICKS;
			case PHASE_OVERHEAT:    return 1f;
			case PHASE_MELTDOWN:    return 1f;
			case PHASE_COOLING:     return phaseTimer / (float) PHASE_COOLING_TICKS;
		}
		return 0f;
	}

	public boolean isMeltdown() {
		return phase == PHASE_MELTDOWN;
	}

	public float getInterpolatedOverheat(float partialTick) {
		return prevOverheatLevel + (overheatLevel - prevOverheatLevel) * partialTick;
	}

	private static float angleDelta(float current, float target) {
		float d = (target - current) % 360f;
		if (d >  180f) d -= 360f;
		if (d < -180f) d += 360f;
		return d;
	}

	protected void readEntityFromNBT(NBTTagCompound nbt) {
		animState.readFromNBT(nbt);
		phase      = nbt.getInteger("Robot_phase");
		phaseTimer = nbt.getInteger("Robot_phaseTimer");
		if (phaseTimer <= 0) phaseTimer = PHASE_NORMAL_TICKS;
	}

	protected void writeEntityToNBT(NBTTagCompound nbt) {
		animState.writeToNBT(nbt);
		nbt.setInteger("Robot_phase", phase);
		nbt.setInteger("Robot_phaseTimer", phaseTimer);
	}
}
