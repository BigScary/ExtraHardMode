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

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class WebCleanupTask implements Runnable {

	private ArrayList<Block> webs;
	
	public WebCleanupTask(ArrayList<Block> changedBlocks)
	{
		this.webs = changedBlocks;
	}

	@Override
	public void run()
	{
		for(int i = 0; i < webs.size(); i++)
		{
			Block web = webs.get(i);
			
			//don't load a chunk just to clean up webs
			if(!web.getChunk().isLoaded()) continue;
			
			//only turn webs to air.  there's a chance the web may have been replaced since it was placed.
			if(web.getType() == Material.WEB)
			{
				web.setType(Material.AIR);
			}
		}
	}
}
