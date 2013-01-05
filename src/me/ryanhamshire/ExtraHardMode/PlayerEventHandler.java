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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

class PlayerEventHandler implements Listener 
{
	//typical constructor, yawn
	PlayerEventHandler()
	{		
	}	
	
	//FEATURE: respawning players start without full health or food
	@EventHandler(ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent respawnEvent)
	{
		Player player = respawnEvent.getPlayer();
		World world = respawnEvent.getPlayer().getWorld();
		if(!ExtraHardMode.instance.config_enabled_worlds.contains(world) || player.hasPermission("extrahardmode.bypass")) return;
		
		SetPlayerHealthAndFoodTask task = new SetPlayerHealthAndFoodTask(player, ExtraHardMode.instance.config_playerRespawnHealth, ExtraHardMode.instance.config_playerRespawnFoodLevel);
		ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 10L);  //half-second delay		
	}
	
	//when a player interacts with the world
	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		World world = event.getPlayer().getWorld();
		if(!ExtraHardMode.instance.config_enabled_worlds.contains(world) || player.hasPermission("extrahardmode.bypass")) return;
		
		Action action = event.getAction();
		
		//FEATURE: bonemeal doesn't work on mushrooms
		if(ExtraHardMode.instance.config_noBonemealOnMushrooms && action == Action.RIGHT_CLICK_BLOCK)
		{
			Block block = event.getClickedBlock();
			if(block.getType() == Material.RED_MUSHROOM || block.getType() == Material.BROWN_MUSHROOM)
			{
				//what's the player holding?
				Material materialInHand = player.getItemInHand().getType();
				
				//if bonemeal, cancel the event
				if(materialInHand == Material.INK_SACK)  //bukkit bug labels bone meal as ink sack
				{
					event.setCancelled(true);
				}
			}
		}
		
		//FEATURE: putting out fire up close catches the player on fire
		Block block = event.getClickedBlock();
		if(ExtraHardMode.instance.config_extinguishingFireIgnitesPlayers && block != null && block.getType() != Material.AIR)
		{
			if(block.getRelative(event.getBlockFace()).getType() == Material.FIRE)
			{
				player.setFireTicks(100);  //20L ~ 1 seconds; 100L ~ 5 seconds 
			}
		}
	}		
	
	//when a player fills a bucket...
	@EventHandler(priority = EventPriority.LOW)
	void onPlayerFillBucket(PlayerBucketFillEvent event)
	{
		//FEATURE: can't move water source blocks
		if(ExtraHardMode.instance.config_dontMoveWaterSourceBlocks)
		{
			Player player = event.getPlayer();
			World world = event.getPlayer().getWorld();
			if(!ExtraHardMode.instance.config_enabled_worlds.contains(world) || player.hasPermission("extrahardmode.bypass")) return;
			
			//only care about stationary (source) water 
			Block block = event.getBlockClicked();
			if(block.getType() == Material.STATIONARY_WATER)
			{
				//cancel the event so that the water doesn't get removed
				event.setCancelled(true);
				
				//fill the player's bucket anyway
				//(beware, player may have a stack of empty buckets, and filled buckets DON'T stack)
				int extraBuckets = player.getItemInHand().getAmount() - 1; 
				player.getItemInHand().setType(Material.WATER_BUCKET);
				player.getItemInHand().setAmount(1);
				if(extraBuckets > 0)
				{
					player.getInventory().addItem(new ItemStack(Material.BUCKET, extraBuckets));
				}
				
				//send the player data so that his client doesn't incorrectly show the water as missing
				player.sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
			}
		}
	}
	
	//when a player empties a bucket...
	@EventHandler(priority = EventPriority.NORMAL)
	void onPlayerEmptyBucket(PlayerBucketEmptyEvent event)
	{
		//FEATURE: can't move water source blocks
		if(ExtraHardMode.instance.config_dontMoveWaterSourceBlocks)
		{
			Player player = event.getPlayer();
			World world = event.getPlayer().getWorld();
			if(!ExtraHardMode.instance.config_enabled_worlds.contains(world) || player.hasPermission("extrahardmode.bypass")) return;
			
			//only care about water buckets
			if(player.getItemInHand().getType() == Material.WATER_BUCKET)
			{
				//plan to change this block into a non-source block on the next tick
				Block block = event.getBlockClicked().getRelative(event.getBlockFace());
				EvaporateWaterTask task = new EvaporateWaterTask(block);
				ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 15L);
			}
		}
	}
}
