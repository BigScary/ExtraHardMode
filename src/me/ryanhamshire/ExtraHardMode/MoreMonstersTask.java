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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;

public class MoreMonstersTask implements Runnable {

	private static ArrayList<SimpleEntry<Player, Location>> previousLocations;
	
	@Override
	public void run()
	{
		if(MoreMonstersTask.previousLocations == null)
		{
			MoreMonstersTask.previousLocations = new ArrayList<SimpleEntry<Player, Location>>();
		}
			
		//spawn monsters from the last pass
		for(int i = 0; i < MoreMonstersTask.previousLocations.size(); i++)
		{
			SimpleEntry<Player, Location> entry = MoreMonstersTask.previousLocations.get(i);
			Player player = entry.getKey();
			Location location = entry.getValue();
			Chunk chunk = location.getChunk();
			World world = location.getWorld();
			
			try
			{
				//chunk must be loaded, player must not be close, and there must be no other players in the chunk
				if(location.getChunk().isLoaded() && player.isOnline() && location.distanceSquared(player.getLocation()) > 150)
				{
					boolean playerInChunk = false;
					Entity [] entities = chunk.getEntities();
					for(int j = 0; j < entities.length; j++)
					{
						if(entities[j].getType() == EntityType.PLAYER)
						{
							playerInChunk = true;
							break;
						}
					}
					
					if(!playerInChunk)
					{
						//spawn random monster(s)
						if(world.getEnvironment() == Environment.NORMAL)
						{
							int random = ExtraHardMode.randomNumberGenerator.nextInt();
							EntityType monsterType;
							int typeMultiplier = 1;
							
							//decide which kind and how many
							if(random < 30)  //silverfish are most common
							{
								monsterType = EntityType.SILVERFISH;
								typeMultiplier = 2;  //twice as many if silverfish
							}
							else if(random < 47)
							{
								monsterType = EntityType.SKELETON;
							}
							else if(random < 64)
							{
								monsterType = EntityType.ZOMBIE;
							}
							else if(random < 81)
							{
								monsterType = EntityType.CREEPER;
							}
							else
							{
								monsterType = EntityType.SPIDER;
							}
							
							int totalToSpawn = typeMultiplier;
							for(int j = 0; j < totalToSpawn; j++)
							{
								world.spawnEntity(location, monsterType);								
							}
						}
						
						else if(world.getEnvironment() == Environment.NETHER)
						{
							int random = ExtraHardMode.randomNumberGenerator.nextInt();
							
							if(random < 80)
							{
								PigZombie zombie = (PigZombie)world.spawnEntity(location, EntityType.PIG_ZOMBIE);
								zombie.setAnger(Integer.MAX_VALUE);
							}
							else
							{
								world.spawnEntity(location, EntityType.BLAZE);
							}
						}
					}
				}
			}
			catch(IllegalArgumentException exception) { }  //in case the player is in a different world from the saved location
		}
		
		//plan for the next pass
		MoreMonstersTask.previousLocations.clear();
		Player [] players = ExtraHardMode.instance.getServer().getOnlinePlayers(); 
		for(int i = 0; i < players.length; i++)
		{
			Player player = players[i];
			
			//skip disabled worlds, players with bypass permission, and players not in survival mode
			if(!ExtraHardMode.instance.config_enabled_worlds.contains(player.getWorld()) || player.hasPermission("extrahardmode.bypass") || player.getGameMode() != GameMode.SURVIVAL) continue;
			
			Location location = player.getLocation();
			World world = player.getWorld();
			
			if(world.getEnvironment() == Environment.THE_END) continue;
			
			//in normal worlds, respect Y level setting in config, and skip any locations where sunlight reaches
			if(world.getEnvironment() == Environment.NORMAL && (location.getY() > ExtraHardMode.instance.config_monsterSpawnsInLightMaxY || location.getBlock().getLightFromSky() > 0)) continue;
			
			//plan to check this location again later to possibly spawn monsters
			MoreMonstersTask.previousLocations.add(new SimpleEntry<Player, Location>(player, location));
		}
	}
}
