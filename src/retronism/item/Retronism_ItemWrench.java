package retronism.item;

import net.minecraft.src.*;
import retronism.*;
import aero.machineapi.*;
import retronism.tile.*;
import retronism.gui.*;

public class Retronism_ItemWrench extends Item {

	public Retronism_ItemWrench(int id) {
		super(id);
		this.maxStackSize = 1;
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side) {
		return refinery.Wrench.use(player, world, x, y, z, side);
	}
}
