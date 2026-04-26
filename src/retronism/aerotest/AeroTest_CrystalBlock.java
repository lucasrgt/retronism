package retronism.aerotest;

import net.minecraft.src.*;

public class AeroTest_CrystalBlock extends BlockContainer {

	public AeroTest_CrystalBlock(int id, int textureIndex) {
		super(id, Material.iron);
		this.blockIndexInTexture = textureIndex;
	}

	protected TileEntity getBlockEntity() { return new AeroTest_CrystalTile(); }
	public boolean renderAsNormalBlock() { return false; }
	public int getRenderType() { return -1; }
	public boolean isOpaqueCube() { return false; }
}
