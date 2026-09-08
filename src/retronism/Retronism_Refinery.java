package retronism;
import net.minecraft.src.*;

import refinery.*;
import refinery.client.RefineryRenderer;

/** Registered by mod_Retronism; the common machine logic has no renderer dependency. */
public final class Retronism_Refinery {
    public static final Item WRENCH = Retronism_Registry.wrench;
    public static final Item FUEL = new Item(2001).setIconIndex(7).setItemName("refinery.fuel");
    public static void register() {
        String[] names = {"Steel scaffolding", "Light engineering", "Fluid pipe", "Heavy engineering", "Iron sheetmetal", "Redstone engineering"};
        int[] textures = {22, 23, 1, 22, 22, 28};
        Object[] centers = {Block.cobblestone, Item.ingotGold, Item.bucketEmpty, Block.stoneOvenIdle, Block.stone, Item.redstone};
        for (int i = 0; i < names.length; i++) {
            Block block = new Block(240 + i, textures[i], Material.iron).setHardness(3).setBlockName("refinery.component" + i);
            ModLoader.RegisterBlock(block); ModLoader.AddName(block, names[i]);
            ModLoader.AddRecipe(new ItemStack(block, 4), new Object[]{"II", "IR", Character.valueOf('I'), Item.ingotIron, Character.valueOf('R'), centers[i]});
        }
        RefineryBlock block = new RefineryBlock(); ModLoader.RegisterBlock(block); ModLoader.AddName(block, "Formed refinery");
        RefineryTile.fuelId = FUEL.shiftedIndex; RefineryBlock.wrenchId = WRENCH.shiftedIndex;
        ModLoader.RegisterTileEntity(RefineryTile.class, "refinery.machine.v1", new RefineryRenderer());
        ModLoader.AddName(FUEL, "Biofuel pellet");
        System.out.println("RETRONISM_REFINERY_READY=0.2.0");
    }

}
