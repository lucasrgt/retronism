package retronism.aerotest;

import net.minecraft.src.*;

public class AeroTest_EasingShowcaseBlock extends BlockContainer {

	public AeroTest_EasingShowcaseBlock(int id, int textureIndex) {
		super(id, Material.iron);
		this.blockIndexInTexture = textureIndex;
	}

	protected TileEntity getBlockEntity() { return new AeroTest_EasingShowcaseTile(); }
	public boolean renderAsNormalBlock() { return false; }
	public int getRenderType() { return -1; }
	public boolean isOpaqueCube() { return false; }
}
