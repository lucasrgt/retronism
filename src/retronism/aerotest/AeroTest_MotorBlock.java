package retronism.aerotest;

import net.minecraft.src.*;

public class AeroTest_MotorBlock extends BlockContainer {

	public AeroTest_MotorBlock(int id, int textureIndex) {
		super(id, Material.iron);
		this.blockIndexInTexture = textureIndex;
	}

	protected TileEntity getBlockEntity() { return new AeroTest_MotorTile(); }
	public boolean renderAsNormalBlock() { return false; }
	public int getRenderType() { return -1; }
	public boolean isOpaqueCube() { return false; }
}
