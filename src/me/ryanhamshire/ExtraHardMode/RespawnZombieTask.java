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
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

public class RespawnZombieTask implements Runnable {

	private Location location;
	private Player player;
	
	public RespawnZombieTask(Location location, Player target)
	{
		this.location = location;
		this.player = target;
	}

	@Override
	public void run()
	{
		Chunk chunk = location.getChunk();
		if(!chunk.isLoaded()) return;
		
		Zombie zombie = (Zombie)location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
		zombie.setHealth(zombie.getHealth() / 2); 	//zombie has half normal zombie health
		EntityEventHandler.markLootLess(zombie);  	//this zombie will not drop loot (again)
		if(this.player != null && this.player.isOnline())  	//zombie is still mad at the same player
		{
			zombie.setTarget(this.player);
		}
	}
}
