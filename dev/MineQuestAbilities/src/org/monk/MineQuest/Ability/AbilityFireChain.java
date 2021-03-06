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
import org.monksanctum.MineQuest.Ability.Ability;
import org.monksanctum.MineQuest.Quester.Quester;

public class AbilityFireChain extends Ability {
	public AbilityFireChain() {
		super();
		config = new int[] {10, 10, 3, 100};
	}
	
	@Override
	public List<ItemStack> getSpellComps() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		
		list.add(new ItemStack(263, 1));
		list.add(new ItemStack(263, 1));
		list.add(new ItemStack(263, 1));
		list.add(new ItemStack(263, 1));
		list.add(new ItemStack(263, 1));
		
		return list;
	}
	
	@Override
	public int getReqLevel() {
		return 10;
	}
	
	@Override
	public String getName() {
		return "Fire Chain";
	}
	
	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		Player player = null;
		if (quester != null) {
			player = quester.getPlayer();
		}
		if (entity != null) {
			Ability fireball = Ability.newAbility("Fireball", myclass);
			LivingEntity this_entity;
			int i;

			this_entity = getRandomEntity(entity, config[0]);
			for (i = 0; i < config[2] + (((double)config[3]) / 100 * myclass.getCasterLevel()); i++) {
				if (this_entity != null) {
					fireball.castAbility(quester, new Location(player.getWorld(), (int) this_entity.getLocation().getX(),
							(int) this_entity.getLocation().getY(), (int) this_entity.getLocation().getZ()),
							this_entity);
					this_entity = getRandomEntity(this_entity, config[0]);
				}
				if (myclass.getGenerator().nextDouble() < (((double)config[1]) / 100)) {
					break;
				}
			}
		} else {
			giveCost(player);
			player.sendMessage("FireChain must be bound to an attack");
			return;
		}	
	}

	@Override
	public String getSkillClass() {
		return "WarMage";
	}

	@Override
	public int getIconLoc() {
		return 17;
	}

}
