package retronism.aerotest;

import net.minecraft.src.*;

public class AeroTest_MegaModelBlock extends BlockContainer {

	public AeroTest_MegaModelBlock(int id, int textureIndex) {
		super(id, Material.iron);
		this.blockIndexInTexture = textureIndex;
	}

	protected TileEntity getBlockEntity() { return new AeroTest_MegaModelTile(); }
	public boolean renderAsNormalBlock() { return false; }
	public int getRenderType() { return -1; }
	public boolean isOpaqueCube() { return false; }
}
