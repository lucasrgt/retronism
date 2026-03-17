package retronism.tile;

import net.minecraft.src.*;
import aero.machineapi.*;
import retronism.Retronism_Registry;

public class Retronism_TileHeavyCrusher extends TileEntity implements IInventory, Aero_IEnergyReceiver, Aero_ISlotAccess {

    private ItemStack[] inventory = new ItemStack[12];
    public boolean isFormed = false;

    private int storedEnergy = 0;
    private int maxEnergy = 64000;

    public int processTime = 0;
    public int maxProcessTime = 200;
    private int energyPerTick = 32;

    // Block type constants for STRUCTURE array
    private static final int TYPE_AIR = 0;
    private static final int TYPE_CONTROLLER = 1;
    private static final int TYPE_ADVANCED_MACHINE_BLOCK = 2;
    private static final int TYPE_MACHINE_BLOCK = 3;

    // Structure definition: [layer Y][row Z][col X]
    // 2 = advanced_machine_block (corners), 3 = machine_block (edges/faces), 1 = controller
    private static final int[][][] STRUCTURE = {
        { // Layer 0 (bottom)
            {2, 3, 2},
            {3, 3, 3},
            {2, 3, 2},
        },
        { // Layer 1 (middle — controller at z=2, x=1)
            {3, 3, 3},
            {3, 3, 3},
            {3, 1, 3},
        },
        { // Layer 2 (top)
            {2, 3, 2},
            {3, 3, 3},
            {2, 3, 2},
        },
    };

    // Port definitions: {structX, structY, structZ, portType, portMode}
    // Matches HeavyCrusher.json portTypes
    private static final int[][] PORTS = {
        {1, 0, 1, Aero_PortRegistry.PORT_TYPE_ENERGY, Aero_PortRegistry.PORT_MODE_INPUT},    // bottom center
        {1, 1, 0, Aero_PortRegistry.PORT_TYPE_ENERGY, Aero_PortRegistry.PORT_MODE_INPUT},    // north center
        {0, 1, 1, Aero_PortRegistry.PORT_TYPE_ITEM, Aero_PortRegistry.PORT_MODE_INPUT},      // west center
        {2, 1, 1, Aero_PortRegistry.PORT_TYPE_ITEM, Aero_PortRegistry.PORT_MODE_OUTPUT},     // east center
        {1, 2, 1, Aero_PortRegistry.PORT_TYPE_ITEM, Aero_PortRegistry.PORT_MODE_INPUT},      // top center
    };

    private int formedRotation = -1;
    private boolean portsRegistered = false;

    // Controller position in STRUCTURE (found once)
    private static final int CTRL_X, CTRL_Y, CTRL_Z;
    static {
        int cx = -1, cy = -1, cz = -1;
        for (int y = 0; y < STRUCTURE.length; y++)
            for (int z = 0; z < STRUCTURE[y].length; z++)
                for (int x = 0; x < STRUCTURE[y][z].length; x++)
                    if (STRUCTURE[y][z][x] == TYPE_CONTROLLER) { cx = x; cy = y; cz = z; }
        CTRL_X = cx; CTRL_Y = cy; CTRL_Z = cz;
    }

    // Rotation matrices: {cos, sin, -sin, cos} for 0/90/180/270 degrees
    private static final int[][] FACINGS = {
        { 1, 0, 0, 1},
        { 0, 1,-1, 0},
        {-1, 0, 0,-1},
        { 0,-1, 1, 0},
    };

    // --- ISlotAccess (for item pipes) ---
    public int[] getInsertSlots() { return new int[]{0, 1, 2, 3}; }
    public int[] getExtractSlots() { return new int[]{4, 5, 6, 7, 8, 9, 10, 11}; }

