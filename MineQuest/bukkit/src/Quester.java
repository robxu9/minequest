

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Quester {
	private ChestSet chests;
	private SkillClass classes[];
	private double cubes;
	private double distance;
	private boolean enabled;
	private int exp;
	private int health;
	private String last;
	private boolean respawn_flag;
	private int level;
	private int max_health;
	private String name;
	private Player player;
	private int poison_timer;
	private int rep;
	private mysql_interface sql_server;
	private long damage_timer;
	
	
	public Quester(Player player, int x, mysql_interface sql) {
		this(player.getName(), 0, sql);
		this.player = player;
	}
	
	public Quester(Player player, mysql_interface sql) {
		sql_server = sql;
		this.name = player.getName();
		update();
		this.player = player;
	}
	
	public Quester(String name, int x, mysql_interface sql) {
		this.name = name;
		sql_server = sql;
		create();
		update();
		distance = 0;
	}

	public Quester(String name, mysql_interface sql) {
		sql_server = sql;
		this.name = name;
		update();
	}

	public void addHealth(int addition) {
		health += addition;
		max_health += addition;
	}

	public void attackEntity(Entity entity, EntityDamageByEntityEvent event) {
		if (checkItemInHand()) return;

		for (SkillClass skill : classes) {
			if (skill.isAbilityItem(player.getItemInHand())) {
				System.out.println("Found Ability Item");
				if (entity instanceof LivingEntity) {
					skill.attack(this, (LivingEntity)entity, event);
					expGain(5);
					return;
				}
			}
		}
		
		for (SkillClass skill : classes) {
			if (skill.isClassItem(player.getItemInHand())) {
				if (entity instanceof LivingEntity) {
					skill.attack(this, (LivingEntity)entity, event);
					expGain(5);
					return;
				}
			}
		}
	}
	
	public void bind(Player player, String name, String lr) {
		int i;
		
		for (i = 0; i < classes.length; i++) {
			classes[i].unBind(player.getItemInHand(), lr.equals("l"));
		}
		
		for (i = 0; i < classes.length; i++) {
			if (classes[i].getAbility(name) != null) {
				if (lr.equals("l")) {
					classes[i].getAbility(name).bindl(player, player.getItemInHand());
				} else {
					classes[i].getAbility(name).bindr(player, player.getItemInHand());
				}
			}
		}
	}

	public int blockToClass(Block block) {
		int miner[] = {
			1,
			4,
			14,
			15,
			16,
			41,42,43,44,45,
			56, 57,
			73, 74, 48, 49
		};
		int lumber[] = {
			5,17,18,47,50,53,
			58,63,64,65,68
		};
		int digger[] = {
			2,3,6,12,13,60,78,
			82
		};
		int farmer[] = {
			37, 38, 39, 40,
			81, 83, 281, 282,
			295, 296, 297, 332
		};
		
		int i;
		
		for (i = 0; i < miner.length; i++) {
			if (block.getType().getId() == miner[i]) {
				return 0;
			}
		}
		for (i = 0; i < lumber.length; i++) {
			if (block.getType().getId() == lumber[i]) {
				return 1;
			}
		}
		for (i = 0; i < digger.length; i++) {
			if (block.getType().getId() == digger[i]) {
				return 2;
			}
		}
		for (i = 0; i < farmer.length; i++) {
			if (block.getType().getId() == farmer[i]) {
				return 3;
			}
		}
		return 4;
	}
	
	public void callAbilityL(Block block) {
		int i;
		
		for (i = 0; i < classes.length; i++) {
			if (classes[i].isAbilityItem(player.getItemInHand())){
				classes[i].callAbilityL(this, block);
				return;
			}
		}
	}
	
	public void callAbilityL(Entity entity) {
		int i;
		
		for (i = 0; i < classes.length; i++) {
			if (classes[i].isAbilityItem(player.getItemInHand())){
				classes[i].callAbilityL(this, entity);
				return;
			}
		}
	}

	public void callAbilityR(Block block) {
		int i;
		
		for (i = 0; i < classes.length; i++) {
			if (classes[i].isAbilityItem(player.getItemInHand())){
				classes[i].callAbilityR(this, block);
				return;
			}
		}
	}

	public void callAbilityR(Entity entity) {
		int i;
		
		for (i = 0; i < classes.length; i++) {
			if (classes[i].isAbilityItem(player.getItemInHand())){
				classes[i].callAbilityR(this, entity);
				return;
			}
		}
	}
	
	public boolean canCast(List<ItemStack> list) {
		//for testing
//		int i;
//		PlayerInventory inven = player.getInventory();
//		
//		for (i = 0; i < list.size(); i++) {
//			if (inven.contains(list.get(i).getType())) {
//				inven.removeItem(list.get(i));
//			} else {
//				while (i-- > 0) {
//					inven.addItem(list.get(i));
//				}
//				return false;
//			}
//		}
		return true;
	}
	
	public void checkEquip(Player player) {
		if (!enabled) return;
		
		PlayerInventory inven = player.getInventory();
		
		int i;
		
		for (i = 0; i < classes.length; i++) {
			classes[i].checkEquip(player, inven);
		}
		
	}
	
	public boolean checkItemInHand() {
		int i;
		PlayerInventory inven = player.getInventory();
		ItemStack item = player.getItemInHand();
		
		for (i = 0; i < classes.length; i++) {
			if (!classes[i].canUse(item)) {
				if (inven.firstEmpty() != -1) {
					inven.addItem(item);
				} else {
					player.getWorld().dropItem(player.getLocation(), item);
				}
				
				inven.setItemInHand(null);
				player.sendMessage("You are not high enough level to use that weapon");
				return true;
			}
		}

		return false;
	}
	
	public boolean checkItemInHandAbil() {
		int i;
		
		if ((player.getItemInHand().getTypeId() == 261) || (player.getItemInHand().getTypeId() == 332)) {
			return false;
		}

		for (i = 0; i < classes.length; i++) {
			if (classes[i].isAbilityItem(player.getItemInHand())) {
				return true;
			}
		}
		
		return false;
	}

	public boolean checkItemInHandAbilL() {
		int i;
		
		if ((player.getItemInHand().getTypeId() == 261) || (player.getItemInHand().getTypeId() == 332)) {
			return false;
		}

		for (i = 0; i < classes.length; i++) {
			if (classes[i].isAbilityItemL(player.getItemInHand())) {
				return true;
			}
		}
		
		return false;
	}

	public boolean checkItemInHandAbilR() {
		int i;
		
		if ((player.getItemInHand().getTypeId() == 261) || (player.getItemInHand().getTypeId() == 332)) {
			return false;
		}

		for (i = 0; i < classes.length; i++) {
			if (classes[i].isAbilityItemR(player.getItemInHand())) {
				return true;
			}
		}
		
		return false;
	}

	public void create() {
		int i, num;
		ResultSet results;
		String class_names[] = {
				"Warrior",
				"Archer",
				"WarMage",
				"PeaceMage",
				"Miner",
				"Lumberjack",
				"Digger",
				"Farmer"
		};
		
		String update_string = "INSERT INTO questers (name, selected_chest, cubes, exp, level, last_town, classes, health, max_health) VALUES('"
			+ name + "', '" + name + "', '500000', '0', '0', 'Bitville', '";
		update_string = update_string + class_names[0];
		for (i = 1; i < class_names.length; i++) {
			update_string = update_string + ", " + class_names[i];
		}
		update_string = update_string + "', '10', '10')";

		sql_server.update(update_string);

		num = 0;
		try {
			results = sql_server.query("SELECT * FROM abilities");
			while (results.next()) {
				num++;
			}
		} catch (SQLException e) {
			System.out.println("Unable to get max ability id");
		}
		
		for (i = 0; i < class_names.length; i++) {
			update_string = "INSERT INTO classes (name, class, exp, level, abil_list_id) VALUES('"
								+ name + "', '" + class_names[i] + "', '0', '0', '" + (num + i) + "')";

			sql_server.update(update_string);
			sql_server.update("INSERT INTO abilities (abil_list_id) VALUES('" + (num + i) + "')");
		}
	}
	
	public void curePoison() {
		poison_timer = 0;
	}

	public void defend(EntityDamageEvent event) {
		healthChange(event.getDamage(), event);
	}

	public void defendBlock(EntityDamageByBlockEvent event) {
		healthChange(event.getDamage(), event);
	}

	public void defendCombust(EntityCombustEvent event) {
	}

	public void defendEntity(Entity entity, EntityDamageByEntityEvent event) {
		int amount = classes[0].getGenerator().nextInt(10);
		int levelAdj = MineQuest.getAdjustment();
		if (levelAdj == 0) {
			levelAdj = 1;
		} else {
			amount *= levelAdj * 3;
		}
		amount /= 4;
		
//		if (MineQuest.isSpecial((LivingEntity)attacker)) {
//			amount = MineQuest.getSpecial((LivingEntity)attacker).attack(this, player, amount);
//		}
		
		MineQuest.log("[INFO] Damage to " + name + " is " + amount);
		if (!enabled) return;
		
		int i, sum = 0;
		
		for (i = 0; i < classes.length; i++) {
			if (entity instanceof LivingEntity) {
				sum += classes[i].defend(this, (LivingEntity)entity, amount);
			}
		}
		
		amount -= sum;

		if (amount < 0) {
			amount = 0;
		}
		
		healthChange(amount, event);
		
		return;
	}
	
	public boolean destroyBlock(Block block) {
		if (!enabled) return false;

		for (SkillClass skill : classes) {
			if (skill.isAbilityItem(player.getItemInHand())) {
				System.out.println("Found Ability Item");
				skill.blockDestroy(block, this);
				expGain(5);
				return false;
			}
		}
		
		for (SkillClass skill : classes) {
			if (skill.isClassItem(player.getItemInHand())) {
				skill.blockDestroy(block, this);
				expGain(1);
				return false;
			}
		}
		
		switch (blockToClass(block)) {
		case 0: // Miner
			getClass("Miner").blockDestroy(block, this);
			return false;
		case 1: // Lumberjack
			getClass("Lumberjack").blockDestroy(block, this);
			return false;
		case 2: // Digger
			getClass("Digger").blockDestroy(block, this);
			return false;
		case 3: // Farmer
			getClass("Farmer").blockDestroy(block, this);
			return false;
		default:
			break;
		}
		
		return false;
	}

	public void disable() {
		enabled = false;
		player.sendMessage("MineQuest is now disabled for your character");
		
		return;
	}
	
	public void disableabil(String string) {
		int i;
		
		for (i = 0; i < classes.length; i++) {
			if (classes[i].getAbility(string) != null) {
				classes[i].getAbility(string).disable();
			}
		}
	}

	public void dropRep(int i) {
		rep -= i;
	}
	
    public void enable() {
		enabled = true;
		player.sendMessage("MineQuest is now enabled for your character");
		
		return;
	}


	public void enableabil(String string) {
		int i;
		
		for (i = 0; i < classes.length; i++) {
			if (classes[i].getAbility(string) != null) {
				classes[i].getAbility(string).enable(this);
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Quester) {
			return name.equals(((Quester)obj).getName());
		}
		if (obj instanceof String) {
			return name.equals(obj);
		}
		return super.equals(obj);
	}

	private void expGain(int i) {
		exp += i;
		if (exp > 400 * (level + 1)) {
			levelUp();
		}
	}

	public ChestSet getChestSet(Player player) {
		// TODO Auto-generated method stub
		return null;
	}

	public SkillClass getClass(String string) {
		int i;
		
		for (i = 0; i < classes.length; i++) {
			if (classes[i].getType().equalsIgnoreCase(string)) {
				return classes[i];
			}
		}
		
		return null;
	}

	public double getCubes() {
		return cubes;
	}

//	public void parseFire(int amount) {
//		int i;
//		
//		for (i = 0; i < classes.length; i++) {
//			if ((classes[i].getAbility("Fire Resistance") != null) && classes[i].getAbility("Fire Resistance").isEnabled()) {
//				classes[i].getAbility("Fire Resistance").parseFire(amount);
//				return;
//			}
//		}
//	}

	public int getExp() {
		return exp;
	}

//	public boolean rightClick(Player player, Block blockClicked, Item item) {
//		if (!enabled) return false;
//		
//		int i;
//		
//		for (i = 0; i < classes.length; i++) {
//			if (classes[i].rightClick(player, blockClicked, item, this)) {
//				blockClicked.setType(0);
//				return true;
//			}
//		}
//		
//		return false;
//	}

	public int getHealth() {
		return health;
	}
	
	public int getLevel() {
		return level;
	}

	public int getMaxHealth() {
		return max_health;
	}

	private String getName() {
		return name;
	}

	public Player getPlayer() {
		return player;
	}

	public Town getTown() {
		return MineQuest.getTown(last);
	}

	public boolean healthChange(int change, EntityDamageEvent event) {
		Calendar now = Calendar.getInstance();
//        boolean flag = false;
//        if (newValue <= 0) {
//                flag = true;
//        }
		int newHealth;
        if (!enabled) return false;
        if ((now.getTimeInMillis() - damage_timer) < 500) { 
        	event.setCancelled(true);
        	return false;
        }
    	damage_timer = now.getTimeInMillis();
        
//        if (oldValue - newValue >= 20) {
//                health = -1;
//                return false;
//        }

        health -= change;
        
        newHealth = 20 * health / max_health;
        
        if ((newHealth == 0) && (health > 0)) {
        	newHealth++;
        }
        
        if (health > max_health) {
        	health = max_health;
        }
        
        if (player.getHealth() >= newHealth) {
        	event.setDamage(player.getHealth() - newHealth);
        } else {
            player.setHealth(newHealth);
            event.setDamage(0);
            event.setCancelled(true);
        }

        MineQuest.log("[INFO] " + name + " - " + health + "/" + max_health);

        return false;
    }

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isPoisoned() {
		return (poison_timer > 0);
	}

	private void levelUp() {
		Random generator = new Random();
		int add_health = generator.nextInt(3) + 1;
		level++;
		exp -= (400 * level);
		max_health += add_health;
		health += add_health;
		
		getPlayer().sendMessage("Congratulations on reaching character level " + level);
	}

	public void listAbil(Quester player) {
		int i;
		
		for (i = 0; i < classes.length; i++) {
			classes[i].listAbil(player);
		}
	}

	public void move(Location from, Location to) {
		if (respawn_flag) {
			player.teleportTo(MineQuest.getTown(last).getSpawn());
			respawn_flag = false;
		}
		checkEquip(player);
		
		updateHealth(player);
		
		Town last_town = MineQuest.getNearestTown(to);
		if (last_town != null) {
			last = last_town.getName();
		} else {
			last = null;
		}
		if (last != null) {
			sql_server.update("UPDATE questers SET last_town='" + last + "'");
		}
		
		if (poison_timer > 0) {
			distance += MineQuest.distance(from, to);
		}
		
		while ((distance > 5) && (poison_timer > 0)) {
			distance -= 5;
			poison_timer -= 1;
			setHealth(getHealth() - 1);
		}
	}

	public void poison() {
		poison_timer += 10;
	}

	public void save() {
			int i;
			
		if (sql_server.update("UPDATE questers SET exp='" + exp + "', level='" + level + "', health='" 
				+ health + "', max_health='" + max_health + "', enabled='" + (enabled?1:0) 
				+ "' WHERE name='" + name + "'") == -1) {
			player.sendMessage("May not have saved properly, please try again");
		}
		for (i = 0; i < classes.length; i++) {
			classes[i].save();
		}
		enabled = false;
	}

	public void sendMessage(String string) {
		if (player != null) {
			player.sendMessage(string);
		} else {
			MineQuest.log("[WARNING] Quester " + name + " doesn't have player, not logged in?");
		}
	}

	public void setCubes(double d) {
		cubes = d;
	}

	public void setHealth(int i) {
		int newValue;

		if (i > max_health) {
			i = max_health;
		}
		health = i;
		
		newValue = 20 * health / max_health;
		
		if ((newValue == 0) && (health > 0)) {
			newValue++;
		}
		if (newValue < 0) {
			newValue = 0;
		}
		
		player.setHealth(newValue);
		
	}

	public void setTown(Town town) {
		last = town.getName();
		sql_server.update("UPDATE players SET town='" + town.getName() + "' WHERE name='" + name + "'");
	}

	public void teleport(PlayerMoveEvent event) {
		if ((health <= 0) && (player.getHealth() == 20)) {
			health = max_health;
			Town town = MineQuest.getNearestTown(event.getFrom());
			event.setTo(town.getSpawn());
		}
	}

	public void unBind(ItemStack itemInHand) {
		int i;
		
		for (i = 0; i < classes.length; i++) {
			classes[i].unBind(itemInHand, true);
			classes[i].unBind(itemInHand, false);
		}
	}

	public void update() {
		ResultSet results;
		String split[];
		int i;
		
		try {
			results = sql_server.query("SELECT * FROM questers WHERE name='" + name + "'");
			results.next();
		} catch (SQLException e) {
			System.out.println("Issue querying name");
			e.printStackTrace();
			return;
		}
		try {
			split = results.getString("classes").split(", ");
			exp = results.getInt("exp");
			level = results.getInt("level");
			health = results.getInt("health");
			max_health = results.getInt("max_health");
			enabled = results.getInt("enabled") > 0;
			
			cubes = results.getDouble("cubes");
			chests = new ChestSet();
			last = results.getString("last_town");
		} catch (SQLException e) {
			System.out.println("Issue getting parameters");
			e.printStackTrace();
			return;
		}
		
		classes = new SkillClass[split.length];
		for (i = 0; i < split.length; i++) {
			classes[i] = new SkillClass(this, split[i], name, sql_server);
		}
		damage_timer = 0;
	}

	public void update(Player player2) {
		this.player = player2;
		update();
	}

	public void updateHealth(Player player) {
		int newValue;
		
		newValue = 20 * health / max_health;
		
		if ((newValue == 0) && (health > 0)) {
			newValue++;
		}

		if (newValue < 0) {
			newValue = 0;
		}
		
		if ((player.getHealth() == 20) && (health <= 0)) {
			health = max_health;
			respawn_flag = true;
		} else {
			player.setHealth(newValue);
		}
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public SkillClass[] getClasses() {
		return classes;
	}
}
