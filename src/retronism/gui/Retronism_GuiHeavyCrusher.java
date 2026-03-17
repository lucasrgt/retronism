package retronism.gui;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import retronism.tile.Retronism_TileHeavyCrusher;
import retronism.container.Retronism_ContainerHeavyCrusher;

public class Retronism_GuiHeavyCrusher extends GuiContainer {

    private Retronism_TileHeavyCrusher tile;
    private int textureID;
    private int mouseX;
    private int mouseY;

    public Retronism_GuiHeavyCrusher(InventoryPlayer playerInv, Retronism_TileHeavyCrusher tile) {
        super(new Retronism_ContainerHeavyCrusher(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 210;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Override
    protected void drawGuiContainerForegroundLayer() {
        fontRenderer.drawString("Heavy Crusher", (xSize - fontRenderer.getStringWidth("Heavy Crusher")) / 2, 6, 4210752);
        fontRenderer.drawString("Inventory", 7, ySize - 96 + 2, 4210752);

        if (!tile.isFormed) {
            fontRenderer.drawString("Structure incomplete!", 8, 20, 0xFF4444);
            return;
        }

        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int relMouseX = this.mouseX - guiLeft;
        int relMouseY = this.mouseY - guiTop;

        String tooltip = null;

        if (relMouseX >= 12 && relMouseX < 20 && relMouseY >= 22 && relMouseY < 108) {
            tooltip = "Energy: " + tile.getStoredEnergy() + " / " + tile.getMaxEnergy() + " RN";
        }

        if (tooltip != null) {
            int tw = this.fontRenderer.getStringWidth(tooltip);
            int tx = relMouseX - tw - 5;
            if (tx < 0) tx = relMouseX + 12;
            int ty = relMouseY - 12;
            this.drawGradientRect(tx - 3, ty - 3, tx + tw + 3, ty + 11, -1073741824, -1073741824);
            this.fontRenderer.drawStringWithShadow(tooltip, tx, ty, -1);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        textureID = this.mc.renderEngine.getTexture("/gui/retronism_heavycrusher.png");
        this.mc.renderEngine.bindTexture(textureID);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        // Energy bar fill
        int barX = x + 13, barY = y + 23, barW = 6, barH = 84;
        int scaled = tile.getEnergyScaled(barH);
        if (scaled > 0) {
            int top = barY + barH - scaled;
            for (int sy = top; sy < barY + barH; sy++) {
                int color = (sy % 2 == 0) ? 0xFF3BFB98 : 0xFF36E38A;
                drawRect(barX, sy, barX + barW, sy + 1, color);
            }
        }

        // Progress arrows (down direction — 4 lanes)
        int cookScale = tile.getCookProgressScaled(17);
        if (cookScale > 0) {
            this.drawTexturedModalRect(x + 36, y + 42, 176, 14, 24, cookScale + 1);
            this.drawTexturedModalRect(x + 71, y + 42, 176, 14, 24, cookScale + 1);
            this.drawTexturedModalRect(x + 106, y + 42, 176, 14, 24, cookScale + 1);
            this.drawTexturedModalRect(x + 141, y + 42, 176, 14, 24, cookScale + 1);
        }
    }
}
