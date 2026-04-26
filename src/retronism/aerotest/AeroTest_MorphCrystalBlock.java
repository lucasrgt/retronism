package retronism.aerotest;

import net.minecraft.src.*;

public class AeroTest_MorphCrystalBlock extends BlockContainer {

	public AeroTest_MorphCrystalBlock(int id, int textureIndex) {
		super(id, Material.iron);
		this.blockIndexInTexture = textureIndex;
	}

	protected TileEntity getBlockEntity() { return new AeroTest_MorphCrystalTile(); }
	public boolean renderAsNormalBlock() { return false; }
	public int getRenderType() { return -1; }
	public boolean isOpaqueCube() { return false; }
}
