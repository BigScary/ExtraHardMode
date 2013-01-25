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

import org.bukkit.Location;

public class CreateExplosionTask implements Runnable {

	private Location location;
	private float power;
	
	public CreateExplosionTask(Location location, float power)
	{
		this.location = location;
		this.power = power;
	}

	@Override
	public void run()
	{
		this.location.getWorld().createExplosion(this.location, this.power);
	}
}
