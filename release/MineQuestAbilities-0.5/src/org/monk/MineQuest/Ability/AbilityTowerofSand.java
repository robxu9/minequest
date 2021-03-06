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
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.monk.MineQuest.Quester.Quester;

public class AbilityTowerofSand extends Ability {
	public AbilityTowerofSand() {
		super();
		config = new int[] {64};
	}

	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		Location loc = new Location(location.getWorld(), location.getX(),
				location.getY(), location.getZ());
		int i;
		
		for (i = 0; i < config[0]; i++) {
			loc.setY(loc.getY() + 1);
			
			Block block = loc.getWorld().getBlockAt(loc);
			
			if ((block != null) && (block.getType() == Material.AIR)) {
				block.setType(Material.SAND);
			}
		}
	}

	@Override
	public List<ItemStack> getSpellComps() {
		List<ItemStack> cost = new ArrayList<ItemStack>();
		int i;
		
		for (i = 0; i < 64; i++) {
			cost.add(new ItemStack(Material.SAND, 1));
		}
		
		return cost;
	}

	@Override
	public String getName() {
		return "Tower of Sand";
	}

	@Override
	public int getReqLevel() {
		return 10;
	}

	@Override
	public String getSkillClass() {
		return "Digger";
	}

}
