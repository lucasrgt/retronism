package retronism;

import net.minecraft.src.*;
import retronism.aerotest.*;

import java.util.Map;

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

	private static final int ROBOT_ENTITY_ID = ModLoader.getUniqueEntityId();

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

		// Spawn egg item
		ModLoader.AddName(robotEgg, "AeroTest Robot Egg");
	}

	public void AddRenderer(Map<Class<? extends Entity>, Render> renderers) {
		renderers.put(AeroTest_RobotEntity.class, new AeroTest_RobotEntityRenderer());
	}

	public String Version() {
		return "0.1.0";
	}
}
