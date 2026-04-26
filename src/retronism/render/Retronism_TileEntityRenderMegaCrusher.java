package retronism.render;

import net.minecraft.src.*;
import net.minecraft.client.Minecraft;
import retronism.tile.Retronism_TileMegaCrusher;
import aero.modellib.Aero_BoneRenderPose;
import aero.modellib.Aero_MeshModel;
import aero.modellib.Aero_MeshRenderer;
import aero.modellib.Aero_ObjLoader;
import aero.modellib.Aero_ProceduralPose;
import aero.modellib.Aero_RenderOptions;

public class Retronism_TileEntityRenderMegaCrusher extends TileEntitySpecialRenderer {

    public static final Aero_MeshModel MODEL = Aero_ObjLoader.load("/models/MegaCrusher.obj");

    public void renderTileEntityAt(TileEntity tileEntity, double d, double d1, double d2, float f) {
        final Retronism_TileMegaCrusher tile = (Retronism_TileMegaCrusher) tileEntity;

        if (!tile.validateStructure()) {
            return;
        }

        // Calculate origin
        double offsetX = tile.originX - tile.xCoord;
        double offsetY = tile.originY - tile.yCoord;
        double offsetZ = tile.originZ - tile.zCoord;

        // Bind high-res texture
        bindTextureByName("/models/retronism_megacrusher.png");

        // Reset GL color to avoid tinting from other renders
        org.lwjgl.opengl.GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        World w = tile.worldObj;
        int ox = tile.originX, oy = tile.originY, oz = tile.originZ;

        float brightness;
        if (Minecraft.isAmbientOcclusionEnabled()) {
            // Smooth: média de 9 pontos em grid 3x3 acima da estrutura
            // → respeita sombras parciais (árvore, parede ao lado)
            float sum = 0;
            for (int dx = 0; dx <= 2; dx++)
                for (int dz = 0; dz <= 2; dz++)
                    sum += w.getLightBrightness(ox + dx, oy + 3, oz + dz);
            brightness = sum / 9f;
        } else {
            // Flat: máximo de 4 cantos — comportamento clássico do Minecraft
            brightness = Math.max(
                Math.max(w.getLightBrightness(ox + 1, oy + 3, oz - 1),
                         w.getLightBrightness(ox + 1, oy + 3, oz + 3)),
                Math.max(w.getLightBrightness(ox - 1, oy + 3, oz + 1),
                         w.getLightBrightness(ox + 3, oy + 3, oz + 1))
            );
        }

        // Procedural pose: tilt hopper_lid open proportional to peak cook
        // progress. The angle comes straight from runtime tile state — no
        // keyframes — and composes additively on top of the working clip's
        // turbine + shredder spin via Aero_ProceduralPose.
        Aero_ProceduralPose hopperLidOpen = new Aero_ProceduralPose() {
            public void apply(String boneName, Aero_BoneRenderPose pose) {
                if (!"hopper_lid".equals(boneName)) return;
                int peak = Math.max(tile.cookTime[0],
                    Math.max(tile.cookTime[1], tile.cookTime[2]));
                float openRatio = peak / 200f;     // COOK_TIME = 200
                pose.rotX -= 60f * openRatio;       // tilt up to 60° as it cooks
            }
        };

        Aero_MeshRenderer.renderAnimated(MODEL, tile.animState.getBundle(), tile.animState.getDef(),
            tile.animState,
            d + offsetX, d1 + offsetY, d2 + offsetZ,
            brightness, f, Aero_RenderOptions.DEFAULT, hopperLidOpen);
    }
}
