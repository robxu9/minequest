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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.martin.bukkit.npclib.NPCManager;
import org.monk.MineQuest.Ability.Ability;
import org.monk.MineQuest.Ability.AbilityConfigManager;
import org.monk.MineQuest.Event.EventQueue;
import org.monk.MineQuest.Event.NoMobs;
import org.monk.MineQuest.Event.Absolute.HealEvent;
import org.monk.MineQuest.Listener.MineQuestBlockListener;
import org.monk.MineQuest.Listener.MineQuestEntityListener;
import org.monk.MineQuest.Listener.MineQuestPlayerListener;
import org.monk.MineQuest.Listener.MineQuestServerListener;
import org.monk.MineQuest.Mob.MQMob;
import org.monk.MineQuest.Mob.SpecialMob;
import org.monk.MineQuest.Quest.Quest;
import org.monk.MineQuest.Quester.NPCQuester;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.Quester.SkillClass.SkillClass;
import org.monk.MineQuest.Store.NPCStringConfig;
import org.monk.MineQuest.World.Town;

import com.iConomy.iConomy;

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
	private static iConomy IConomy = null;
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
	private MineQuestServerListener sl;
//	private MineQuestVehicleListener vl;
//	private MineQuestWorldListener wl;
	private static String server_owner;
	private static boolean town_enable = true;
	private static boolean debug_enable = true;
	private static boolean cubonomy_enable = true;
	private static int armor_req_level;
	private static NPCManager npc_m;
	
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
	
	public static boolean isTownEnabled() {
		return town_enable;
	}
	
	public static boolean isDebugEnabled() {
		return debug_enable;
	}
	
	public static boolean isCubonomyEnabled() {
		return cubonomy_enable;
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
			if (namer == null) {
				player.sendMessage("You have to use /createtown first...");
				return;
			}

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
				towns.add(new Town(name, getSServer().getWorlds().get(0)));
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
            if (quester.getPlayer() != null) {
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
	 * @param player Player that is a Quester
	 * @return Quester or NULL if none found
	 */
	static public Quester getQuester(LivingEntity player) {
		if (!(player instanceof HumanEntity)) return null;
		
		return getQuester(((HumanEntity)player).getName());
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
	static public Town getTown(HumanEntity player) {
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
		
		if (ability == null) {
			ret = string + " is not a valid ability";
			return ret;
		}
		
		ability.setSkillClass(new SkillClass());
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
		if (quester.getPlayer() != null) {
			for (Quester npc : questers) {
				if (npc instanceof NPCQuester) {
					((NPCQuester)npc).clearTarget(quester.getPlayer());
				}
			}
		}
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
	private String version;
	private static int gold_req_level;
	private static int stone_req_level;
	private static int iron_req_level;
	private static int diamond_req_level;
	private static int leather_armor_miner_level;
	private static int destroy_materials_level;
	private static int destroy_class_exp;
	private static int destroy_non_class_exp;
	private static int destroy_block_exp;
	private static int adjustment_multiplier;
	private static int exp_damage;
	private static int cast_ability_exp;
	private static int exp_class_damage;
	private static AbilityConfigManager ability_config;
	private static double sell_percent;
	private static double price_change;
	private static int starting_health;
	private static int npc_cost;
	private static int npc_cost_class;
	private static List<String> noMobs;
	private static NPCStringConfig npc_strings = new NPCStringConfig();
	private static boolean town_protect;
	private static boolean npc_enabled;
	private static boolean track_kills;
	private static boolean track_destroy;
	private static boolean town_no_mobs;
	private static boolean log_health_change;
	private static boolean health_spawn_enable;
	private static int[] town_exceptions;

	public MineQuest() {
	}

	@Override
	public void onDisable() {
		for (Quest quest : quests) {
			quest.issueNextEvents(-1);
		}
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
        
        PluginDescriptionFile pdfFile = this.getDescription();
        version = pdfFile.getVersion();		
		server = getServer();
        
		mobs = new MQMob[1];
		
        eventQueue = new EventQueue(this);
        
        quests = new Quest[0];
        
        npc_m = new NPCManager(this);
        
        (new File("MineQuest/")).mkdir();
        
        if (!((new File("MineQuest/abilities.jar")).exists())) {
        	log("MineQuest/abilities.jar not found: Downloading...");
        	try {
				downloadAbilities();
	        	log("MineQuest/abilities.jar download complete");
			} catch (Exception e) {
				log("Failed to download abilities.jar");
			}
        }
        
        ability_config = new AbilityConfigManager();
        
        noMobs = new ArrayList<String>();
        
        try {
			String url, port, db, user, pass;
			PropertiesFile minequest = new PropertiesFile("MineQuest/main.properties");
			PropertiesFile restrictions = new PropertiesFile("MineQuest/restrictions.properties");
			PropertiesFile experience = new PropertiesFile("MineQuest/experience.properties");
			PropertiesFile general = new PropertiesFile("MineQuest/general.properties");
			PropertiesFile npc = new PropertiesFile("MineQuest/npc.properties");
			url = minequest.getString("url", "localhost");
			port = minequest.getString("port", "3306");
			db = minequest.getString("db", "MineQuest/minequest");
			user = minequest.getString("user", "root");
			pass = minequest.getString("pass", "1234");
			maxClass = minequest.getInt("max_classes", 4);
			boolean real = minequest.getBoolean("mysql", false);
			boolean nomobs_main = minequest.getBoolean("no_mobs_main_world", false);
			if (nomobs_main) {
				eventQueue.addEvent(new NoMobs(5000, "world"));
				noMobs.add("world");
			}
			sql_server = new MysqlInterface(url, port, db, user, pass, minequest.getInt("silent", 1), real);
			
			armor_req_level = restrictions.getInt("armor_req_level", 20);
			gold_req_level = restrictions.getInt("gold_req_level", 2);
			stone_req_level = restrictions.getInt("stone_req_level", 5);
			iron_req_level = restrictions.getInt("iron_req_level", 20);
			diamond_req_level = restrictions.getInt("diamond_req_level", 50);
			destroy_materials_level = restrictions.getInt("destroy_materials_level", 5);
			leather_armor_miner_level = restrictions.getInt("leather_armor_miner_level", 2);
			
			town_enable = general.getBoolean("town_enable", true);
			npc_enabled = general.getBoolean("npc_enable", true);
			track_kills = general.getBoolean("track_kills", true);
			track_destroy = general.getBoolean("track_destroy", true);
			town_protect = general.getBoolean("town_protect", true);
			town_no_mobs = general.getBoolean("town_no_mobs", true);
			log_health_change = general.getBoolean("log_health_change", true);
			cubonomy_enable = general.getBoolean("cubonomy_enable", true);
			debug_enable = general.getBoolean("debug_enable", true);
			health_spawn_enable = general.getBoolean("health_spawn_enable", false);
			boolean slow_heal = general.getBoolean("slow_heal", false);
			if (slow_heal) {
				int amount = general.getInt("slow_heal_amount", 1);
				int delay = general.getInt("slow_heal_delay_ms", 1500);
				getEventParser().addEvent(new HealEvent(delay, amount));
			}
			server_owner = general.getString("mayor", "jmonk");
			sell_percent = general.getDouble("sell_return", .92);
			price_change = general.getDouble("price_change", .009);
			starting_health = general.getInt("starting_health", 10);
			String exceptions = general.getString("town_edit_exception", "64,77");
			if (exceptions.contains(",")) {
				String[] split = exceptions.split(",");
				town_exceptions = new int[split.length];
				int i;
				for (i = 0; i < split.length; i++) {
					town_exceptions[i] = Integer.parseInt(split[i]);
				}
			} else {
				try {
					town_exceptions = new int[] {Integer.parseInt(exceptions)};
				} catch (Exception e) {
					town_exceptions = new int[0];
				}
			}
			
			npc_cost = npc.getInt("npc_cost_level", 1000);
			npc_cost_class = npc.getInt("npc_cost_class", 1000);

			destroy_class_exp = experience.getInt("destroy_class", 5);
			destroy_non_class_exp = experience.getInt("destroy_non_class", 2);
			destroy_block_exp = experience.getInt("destroy_block", 2);
			adjustment_multiplier = experience.getInt("adjustment_multiplier",
					1);
			exp_damage = experience.getInt("damage", 3);
			cast_ability_exp = experience.getInt("cast_ability", 5);
			exp_class_damage = experience.getInt("class_damage", 5);

			sql_server
					.update("CREATE TABLE IF NOT EXISTS questers (name VARCHAR(30), health INT, max_health INT, cubes DOUBLE, exp INT, "
							+ "last_town VARCHAR(30), level INT, enabled INT, selected_chest VARCHAR(33), classes VARCHAR(150), "
							+ "mode VARCHAR(30) DEFAULT 'Quester', world VARCHAR(30) DEFAULT 'world', x DOUBLE DEFAULT '0', y DOUBLE DEFAULT '0', z DOUBLE DEFAULT '0', "
							+ "pitch DOUBLE DEFAULT '0', yaw DOUBLE DEFAULT '0')");
			sql_server
					.update("CREATE TABLE IF NOT EXISTS classes (name VARCHAR(30), class VARCHAR(30), exp INT, level INT, abil_list_id INT)");
			sql_server
					.update("CREATE TABLE IF NOT EXISTS abilities (abil_list_id INT, abil0 VARCHAR(30) DEFAULT '0', abil1 VARCHAR(30) DEFAULT '0', abil2 VARCHAR(30) DEFAULT '0',"
							+ "abil3 VARCHAR(30) DEFAULT '0', abil4 VARCHAR(30) DEFAULT '0', abil5 VARCHAR(30) DEFAULT '0', abil6 VARCHAR(30) DEFAULT '0', abil7 VARCHAR(30) DEFAULT '0', abil8 VARCHAR(30) DEFAULT '0', abil9 VARCHAR(30) DEFAULT '0')");
		} catch (Exception e) {
			MineQuest.log("Unable to initialize configuration");
        	MineQuest.log("Check configuration in MineQuest directory");
        	setEnabled(false);
        	return;
        }
        
//        getEventParser().addEvent(new CheckMQMobs(10000));
		sql_server.update("CREATE TABLE IF NOT EXISTS towns (name VARCHAR(30), x INT, z INT, max_x INT, max_z INT, spawn_x INT, spawn_y INT, spawn_z INT, " +
		"owner VARCHAR(30), height INT, y INT, merc_x DOUBLE, merc_y DOUBLE, merc_z DOUBLE)");

		ResultSet results = sql_server.query("SELECT * FROM version");
		
		try {
			if ((results == null) || (!results.next())) {
				upgradeDB();
			} else {
				if (!results.getString("version").equals(version)) {
					upgradeDB();
				}
				MineQuest.log("DB Version: " + results.getString("version"));
			}
		} catch (Exception e1) {
			upgradeDB();
		}
        
        checkAllMobs();

		results = sql_server.query("SELECT * FROM questers");
		List<String> names = new ArrayList<String>();
		List<String> npcs = new ArrayList<String>();
		
		try {
			while (results.next()) {
				if (results.getString("mode").equals("Quester")) {
					names.add(results.getString("name"));
				} else {
					if (npc_enabled) {
						npcs.add(results.getString("name"));
					}
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
			towns.add(new Town(name, getServer().getWorlds().get(0)));
		}
		
		bl = new MineQuestBlockListener();
		el = new MineQuestEntityListener();
		pl = new MineQuestPlayerListener();
		sl = new MineQuestServerListener();
//		wl = new MineQuestWorldListener();
//		vl = new MineQuestVehicleListener();
		
        PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvent(Event.Type.PLAYER_JOIN, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_TELEPORT, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_ANIMATION, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_COMBUST, el, Priority.Highest, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, el, Priority.Highest, this);
        pm.registerEvent(Event.Type.ENTITY_EXPLODE, el, Priority.Highest, this);
        pm.registerEvent(Event.Type.ENTITY_TARGET, el, Priority.Highest, this);
        pm.registerEvent(Event.Type.CREATURE_SPAWN, el, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, bl, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, bl, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, bl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLUGIN_DISABLE, sl, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, sl, Priority.Monitor, this);
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
        start = null;

		setupIConomy();
	}
	
	private void setupIConomy() {
		Plugin test = this.getServer().getPluginManager().getPlugin(
				"iConomy");

		if (MineQuest.IConomy == null) {
			if (test != null) {
			} else {
				log("iConomy system not detected, defaulting to MineQuest Storage");
			}
		}
	}

	private void downloadAbilities() throws MalformedURLException, IOException {
		BufferedInputStream in = new BufferedInputStream(
				new

				java.net.URL("http://www.theminequest.com/download/abilities.jar")
						.openStream());
		FileOutputStream fos = new FileOutputStream(
				"MineQuest/abilities.jar");
		BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
		byte data[] = new byte[1024];
		int size;
		
		while ((size = in.read(data, 0, 1024)) >= 0) {
			bout.write(data,0,size);
		}
		
		bout.close();
		in.close();
	}

	private void upgradeDB() {
		MineQuest.log("Your DB is too old to determine version");
		MineQuest.log("Upgrading DB to 0.40");
		
		upgradeDB(0, 4);
	}
	
	private void upgradeDB(int oldVersion, int newVersion) {
		String cols[] = null;
		String types[] = null;
		if (newVersion == 4) {
			cols = new String[] {
					"world",
					"x",
					"y",
					"z",
					"mode",
					"pitch",
					"yaw"
			};
			types = new String[] {
					"varchar(30) DEFAULT 'world'",
					"double DEFAULT '0'",
					"double DEFAULT '0'",
					"double DEFAULT '0'",
					"varchar(30) DEFAULT 'Quester'",
					"double DEFAULT '0'",
					"double DEFAULT '0'"
			};
			
			addColumns("questers", cols, types);
			
			cols = new String[] {
					"merc_x",
					"merc_y",
					"merc_z"
			};

			types = new String[] {
					"double DEFAULT '0'",
					"double DEFAULT '0'",
					"double DEFAULT '0'"
			};

			addColumns("towns", cols, types);
		}
		
		sql_server.update("CREATE TABLE IF NOT EXISTS version (version VARCHAR(30))");
		sql_server.update("DELETE FROM version");
		sql_server.update("INSERT INTO version (version) VALUES('" + version + "')");
		
	}
	
	private void addColumns(String db, String cols[], String types[]) {
		int i;
		for (i = 0; i < cols.length; i++) {
			try {
				if (!column_exists(db, cols[i])) {
					sql_server.update("ALTER TABLE " + db + " ADD COLUMN " + cols[i] + " " + types[i], false);
				}
			} catch (SQLException e) {
			}
		}
	}
	
	private boolean column_exists(String db, String column) throws SQLException {
		ResultSet results = sql_server.query("SELECT * FROM " + db);
		if (results == null) return false;
		ResultSetMetaData meta = results.getMetaData();
		
		int i;
		for (i = 0; i < meta.getColumnCount(); i++) {
			if (meta.getColumnName(i + 1).equals(column)) {
				return true;
			}
		}
		
		return false;
	}
	public static void checkAllMobs() {
    	for (World world : getSServer().getWorlds()) {
    		for (LivingEntity entity : world.getLivingEntities()) {
    			if ((entity instanceof Monster) || (entity instanceof PigZombie) || (entity instanceof Ghast)) {
    				if (getMob(entity) == null) {
    					addMob(entity);
    				}
    			}
    		}
    	}
	}
    
    public static void checkMobs() {
    	int i;
    	
    	for (i = 0; i < mobs.length; i++) {
    		if ((mobs[i] != null) && ((mobs[i].getHealth() <= 0) || (mobs[i].isDead()))) {
    			mobs[i].dropLoot();
    			if (mobs[i].getLastAttacker() != null) {
    				mobs[i].getLastAttacker().addKill(mobs[i]);
    			}
    			mobs[i] = null;
    		}
    	}
    }

	public static void addMob(LivingEntity entity) {
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
	
	public static void damage(LivingEntity entity, int i, Quester source) {
		if (entity instanceof HumanEntity) {
			Quester quester = getQuester((HumanEntity)entity);
			quester.setHealth(quester.getHealth() - i);
		} else if (getMob(entity) != null) {
			getMob(entity).damage(i, source);
		} else {
			int newHealth = entity.getHealth() - i;
			
			if (newHealth <= 0) newHealth = 0;
			
			entity.setHealth(newHealth);
		}
	}
	
	public static void damage(LivingEntity entity, int i) {
		if (entity instanceof HumanEntity) {
			Quester quester = getQuester((HumanEntity)entity);
			quester.setHealth(quester.getHealth() - i);
		} else if (getMob(entity) != null) {
			getMob(entity).damage(i);
		} else {
			int newHealth = entity.getHealth() - i;
			
			if (newHealth <= 0) newHealth = 0;
			
			entity.setHealth(newHealth);
		}
	}
	
	public static void setHealth(LivingEntity entity, double percent) {
		if (entity instanceof HumanEntity) {
			getQuester((HumanEntity)entity).setHealth((int)(percent * getQuester((HumanEntity)entity).getHealth()));
		} else if (getMob(entity) != null) {
			getMob(entity).setHealth((int)(getMob(entity).getHealth() * percent));
		} else {
			entity.setHealth((int)(entity.getHealth() * percent));
		}
	}

	public static void setHealth(LivingEntity entity, int health) {
		if (entity instanceof HumanEntity) {
			getQuester((HumanEntity)entity).setHealth(health);
		} else if (getMob(entity) != null) {
			getMob(entity).setHealth(health);
		} else {
			if (health > 20) health = 20;
			if (health < 0) health = 0;
			entity.setHealth(health);
		}
	}
	public static int getMobSize() {
		int i = 0;
		
		for (MQMob mob : mobs) {
			if (mob != null) i++;
		}
		
		return i;
	}
	public static int getArmorReqLevel() {
		return armor_req_level;
	}
	
	public static int getGoldReqLevel() {
		return gold_req_level;
	}
	
	public static int getStoneReqLevel() {
		return stone_req_level;
	}
	
	public static int getIronReqLevel() {
		return iron_req_level;
	}
	
	public static int getDiamondReqLevel() {
		return diamond_req_level;
	}
	
	public static int getMinerArmorLevel() {
		return leather_armor_miner_level;
	}
	
	public static int getDestroyMaterialsLevel() {
		return destroy_materials_level;
	}
	
	public static int getDestroyClassExp() {
		return destroy_class_exp;
	}
	
	public static int getDestroyNonClassExp() {
		return destroy_non_class_exp;
	}
	
	public static int getDestroyBlockExp() {
		return destroy_block_exp;
	}
	
	public static int getAdjustmentMultiplier() {
		return adjustment_multiplier;
	}
	
	public static int getExpMob() {
		return exp_damage;
	}
	
	public static int getCastAbilityExp() {
		return cast_ability_exp;
	}
	
	public static int getExpClassDamage() {
		return exp_class_damage;
	}
	
	public static AbilityConfigManager getAbilityConfiguration() {
		return ability_config;
	}
	
	public static double getSellPercent() {
		return sell_percent;
	}
	
	public static double getPriceChange() {
		return price_change;
	}
	
	public static int getStartingHealth() {
		return starting_health;
	}
	
	public static int getNPCCostLevel() {
		return npc_cost;
	}
	
	public static int getNPCCostWarrior() {
		return npc_cost_class;
	}
	
	public static int getNPCCostArcher() {
		return npc_cost_class;
	}
	
	public static int getNPCCostWarMage() {
		return npc_cost_class;
	}
	
	public static int getNPCCostPeaceMage() {
		return npc_cost_class;
	}
	
	public static boolean canCreate(Entity entity) {
		String name = entity.getWorld().getName();
		
		if (noMobs.contains(name)) {
			return false;
		}
		
		return true;
	}
	
	public static void noMobs(World world) {
		for (LivingEntity entity : world.getLivingEntities()) {
			if (!(entity instanceof HumanEntity)) {
				entity.setHealth(0);
			}
		}

		noMobs.add(world.getName());
	}

	public static void yesMobs(World world) {
		noMobs.remove(world.getName());
		
		return;
	}

	public static NPCStringConfig getNPCStringConfiguration() {
		return npc_strings;
	}

	public static boolean getIsConomyOn() {
		return IConomy != null;
	}

	public static iConomy getIConomy() {
		return IConomy;
	}

	public static boolean isTownProtect() {
		return town_protect;
	}

	public static NPCManager getNPCManager() {
		return npc_m;
	}

	public static boolean isMercEnabled() {
		return npc_enabled;
	}

	public static boolean isTrackingKills() {
		return track_kills;
	}

	public static boolean isTrackingDestroy() {
		return track_destroy;
	}

	public static boolean isTownNoMobs() {
		return town_no_mobs;
	}

	public static boolean logHealthChange() {
		return log_health_change;
	}

	public static boolean healSpawnEnable() {
		return health_spawn_enable;
	}

	public static void disconnect(String name) {
		log(name + " disconnected");
	}

	public static void respawnNPCs() {
		for (Quester quester : questers) {
			if (quester instanceof NPCQuester) {
				((NPCQuester)quester).redo();
			}
		}
	}
	public static void setIConomy(Plugin object) {
		MineQuest.IConomy = ((iConomy)object);
	}

	public static int[] getTownExceptions() {
		return town_exceptions;
	}
	public static boolean isOpen(Material type) {
		if (type == Material.AIR) {
			return true;
		}
		if (type == Material.TORCH) {
			return true;
		}
		if (type == Material.SNOW) {
			return true;
		}
		if (type == Material.FIRE) {
			return true;
		}
		if (type == Material.SIGN) {
			return true;
		}
		if (type == Material.WALL_SIGN) {
			return true;
		}
		if (type == Material.SIGN_POST) {
			return true;
		}
		if (type == Material.FENCE) {
			return true;
		}
		
		return false;
	}
}