    // --- Structure Check ---
    public boolean checkStructure(World world, int cx, int cy, int cz) {
        if (CTRL_X == -1) { isFormed = false; return false; }

        int machineBlockId = Block.blockSteel.blockID;       // iron block (42)
        int advMachineBlockId = Block.blockGold.blockID;     // gold block (41)
        int controllerBlockId = Retronism_Registry.heavyCrusherControllerBlock.blockID;
        int portBlockId = Retronism_Registry.machinePortBlock.blockID;

        for (int f = 0; f < 4; f++) {
            boolean ok = true;
            for (int sy = 0; sy < STRUCTURE.length && ok; sy++) {
                for (int sz = 0; sz < STRUCTURE[sy].length && ok; sz++) {
                    for (int sx = 0; sx < STRUCTURE[sy][sz].length && ok; sx++) {
                        int expected = STRUCTURE[sy][sz][sx];
                        if (expected == TYPE_AIR) continue;

                        int relX = sx - CTRL_X;
                        int relZ = sz - CTRL_Z;
                        int wx = cx + relX * FACINGS[f][0] + relZ * FACINGS[f][2];
                        int wy = cy + (sy - CTRL_Y);
                        int wz = cz + relX * FACINGS[f][1] + relZ * FACINGS[f][3];
                        int blockId = world.getBlockId(wx, wy, wz);

                        boolean match;
                        if (expected == TYPE_CONTROLLER) {
                            match = (blockId == controllerBlockId);
                        } else if (expected == TYPE_MACHINE_BLOCK) {
                            match = (blockId == machineBlockId || blockId == portBlockId);
                        } else if (expected == TYPE_ADVANCED_MACHINE_BLOCK) {
                            match = (blockId == advMachineBlockId);
                        } else {
                            match = false;
                        }

                        if (!match) ok = false;
                    }
                }
            }
            if (ok) {
                boolean wasFormed = isFormed;
                isFormed = true;
                formedRotation = f;
                if (!wasFormed) {
                    registerPorts(world, cx, cy, cz, FACINGS[f]);
                    applyPortBlocks(world, cx, cy, cz, FACINGS[f]);
                }
                return true;
            }
        }

        if (isFormed) {
            unregisterPorts(cx, cy, cz);
            restorePortBlocks(world, cx, cy, cz);
        }
        isFormed = false;
        formedRotation = -1;
        return false;
    }

    private void registerPorts(World world, int cx, int cy, int cz, int[] facing) {
        for (int[] port : PORTS) {
            int relX = port[0] - CTRL_X, relZ = port[2] - CTRL_Z;
            int wx = cx + relX * facing[0] + relZ * facing[2];
            int wy = cy + (port[1] - CTRL_Y);
            int wz = cz + relX * facing[1] + relZ * facing[3];
            Aero_PortRegistry.registerPort(wx, wy, wz, cx, cy, cz, port[3], port[4]);
        }
        portsRegistered = true;
    }

    private void unregisterPorts(int cx, int cy, int cz) {
        Aero_PortRegistry.unregisterAllForController(cx, cy, cz);
        portsRegistered = false;
    }

    /** Replace shell blocks at port positions with machinePortBlock + set metadata for visual */
    private void applyPortBlocks(World world, int cx, int cy, int cz, int[] facing) {
        int portBlockId = Retronism_Registry.machinePortBlock.blockID;
        for (int[] port : PORTS) {
            int relX = port[0] - CTRL_X, relZ = port[2] - CTRL_Z;
            int wx = cx + relX * facing[0] + relZ * facing[2];
            int wy = cy + (port[1] - CTRL_Y);
            int wz = cz + relX * facing[1] + relZ * facing[3];
            // metadata: energy=0, fluid=1, gas=2, item=3
            int meta = port[3] - 1; // PORT_TYPE_ENERGY=1->0, FLUID=2->1, GAS=3->2, ITEM=4->3
            world.setBlockAndMetadataWithNotify(wx, wy, wz, portBlockId, meta);
        }
    }

