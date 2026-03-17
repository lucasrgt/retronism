package retronism.container;

import net.minecraft.src.*;
import retronism.slot.Retronism_SlotHeavyCrusherOutput;
import retronism.tile.Retronism_TileHeavyCrusher;

public class Retronism_ContainerHeavyCrusher extends Container {

    private Retronism_TileHeavyCrusher tile;
    private int lastEnergy = -1;
    private int lastProcessTime = -1;
    private int lastMaxProcessTime = -1;

    public Retronism_ContainerHeavyCrusher(InventoryPlayer playerInv, Retronism_TileHeavyCrusher tile) {
        this.tile = tile;

        // Machine slots (4 input + 8 output)
        addSlot(new Slot(tile, 0, 37, 23));
        addSlot(new Slot(tile, 1, 72, 23));
        addSlot(new Slot(tile, 2, 107, 23));
        addSlot(new Slot(tile, 3, 142, 23));
        addSlot(new Retronism_SlotHeavyCrusherOutput(tile, 4, 37, 69));
        addSlot(new Retronism_SlotHeavyCrusherOutput(tile, 5, 72, 69));
        addSlot(new Retronism_SlotHeavyCrusherOutput(tile, 6, 107, 69));
        addSlot(new Retronism_SlotHeavyCrusherOutput(tile, 7, 142, 69));
        addSlot(new Retronism_SlotHeavyCrusherOutput(tile, 8, 37, 91));
        addSlot(new Retronism_SlotHeavyCrusherOutput(tile, 9, 72, 91));
        addSlot(new Retronism_SlotHeavyCrusherOutput(tile, 10, 107, 91));
        addSlot(new Retronism_SlotHeavyCrusherOutput(tile, 11, 142, 91));

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 7 + col * 18, 127 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 7 + col * 18, 185));
        }
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        return tile.canInteractWith(player);
    }

    public void updateCraftingResults() {
        super.updateCraftingResults();
        if (lastEnergy != tile.getStoredEnergy()) {
            lastEnergy = tile.getStoredEnergy();
            for (int j = 0; j < this.field_20121_g.size(); j++) {
                ((ICrafting)this.field_20121_g.get(j)).func_20158_a(this, 0, lastEnergy);
            }
        }
        if (lastProcessTime != tile.processTime) {
            lastProcessTime = tile.processTime;
            for (int j = 0; j < this.field_20121_g.size(); j++) {
                ((ICrafting)this.field_20121_g.get(j)).func_20158_a(this, 1, lastProcessTime);
            }
        }
        if (lastMaxProcessTime != tile.maxProcessTime) {
            lastMaxProcessTime = tile.maxProcessTime;
            for (int j = 0; j < this.field_20121_g.size(); j++) {
                ((ICrafting)this.field_20121_g.get(j)).func_20158_a(this, 2, lastMaxProcessTime);
            }
        }
    }

    public void func_20112_a(int id, int value) {
        if (id == 0) { /* lastEnergy */ }
        if (id == 1) { /* lastProcessTime */ }
        if (id == 2) { /* lastMaxProcessTime */ }
    }

    public ItemStack getStackInSlot(int slotIndex) {
        ItemStack result = null;
        Slot slot = (Slot) this.slots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();
            if (slotIndex < 12) {
                this.func_28125_a(slotStack, 12, 48, true);
            } else {
                this.func_28125_a(slotStack, 0, 4, false);
            }
            if (slotStack.stackSize == 0) slot.putStack((ItemStack) null);
            else slot.onSlotChanged();
            if (slotStack.stackSize == result.stackSize) return null;
            slot.onPickupFromSlot(slotStack);
        }
        return result;
    }
}
