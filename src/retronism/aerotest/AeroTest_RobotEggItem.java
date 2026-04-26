package retronism.aerotest;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

/**
 * Spawn egg for {@link AeroTest_RobotEntity}. Right-click any block to drop
 * a robot one block above the clicked face. Single-stack item, no
 * texture override applied — uses the placeholder item icon (the lib's
 * showcase doesn't ship a 16×16 sprite for this egg).
 */
public class AeroTest_RobotEggItem extends Item {

	public AeroTest_RobotEggItem(int id) {
		super(id);
		this.maxStackSize = 64;
		this.setItemName("aerotest.robotEgg");
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
	                          int x, int y, int z, int side) {
		if (world.multiplayerWorld) return true;
		double sx = x + 0.5;
		double sy = y + 1.0;
		double sz = z + 0.5;
		AeroTest_RobotEntity bot = new AeroTest_RobotEntity(world);
		bot.setPositionAndRotation(sx, sy, sz, player.rotationYaw + 180f, 0f);
		world.entityJoinedWorld(bot);
		stack.stackSize--;
		return true;
	}
}
