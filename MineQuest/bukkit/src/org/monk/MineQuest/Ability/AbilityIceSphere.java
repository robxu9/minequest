package org.monk.MineQuest.Ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.Quester.SkillClass;

public class AbilityIceSphere extends Ability {

	public AbilityIceSphere(String name, SkillClass myclass) {
		super(name, myclass);
		// TODO Auto-generated constructor stub
	}
	
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
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		Player player = quester.getPlayer();
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
						getNearestY((int)location.getX() + j, (int)location.getY(), (int)location.getZ() + k), 
						(int)location.getZ() + k);
				nblock.setTypeId(78);
			}
		}
		
		entity.setHealth(entity.getHealth() - 3 - (myclass.getCasterLevel() / 2));
	}

}
