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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Event.Absolute.BlockCDEvent;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.Quester.SkillClass.SkillClass;
import org.monk.MineQuest.Quester.SkillClass.Combat.WarMage;

public class AbilityFireball extends Ability {

	@Override
	public List<ItemStack> getManaCost() {
		List<ItemStack> list = new ArrayList<ItemStack>();

		list.add(new ItemStack(263, 1));
		
		return list;
	}
	
	@Override
	public String getName() {
		return "Fireball";
	}
	
	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		Player player = null;
		World world;
		if (quester != null) {
			player = quester.getPlayer();
		}
		if (entity == null) {
			if (quester != null) {
				quester.sendMessage("Must be cast on a Living Entity");
				giveManaCost(player);
			}
			return;
		}
		
		if (player != null) {
			world = player.getWorld();
		} else {
			world = entity.getWorld();
		}
		
		double leftx, leftz;
		int x, z;
		if ((location.getX() == 0) && (location.getY() == 0) && (location.getZ() == 0)) {
			giveManaCost(player);
			return;
		}
		
		leftx = location.getX() % 1;
		leftz = location.getZ() % 1;
		x = (leftx < .5)?-1:1;
		z = (leftz < .5)?-1:1;
		
		Block nblock = world.getBlockAt((int)location.getX(), 
				getNearestY(location.getWorld(), (int)location.getX(), (int)location.getY(), (int)location.getZ()), 
				(int)location.getZ());
		MineQuest.getEventParser().addEvent(new BlockCDEvent(10, 30000, nblock, Material.FIRE));
		
		nblock = world.getBlockAt((int)location.getX() + x, 
				getNearestY(location.getWorld(), (int)location.getX() + x, (int)location.getY(), (int)location.getZ()), 
				(int)location.getZ());
		MineQuest.getEventParser().addEvent(new BlockCDEvent(10, 30000, nblock, Material.FIRE));
		
		nblock = world.getBlockAt((int)location.getX() + x, 
				getNearestY(location.getWorld(), (int)location.getX() + x, (int)location.getY(), (int)location.getZ() + z), 
				(int)location.getZ() + z);
		MineQuest.getEventParser().addEvent(new BlockCDEvent(10, 30000, nblock, Material.FIRE));
		
		nblock = world.getBlockAt((int)location.getX(), 
				getNearestY(location.getWorld(), (int)location.getX(), (int)location.getY(), (int)location.getZ() + z), 
				(int)location.getZ() + z);
		MineQuest.getEventParser().addEvent(new BlockCDEvent(10, 30000, nblock, Material.FIRE));
		
		if (entity != null) {
			MineQuest.damage(entity, 2 + (myclass.getCasterLevel() / 3));
		}
	}

	@Override
	public SkillClass getClassType() {
		return new WarMage();
	}

	@Override
	public int getReqLevel() {
		return 0;
	}
}
