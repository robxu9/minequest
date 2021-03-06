package org.monk.MineQuest.Ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Event.Absolute.ExplosionEvent;
import org.monk.MineQuest.Quester.Quester;

public class AbilityIceBlast extends Ability {
	public AbilityIceBlast() {
		super();
		config = new int[] {10};
	}

	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		AbilityIceSphere ability = new AbilityIceSphere();
		ability.setSkillClass(myclass);
		
		ability.castAbility(quester, location, entity);

		MineQuest.getEventQueue().addEvent(
				new ExplosionEvent(10, location.getWorld(), location.getX(),
						location.getY(), location.getZ(), 0, 0));

		for (LivingEntity lentity : Ability.getEntities(location, config[0])) {
			ability.castAbility(quester, lentity.getLocation(), lentity);
		}
	}

	@Override
	public List<ItemStack> getSpellComps() {
		List<ItemStack> cost = new ArrayList<ItemStack>();
		int i;
		
		for (i = 0; i < 5; i++) {
			cost.add(new ItemStack(Material.WATER_BUCKET, 1));
		}
		
		for (i = 0; i < 3; i++) {
			cost.add(new ItemStack(Material.SNOW_BLOCK, 1));
		}
		
		return cost;
	}

	@Override
	public String getName() {
		return "IceBlast";
	}

	@Override
	public int getReqLevel() {
		return 12;
	}

	@Override
	public String getSkillClass() {
		return "WarMage";
	}

}
