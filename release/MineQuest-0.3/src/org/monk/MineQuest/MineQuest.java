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
package org.monk.MineQuest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.monk.MineQuest.Ability.Ability;
import org.monk.MineQuest.Event.CheckMobEvent;
import org.monk.MineQuest.Event.EventQueue;
import org.monk.MineQuest.Listener.MineQuestBlockListener;
import org.monk.MineQuest.Listener.MineQuestEntityListener;
import org.monk.MineQuest.Listener.MineQuestPlayerListener;
import org.monk.MineQuest.Mob.MQMob;
import org.monk.MineQuest.Mob.SpecialMob;
import org.monk.MineQuest.Quest.Quest;
import org.monk.MineQuest.Quester.NPCQuester;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.World.Town;

/**
 * This is the main class of MineQuest. It holds static lists of players in the server,
 * Towns in the server, Properties owned by players, and in the future will hold lists
 * of quest status information. It has public static methods to access all of these
 * including the bukkit server.
 * 
 * @author jmonk
 *
 */
public class MineQuest extends JavaPlugin {
	private static EventQueue eventQueue;
	private static String namer;
	private static List<Quester> questers = new ArrayList<Quester>();
	private static Server server;
	private static MysqlInterface sql_server;
	private static Location start;
	private static List<Town> towns = new ArrayList<Town>();
	private static MQMob mobs[];
	private static Quest[] quests;
	private static int maxClass;
//	private MineQuestServerListener sl;
//	private MineQuestVehicleListener vl;
//	private MineQuestWorldListener wl;
	private static String server_owner;
	
	/**
	 * Adds a Quester to the MineQuest Server.
	 * Does not modify mysql database.
	 * 
	 * @param quester Quester to be added
	 */
	static public void addQuester(Quester quester) {
		questers.add(quester);
	}
	/**
	 * Adds a town to the MineQuest Server. 
	 * Does not modify mysql database.
	 * 
	 * @param town Town to be added
	 */
	static public void addTown(Town town) {
		towns.add(town);
	}
	/**
	 * Starts the creation of town based on Player
	 * Location.
	 * 
	 * @param player Player Creating the Town
	 */
	public static void createTown(Player player) {
		if (!isMayor(getQuester(player))) {
			player.sendMessage("Only mayors are allowed to create towns");
		} else {
			start = player.getLocation();
			namer = player.getName();
		}
	}
	
	public static int getMaxClasses() {
		return maxClass;
	}
	

	
	
