package retronism.block;

import net.minecraft.src.*;
import aero.machineapi.Aero_PortRegistry;
import retronism.tile.Retronism_TileHeavyCrusher;
import retronism.gui.Retronism_GuiHeavyCrusher;

public class Retronism_BlockHeavyCrusherController extends BlockContainer {
    public Retronism_BlockHeavyCrusherController(int id, int tex) {
        super(id, tex, Material.iron);
        setHardness(3.5F);
        setResistance(5.0F);
        setStepSound(Block.soundMetalFootstep);
        setBlockName("heavyCrusherController");
    }

    @Override
    protected TileEntity getBlockEntity() {
        return new Retronism_TileHeavyCrusher();
    }

    @Override
    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
        if (world.multiplayerWorld) return true;
        Retronism_TileHeavyCrusher tile = (Retronism_TileHeavyCrusher) world.getBlockTileEntity(x, y, z);
        if (tile == null) return false;

        if (!tile.checkStructure(world, x, y, z)) {
            player.addChatMessage("Structure incomplete!");
            return true;
        }

        ModLoader.OpenGUI(player, new Retronism_GuiHeavyCrusher(player.inventory, tile));
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, int neighborId) {
        Retronism_TileHeavyCrusher tile = (Retronism_TileHeavyCrusher) world.getBlockTileEntity(x, y, z);
        if (tile != null) {
            tile.checkStructure(world, x, y, z);
        }
    }

    @Override
    public void onBlockRemoval(World world, int x, int y, int z) {
        Aero_PortRegistry.unregisterAllForController(x, y, z);
        Retronism_TileHeavyCrusher tile = (Retronism_TileHeavyCrusher) world.getBlockTileEntity(x, y, z);
        if (tile != null) {
            tile.isFormed = false;
            for (int i = 0; i < tile.getSizeInventory(); i++) {
                ItemStack stack = tile.getStackInSlot(i);
                if (stack != null) {
                    float rx = world.rand.nextFloat() * 0.6F + 0.1F;
                    float ry = world.rand.nextFloat() * 0.6F + 0.1F;
                    float rz = world.rand.nextFloat() * 0.6F + 0.1F;
                    EntityItem entity = new EntityItem(world, x + rx, y + ry, z + rz, stack);
                    world.entityJoinedWorld(entity);
                }
            }
        }
        super.onBlockRemoval(world, x, y, z);
    }
}
