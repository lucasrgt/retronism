package retronism.aerotest;

import net.minecraft.src.World;

/**
 * Beta 1.7.3 / RetroMCP port of the StationAPI test mod's AeroLight helper.
 *
 * The naive {@code entity.getEntityBrightness(partialTick)} call samples
 * brightness at the entity's eye Y. If that Y lands inside an opaque
 * block (entity standing on a half-step, brushing against a wall, or just
 * flickering across a chunk boundary at dusk), the lookup returns 0 and
 * the rendered model goes pure black for that frame.
 *
 * <p>Sampling at the world's topmost solid Y for the entity's column
 * instead gives a sky-lit reference brightness — robust across all those
 * edge cases. Falls back to the literal {@code y+1} read when the chunk's
 * heightmap isn't loaded yet (newly-spawned entity in an unsynced
 * chunk).
 */
public final class AeroTest_Light {

	private AeroTest_Light() {}

	/**
	 * Returns the world's float light brightness for the column the entity
	 * stands in, sampled above any opaque block at that (x, z).
	 */
	public static float brightnessAbove(World world, int x, int y, int z) {
		int top = world.getHeightValue(x, z);
		int sampleY = Math.max(y + 1, top);
		float bright = world.getLightBrightness(x, sampleY, z);
		if (bright <= 0f) bright = world.getLightBrightness(x, y + 1, z);
		return bright;
	}

	/**
	 * Entity-position brightness sample — does NOT walk to the column top.
	 * Use for mobs that move through water / ice / glass: column-top
	 * sampling would give full sky brightness and make submerged entities
	 * glow.
	 */
	public static float brightnessAt(World world, int x, int y, int z) {
		return world.getLightBrightness(x, y, z);
	}
}
