package org.monk.MineQuest.Ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.Quester.SkillClass.SkillClass;

public class AbilityHailofArrows extends Ability {

	public AbilityHailofArrows(String name, SkillClass myclass) {
		super(name, myclass);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public List<ItemStack> getManaCost() {
		List<ItemStack> list = new ArrayList<ItemStack>();

		list.add(new ItemStack(262, 1));
		list.add(new ItemStack(262, 1));
		list.add(new ItemStack(262, 1));
		list.add(new ItemStack(262, 1));
		list.add(new ItemStack(262, 1));
		list.add(new ItemStack(262, 1));
		list.add(new ItemStack(262, 1));
		list.add(new ItemStack(262, 1));
		list.add(new ItemStack(262, 1));
		list.add(new ItemStack(262, 1));
		
		return list;
	}
	
	@Override
	public int getReqLevel() {
		return 5;
	}
	
	@Override
	public String getName() {
		return "Hail of Arrows";
	}
	
	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		if (entity != null) {
			Location start = entity.getLocation();
			start.setZ(start.getZ() - 3);
			start.setY(start.getY() + 5);
			Vector vel = new Vector(0, -5, 0);
			int i, j;
			
			for (i = 0; i < 3; i++) {
				start.setX(entity.getLocation().getX() - 3);
				for (j = 0; j < 3; j++) {
					entity.getWorld().spawnArrow(start, vel, (float).8, (float)12.0);
					start.setX(start.getX() + 3);
				}
				start.setZ(start.getZ() + 3);
			}
		} else {
			giveManaCost(quester.getPlayer());
			quester.getPlayer().sendMessage("Hail of Arrows must be bound to an attack - Recommended that it is ranged");
			return;
		}
	}

}