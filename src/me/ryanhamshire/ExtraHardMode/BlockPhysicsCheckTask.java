package me.ryanhamshire.ExtraHardMode;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class BlockPhysicsCheckTask implements Runnable {

	Block block;
	int recursionCount;
	
	public BlockPhysicsCheckTask(Block block, int recursionCount)
	{
		this.block = block;
		this.recursionCount = recursionCount;
	}

	@Override
	public void run()
	{
		block = block.getWorld().getBlockAt(block.getLocation());
		boolean fall = false;
		Material material = block.getType();
		if(	(block.getRelative(BlockFace.DOWN).getType() == Material.AIR || block.getRelative(BlockFace.DOWN).isLiquid() || block.getRelative(BlockFace.DOWN).getType() == Material.TORCH) &&
			(material == Material.SAND || material == Material.GRAVEL || ExtraHardMode.instance.config_moreFallingBlocks.contains(material)))
		{
			ExtraHardMode.applyPhysics(block);
			fall = true;
		}		
		
		if(fall || this.recursionCount == 0)
		{
			if(recursionCount < 10)
			{
				Block neighbor = block.getRelative(BlockFace.UP);
				ExtraHardMode.physicsCheck(neighbor, recursionCount + 1, false);
				
				neighbor = block.getRelative(BlockFace.DOWN);
				ExtraHardMode.physicsCheck(neighbor, recursionCount + 1, false);
				
				neighbor = block.getRelative(BlockFace.EAST);
				ExtraHardMode.physicsCheck(neighbor, recursionCount + 1, false);
				
				neighbor = block.getRelative(BlockFace.WEST);
				ExtraHardMode.physicsCheck(neighbor, recursionCount + 1, false);
				
				neighbor = block.getRelative(BlockFace.NORTH);
				ExtraHardMode.physicsCheck(neighbor, recursionCount + 1, false);
				
				neighbor = block.getRelative(BlockFace.SOUTH);
				ExtraHardMode.physicsCheck(neighbor, recursionCount + 1, false);
			}
		}
	}

}
