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
import org.monksanctum.MineQuest.Ability.Ability;
import org.monksanctum.MineQuest.Ability.PurgeType;
import org.monksanctum.MineQuest.Quester.Quester;

public class AbilityPurgeSpider extends Ability {
	public AbilityPurgeSpider() {
		super();
		config = new int[] {10};
	}

	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		purgeEntities(quester.getPlayer(), config[0], PurgeType.SPIDER);
	}

	@Override
	public List<ItemStack> getSpellComps() {
		List<ItemStack> cost = new ArrayList<ItemStack>();
		
		cost.add(new ItemStack(Material.TORCH, 1));
		cost.add(new ItemStack(Material.TORCH, 1));
		cost.add(new ItemStack(Material.CACTUS, 1));
		cost.add(new ItemStack(Material.CACTUS, 1));
		
		return cost;
	}

	@Override
	public String getName() {
		return "Purge Spider";
	}

	@Override
	public int getReqLevel() {
		return 10;
	}

	@Override
	public String getSkillClass() {
		return "PeaceMage";
	}

	@Override
	public int getIconLoc() {
		return 32;
	}

}
