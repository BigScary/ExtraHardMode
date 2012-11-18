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

import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class DropItemsTask implements Runnable {

	private List<ItemStack> itemsToDrop;
	private Location location;
	
	public DropItemsTask(List<ItemStack> itemsToDrop, Location location)
	{
		this.itemsToDrop = itemsToDrop;
		this.location = location;
	}

	@Override
	public void run()
	{
		for(int i = 0; i < itemsToDrop.size(); i++)
		{
			location.getWorld().dropItemNaturally(location, itemsToDrop.get(i));
		}
	}

}
