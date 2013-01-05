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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

//event handlers related to blocks
public class BlockEventHandler implements Listener 
{
	//constructor
	public BlockEventHandler()
	{		
	}
	
	//when a player breaks a block...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent breakEvent)
	{	
		Block block = breakEvent.getBlock();
		World world = block.getWorld();
		Player player = breakEvent.getPlayer();
		
		if(!ExtraHardMode.instance.config_enabled_worlds.contains(world) || player.hasPermission("extrahardmode.bypass")) return;
		
		//FEATURE: stone breaks tools much more quickly
		if(ExtraHardMode.instance.config_superHardStone)
		{			
			ItemStack inHandStack = player.getItemInHand();			
			
			//if breaking stone with an item in hand and the player does NOT have the bypass permission
			if(	(block.getType() == Material.STONE || block.getType() == Material.ENDER_STONE) && 
				inHandStack != null)
			{				
				//if not using an iron or diamond pickaxe, don't allow breakage and explain to the player
				Material tool = inHandStack.getType();
				if(tool != Material.IRON_PICKAXE && tool != Material.DIAMOND_PICKAXE)
				{
					ExtraHardMode.sendMessage(player, TextMode.Instr, Messages.StoneMiningHelp);
					breakEvent.setCancelled(true);
					return;
				}
				
				//otherwise, drastically reduce tool durability when breaking stone
				else
				{
					short amount = 0;
					
					if(tool == Material.IRON_PICKAXE)
						amount = 8;
					else
						amount = 22;
					
					inHandStack.setDurability((short)(inHandStack.getDurability() + amount));
				}
			}
			
			//when ore is broken, it softens adjacent stone
			//important to ensure players can reach the ore they break
			if(block.getType().name().endsWith("_ORE"))
			{
				Block adjacentBlock = block.getRelative(BlockFace.DOWN);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
				
				adjacentBlock = block.getRelative(BlockFace.UP);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
				
				adjacentBlock = block.getRelative(BlockFace.EAST);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
				
				adjacentBlock = block.getRelative(BlockFace.WEST);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
				
				adjacentBlock = block.getRelative(BlockFace.NORTH);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
				
				adjacentBlock = block.getRelative(BlockFace.SOUTH);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
			}
		}
		
		//FEATURE: more falling blocks
		ExtraHardMode.physicsCheck(block, 0, true);
		
		//FEATURE: breaking a melon stem can result in 0-2 seeds returned
		if(ExtraHardMode.instance.config_seedReduction)
		{
			if(block.getType() == Material.MELON_STEM)
			{
				Collection<ItemStack> drops = block.getDrops();
				drops.clear();
				
				int randomNumber = ExtraHardMode.randomNumberGenerator.nextInt(100);
				
				if(randomNumber >= 30)
				{
					drops.add(new ItemStack(Material.MELON_SEEDS));
				}
				
				if(randomNumber >= 70)
				{
					drops.add(new ItemStack(Material.MELON_SEEDS));
				}				
			}
		}
		
		//FEATURE: breaking a wheat can result in 0-2 seeds returned
		if(ExtraHardMode.instance.config_seedReduction)
		{
			if(block.getType() == Material.CROPS)
			{
				Collection<ItemStack> drops = block.getDrops();
				
				//remove any seeds
				Iterator<ItemStack> iterator = drops.iterator();
				while(iterator.hasNext())
				{
					ItemStack itemStack = iterator.next();
					if(itemStack.getType() == Material.SEEDS)
					{
						iterator.remove();
					}
				}				
				
				//add back in the right amount
				int randomNumber = ExtraHardMode.randomNumberGenerator.nextInt(100);
				
				if(randomNumber >= 50)
				{
					//drops.add(new ItemStack(Material.SEEDS));
				}
			}
		}
		
		//FEATURE: no nether wart farming (always drops exactly 1 nether wart when broken)
		if(ExtraHardMode.instance.config_noFarmingNetherWart)
		{
			if(block.getType() == Material.NETHER_WARTS)
			{
				block.getDrops().clear();
				block.getDrops().add(new ItemStack(Material.NETHER_STALK));
			}
		}
	}
	
