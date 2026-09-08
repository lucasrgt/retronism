package refinery;

import net.minecraft.src.*;
import aero.machineapi.*;

/** The master owns all persisted state. Parts carry only their address and local cell. */
public final class RefineryTile extends TileEntity implements IInventory, Aero_ISlotAccess, Aero_ISideConfigurable, Aero_IEnergyReceiver {
    public int mx, my, mz, lx, ly, lz, facing, energy, progress;
    public boolean configured, dismantling;
    private final ItemStack[] inventory = new ItemStack[3];
    private final int[] sideConfig = new int[24];
    private static final int[] NONE = {}, LEFT = {0}, RIGHT = {1}, OUTPUT = {2};
    public int[] getSideConfig() { return sideConfig; }
    public void setSideMode(int side, int type, int mode) { /* Geometry fixes the ports. */ }
    public boolean supportsType(int type) { return type == Aero_SideConfig.TYPE_ITEM || type == Aero_SideConfig.TYPE_ENERGY; }
    public int[] getAllowedModes(int type) { return new int[]{Aero_SideConfig.MODE_NONE}; }
    public int[] getInsertSlots() {
        int kind = Ports.kind(this, NetworkPorts.face(this));
        return kind == 0 ? LEFT : kind == 1 ? RIGHT : NONE;
    }
    public int[] getExtractSlots() { return Ports.kind(this, NetworkPorts.face(this)) == 2 ? OUTPUT : NONE; }
    public int receiveEnergy(int amount) { return NetworkPorts.receive(this, amount); }
    public int getStoredEnergy() { RefineryTile value = master(); return value == null ? 0 : value.energy; }
    public int getMaxEnergy() { return Ports.kind(this, NetworkPorts.face(this)) == 3 ? 1600 : 0; }
    public static int fuelId;
    public void configure(int x, int y, int z, int a, int b, int c, int direction) {
        mx = x; my = y; mz = z; lx = a; ly = b; lz = c; facing = direction; configured = true;
        NetworkPorts.configure(this, sideConfig);
        onInventoryChanged();
    }
    public boolean isMaster() { return configured && xCoord == mx && yCoord == my && zCoord == mz; }
    public RefineryTile master() {
        if (!configured || worldObj == null || !worldObj.blockExists(mx, my, mz)) return null;
        TileEntity value = worldObj.getBlockTileEntity(mx, my, mz);
        return value instanceof RefineryTile && ((RefineryTile) value).isMaster() ? (RefineryTile) value : null;
    }
    @Override public void updateEntity() {
        if (!isMaster() || dismantling || worldObj.multiplayerWorld || !Structure.intact(this)) return;
        if (!recipeReady() || energy < 8) return;
        energy -= 8;
        if (++progress == 20) {
            consume(0); consume(1);
            if (inventory[2] == null) inventory[2] = new ItemStack(fuelId, 1, 0); else inventory[2].stackSize++;
            progress = 0;
        }
        onInventoryChanged();
    }
    private boolean recipeReady() {
        return inventory[0] != null && inventory[0].itemID == Item.seeds.shiftedIndex
            && inventory[1] != null && inventory[1].itemID == Item.sugar.shiftedIndex
            && (inventory[2] == null || inventory[2].itemID == fuelId && inventory[2].stackSize < 64);
    }
    private void consume(int slot) { if (--inventory[slot].stackSize == 0) inventory[slot] = null; }
    public String status() { return "Refinery: " + energy + "/1600 energy, " + progress + "/20 ticks, fuel " + count(2); }
    public int count(int slot) { return inventory[slot] == null ? 0 : inventory[slot].stackSize; }
    public int getSizeInventory() { return 3; }
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= 3) return null;
        RefineryTile owner = isMaster() ? this : master();
        return owner == null || owner.dismantling ? null : owner.inventory[slot];
    }
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot < 0 || slot >= 3) return null;
        if (!isMaster()) return slot == 2 ? Ports.extract(this, NetworkPorts.face(this), amount) : null;
        if (amount <= 0 || inventory[slot] == null) return null;
        ItemStack result = inventory[slot].splitStack(Math.min(amount, inventory[slot].stackSize));
        if (inventory[slot].stackSize == 0) inventory[slot] = null;
        onInventoryChanged(); return result;
    }
    public void setInventorySlotContents(int slot, ItemStack value) {
        if (slot < 0 || slot >= 3) return;
        if (!isMaster()) { throw new IllegalStateException("Insert through the refinery port transfer API"); }
        inventory[slot] = value;
        if (value != null) value.stackSize = Math.min(64, value.stackSize);
        onInventoryChanged();
    }
    public String getInvName() { return "Refinery"; }
    public int getInventoryStackLimit() { return 64; }
    public boolean canInteractWith(EntityPlayer player) {
        return !dismantling && master() == this && getDistanceFrom(player.posX, player.posY, player.posZ) <= 64;
    }
    public void dropInventory() {
        for (int i = 0; i < 3; i++) {
            ItemStack stack = inventory[i]; inventory[i] = null;
            if (stack != null) worldObj.entityJoinedWorld(new EntityItem(worldObj, mx + .5, my + .5, mz + .5, stack));
        }
        energy = 0; progress = 0; onInventoryChanged();
    }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("Configured", configured); tag.setInteger("MasterX", mx); tag.setInteger("MasterY", my); tag.setInteger("MasterZ", mz);
        tag.setInteger("LocalX", lx); tag.setInteger("LocalY", ly); tag.setInteger("LocalZ", lz); tag.setInteger("Facing", facing);
        tag.setInteger("Energy", energy); tag.setInteger("Progress", progress);
        NBTTagList stacks = new NBTTagList();
        for (int i = 0; i < 3; i++) if (inventory[i] != null) {
            NBTTagCompound stack = new NBTTagCompound(); stack.setByte("Slot", (byte)i); inventory[i].writeToNBT(stack); stacks.setTag(stack);
        }
        tag.setTag("Items", stacks);
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        configured = tag.getBoolean("Configured"); mx = tag.getInteger("MasterX"); my = tag.getInteger("MasterY"); mz = tag.getInteger("MasterZ");
        lx = tag.getInteger("LocalX"); ly = tag.getInteger("LocalY"); lz = tag.getInteger("LocalZ"); facing = tag.getInteger("Facing") & 3;
        NetworkPorts.configure(this, sideConfig);
        energy = Math.max(0, Math.min(1600, tag.getInteger("Energy"))); progress = Math.max(0, Math.min(19, tag.getInteger("Progress")));
        for (int i = 0; i < 3; i++) inventory[i] = null;
        NBTTagList stacks = tag.getTagList("Items");
        for (int i = 0; i < stacks.tagCount(); i++) {
            NBTTagCompound stack = (NBTTagCompound) stacks.tagAt(i); int slot = stack.getByte("Slot");
            if (slot >= 0 && slot < 3) inventory[slot] = new ItemStack(stack);
        }
    }
}