    /**
     * This is a utility for various parts of MineQuest to calculate
     * the distance between two locations.
     * 
     * @param loc1 First Location
     * @param loc2 Second Location
     * @return Distance between first and second locations
     */
	static public double distance(Location loc1, Location loc2) {
		double x, y, z;
		
		x = loc1.getX() - loc2.getX();
		y = loc1.getY() - loc2.getY();
		z = loc1.getZ() - loc2.getZ();
		
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	public static int getNextAbilId() {
		int num = 0;
		try {
			ResultSet results = sql_server.query("SELECT * FROM abilities");
			while (results.next()) {
				num++;
			}
		} catch (SQLException e) {
			System.out.println("Unable to get max ability id");
		}
		
		return num;
	}
    
    /**
	 * Finishes creation of town based on Player
	 * Location.
	 * 
	 * @param player Player Creating Town
	 * @param name Name of Town
	 */
	public static void finishTown(Player player, String name) {
		if (!isMayor(getQuester(player))) {
			player.sendMessage("Only mayors are allowed to create towns");
		} else {
			if (namer.equals(player.getName())) {
				Location end = player.getLocation();
				int x, z, max_x, max_z;
				int spawn_x, spawn_y, spawn_z;
				if (end.getX() > start.getX()) {
					x = (int)start.getX();
					max_x = (int)end.getX();
				} else {
					x = (int)end.getX();
					max_x = (int)start.getX();
				}
				if (end.getZ() > start.getZ()) {
					z = (int)start.getZ();
					max_z = (int)end.getZ();
				} else {
					z = (int)end.getZ();
					max_z = (int)start.getZ();
				}
				spawn_x = (x + max_x) / 2;
				spawn_y = (int)(start.getY() + end.getY()) / 2;
				spawn_z = (z + max_z) / 2;
				sql_server.update("INSERT INTO towns (name, x, z, max_x, max_z, spawn_x, spawn_y, spawn_z, owner, height, y) VALUES('"
						+ name + "', '" + x + "', '" + z + "', '" + max_x + "', '" + max_z + "', '" + spawn_x + "', '"
						+ spawn_y + "', '" + spawn_z + "', '" + player.getName() + "', '0', '0')");
				sql_server.update("CREATE TABLE IF NOT EXISTS " + name + 
						"(height INT, x INT, y INT, z INT, max_x INT, max_z INT, price INT, name VARCHAR(30), store_prop BOOLEAN)");
				towns.add(new Town(name, getSServer().getWorld("world")));
				player.sendMessage("Town " + name + " created");
			} else {
				player.sendMessage(namer + " is in the process of creating a town - use /createtown to start a new creation");
			}
		}
	}
    
    /**
	 * Gets the difficulty adjustement of the MineQuest Server.
	 * As the level of players goes up the natural encounter
	 * of monsters gets harder to compensate.
	 * 
	 * @return Adjustment Factor to be used
	 */
	public static int getAdjustment() {
        int avgLevel = 0;
        int size = 0;
        for (Quester quester : questers) {
            if (quester.getPlayer() == null) {
	            avgLevel += quester.getLevel();
	            size++;
            }
        }
        if (size == 0) return 0;
        avgLevel /= size;
        
        return (avgLevel / 10);
	}
	
    /**
     * Gets the EventParser being used by MineQuest.
     * 
     * @return EventParser
     */
    static public EventQueue getEventParser() {
    	return eventQueue;
    }
	
	/**
	 * Returns whatever town has the closest spawn point to
	 * the Location.
	 * 
	 * @param to Location
	 * @return Closest Town
	 */
	public static Town getNearestTown(Location to) {
		if (towns.size() == 0) return null;
		
		Town town = towns.get(0);
		int i;
		
		for (i = 1; i < towns.size(); i++) {
			if (distance(to, town.getLocation()) > distance(to, towns.get(i).getLocation())) {
				town = towns.get(i);
			}
		}
		
		return town;
	}
	
	/**
	 * Gets a Quester of a specific Player
	 * 
	 * @param entity Player that is a Quester
	 * @return Quester or NULL if none found
	 */
	static public Quester getQuester(HumanEntity entity) {
		return getQuester(entity.getName());
	}
	
	/**
	 * Gets a Quester with a specific player name
	 * 
	 * @param name Name of Quester
	 * @return Quester with Name name or NULL
	 */
	static public Quester getQuester(String name) {
		int i;
		
		for (i = 0; i < questers.size(); i++) {
			if (questers.get(i).equals(name)) {
				return questers.get(i);
			}
		}

		log("[WARNING] Cannot find quester " + name);
		return null;
	}
	
	/**
	 * Returns lists of Questers within server.
	 * 
	 * @return List of Questers
	 */
	static public List<Quester> getQuesters() {
		return questers;
	}

	/**
	 * Gets an interface to the mysql server being used by
	 * MineQuest.
	 * 
	 * @return mysql_interface of MineQuest DB
	 */
	public static MysqlInterface getSQLServer() {
		return sql_server;
	}
	
	/**
	 * Returns the Bukkit Server.
	 * 
	 * @return Bukkit Server
	 */
	public static Server getSServer() {
		return server;
	}
	 
	/**
	 * Gets a town that a specific location is within.
	 * 
	 * @param loc Location within town.
	 * @return Town that location is in or NULL is none exists
	 */
	static public Town getTown(Location loc) {
		int i;
		
		for (i = 0; i < towns.size(); i++) {
			if (towns.get(i).inTown(loc)) {
				return towns.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Gets a town that a specific player is within.
	 * 
	 * @param player Player within town
	 * @return Town that player is in or NULL if none exists
	 */
	static public Town getTown(Player player) {
		return getTown(player.getLocation());
	}
	
	/**
	 * Gets a town based on name of the town.
	 * 
	 * @param name Name of the town
	 * @return Town with Name name or NULL is none exists
	 */
	static public Town getTown(String name) {
		int i;
		
		for (i = 0; i < towns.size(); i++) {
			if (towns.get(i).equals(name)) {
				return towns.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the list of towns in the server.
	 * 
	 * @return List of towns
	 */
	static public List<Town> getTowns() {
		return towns;
	}
	
	/**
	 * Determines if all three axis of loc have higher value
	 * than loc2.
	 * 
	 * @param loc Larger Location
	 * @param loc2 Smaller Location
	 * @return Boolean true if loc is greater
	 */
	public static boolean greaterLoc(Location loc, Location loc2) {
		if (loc.getX() < loc2.getX()) {
			return false;
		}
		if (loc.getY() < loc2.getY()) {
			return false;
		}
		if (loc.getZ() < loc2.getZ()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Determines if a Quester is a Mayor of any town.
	 * Used to determine permissions for creation of towns.
	 * 
	 * @param quester Quester to Test if Mayor
	 * @return Boolean true if Quester is a Mayor
	 */
	public static boolean isMayor(Quester quester) {
		if (quester.equals(server_owner)) {
			return true;
		}
		if (quester.getPlayer().isOp()) {
			return true;
		}
		
		for (Town t : towns) {
			if (t.getTownProperty().getOwner().equals(quester)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Gets a string containing the spell components for a given ability.
	 * 
	 * @param string Ability Name
	 * @return String of Components
	 */
	public static String listSpellComps(String string) {
		Ability ability = Ability.newAbility(string, null);
		String ret = new String();
		
		for (ItemStack item : reduce(ability.getRealManaCost())) {
			ret = ret + item.getAmount() + " " + item.getType().toString() + " ";
		}
		
		return ret;
	}

	private static List<ItemStack> reduce(List<ItemStack> manaCost) {
		List<ItemStack> ret = new ArrayList<ItemStack>();
		boolean flag;
		
		for (ItemStack itm : manaCost) {
			flag = false;
			for (ItemStack item : ret) {
				if (item.getTypeId() == itm.getTypeId()) {
					flag = true;
					item.setAmount(item.getAmount() + itm.getAmount());
					break;
				}
			}
			if (!flag) {
				ret.add(itm);
			}
		}
		
		return ret;
	}
	/**
	 * Prints to screen the message preceded by [MineQuest].
	 * 
	 * @param string Message to Print
	 */
	public static void log(String string) {
		//log.info("[MineQuest] " + string);
		System.out.println("[MineQuest] " + string);
	}

	/**
	 * Removes a Quester from the MineQuest Server.
	 * Does not modify mysql database.
	 * 
	 * @param quester Quester to be removed
	 */
	static public void remQuester(Quester quester) {
		questers.remove(quester);
	}

	/**
	 * Removes a Quester from the MineQuest Server.
	 * Does not modify mysql database.
	 * 
	 * @param name Name of Quester to be removed
	 */
	static public void remQuester(String name) {
		questers.remove(getQuester(name));
	}

	/**
	 * Removes a Town from the MineQuester Server.
	 * Does not modify mysql database.
	 * 
	 * @param name Name of Town to remove
	 */
	static public void remTown(String name) {
		towns.remove(getTown(name));
	}

	/**
	 * Removes a Town from the MineQuester Server.
	 * Does not modify mysql database.
	 * 
	 * @param town Town to remove
	 */
	static public void remTown(Town town) {
		towns.remove(town);
	}

	private MineQuestBlockListener bl;

	private MineQuestEntityListener el;

	private MineQuestPlayerListener pl;

	public MineQuest() {
	}

	@Override
	public void onDisable() {
		for (Quester quester : questers) {
			if (quester.getPlayer() != null) {
				quester.save();
			}
		}
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!" );
	}
	
	/**
	 * Sets up an instance of MineQuest. There should never be more than
	 * one instance of MineQuest required. If enabled this method will load all of the
	 * static variables with required information and creating a second instance
	 * will reset all of those, and eventually create inconsistancies in the server.
	 * 
	 * This loads all adjustable parameters from minequest.properties, including
	 * database location and login parameters.
	 */
	@Override
	public void onEnable() {			
		server = getServer();
        
		mobs = new MQMob[1];
		
        eventQueue = new EventQueue(this);
        
        quests = new Quest[0];
        
        try {
			String url, port, db, user, pass;
			PropertiesFile minequest = new PropertiesFile("minequest.properties");
			url = minequest.getString("url", "localhost");
			port = minequest.getString("port", "3306");
			db = minequest.getString("db", "minequest");
			user = minequest.getString("user", "root");
			pass = minequest.getString("pass", "1234");
			maxClass = minequest.getInt("max_classes", 4);
			boolean real = minequest.getBoolean("mysql", true);
			server_owner = minequest.getString("mayor", "jmonk");
			sql_server = new MysqlInterface(url, port, db, user, pass, minequest.getInt("silent", 1), real);
			
			sql_server.update("CREATE TABLE IF NOT EXISTS questers (name VARCHAR(30), health INT, max_health INT, cubes DOUBLE, exp INT, " +
					"last_town VARCHAR(30), level INT, enabled INT, selected_chest VARCHAR(33), classes VARCHAR(150), " +
					"mode VARCHAR(30) DEFAULT 'Quester', world VARCHAR(30) DEFAULT 'world', x DOUBLE DEFAULT '0', y DOUBLE DEFAULT '0', z DOUBLE DEFAULT '0', " +
					"pitch DOUBLE DEFAULT '0', yaw DOUBLE DEFAULT '0')");
			sql_server.update("CREATE TABLE IF NOT EXISTS classes (name VARCHAR(30), class VARCHAR(30), exp INT, level INT, abil_list_id INT)");
			sql_server.update("CREATE TABLE IF NOT EXISTS abilities (abil_list_id INT, abil0 VARCHAR(30) DEFAULT '0', abil1 VARCHAR(30) DEFAULT '0', abil2 VARCHAR(30) DEFAULT '0'," +
					"abil3 VARCHAR(30) DEFAULT '0', abil4 VARCHAR(30) DEFAULT '0', abil5 VARCHAR(30) DEFAULT '0', abil6 VARCHAR(30) DEFAULT '0', abil7 VARCHAR(30) DEFAULT '0', abil8 VARCHAR(30) DEFAULT '0', abil9 VARCHAR(30) DEFAULT '0')");
        } catch (Exception e) {
        	MineQuest.log("Unable to initialize database");
        	MineQuest.log("Check minequest.properties");
        	setEnabled(false);
        	return;
        }

		ResultSet results = sql_server.query("SELECT * FROM questers");
		List<String> names = new ArrayList<String>();
		List<String> npcs = new ArrayList<String>();
		
		try {
			while (results.next()) {
				if (results.getString("mode").equals("Quester")) {
					names.add(results.getString("name"));
				} else {
					npcs.add(results.getString("name"));
				}
			}
		} catch (SQLException e) {
			log("Error: Couldn't get list of questers");
		}
		
		for (String name : names) {
			questers.add(new Quester(name));
		}
		
		for (String name : npcs) {
			questers.add(new NPCQuester(name));
		}
		
		sql_server.update("CREATE TABLE IF NOT EXISTS towns (name VARCHAR(30), x INT, z INT, max_x INT, max_z INT, spawn_x INT, spawn_y INT, spawn_z INT, " +
				"owner VARCHAR(30), height INT, y INT)");
		names.clear();
		results = sql_server.query("SELECT * FROM towns");
		
		try {
			while (results.next()) {
				names.add(results.getString("name"));
			}
		} catch (SQLException e) {
			log("Unable to get list of towns");
		}
		
		for (String name : names) {
			towns.add(new Town(name, getServer().getWorld("world")));
		}
		
		bl = new MineQuestBlockListener();
		el = new MineQuestEntityListener();
		pl = new MineQuestPlayerListener();
//		sl = new MineQuestServerListener();
//		wl = new MineQuestWorldListener();
//		vl = new MineQuestVehicleListener();
		
        PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvent(Event.Type.PLAYER_JOIN, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_TELEPORT, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_ANIMATION, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_COMBUST, el, Priority.Highest, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, el, Priority.Highest, this);
        pm.registerEvent(Event.Type.CREATURE_SPAWN, el, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, bl, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, bl, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, bl, Priority.Normal, this);
//		pm.registerEvent(Event.Type.BLOCK_INTERACT, bl, Priority.Normal, this);
//		pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKE, bl, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
        start = null;
		
		for (Town town : towns) {
			eventQueue.addEvent(new CheckMobEvent(town));
		}
	}
    
    public static void checkMobs() {
    	int i;
    	
    	for (i = 0; i < mobs.length; i++) {
    		if ((mobs[i] != null) && (mobs[i].getHealth() <= 0)) {
    			mobs[i].dropLoot();
    			mobs[i] = null;
    		}
    	}
    }

	public static void addMob(Monster entity) {
		Random generator = new Random();
		MQMob newMob;
		
		if (getMob(entity) != null) return;
		
		if (generator.nextDouble() < (getAdjustment() / 100.0)) {
			newMob = new SpecialMob(entity);
		} else {
			newMob = new MQMob(entity);
		}
		
		addMQMob(newMob);
	}
	
	public static void addMQMob(MQMob newMob) {
		int i;
		for (i = 0; i < mobs.length; i++) {
			if (mobs[i] == null) {
				mobs[i] = newMob;
				return;
			}
		}
		
		MQMob newList[] = new MQMob[mobs.length*2];
		i = 0;
		for (MQMob mob : mobs) {
			newList[i++] = mob;
		}
		newList[i++] = newMob;
		while (i < newList.length){
			newList[i++] = null;
		}
		
		mobs = newList;
	}
	
	public static MQMob getMob(LivingEntity entity) {
		for (MQMob mob : mobs) {
			if (mob != null) {
				if (mob.getId() == entity.getEntityId()) {
					return mob;
				}
			}
		}
		
		return null;
	}
	
	public static Quester[] getActiveQuesters() {
		List<Quester> active = new ArrayList<Quester>();
		
		for (Quester quester : questers) {
			if (quester.getPlayer() != null) {
				active.add(quester);
			}
		}
		
		Quester[] questers = new Quester[active.size()];
		int i;
		for (i = 0; i < active.size(); i++) {
			questers[i] = active.get(i);
		}
		
		return questers;
	}
	
	public static void setMQMob(MQMob newMob) {
		int i;
		
		for (i = 0; i < mobs.length; i++) {
			if (mobs[i] != null) {
				if (mobs[i].getId() == newMob.getId()) {
					mobs[i].cancel();
					mobs[i] = newMob;
				}
			}
		}
		
		addMQMob(newMob);
	}
	public static Quest[] getQuests() {
		return quests;
	}
	
	public static void remQuest(Quest quest) {
		Quest[] new_quests = new Quest[quests.length - 1];
		int i = 0;
		for (Quest qst : quests) {
			if (!qst.equals(quest)) {
				new_quests[i++] = qst;
			}
		}
		
		quests = new_quests;
	}
	
	public static void addQuest(Quest quest) {
		Quest[] new_quests = new Quest[quests.length + 1];
		int i = 0;
		for (Quest qst : quests) {
			new_quests[i++] = qst;
		}
		
		new_quests[i] = quest;
		
		quests = new_quests;
	}
	public static void damage(LivingEntity entity, int i) {
		if (entity instanceof Player) {
			Quester quester = getQuester((Player)entity);
			quester.setHealth(quester.getHealth() - i);
		} else if (getMob(entity) != null) {
			getMob(entity).damage(i);
		} else {
			int newHealth = entity.getHealth() - i;
			
			if (newHealth <= 0) newHealth = 0;
			
			entity.setHealth(newHealth);
		}
	}
}
