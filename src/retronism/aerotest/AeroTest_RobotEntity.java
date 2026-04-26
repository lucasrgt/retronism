package retronism.aerotest;

import net.minecraft.src.EntityCreature;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import aero.modellib.Aero_AnimationSpec;
import aero.modellib.Aero_AnimationState;

import java.util.Random;

/**
 * Walking robot mob — Beta 1.7.3 / RetroMCP port of the StationAPI showcase
 * AeroRobotEntity. Drops the thermal-cycle / overheat tinting logic and just
 * exercises the animation state machine: idle vs walk based on horizontal
 * displacement.
 */
public class AeroTest_RobotEntity extends EntityCreature {

	public static final int STATE_IDLE = 0;
	public static final int STATE_WALK = 1;

	public static final Aero_AnimationSpec ANIMATION =
		Aero_AnimationSpec.builder("/models/Robot.anim.json")
			.state(STATE_IDLE, "idle")
			.state(STATE_WALK, "walk")
			.build();

	public final Aero_AnimationState animState = ANIMATION.createState();

	private final Random rng = new Random();
	private float walkYaw;
	private int turnCooldown;
	private boolean walking;

	public AeroTest_RobotEntity(World world) {
		super(world);
		setSize(0.6f, 1.25f);
		this.health = 20;
	}

	public void onLivingUpdate() {
		super.onLivingUpdate();

		// Wander logic — server-only, like AeroTestEntity.
		if (!worldObj.multiplayerWorld) {
			if (turnCooldown-- <= 0) {
				walking = rng.nextFloat() < 0.7f;
				walkYaw = rng.nextFloat() * 360f;
				turnCooldown = walking ? 60 + rng.nextInt(40) : 40 + rng.nextInt(40);
			}
			if (walking) {
				float yawRad = walkYaw * 0.017453292f;
				float speed = 0.05f;
				motionX = -((double) speed) * (double) Math.sin(yawRad);
				motionZ =  ((double) speed) * (double) Math.cos(yawRad);
				prevRotationYaw = rotationYaw;
				rotationYaw += angleDelta(rotationYaw, walkYaw) * 0.2f;
			} else {
				motionX = 0;
				motionZ = 0;
			}
		}

		// Animation state — derive movement from horizontal position delta so
		// both client and server agree without an extra packet.
		double dx = posX - prevPosX;
		double dz = posZ - prevPosZ;
		boolean isMoving = dx * dx + dz * dz > 1.0e-4;
		animState.setState(isMoving ? STATE_WALK : STATE_IDLE);
		animState.tick();

		// Yaw shortest-path normalisation so the renderer's lerp doesn't flick.
		while (rotationYaw - prevRotationYaw < -180f) prevRotationYaw -= 360f;
		while (rotationYaw - prevRotationYaw >= 180f) prevRotationYaw += 360f;
	}

	private static float angleDelta(float current, float target) {
		float d = (target - current) % 360f;
		if (d >  180f) d -= 360f;
		if (d < -180f) d += 360f;
		return d;
	}

	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		animState.readFromNBT(nbt);
	}

	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		animState.writeToNBT(nbt);
	}
}
