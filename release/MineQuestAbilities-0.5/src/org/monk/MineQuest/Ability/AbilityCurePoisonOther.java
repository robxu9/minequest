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
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Quester.Quester;

public class AbilityCurePoisonOther extends Ability {
	
	@Override
	public List<ItemStack> getSpellComps() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		
		list.add(new ItemStack(39, 1));
		
		return list;
	}
	
	@Override
	public int getReqLevel() {
		return 5;
	}
	
	@Override
	public int getCastTime() {
		return 5000;
	}
	
	@Override
	public String getName() {
		return "Cure Poison Other";
	}
	
	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		Player player = quester.getPlayer();
		if (entity instanceof Player) {
			Quester other = MineQuest.getQuester((Player)entity);
			if (other != null) {
				if (other.isPoisoned()) {
					other.curePoison();
				} else {
					player.sendMessage("Quester must be poisoned to Cure Poison");
					return;
				}
			} else {
				giveCost(player);
				player.sendMessage("entity is not a Quester");
				return;
			}
		} else {
			giveCost(player);
			player.sendMessage(getName() + " must be cast on another player");
		}
	}

	@Override
	public String getSkillClass() {
		return "PeaceMage";
	}

}