	//when a player places a block...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent placeEvent)
	{
		Player player = placeEvent.getPlayer();
		Block block = placeEvent.getBlock();
		World world = block.getWorld();
		
		if(!ExtraHardMode.instance.config_enabled_worlds.contains(world) || player.hasPermission("extrahardmode.bypass")) return;
		
		//FIX: prevent players from placing ore as an exploit to work around the hardened stone rule
		if(ExtraHardMode.instance.config_superHardStone && block.getType().name().endsWith("_ORE"))
		{
			Block [] adjacentBlocks = new Block [] {
				block.getRelative(BlockFace.DOWN),
				block.getRelative(BlockFace.UP),
				block.getRelative(BlockFace.EAST),
				block.getRelative(BlockFace.WEST),
				block.getRelative(BlockFace.NORTH),
				block.getRelative(BlockFace.SOUTH) };
				
			for(int i = 0; i < adjacentBlocks.length; i++)
			{
				Block adjacentBlock = adjacentBlocks[i];
				if(adjacentBlock.getType() == Material.STONE)
				{
					ExtraHardMode.sendMessage(player, TextMode.Err, Messages.NoPlacingOreAgainstStone);
					placeEvent.setCancelled(true);
					return;
				}
			}
		}
			
		//FEATURE: no farming nether wart
		if(block.getType() == Material.NETHER_WARTS && ExtraHardMode.instance.config_noFarmingNetherWart)
		{
			placeEvent.setCancelled(true);
			return;
		}
		
		//FEATURE: more falling blocks
		ExtraHardMode.physicsCheck(block, 0, true);
		
		//FEATURE: no standard torches, jack o lanterns, or fire on top of netherrack near diamond level
		if(ExtraHardMode.instance.config_standardTorchMinY > 0)
		{
			if(	world.getEnvironment() == Environment.NORMAL &&
				block.getY() < ExtraHardMode.instance.config_standardTorchMinY &&
				(block.getType() == Material.TORCH || block.getType() == Material.JACK_O_LANTERN || (block.getType() == Material.FIRE && block.getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK)))
				{
					ExtraHardMode.sendMessage(player, TextMode.Instr, Messages.NoTorchesHere);
					placeEvent.setCancelled(true);
					return;
				}
		}
		
		//FEATURE: players can't place blocks from weird angles (using shift to hover over in the air beyond the edge of solid ground)
		//or directly beneath themselves, for that matter
		if(ExtraHardMode.instance.config_limitedBlockPlacement)
		{
			if(	block.getX() == player.getLocation().getBlockX() &&
				block.getZ() == player.getLocation().getBlockZ() &&
				block.getY() <  player.getLocation().getBlockY() )
			{
				placeEvent.setCancelled(true);
				return;
			}
			
			Block underBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
			
			//if over standing directly over lava, prevent placement
			if(underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA)
			{
				placeEvent.setCancelled(true);
				return;
			}
			
			//otherwise if hovering over air, check one block lower
			else if(underBlock.getType() == Material.AIR)
			{
				underBlock = underBlock.getRelative(BlockFace.DOWN);
				
				//if over lava or more air, prevent placement
				if(underBlock.getType() == Material.AIR || underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA)
				{
					placeEvent.setCancelled(true);
					return;
				}
			}
		}
	}
		
	//when a dispenser dispenses...
	void onBlockDispense(BlockDispenseEvent event)
	{
		//FEATURE: can't move water source blocks
		if(ExtraHardMode.instance.config_dontMoveWaterSourceBlocks)
		{
			World world = event.getBlock().getWorld();
			if(!ExtraHardMode.instance.config_enabled_worlds.contains(world)) return;
			
			//only care about water
			if(event.getItem().getType() == Material.WATER_BUCKET)
			{
				//plan to evaporate the water next tick
				Block block;
				Vector velocity = event.getVelocity();
				if(velocity.getX() > 0)
				{
					block = event.getBlock().getLocation().add(1, 0, 0).getBlock();
				}
				else if(velocity.getX() < 0)
				{
					block = event.getBlock().getLocation().add(-1, 0, 0).getBlock();
				}
				else if(velocity.getZ() > 0)
				{
					block = event.getBlock().getLocation().add(0, 0, 1).getBlock();
				}
				else
				{
					block = event.getBlock().getLocation().add(0, 0, -1).getBlock();
				}
				
				EvaporateWaterTask task = new EvaporateWaterTask(block);
				ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 1L);
			}				
		}
	}
	
	//when a piston pushes...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPistonExtend (BlockPistonExtendEvent event)
	{		
		List<Block> blocks = event.getBlocks();
		World world = event.getBlock().getWorld();
		
		//FEATURE: prevent players from circumventing hardened stone rules by placing ore, then pushing the ore next to stone before breaking it
		
		if(!ExtraHardMode.instance.config_superHardStone || !ExtraHardMode.instance.config_enabled_worlds.contains(world)) return;
				
		//which blocks are being pushed?
		for(int i = 0; i < blocks.size(); i++)
		{
			//if any are ore or stone, don't push
			Block block = blocks.get(i);
			Material material = block.getType();
			if(material == Material.STONE || material.name().endsWith("_ORE"))
			{
				event.setCancelled(true);
				return;
			}
		}		
	}
	
	//when a piston pulls...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPistonRetract (BlockPistonRetractEvent event)
	{
		//FEATURE: prevent players from circumventing hardened stone rules by placing ore, then pulling the ore next to stone before breaking it
		
		//we only care about sticky pistons
		if(!event.isSticky()) return;
		
		Block block = event.getRetractLocation().getBlock();
		World world = block.getWorld();
		
		if(!ExtraHardMode.instance.config_superHardStone || !ExtraHardMode.instance.config_enabled_worlds.contains(world)) return;
		
		Material material = block.getType();
		if(material == Material.STONE || material.name().endsWith("_ORE"))
		{
			event.setCancelled(true);
			return;
		}
	} 
}