    /** Restore original shell blocks when structure breaks */
    private void restorePortBlocks(World world, int cx, int cy, int cz) {
        if (formedRotation < 0) return;
        int[] facing = FACINGS[formedRotation];
        int machineBlockId = Block.blockSteel.blockID;
        for (int[] port : PORTS) {
            int relX = port[0] - CTRL_X, relZ = port[2] - CTRL_Z;
            int wx = cx + relX * facing[0] + relZ * facing[2];
            int wy = cy + (port[1] - CTRL_Y);
            int wz = cz + relX * facing[1] + relZ * facing[3];
            // Ports are always at machine_block positions (TYPE_MACHINE_BLOCK)
            if (world.getBlockId(wx, wy, wz) == Retronism_Registry.machinePortBlock.blockID) {
                world.setBlockWithNotify(wx, wy, wz, machineBlockId);
            }
        }
    }

    private int recheckTimer = 0;

    @Override
    public void updateEntity() {
        if (worldObj.multiplayerWorld) return;

        // Re-register ports after world load
        if (isFormed && !portsRegistered && formedRotation >= 0) {
            registerPorts(worldObj, xCoord, yCoord, zCoord, FACINGS[formedRotation]);
        }

        // Periodically recheck structure integrity
        if (isFormed && ++recheckTimer >= 20) {
            recheckTimer = 0;
            checkStructure(worldObj, xCoord, yCoord, zCoord);
        }

        if (!isFormed) return;

        boolean canProcess = canProcess();
        if (canProcess && storedEnergy >= energyPerTick) {
            storedEnergy -= energyPerTick;
            processTime++;
            if (processTime >= maxProcessTime) {
                processTime = 0;
                processItem();
            }
        } else if (!canProcess) {
            processTime = 0;
        }
    }

    private boolean canProcess() {
        if (inventory[0] == null) return false;
        // TODO: Add recipe lookup here
        return true;
    }

    private void processItem() {
        // TODO: Add recipe processing here
    }

    // --- IInventory ---
    @Override
    public int getSizeInventory() { return inventory.length; }

    @Override
    public ItemStack getStackInSlot(int slot) { return inventory[slot]; }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] != null) {
            if (inventory[slot].stackSize <= amount) {
                ItemStack stack = inventory[slot];
                inventory[slot] = null;
                return stack;
            }
            ItemStack split = inventory[slot].splitStack(amount);
            if (inventory[slot].stackSize == 0) inventory[slot] = null;
            return split;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public String getInvName() { return "HeavyCrusher"; }

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    // --- IEnergyReceiver ---
    @Override
    public int receiveEnergy(int amount) {
        int accepted = Math.min(amount, maxEnergy - storedEnergy);
        storedEnergy += accepted;
        return accepted;
    }

    @Override
    public int getStoredEnergy() { return storedEnergy; }

    @Override
    public int getMaxEnergy() { return maxEnergy; }

    public int getEnergyScaled(int scale) {
        return maxEnergy > 0 ? storedEnergy * scale / maxEnergy : 0;
    }

    public int getCookProgressScaled(int scale) {
        return maxProcessTime > 0 ? processTime * scale / maxProcessTime : 0;
    }

    // --- NBT ---
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        isFormed = nbt.getBoolean("Formed");
        formedRotation = nbt.getInteger("FormedRotation");
        storedEnergy = nbt.getInteger("Energy");
        processTime = nbt.getShort("ProcessTime");

        NBTTagList items = nbt.getTagList("Items");
        inventory = new ItemStack[12];
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound slot = (NBTTagCompound) items.tagAt(i);
            int idx = slot.getByte("Slot") & 255;
            if (idx < inventory.length) {
                int id = slot.getShort("id");
                int count = slot.getByte("Count");
                int dmg = slot.getShort("Damage");
                inventory[idx] = new ItemStack(id, count, dmg);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("Formed", isFormed);
        nbt.setInteger("FormedRotation", formedRotation);
        nbt.setInteger("Energy", storedEnergy);
        nbt.setShort("ProcessTime", (short) processTime);

        NBTTagList items = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound slot = new NBTTagCompound();
                slot.setByte("Slot", (byte) i);
                slot.setShort("id", (short) inventory[i].itemID);
                slot.setByte("Count", (byte) inventory[i].stackSize);
                slot.setShort("Damage", (short) inventory[i].getItemDamage());
                items.setTag(slot);
            }
        }
        nbt.setTag("Items", items);
    }
}
