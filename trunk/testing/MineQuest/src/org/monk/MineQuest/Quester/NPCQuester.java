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
package org.monk.MineQuest.Quester;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.entity.EntityDamageEvent;
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Event.NPCEvent;
import org.monk.MineQuest.Event.Absolute.SpawnNPCEvent;

import redecouverte.npcspawner.BasicHumanNpc;
import redecouverte.npcspawner.NpcSpawner;

public class NPCQuester extends Quester {
	private BasicHumanNpc entity;
	private NPCMode mode;
	private double speed = .2;
	private Quester follow;
	private Location target = null;
	
	public NPCQuester(String name) {
		super(name);
		this.entity = null;
	}
	
	public NPCQuester(String name, NPCMode mode, World world, Location location) {
		this.name = name;
		this.mode = mode;
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		double pitch = location.getPitch();
		double yaw = location.getYaw();
		if (mode != NPCMode.QUEST_NPC) {
			create(mode, world, x, y, z, pitch, yaw);
			update();
		} else {
			makeNPC(world.getName(), x, y, z, (float)pitch, (float)yaw);
			health = max_health = 2000;
		}
		distance = 0;
		entity = null;
	}
	
	public void setFollow(Quester quester) {
		this.follow = quester;
		target = null;
		MineQuest.getEventParser().addEvent(new NPCEvent(10, this));
	}
	
	public void setMode(NPCMode mode) {
		this.mode = mode;
		target = null;
	}
	
	public void setEntity(BasicHumanNpc entity) {
		this.entity = entity;
		setPlayer(entity.getBukkitEntity());
	}
	
	public BasicHumanNpc getEntity() {
		return entity;
	}
	
	public void create(NPCMode mode, World world, double x, double y, double z, double pitch, double yaw) {
		if (mode != NPCMode.QUEST_NPC) {
			super.create();
	
			MineQuest.getSQLServer().update("UPDATE questers SET x='" + 
					x + "', y='" + 
					y + "', z='" + 
					z + "', pitch='" + 
					pitch + "', yaw='" + 
					yaw + "', mode='" + 
					mode + "', world='" + 
					world.getName() + "' WHERE name='"
					+ name + "'");
		}
	}
	
	@Override
	public void save() {
		if (mode == NPCMode.QUEST_NPC) return;
		super.save();
		
		if (entity == null) return;
		MineQuest.getSQLServer().update("UPDATE questers SET x='" + 
				entity.getBukkitEntity().getLocation().getX() + "', y='" + 
				entity.getBukkitEntity().getLocation().getY() + "', z='" + 
				entity.getBukkitEntity().getLocation().getZ() + "', mode='" + 
				mode + "', world='" + 
				entity.getBukkitEntity().getWorld().getName() + "' WHERE name='"
				+ name + "'");
	}
	
	@Override
	public boolean healthChange(int change, EntityDamageEvent event) {
		boolean ret = super.healthChange(change, event);
		
		if (mode == NPCMode.GOD) {
			health = max_health;
			event.setDamage(0);
		} else {
			if (getHealth() <= 0) {
				setPlayer(null);
				NpcSpawner.RemoveBasicHumanNpc(entity);
				entity = null;
				removeSql();
				MineQuest.remQuester(this);
			}
		}
		
		return ret;
	}
	
	private void removeSql() {
		MineQuest.getSQLServer().update("DELETE FROM questers WHERE name='" + name + "'");
		MineQuest.getSQLServer().update("DROP TABLE " + name);
		MineQuest.getSQLServer().update("DROP TABLE " + name + "_chests");
	}

	@Override
	public void setHealth(int i) {
		super.setHealth(i);
		
		if (getHealth() <= 0) {
			setPlayer(null);
			NpcSpawner.RemoveBasicHumanNpc(entity);
			entity = null;
			removeSql();
		}
	}
	
	@Override
	public void damage(int i) {
		super.damage(i);
	}

	public void update() {
		if (mode == NPCMode.QUEST_NPC) {
			return;
		}
		super.update();

		ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM questers WHERE name='" + name + "'");

		try {
			if (!results.next()) return;
			this.mode = NPCMode.getNPCMode(results.getString("mode"));
			double x = results.getDouble("x");
			double y = results.getDouble("y");
			double z = results.getDouble("z");
			double pitch = results.getDouble("pitch");
			double yaw = results.getDouble("yaw");
			String world = results.getString("world");
			makeNPC(world, x, y, z, (float)pitch, (float)yaw);
		} catch (SQLException e) {
			MineQuest.log("Unable to add NPCQuester");
		}
	}

	private void makeNPC(String world, double x, double y, double z,
			float pitch, float yaw) {
		if (entity != null) {
			entity.getBukkitEntity().setHealth(0);
			entity = null;
		}
		MineQuest.getEventParser().addEvent(new SpawnNPCEvent(200, this, world, x, y, z, (float)pitch, (float)yaw));
	}

	public void activate() {
		if (mode == NPCMode.FOLLOW) {
			if (follow == null) return;
			if (target == null) {
				if (MineQuest.distance(follow.getPlayer().getLocation(), player.getLocation()) < 4) {
					return;
				} else {
					target = new Location(follow.getPlayer().getLocation().getWorld(),
							follow.getPlayer().getLocation().getX(),
							follow.getPlayer().getLocation().getY(),
							follow.getPlayer().getLocation().getZ(),
							follow.getPlayer().getLocation().getYaw(),
							follow.getPlayer().getLocation().getPitch());
				}
			}
			if (MineQuest.distance(player.getLocation(), target) < speed) {
				entity.moveTo(target.getX(), target.getY(), target.getZ(), target.getYaw(), target.getPitch());
				target = null;
			} else {
				double distance = MineQuest.distance(player.getLocation(), target);
				entity.moveTo(
						player.getLocation().getX() + 
						(speed * (target.getX() - player.getLocation().getX()) / distance),
						player.getLocation().getY() + 
						(speed * (target.getY() - player.getLocation().getY()) / distance),
						player.getLocation().getZ() + 
						(speed * (target.getZ() - player.getLocation().getZ()) / distance),
						target.getYaw(),
						target.getPitch());
			}
		}
	}

	public NPCMode getMode() {
		return mode;
	}
}