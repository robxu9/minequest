package org.monk.MineQuest.Ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Event.Absolute.EntityTeleportEvent;
import org.monk.MineQuest.Event.Relative.AuraEvent;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.Quester.SkillClass.SkillClass;
import org.monk.MineQuest.Quester.SkillClass.Combat.Warrior;

public class AbilityWhirlwind extends Ability {

	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		if (quester == null) return;
		Location loc = quester.getPlayer().getLocation();
		loc = new Location(loc.getWorld(),
				loc.getX(), loc.getY(), loc.getZ(),
				loc.getYaw(), loc.getPitch());
		int i;
		for (i = 0; i < 10; i++) {
			loc= new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw() + (360 / 10), loc.getPitch());
			MineQuest.getEventParser().addEvent(new EntityTeleportEvent(100 * (i + 1), quester, loc));
		}
		purgeEntities(quester.getPlayer(), 2, PurgeType.ALL);
		MineQuest.getEventParser().addEvent(new AuraEvent(quester, 500, 500, -9, false, 3));
	}

	@Override
	public SkillClass getClassType() {
		return new Warrior();
	}

	@Override
	public List<ItemStack> getManaCost() {
		List<ItemStack> cost = new ArrayList<ItemStack>();
		
		cost.add(new ItemStack(Material.STONE_SWORD, 1));
		cost.add(new ItemStack(Material.FEATHER, 1));
		cost.add(new ItemStack(Material.FEATHER, 1));
		
		return cost;
	}

	@Override
	public String getName() {
		return "Whirlwind";
	}

	@Override
	public int getReqLevel() {
		return 15;
	}

}
