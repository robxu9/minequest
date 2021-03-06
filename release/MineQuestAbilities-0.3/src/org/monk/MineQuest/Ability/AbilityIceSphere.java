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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.Quester.SkillClass.SkillClass;
import org.monk.MineQuest.Quester.SkillClass.Combat.WarMage;

public class AbilityIceSphere extends Ability {

	@Override
	public List<ItemStack> getManaCost() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		
		list.add(new ItemStack(332, 1));
		list.add(new ItemStack(332, 1));
		list.add(new ItemStack(332, 1));
		list.add(new ItemStack(332, 1));
		list.add(new ItemStack(332, 1));
		
		return list;
	}

	@Override
	public int getReqLevel() {
		return 7;
	}

	@Override
	public String getName() {
		return "IceSphere";
	}

	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		Player player = null;
		if (quester != null) {
			player = quester.getPlayer();
		}
		int j,k;
		if (entity == null) {
			player.sendMessage("Cannot cast on null entity");
			giveManaCost(player);
			return;
		}
		
		World world = entity.getWorld();
		for (j = -1; j < 2; j++) {
			for (k = -1; k < 2; k++) {
				Block nblock = world.getBlockAt((int)location.getX() + j, 
						getNearestY(location.getWorld(), (int)location.getX() + j, (int)location.getY(), (int)location.getZ() + k), 
						(int)location.getZ() + k);
				nblock.setTypeId(78);
			}
		}
		
		if (entity instanceof Player) {
			Quester quest = MineQuest.getQuester((Player)entity);
			quest.setHealth(quest.getHealth() - 3 - (myclass.getCasterLevel() / 2));
		} else {
			MineQuest.getMob(entity).damage(3 + (myclass.getCasterLevel() / 2));
		}
	}

	@Override
	public SkillClass getClassType() {
		return new WarMage();
	}

}
