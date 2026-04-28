package retronism;

import net.minecraft.src.*;
import retronism.aerotest.*;

import java.util.Map;
import java.util.Random;

/**
 * ModLoader companion mod that ships only the Aero modellib showcase blocks
 * + robot entity. Lives next to {@link mod_Retronism} so the production tech
 * mod stays free of test artifacts; both load together when the user runs
 * {@code scripts/test.sh}.
 */
public class mod_RetronismAeroTest extends BaseMod {

	// Block IDs — start at 220 to stay clear of mod_Retronism's 200..217 range.
	public static final Block motorBlock          = (new AeroTest_MotorBlock(220, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.motor");
	public static final Block pumpBlock           = (new AeroTest_PumpBlock(221, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.pump");
	public static final Block crystalBlock        = (new AeroTest_CrystalBlock(222, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.crystal");
	public static final Block crystalChaosBlock   = (new AeroTest_CrystalChaosBlock(223, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.crystal_chaos");
	public static final Block easingShowcaseBlock  = (new AeroTest_EasingShowcaseBlock(224, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.easing_showcase");
	public static final Block easingShowcase2Block = (new AeroTest_EasingShowcase2Block(225, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.easing_showcase_2");
	public static final Block easingShowcase3Block = (new AeroTest_EasingShowcase3Block(226, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.easing_showcase_3");
	public static final Block plasmaCrystalBlock  = (new AeroTest_PlasmaCrystalBlock(227, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.plasma_crystal");
	public static final Block conveyorBlock       = (new AeroTest_ConveyorBlock(228, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.conveyor");
	public static final Block spellCircleBlock   = (new AeroTest_SpellCircleBlock(229, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.spell_circle");
	// IDs 230-238 + 240-255 occupied by beta-energistics. Slot 218, 219 sit
	// between mod_Retronism (200-217) and aerotest's first showcase (220);
	// 239 fills beta-energistics's gap between fluid + non-fluid groups.
	public static final Block turretIkBlock      = (new AeroTest_TurretIKBlock(218, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.turret_ik");
	public static final Block morphCrystalBlock  = (new AeroTest_MorphCrystalBlock(219, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.morph_crystal");
	public static final Block graphPoweredBlock  = (new AeroTest_GraphPoweredBlock(239, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.graph_powered");
	// MegaModel + AnimatedMegaModel ported from stationapi for full parity.
	// IDs picked from free holes: 215 (gap inside mod_Retronism's 200-217 range)
	// + 198 (just below mod_Retronism's first ID 200).
	public static final Block megaModelBlock         = (new AeroTest_MegaModelBlock(215, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.mega_model");
	public static final Block animatedMegaModelBlock = (new AeroTest_AnimatedMegaModelBlock(198, 1))
		.setHardness(1.0F).setStepSound(Block.soundMetalFootstep).setBlockName("aerotest.mega_model_animated");

	private static final int ROBOT_ENTITY_ID = ModLoader.getUniqueEntityId();

	/**
	 * Mirror of stationapi's AeroTestMod stress configuration so the modloader
	 * runtime can be exercised under the same load. Activated via JVM flag
	 *   -Daero.stresstest=true
	 * When on, every chunk gets the showcase placement plus a 3×3 motor grid;
	 * animated LOD is bumped to 256 blocks so far entities still animate.
	 * Off by default — normal `runClient` keeps the original showcase density.
	 */
	public static final boolean STRESS_TEST = Boolean.getBoolean("aero.stresstest");

	private static final int DEMO_BLOCK_SPACING_CHUNKS = 2;
	private static final int DEMO_ENTITY_SPACING_CHUNKS = STRESS_TEST ? 2 : 4;

	/**
	 * Animated-LOD threshold (blocks) — beyond this, BEs render via the
	 * display-list at-rest path instead of Tessellator-driven renderAnimated.
	 *
	 * <p>Resolved per-call so the value tracks the player's render distance
	 * setting:
	 * <ul>
	 *   <li>{@code -Daero.animatedLOD=N}: explicit override</li>
	 *   <li>else, stress mode: scaled via {@link aero.modellib.Aero_AnimationTickLOD#recommendedAnimatedDistance}</li>
	 *   <li>else: 48 blocks (the historical default for showcase density)</li>
	 * </ul>
	 */
	public static double demoAnimatedLodDistance() {
		String override = System.getProperty("aero.animatedLOD");
		if (override != null) return Double.parseDouble(override);
		if (STRESS_TEST) {
			return aero.modellib.Aero_AnimationTickLOD.recommendedAnimatedDistance(
				aero.modellib.Aero_RenderDistance.currentViewDistance());
		}
		return 48d;
	}

	// Robot spawn egg — item ID picked above the vanilla 256-block range so
	// it doesn't collide with anything else. The item icon is the default
	// (no addOverride applied) since the lib's showcase doesn't ship a
	// dedicated 16×16 sprite.
	public static final Item robotEgg =
		new AeroTest_RobotEggItem(2200).setItemName("aerotest.robotEgg");

	public mod_RetronismAeroTest() {
		// Blocks
		ModLoader.RegisterBlock(motorBlock);
		ModLoader.RegisterBlock(pumpBlock);
		ModLoader.RegisterBlock(crystalBlock);
		ModLoader.RegisterBlock(crystalChaosBlock);
		ModLoader.RegisterBlock(easingShowcaseBlock);
		ModLoader.RegisterBlock(easingShowcase2Block);
		ModLoader.RegisterBlock(easingShowcase3Block);
		ModLoader.RegisterBlock(plasmaCrystalBlock);
		ModLoader.RegisterBlock(conveyorBlock);
		ModLoader.RegisterBlock(spellCircleBlock);
		ModLoader.RegisterBlock(turretIkBlock);
		ModLoader.RegisterBlock(morphCrystalBlock);
		ModLoader.RegisterBlock(graphPoweredBlock);
		ModLoader.RegisterBlock(megaModelBlock);
		ModLoader.RegisterBlock(animatedMegaModelBlock);

		// Tile entities + their renderers
		ModLoader.RegisterTileEntity(AeroTest_MotorTile.class,           "aerotest:motor",            new AeroTest_MotorRenderer());
		ModLoader.RegisterTileEntity(AeroTest_PumpTile.class,            "aerotest:pump",             new AeroTest_PumpRenderer());
		ModLoader.RegisterTileEntity(AeroTest_CrystalTile.class,         "aerotest:crystal",          new AeroTest_CrystalRenderer());
		ModLoader.RegisterTileEntity(AeroTest_CrystalChaosTile.class,    "aerotest:crystal_chaos",    new AeroTest_CrystalChaosRenderer());
		ModLoader.RegisterTileEntity(AeroTest_EasingShowcaseTile.class,  "aerotest:easing_showcase",  new AeroTest_EasingShowcaseRenderer());
		ModLoader.RegisterTileEntity(AeroTest_EasingShowcase2Tile.class, "aerotest:easing_showcase_2",new AeroTest_EasingShowcase2Renderer());
		ModLoader.RegisterTileEntity(AeroTest_EasingShowcase3Tile.class, "aerotest:easing_showcase_3",new AeroTest_EasingShowcase3Renderer());
		ModLoader.RegisterTileEntity(AeroTest_PlasmaCrystalTile.class,   "aerotest:plasma_crystal",   new AeroTest_PlasmaCrystalRenderer());
		ModLoader.RegisterTileEntity(AeroTest_ConveyorTile.class,        "aerotest:conveyor",         new AeroTest_ConveyorRenderer());
		ModLoader.RegisterTileEntity(AeroTest_SpellCircleTile.class,     "aerotest:spell_circle",     new AeroTest_SpellCircleRenderer());
		ModLoader.RegisterTileEntity(AeroTest_TurretIKTile.class,        "aerotest:turret_ik",        new AeroTest_TurretIKRenderer());
		ModLoader.RegisterTileEntity(AeroTest_MorphCrystalTile.class,    "aerotest:morph_crystal",    new AeroTest_MorphCrystalRenderer());
		ModLoader.RegisterTileEntity(AeroTest_GraphPoweredTile.class,    "aerotest:graph_powered",    new AeroTest_GraphPoweredRenderer());
		ModLoader.RegisterTileEntity(AeroTest_MegaModelTile.class,         "aerotest:mega_model",          new AeroTest_MegaModelRenderer());
		ModLoader.RegisterTileEntity(AeroTest_AnimatedMegaModelTile.class, "aerotest:mega_model_animated", new AeroTest_AnimatedMegaModelRenderer());

		// Entity
		ModLoader.RegisterEntityID(AeroTest_RobotEntity.class, "aerotest:robot", ROBOT_ENTITY_ID);

		// Names
		ModLoader.AddName(motorBlock,           "AeroTest Motor");
		ModLoader.AddName(pumpBlock,            "AeroTest Pump");
		ModLoader.AddName(crystalBlock,         "AeroTest Crystal");
		ModLoader.AddName(crystalChaosBlock,    "AeroTest Crystal Chaos");
		ModLoader.AddName(easingShowcaseBlock,  "AeroTest Easing Showcase");
		ModLoader.AddName(easingShowcase2Block, "AeroTest Easing Showcase 2");
		ModLoader.AddName(easingShowcase3Block, "AeroTest Easing Showcase 3");
		ModLoader.AddName(plasmaCrystalBlock,   "AeroTest Plasma Crystal");
		ModLoader.AddName(conveyorBlock,        "AeroTest Conveyor (UV Anim)");
		ModLoader.AddName(spellCircleBlock,    "AeroTest Spell Circle (UV combo)");
		ModLoader.AddName(turretIkBlock,       "AeroTest Turret (IK CCD)");
		ModLoader.AddName(morphCrystalBlock,   "AeroTest Morph Crystal");
		ModLoader.AddName(graphPoweredBlock,   "AeroTest Graph Powered (redstone)");
		ModLoader.AddName(megaModelBlock,         "AeroTest Mega Model (static)");
		ModLoader.AddName(animatedMegaModelBlock, "AeroTest Mega Model (animated)");

		// Spawn egg item
		ModLoader.AddName(robotEgg, "AeroTest Robot Egg");
	}

	public void AddRenderer(Map<Class<? extends Entity>, Render> renderers) {
		renderers.put(AeroTest_RobotEntity.class, new AeroTest_RobotEntityRenderer());
	}

	public String Version() {
		return "0.1.0";
	}

	// -----------------------------------------------------------------------
	// World-gen showcase placement (mirrors stationapi AeroTestMod.populateChunk)
	// -----------------------------------------------------------------------
	//
	// ModLoader fires {@link #generateSurface(World, Random, int, int)} for
	// every newly generated chunk. We only place in qualifying chunks (every
	// DEMO_BLOCK_SPACING_CHUNKSth) so the showcase is searchable but the world
	// stays mostly natural. Each qualifying chunk receives all 13 demo blocks
	// laid out on a 16-block grid; in stress mode, an extra 3×3 motor grid
	// is added in the chunk's free quadrant for raw render-throughput
	// measurement (mirror of the stationapi stress build).
	@Override
	public void GenerateSurface(World world, Random random, int chunkX, int chunkZ) {
		int blockChunkX = chunkX >> 4;
		int blockChunkZ = chunkZ >> 4;
		if (Math.floorMod(blockChunkX, DEMO_BLOCK_SPACING_CHUNKS) != 0) return;
		if (Math.floorMod(blockChunkZ, DEMO_BLOCK_SPACING_CHUNKS) != 0) return;

		// Showcase grid — same layout as stationapi AeroTestMod for visual parity.
		// motor + pump + mega-equivalent slots use placeAtSurface; crystals get
		// one block of clearance via placeAbove so the scale animation doesn't
		// clip into terrain.
		placeAtSurface(world, chunkX, 8,  chunkZ, 8,  megaModelBlock.blockID,         new AeroTest_MegaModelTile());
		placeAtSurface(world, chunkX, 4,  chunkZ, 8,  animatedMegaModelBlock.blockID, new AeroTest_AnimatedMegaModelTile());
		placeAtSurface(world, chunkX, 12, chunkZ, 8,  motorBlock.blockID,             new AeroTest_MotorTile());
		placeAtSurface(world, chunkX, 12, chunkZ, 12, pumpBlock.blockID,              new AeroTest_PumpTile());

		placeAbove(world, chunkX, 8,  chunkZ, 12, crystalBlock.blockID,         new AeroTest_CrystalTile());
		placeAbove(world, chunkX, 4,  chunkZ, 12, crystalChaosBlock.blockID,    new AeroTest_CrystalChaosTile());
		placeAbove(world, chunkX, 0,  chunkZ, 12, easingShowcaseBlock.blockID,  new AeroTest_EasingShowcaseTile());
		placeAbove(world, chunkX, 0,  chunkZ, 8,  easingShowcase2Block.blockID, new AeroTest_EasingShowcase2Tile());
		placeAbove(world, chunkX, 0,  chunkZ, 4,  easingShowcase3Block.blockID, new AeroTest_EasingShowcase3Tile());
		placeAbove(world, chunkX, 12, chunkZ, 0,  plasmaCrystalBlock.blockID,   new AeroTest_PlasmaCrystalTile());
		placeAbove(world, chunkX, 0,  chunkZ, 0,  conveyorBlock.blockID,        new AeroTest_ConveyorTile());
		placeAbove(world, chunkX, 4,  chunkZ, 0,  spellCircleBlock.blockID,     new AeroTest_SpellCircleTile());
		placeAbove(world, chunkX, 8,  chunkZ, 0,  turretIkBlock.blockID,        new AeroTest_TurretIKTile());
		placeAbove(world, chunkX, 8,  chunkZ, 4,  morphCrystalBlock.blockID,    new AeroTest_MorphCrystalTile());
		placeAbove(world, chunkX, 12, chunkZ, 4,  graphPoweredBlock.blockID,    new AeroTest_GraphPoweredTile());

		// Stress mode: 3×3 motor grid in the chunk's free quadrant. placeAbove
		// avoids overwriting surface terrain and keeps neighbor block updates
		// shallow (a 4×4 grid via placeAtSurface caused a worldgen-cascade
		// StackOverflowError on the stationapi side).
		if (STRESS_TEST) {
			for (int gx = 0; gx < 3; gx++) {
				for (int gz = 0; gz < 3; gz++) {
					int sx = 1 + gx * 3;     // 1, 4, 7
					int sz = 1 + gz * 3;
					placeAbove(world, chunkX, sx, chunkZ, sz, motorBlock.blockID, new AeroTest_MotorTile());
				}
			}

			// 3×3×3 stack of AnimatedMegaModel (27 BEs/chunk) for heavy-render
			// stress: per-frame Tessellator cost × 27 instances × N chunks.
			// Placed in the chunk's far corner (13-15, 13-15) so it doesn't
			// collide with the showcase grid. 3-block vertical spacing leaves
			// breathing room for the 4-block render radius.
			for (int sx = 0; sx < 3; sx++) {
				for (int sz = 0; sz < 3; sz++) {
					int x = chunkX + 13 + sx;
					int z = chunkZ + 13 + sz;
					int baseY = world.getHeightValue(x, z) + 2;
					for (int sy = 0; sy < 3; sy++) {
						int y = baseY + sy * 3;
						world.setBlockWithNotify(x, y, z, animatedMegaModelBlock.blockID);
						world.setBlockTileEntity(x, y, z, new AeroTest_AnimatedMegaModelTile());
					}
				}
			}
		}

		// Robot entity — every DEMO_ENTITY_SPACING_CHUNKS chunks, server-side only.
		// Disabled by default in modloader: a known TMI/SPC EntityRenderer
		// reobf mismatch fires NoSuchMethodError on `Entity.setPosition(DDD)V`
		// when the proxy renders custom mob entities. Re-enable by setting
		// `-Daero.spawnRobot=true` once retronism's vanilla EntityRenderer
		// patch is validated.
		boolean spawnRobot = Boolean.getBoolean("aero.spawnRobot");
		if (spawnRobot && !world.multiplayerWorld
				&& Math.floorMod(blockChunkX, DEMO_ENTITY_SPACING_CHUNKS) == 0
				&& Math.floorMod(blockChunkZ, DEMO_ENTITY_SPACING_CHUNKS) == 0) {
			int ex = chunkX + 12;
			int ez = chunkZ + 4;
			int ey = world.getHeightValue(ex, ez) + 2;
			AeroTest_RobotEntity robot = new AeroTest_RobotEntity(world);
			robot.setLocationAndAngles(ex + 0.5, ey, ez + 0.5, 0f, 0f);
			world.entityJoinedWorld(robot);
		}
	}

	/** Places a block + its TileEntity on top of the chunk column. */
	private static void placeAtSurface(World world, int chunkX, int dx, int chunkZ, int dz,
	                                   int blockId, TileEntity te) {
		int x = chunkX + dx;
		int z = chunkZ + dz;
		int y = world.getHeightValue(x, z);
		world.setBlockWithNotify(x, y, z, blockId);
		world.setBlockTileEntity(x, y, z, te);
	}

	/** Like {@link #placeAtSurface}, but with one extra block of clearance above. */
	private static void placeAbove(World world, int chunkX, int dx, int chunkZ, int dz,
	                               int blockId, TileEntity te) {
		int x = chunkX + dx;
		int z = chunkZ + dz;
		int y = world.getHeightValue(x, z) + 1;
		world.setBlockWithNotify(x, y, z, blockId);
		world.setBlockTileEntity(x, y, z, te);
	}
}
