package org.monk.MineQuest.Ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.Quester.SkillClass;

public class AbilityHeal extends Ability {

	public AbilityHeal(String name, SkillClass myclass) {
		super(name, myclass);
		// TODO Auto-generated constructor stub
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
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		Player player = quester.getPlayer();
		player.getInventory().addItem(new ItemStack(325, 1));
		if (quester.getHealth() < quester.getMaxHealth()) {
			quester.setHealth(quester.getHealth() + myclass.getCasterLevel() + myclass.getGenerator().nextInt(8) + 1);
		} else {
			player.sendMessage("Quester must not be at full health to heal");
			return;
		}
	}
	

}
