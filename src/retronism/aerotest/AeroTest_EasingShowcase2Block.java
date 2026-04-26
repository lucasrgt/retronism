package retronism.aerotest;

import net.minecraft.src.*;

public class AeroTest_EasingShowcase2Block extends BlockContainer {

	public AeroTest_EasingShowcase2Block(int id, int textureIndex) {
		super(id, Material.iron);
		this.blockIndexInTexture = textureIndex;
	}

	protected TileEntity getBlockEntity() { return new AeroTest_EasingShowcase2Tile(); }
	public boolean renderAsNormalBlock() { return false; }
	public int getRenderType() { return -1; }
	public boolean isOpaqueCube() { return false; }
}
