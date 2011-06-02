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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.monk.MineQuest.Ability.Ability;
import org.monk.MineQuest.Quester.Quester;

public class AbilityHeal extends Ability {
	public AbilityHeal() {
		super();
		config = new int[] {1, 100, 8, 0};
	}
	
	@Override
	public List<ItemStack> getManaCost() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		
		list.add(new ItemStack(326, 1));
		
		return list;
	}
	
	@Override
	public int getCastTime() {
		return 20000;
	}
	
	@Override
	public String getName() {
		return "Heal";
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		Player player = quester.getPlayer();
		player.getInventory().addItem(new ItemStack(325, 1));
		player.updateInventory();
		if (quester.getHealth() < quester.getMaxHealth()) {
			int min = config[0] + (int)(((double)config[1] / 100) * myclass.getCasterLevel());
			int max = config[2] + (int)(((double)config[3] / 100) * myclass.getCasterLevel());
			quester.setHealth(quester.getHealth() + myclass.getGenerator().nextInt(max - min + 1) + min);
		} else {
			player.sendMessage("Quester must not be at full health to heal");
			return;
		}
	}

	@Override
	public int getReqLevel() {
		return 0;
	}


	@Override
	public String getSkillClass() {
		return "PeaceMage";
	}

}
