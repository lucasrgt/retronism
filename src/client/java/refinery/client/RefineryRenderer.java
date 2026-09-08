package refinery.client;

import aero.modellib.Aero_MeshRenderer;
import aero.modellib.model.Aero_MeshModel;
import aero.modellib.model.Aero_ObjLoader;
import aero.modellib.render.Aero_RenderOptions;
import net.minecraft.src.*;
import refinery.RefineryTile;

/** AeroModelLib owns loading, culling, lighting buckets, GL state and cached mesh drawing. */
public final class RefineryRenderer extends TileEntitySpecialRenderer {
    private static final Aero_MeshModel[] MODELS = {
        Aero_ObjLoader.load("/refinery/model-0.obj"), Aero_ObjLoader.load("/refinery/model-1.obj"),
        Aero_ObjLoader.load("/refinery/model-2.obj"), Aero_ObjLoader.load("/refinery/model-3.obj")
    };
    private static final String[] TEXTURES = {
        "/refinery/texture-0.png", "/refinery/texture-1.png", "/refinery/texture-2.png", "/refinery/texture-3.png"
    };
    private static final Aero_RenderOptions OPTIONS = Aero_RenderOptions.builder().cullFaces(true).alphaClip(.1f).build();
    @Override public void renderTileEntityAt(TileEntity value, double x, double y, double z, float partialTick) {
        RefineryTile tile = (RefineryTile)value;
        if (!tile.isMaster() || tile.dismantling) return;
        float brightness = tile.worldObj.getLightBrightness(tile.xCoord, tile.yCoord + 2, tile.zCoord);
        for (int i = 0; i < MODELS.length; i++) {
            bindTextureByName(TEXTURES[i]);
            Aero_MeshRenderer.renderModelAtRest(MODELS[i], x, y, z, -90 * tile.facing, brightness, OPTIONS);
        }
    }
}
