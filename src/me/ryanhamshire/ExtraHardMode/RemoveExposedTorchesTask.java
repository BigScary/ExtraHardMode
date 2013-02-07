/*
    ExtraHardMode Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.ExtraHardMode;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

//removes torches and/or covers exposed crops
public class RemoveExposedTorchesTask implements Runnable
{
	private Chunk chunk;
	
	public RemoveExposedTorchesTask(Chunk chunk)
	{
		this.chunk = chunk;
	}

	@Override
	public void run()
	{
		//if rain has stopped, don't do anything
		if(!this.chunk.getWorld().hasStorm()) return;
		
		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{				
				for(int y = chunk.getWorld().getMaxHeight() - 1; y > 0; y--)
				{
					Block block = chunk.getBlock(x, y, z);
					Material blockType = block.getType();
					
					if(blockType != Material.AIR)
					{
						if(ExtraHardMode.instance.config_rainBreaksTorches && blockType == Material.TORCH)
						{
							Biome biome = block.getBiome();
							if(biome == Biome.DESERT || biome == Biome.DESERT_HILLS) break;
							
							block.setType(Material.AIR);
							chunk.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.TORCH, 1));
						}
						
						else if(ExtraHardMode.instance.config_weakFoodCrops && (blockType == Material.CROPS || blockType == Material.MELON_STEM || blockType == Material.CARROT || blockType == Material.PUMPKIN_STEM || blockType == Material.POTATO || blockType == Material.RED_ROSE || blockType == Material.YELLOW_FLOWER || blockType == Material.LONG_GRASS))
						{
							Biome biome = block.getBiome();
							if( biome == Biome.FROZEN_OCEAN ||
								biome == Biome.FROZEN_RIVER ||
								biome == Biome.ICE_MOUNTAINS ||
								biome == Biome.ICE_PLAINS ||
								biome == Biome.TAIGA ||
								biome == Biome.TAIGA_HILLS)
							{
								block.setType(Material.SNOW);
								if(ExtraHardMode.randomNumberGenerator.nextBoolean())
								{
									block.setData((byte)1);
								}
								else
								{
									block.setData((byte)2);
								}
							}
						}
						else
						{
							break;
						}
					}
				}
			}
		}
	}
}
