package retronism.aerotest;

import net.minecraft.src.*;

public class AeroTest_SpellCircleBlock extends BlockContainer {

	public AeroTest_SpellCircleBlock(int id, int textureIndex) {
		super(id, Material.iron);
		this.blockIndexInTexture = textureIndex;
	}

	protected TileEntity getBlockEntity() { return new AeroTest_SpellCircleTile(); }
	public boolean renderAsNormalBlock() { return false; }
	public int getRenderType() { return -1; }
	public boolean isOpaqueCube() { return false; }
}
