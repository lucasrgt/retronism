package retronism.aerotest;

import net.minecraft.src.*;

public class AeroTest_PlasmaCrystalBlock extends BlockContainer {

	public AeroTest_PlasmaCrystalBlock(int id, int textureIndex) {
		super(id, Material.iron);
		this.blockIndexInTexture = textureIndex;
	}

	protected TileEntity getBlockEntity() { return new AeroTest_PlasmaCrystalTile(); }
	public boolean renderAsNormalBlock() { return false; }
	public int getRenderType() { return -1; }
	public boolean isOpaqueCube() { return false; }
}
