package org.monk.MineQuest.Listener;


import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Event.NoMobs;
import org.monk.MineQuest.Quest.Quest;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.Quester.SkillClass.SkillClass;
import org.monk.MineQuest.Store.Store;
import org.monk.MineQuest.World.Property;
import org.monk.MineQuest.World.Town;

public class MineQuestPlayerListener extends PlayerListener {
	
	private NoMobs event;

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		MineQuest.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
		MineQuest.getQuester(event.getPlayer()).move(event.getFrom(), event.getTo());
		super.onPlayerMove(event);
	}

	public void onPlayerJoin(PlayerEvent event) {
		if (MineQuest.getQuester(event.getPlayer()) == null) {
			MineQuest.addQuester(new Quester(event.getPlayer(), 0));
		}
		MineQuest.getQuester(event.getPlayer()).update(event.getPlayer());
		super.onPlayerJoin(event);
	}
	
	@Override
	public void onPlayerTeleport(PlayerMoveEvent event) {
		MineQuest.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
		MineQuest.getQuester(event.getPlayer()).teleport(event);
		super.onPlayerTeleport(event);
	}
	
	public void onPlayerQuit(PlayerEvent event) {
		MineQuest.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
		if (MineQuest.getQuester(event.getPlayer()) != null) {
			MineQuest.getQuester(event.getPlayer()).save();
			MineQuest.getQuester(event.getPlayer()).setPlayer(null);
		}
		super.onPlayerQuit(event);
	}
	
	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		MineQuest.getQuester(event.getPlayer()).respawn(event);
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerChatEvent event) {
		MineQuest.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
		String split[] = event.getMessage().split(" ");
		Player player = event.getPlayer();
		
		if (split[0].equals("/updatespawn")) {
			player.sendMessage("Not implemented yet");
			event.setCancelled(true);
		} else if (split[0].equals("/spawn")) {
			int index, i;
			double distance;
			List<Town> towns = MineQuest.getTowns();
			
			if (towns.size() == 0) {
				player.sendMessage("There are no spawns - Contact your administrator");
				event.setCancelled(true);
			}
			index = 0;
			distance = towns.get(0).calcDistance(player);
			for (i = 1; i < towns.size(); i++) {
				if (towns.get(i).calcDistance(player) < distance) {
					distance = towns.get(i).calcDistance(player);
					index = i;
				}
			}
			player.sendMessage("Welcome to " + towns.get(index).getName());
			player.teleportTo(towns.get(index).getLocation());
			event.setCancelled(true);
		} else if (split[0].equals("/townloc")) {
			player.sendMessage("You are at " + player.getLocation().getX() + " " + player.getLocation().getY() + " " + player.getLocation().getZ());
			event.setCancelled(true);
		} else if (split[0].equals("/mystash")) {
			MineQuest.getQuester(player).getChestSet(player).add(player);
			event.setCancelled(true);
		} else if (split[0].equals("/cancel")) {
			MineQuest.getQuester(player).getChestSet(player).cancelAdd(player);
			event.setCancelled(true);
		} else if (split[0].equals("/dropstash")) {
			Town town = MineQuest.getTown(player);
			MineQuest.getQuester(player).getChestSet(player).rem(player, town);
			event.setCancelled(true);
		} else if (split[0].equals("/char")) {
			Quester quester = MineQuest.getQuester(player);
			player.sendMessage("You are level " + quester.getLevel() + " with " + quester.getExp() + "/" + (400 * (quester.getLevel() + 1)) + " Exp");

			for (SkillClass skill : quester.getClasses()) {
				skill.display();
			}
			event.setCancelled(true);
		} else if (split[0].equals("/minequest")) {
			player.sendMessage("Minequest Commands:");
			player.sendMessage("    /save - save progress of character");
			player.sendMessage("    /load - load progress - removing unsaved experience/levels");
			player.sendMessage("    /quest - enable minequest for your character (enabled by default)");
			player.sendMessage("    /noquest - disable minequest for your character");
			player.sendMessage("    /char - information about your character level");
			player.sendMessage("    /class <classname> - information about a specific class");
			player.sendMessage("    /health - display your health");
			player.sendMessage("    /abillist [classname] - display all abilities or for a specific class");
			player.sendMessage("    /enableabil <ability name> - enable an ability (enabled by default)");
			player.sendMessage("    /disableabil <ability name> - disable an ability");
			player.sendMessage("    /bind <ability name> <l or r> - bind an ability to current item");
			player.sendMessage("    /unbind - unbind current item from all abilities");
			player.sendMessage("    /spellcomp <ability name> - list the components required for an ability");
			event.setCancelled(true);
		} else if (split[0].equals("/save")) {
			MineQuest.getQuester(player).save();
			event.setCancelled(true);
		} else if (split[0].equals("/load")) {
			MineQuest.getQuester(player).update();
			event.setCancelled(true);
		} else if (split[0].equals("/zombie")) {
			//Mob mymob = new Mob("Zombie", new Location(player.getX() + 3, player.getY(), player.getZ()));
			//mymob.spawn();
			event.setCancelled(true);
		} else if (split[0].equals("/abillist")) {
			if (split.length < 2) {
				MineQuest.getQuester(player).listAbil();
			} else {
				MineQuest.getQuester(player).getClass(split[1]).listAbil();
			}
			event.setCancelled(true);
		} else if (split[0].equals("/unbind")) {
			MineQuest.getQuester(player).unBind(player.getItemInHand());
			event.setCancelled(true);
		} else if (split[0].equals("/enableabil")) {
			if (split.length < 2) return;
			String abil = split[1];
			int i;
			for (i = 2; i < split.length; i++) abil = abil + " " + split[i];
			MineQuest.getQuester(player).enableabil(abil);
			event.setCancelled(true);
		} else if (split[0].equals("/disableabil")) {
			if (split.length < 2) return;
			String abil = split[1];
			int i;
			for (i = 2; i < split.length; i++) abil = abil + " " + split[i];
			MineQuest.getQuester(player).disableabil(abil);
			event.setCancelled(true);
		} else if (split[0].equals("/bind")) {
			if (split.length < 3) {
				player.sendMessage("Usage: /bind <ability> <l or r>");
				event.setCancelled(true);
			}
			String abil = split[1];
			int i;
			for (i = 2; i < split.length - 1; i++) abil = abil + " " + split[i];
			MineQuest.getQuester(player).bind(player, abil, split[split.length - 1]);
			event.setCancelled(true);
		} else if (split[0].equals("/class")) {
			if (split.length < 2) {
				player.sendMessage("Usage: /class <class_name>");
				event.setCancelled(true);
			}
			MineQuest.getQuester(player).getClass(split[1]).display();
			event.setCancelled(true);
		} else if (split[0].equals("/health")) {
			player.sendMessage("Your health is " + MineQuest.getQuester(player).getHealth() + "/" + MineQuest.getQuester(player).getMaxHealth());
			event.setCancelled(true);
		} else if (split[0].equals("/spellcomp")) {
			if (split.length < 2) {
				return;
			}
			String abil = split[1];
			int i;
			for (i = 2; i < split.length; i++) abil = abil + " " + split[i];
			player.sendMessage(MineQuest.listSpellComps(abil));
			event.setCancelled(true);
		} else if (split[0].equals("/store")) {
    		int page;
    		if (split.length > 1) {
    			page = Integer.parseInt(split[1]);
    		} else {
    			page = 0;
    		}
    		Town town = MineQuest.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.displayPage(MineQuest.getQuester(player), page);
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        } else if (split[0].equals("/buy")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.buy(MineQuest.getQuester(player), split[1], Integer.parseInt(split[2]));
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/buyi")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.buy(MineQuest.getQuester(player), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/costb")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.cost(MineQuest.getQuester(player), split[1], Integer.parseInt(split[2]), true);
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/costs")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.cost(MineQuest.getQuester(player), split[1], Integer.parseInt(split[2]), false);
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/sell")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.sell(MineQuest.getQuester(player), split[1], Integer.parseInt(split[2]));
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/selli")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.sell(MineQuest.getQuester(player), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/cubes")) {
        	String cubes_string;
        	double cubes = MineQuest.getQuester(player).getCubes();
        	
	    	if (cubes > 1000000) {
	    		cubes_string = ((double)cubes / 1000000.0) + "MC";
	    	} else if (cubes > 1000) {
	    		cubes_string = ((double)cubes / 1000.0) + "KC";
	    	} else {
	    		cubes_string = cubes + "C";
	    	}
			player.sendMessage("You have " + cubes_string);
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/updatedb")) {
        	player.sendMessage("This is not imlpemented yet");
			event.setCancelled(true);
			return;
        } else if (split[0].equals("/cubonomy")) {
        	if ((split.length == 2) && (Integer.parseInt(split[1]) == 2)) {
				player.sendMessage("Available Commands (2 of 2):");
				player.sendMessage("    /costb <store_index or nam> <amount>");
				player.sendMessage("    /costs <store_index or nam> <amount>");
				player.sendMessage("    /cubes");
				player.sendMessage("    /location");
				player.sendMessage("In case of crash try /updatedb");
        	} else {
				player.sendMessage("Available Commands (1 of 2):");
				player.sendMessage("    /store <page_num>");
				player.sendMessage("    /register <drupal_name>");
				player.sendMessage("    /buy <store_index or nam> <amount>");
				player.sendMessage("    /sell <store_index or name> <amount>");
				player.sendMessage("    /buyi <item_id> <amount>");
				player.sendMessage("    /selli <item_id> <amount>");
				player.sendMessage("for more type: /cubonomy 2");
        	}
			event.setCancelled(true);
			return;
        } else if (split[0].equals("/createtown")) {
        	MineQuest.createTown(player);
			event.setCancelled(true);
        } else if (split[0].equals("/finishtown")) {
        	if (split.length <= 1) {
        		player.sendMessage("Usage: /finishtown <name>");
        	} else {
        		MineQuest.finishTown(player, split[1]);
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/setowner")) {
        	if (MineQuest.getTown(player) != null) {
        		MineQuest.getTown(player).setOwner(split[1]);
        	} else {
        		player.sendMessage("You are not in a town");
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/createproperty")) {
        	if (MineQuest.getTown(player) != null) {
        		MineQuest.getTown(player).createProperty(player);
        	} else {
        		player.sendMessage("You are not in a town");
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/finishproperty")) {
        	if (split.length <= 0) {
        		player.sendMessage("Usage: /finishproperty [set-height]");
        	} else {
	        	if (MineQuest.getTown(player) != null) {
	        		MineQuest.getTown(player).finishProperty(player, split.length > 1);
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/createstore")) {
        	if (MineQuest.getTown(player) != null) {
        		MineQuest.getTown(player).createStore(player);
        	} else {
        		player.sendMessage("You are not in a town");
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/finishstore")) {
        	if (split.length <= 0) {
        		player.sendMessage("Usage: /finishstore <unique name>");
        	} else {
	        	if (MineQuest.getTown(player) != null) {
	        		MineQuest.getTown(player).finishStore(player, split[1]);
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/setprice")) {
        	if (split.length <= 1) {
        		player.sendMessage("Usage: /setprice <price>");
        	} else {
	        	if (MineQuest.getTown(player) != null) {
	        		MineQuest.getTown(player).setPrice(player, Long.parseLong(split[1]));
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/town")) {
			if (MineQuest.getTown(player) != null) {
				player.sendMessage("You are in " + MineQuest.getTown(player).getName());
			} else {
				player.sendMessage("You are not in a town");
			}
			event.setCancelled(true);
        } else if (split[0].equals("/setspawn")) {
			if (MineQuest.getTown(player) != null) {
				MineQuest.getTown(player).setSpawn(player.getLocation());
			} else {
				player.sendMessage("You are not in a town");
			}
			event.setCancelled(true);
        } else if (split[0].equals("/give_spare")) {
        	MineQuest.getQuester(event.getPlayer()).giveSpareInventory();
        	event.setCancelled(true);
        } else if (split[0].equals("/startquest")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /startquest filename");
        	} else {
        		if (MineQuest.getQuester(player).getParty() == null) {
        			MineQuest.getQuester(player).createParty();
        		}
    			MineQuest.addQuest(new Quest(split[1], MineQuest.getQuester(player).getParty()));
        	}
           	event.setCancelled(true);
        } else if (split[0].equals("/class_exp")) {
        	MineQuest.getQuester(player).sendMessage("You have " + MineQuest.getQuester(player).getClassExp() + " unassigned experience");
        	event.setCancelled(true);
        } else if (split[0].equals("/assign_exp")) {
        	if (split.length < 3) {
        		player.sendMessage("Usage: /assign_exp class_name amount");
        	} else {
        		MineQuest.getQuester(player).spendClassExp(split[1], Integer.parseInt(split[2]));
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/goto")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /goto world_name type");
        	} else {
        		World world = MineQuest.getSServer().getWorld(split[1]);
        		if (world == null) {
        			if ((split.length < 3) || !split[2].equals("Nether")) {
        				world = MineQuest.getSServer().createWorld(split[1], Environment.NORMAL);
        			} else {
        				world = MineQuest.getSServer().createWorld(split[1], Environment.NETHER);
        			}
        		}
        		player.teleportTo(world.getSpawnLocation());
        		event.setCancelled(true);
        	}
        } else if (split[0].equals("/setworldtime")) {
        	World world = MineQuest.getSServer().getWorld(split[1]);
        	world.setTime(Long.parseLong(split[2]));
        	event.setCancelled(true);
        } else if (split[0].equals("/price")) {
        	if ((MineQuest.getTown(player) != null) && (MineQuest.getTown(player).getProperty(player) != null)) {
        		Property prop = MineQuest.getTown(player).getProperty(player);
        		if (prop.getOwner() == null) {
        			player.sendMessage("The price of this property is " + prop.getPrice() + " cubes");
        		} else {
        			player.sendMessage("This property is not for sale");
        		}
        	} else {
        		player.sendMessage("You are not on a property");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/buyprop")) {
        	Town town = MineQuest.getTown(player);
        	Property prop = town.getProperty(player);
        	Quester quester = MineQuest.getQuester(player);
        	if (prop.getOwner() == null) {
        		if (quester.getCubes() > prop.getPrice()) {
        			town.buy(quester, prop);
        		} else {
        			player.sendMessage("You cannot afford this property");
        		}
        	} else {
        		player.sendMessage("This Property is not for sale");
        	}
        } else if (split[0].equals("/debug")) {
        	MineQuest.getQuester(player).debug();
        	event.setCancelled(true);
        } else if (split[0].equals("/nomobs")) {
        	this.event = new NoMobs(100, MineQuest.getSServer().getWorld(split[1]));
        	MineQuest.getEventParser().addEvent(this.event);
        	event.setCancelled(true);
        } else if (split[0].equals("/mobs")) {
        	this.event.setComplete(true);
        	event.setCancelled(true);
        } else if (split[0].equals("/createparty")) {
        	MineQuest.getQuester(player).createParty();
        	player.sendMessage("Party Created");
        	event.setCancelled(true);
        } else if (split[0].equals("/joinparty")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /joinparty player_name");
        	} else {
        		if (MineQuest.getQuester(split[1]) == null) {
        			player.sendMessage(split[1] + " is not a valid quester");
        		} else if (MineQuest.getQuester(split[1]).getParty() == null) {
        			player.sendMessage(split[1] + " is not in a party");
        		} else {
        			MineQuest.getQuester(split[1]).getParty().addQuester(MineQuest.getQuester(player));
        		}
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/quit_quest")) {
        	MineQuest.getQuester(player).getQuest().removeQuester(MineQuest.getQuester(player));
        	event.setCancelled(true);
        } else if (split[0].equals("/listparty")) {
        	for (Quester quester : MineQuest.getQuester(player).getParty().getQuesters()) {
        		player.sendMessage(quester.getName());
        	}
        	event.setCancelled(true);
        }
		
		
		
		super.onPlayerCommandPreprocess(event);
	}
}
