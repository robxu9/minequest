/*
 * MineQuest - Bukkit Plugin for adding RPG characteristics to minecraft
 * Copyright (C) 2011  Jason Monk
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.monk.MineQuest.Ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.Quester.SkillClass.SkillClass;
import org.monk.MineQuest.Quester.SkillClass.Resource.Miner;

public class AbilityLavaToWater extends Ability {

	@Override
	public int getReqLevel() {
		return 30;
	}
	
	@Override
	public List<ItemStack> getManaCost() {
		List<ItemStack> cost = new ArrayList<ItemStack>();
		
		cost.add(new ItemStack(Material.SNOW_BLOCK, 1));
		cost.add(new ItemStack(Material.SNOW_BLOCK, 1));
		cost.add(new ItemStack(Material.SNOW_BLOCK, 1));
		cost.add(new ItemStack(Material.SNOW_BLOCK, 1));
		cost.add(new ItemStack(Material.SNOW_BLOCK, 1));
		cost.add(new ItemStack(Material.SNOW_BLOCK, 1));
		cost.add(new ItemStack(Material.SNOW_BLOCK, 1));
		cost.add(new ItemStack(Material.SNOW_BLOCK, 1));
		cost.add(new ItemStack(Material.SNOW_BLOCK, 1));
		cost.add(new ItemStack(Material.SNOW_BLOCK, 1));
		
		return cost;
	}
	
	@Override
	public String getName() {
		return "Lava To Water";
	}
	
	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		Location loc = quester.getPlayer().getLocation();
		int x, y, z;
		
		for (x = (int)(loc.getX() - 15); x < (int)(loc.getX() + 15); x++) {
			for (y = (int)(loc.getY() - 15); y < (int)(loc.getY() + 15); y++) {
				for (z = (int)(loc.getZ() - 15); z < (int)(loc.getZ() + 15); z++) {
					Location block_loc = new Location(loc.getWorld(), x, y, z);
					
					if (loc.getWorld().getBlockAt(block_loc).getType() == Material.LAVA) {
						loc.getWorld().getBlockAt(block_loc).setType(Material.WATER);
					}
					if (loc.getWorld().getBlockAt(block_loc).getType() == Material.STATIONARY_LAVA) {
						loc.getWorld().getBlockAt(block_loc).setType(Material.WATER);
					}
				}
			}
		}
	}

	@Override
	public SkillClass getClassType() {
		return new Miner();
	}

}
