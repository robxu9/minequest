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
package org.monk.MineQuest.Event.Relative;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Ability.Ability;
import org.monk.MineQuest.Event.EventParser;
import org.monk.MineQuest.Event.NormalEvent;
import org.monk.MineQuest.Quester.Quester;

public class AuraEvent extends NormalEvent {
	protected LivingEntity player;
	protected World world;
	private long total_time;
	private int change;
	private boolean players;
	private long count;

	public AuraEvent(Ability ability, Quester quester, long delay, long total_time, int change, boolean players) {
		super(delay);
		player = quester.getPlayer();
		world = player.getWorld();
		this.total_time = total_time;
		this.count = 0;
		this.change = change;
		this.players = players;
	}

	public void activate(EventParser eventParser) {
		List<LivingEntity> nearby = Ability.getEntities(player, 15);
		List<LivingEntity> affected = sort(nearby, players);
		
		for (LivingEntity entity : affected) {
			if (players) {
				MineQuest.getQuester((Player)entity).setHealth(MineQuest.getQuester((Player)entity).getHealth() + change);
			} else {
				MineQuest.getMob(entity).damage(change);
			}
		}
		
		count += delay;
		if (count < total_time) {
			eventParser.setComplete(false);
		} else {
			eventParser.setComplete(true);
		}
	}

	private List<LivingEntity> sort(List<LivingEntity> nearby, boolean players) {
		List<LivingEntity> ret = new ArrayList<LivingEntity>();
		
		for (LivingEntity entity : nearby) {
			if (players && (entity instanceof Player)) {
				ret.add(entity);
			} else if (!players && !(entity instanceof Player)){
				ret.add(entity);
			}
		}
		
		return ret;
	}
}
