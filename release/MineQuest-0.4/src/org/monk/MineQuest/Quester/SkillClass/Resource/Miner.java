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
package org.monk.MineQuest.Quester.SkillClass.Resource;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Ability.DefendingAbility;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.Quester.SkillClass.DefendingClass;
import org.monk.MineQuest.Quester.SkillClass.ResourceClass;

public class Miner extends ResourceClass implements DefendingClass {

	public Miner(Quester quester, String type) {
		super(quester, type);
		// TODO Auto-generated constructor stub
	}
	
	public Miner() {
		// Shell
		super();
	}

	@Override
	public boolean canUse(ItemStack itemStack) {
		int item = itemStack.getTypeId();
		
		if (item == 274) return (level >= MineQuest.getStoneReqLevel());			// Stone
		else if (item == 278) return (level >= MineQuest.getDiamondReqLevel());	// Diamond
		else if (item == 285) return (level >= MineQuest.getGoldReqLevel());	// Gold
		else if (item == 257) return (level >= MineQuest.getIronReqLevel());	// Iron
		
		return super.canUse(itemStack);
	}
	
	/**
	 * Called whenever the Quester that has this class is
	 * being attacked.
	 * 
	 * @param entity Entity that is attacker
	 * @param amount Amount of damage being dealt
	 * @return The amount of damage negated by this class
	 */
	public int defend(LivingEntity entity, int amount) {
		int i;
		int armor[] = getClassArmorIds();
		boolean flag = true;
		int sum = 0;
		
		if (armor == null) return 0;
		
		for (i = 0; i < armor.length; i++) {
			if (isWearing(armor[i])) {
				if (generator.nextDouble() < (.05 * i)) {
					sum++;
				}
			} else {
				flag = false;
			}
		}
		if (flag) {
			if (generator.nextDouble() < .4) {
				sum++;
			}
			
			if (generator.nextDouble() < armorBlockChance(armor)) {
				return amount;
			}
		}
		
		for (i = 0; i < ability_list.length; i++) {
			if (ability_list[i] instanceof DefendingAbility) {
				sum += ((DefendingAbility)ability_list[i]).parseDefend(quester, entity, amount - sum);
			}
		}
		
		return sum;
	}

	/**
	 * Gets the chance for an armor set to block an
	 * attack.
	 * 
	 * @param armor Set of armor to check.
	 * @return Chance of armor blocking attack.
	 */
	private double armorBlockChance(int[] armor) {
		switch (armor[0]) {
		case 302:
			return .25;
		case 310:
			return .20;
		case 306:
			return .15;
		case 314:
			return .10;
		case 298: 
			return .05;
		}
		
		return 0;
	}
	
	@Override
	public int[] getClassArmorIds() {
		int item_ids[] = new int[4];
		
		item_ids[0] = 298;
		item_ids[1] = 301;
		item_ids[2] = 300;
		item_ids[3] = 299;

		return item_ids;
	}
	
	@Override
	public boolean isClassItem(ItemStack item) {
		int item_id = item.getTypeId();

		if (item_id == 274) return true;		// Stone
		else if (item_id == 278) return true;	// Diamond
		else if (item_id == 285) return true;	// Gold
		else if (item_id == 257) return true;	// Iron
		else if (item_id == 270) return true;	// Wooden
		
		return super.isClassItem(item);
	}

}
