package retronism.aerotest;

import net.minecraft.src.*;

public class AeroTest_GraphPoweredBlock extends BlockContainer {

	public AeroTest_GraphPoweredBlock(int id, int textureIndex) {
		super(id, Material.iron);
		this.blockIndexInTexture = textureIndex;
	}

	protected TileEntity getBlockEntity() { return new AeroTest_GraphPoweredTile(); }
	public boolean renderAsNormalBlock() { return false; }
	public int getRenderType() { return -1; }
	public boolean isOpaqueCube() { return false; }
	public boolean canProvidePower() { return false; }

	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborId) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof AeroTest_GraphPoweredTile) {
			((AeroTest_GraphPoweredTile) te).updatePower();
		}
	}
}
