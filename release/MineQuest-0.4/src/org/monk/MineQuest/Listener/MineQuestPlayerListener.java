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
package org.monk.MineQuest.Listener;


import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Quest.QuestProspect;
import org.monk.MineQuest.Quester.NPCMode;
import org.monk.MineQuest.Quester.NPCQuester;
import org.monk.MineQuest.Quester.Quester;
import org.monk.MineQuest.Quester.SkillClass.SkillClass;
import org.monk.MineQuest.Store.NPCSignShop;
import org.monk.MineQuest.Store.Store;
import org.monk.MineQuest.Store.StoreBlock;
import org.monk.MineQuest.World.Property;
import org.monk.MineQuest.World.Town;

public class MineQuestPlayerListener extends PlayerListener {

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		Quester quester = MineQuest.getQuester(event.getPlayer());
		
		if (!MineQuest.getQuester(event.getPlayer()).healthIncrease(event)) {
			event.setCancelled(!quester.canEdit(event.getClickedBlock()));
		}
		
		if (event.getClickedBlock() != null) {
			if (event.getClickedBlock().getType() == Material.CHEST) {
				quester.getChestSet().clicked(event.getPlayer(), event.getClickedBlock());
			}
		}
		
		super.onPlayerInteract(event);
	}
	
	@Override
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
			Quester quester = MineQuest.getQuester(event.getPlayer());
			
			quester.armSwing();
		}
		
		super.onPlayerAnimation(event);
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		MineQuest.checkMobs();
		MineQuest.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
		MineQuest.getQuester(event.getPlayer()).move(event.getFrom(), event.getTo());
		super.onPlayerMove(event);
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (MineQuest.getQuester(event.getPlayer()) == null) {
			MineQuest.addQuester(new Quester(event.getPlayer(), 0));
		}
		MineQuest.getQuester(event.getPlayer()).update(event.getPlayer());
		MineQuest.getQuester(event.getPlayer()).update();
		if (MineQuest.getSServer().getOnlinePlayers().length == 1) {
			MineQuest.respawnNPCs();
		}
	}
	
	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		MineQuest.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
		MineQuest.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		MineQuest.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
		if (MineQuest.getQuester(event.getPlayer()) != null) {
			MineQuest.getQuester(event.getPlayer()).save();
			MineQuest.getQuester(event.getPlayer()).setPlayer(null);
		}
	}
	
	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		MineQuest.getQuester(event.getPlayer()).respawn(event);
		MineQuest.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		MineQuest.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
		String split[] = event.getMessage().split(" ");
		Player player = event.getPlayer();
		
		try {
			processQuester(split, player, event);
			if (event.isCancelled()) return;
			if (MineQuest.isCubonomyEnabled()) {
				processStore(split, player, event);
				if (event.isCancelled()) return;
			}
			processQuest(split, player, event);
			if (event.isCancelled()) return;
			if (MineQuest.isTownEnabled()) {
				processTown(split, player, event);
				if (event.isCancelled()) return;
			}
			if (MineQuest.isMercEnabled()) {
				processMerc(split, player, event);
				if (event.isCancelled()) return;
			}
			if (MineQuest.isDebugEnabled()) {
				processDebug(split, player, event);
				if (event.isCancelled()) return;
			}
			processHelp(split, player, event);
		} catch (Exception e) {
			player.sendMessage("Congratulations! You found a bug!");
			player.sendMessage("Please contact help@theminequest.com");
			e.printStackTrace();
		}
		
		super.onPlayerCommandPreprocess(event);
	}
	
	private void processHelp(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/help")) {
			player.sendMessage("To get started with the leveling, abilities");
			player.sendMessage("and experience system type /minequest");
			player.sendMessage("To get started with the economy system try");
			player.sendMessage("/cubonomy");
			player.sendMessage("To get started with the questing system try ");
			player.sendMessage("/quest");
		} else if (split[0].equals("/minequest")) {
			player.sendMessage("Minequest Commands:");
			player.sendMessage("    /save - save progress of character");
			player.sendMessage("    /load - load progress - removing unsaved experience/levels");
			player.sendMessage("    /char - information about your character level");
			player.sendMessage("    /class <classname> - information about a specific class");
			player.sendMessage("    /health - display your health");
			player.sendMessage("    /abillist [classname] - display all abilities or for a specific class");
			player.sendMessage("    /enableabil <ability name> - enable an ability (enabled by default)");
			player.sendMessage("    /disableabil <ability name> - disable an ability");
			player.sendMessage("    /bind <ability name> - bind an ability to current item");
			player.sendMessage("    /unbind - unbind current item from all abilities");
			player.sendMessage("    /spellcomp <ability name> - list the components required for an ability");
			event.setCancelled(true);
		} else if (split[0].equals("/cubonomy")) {
        	if ((split.length == 2) && (Integer.parseInt(split[1]) == 2)) {
				player.sendMessage("Available Commands (2 of 2):");
				player.sendMessage("    /costb <store_index or name> <amount>");
				player.sendMessage("          - cost of buying amount of name");
				player.sendMessage("    /costs <store_index or name> <amount>");
				player.sendMessage("          - cost of selling amount of name");
				player.sendMessage("    /cubes - money you have");
        	} else {
				player.sendMessage("Available Commands (1 of 2):");
				player.sendMessage("    /store <page_num> - list contents");
				player.sendMessage("    /buy <name> <amount> - buy amount of name");
				player.sendMessage("    /sell <name> <amount> - sell amount of name");
				player.sendMessage("    /buyi <item_index> <amount> - buy amount of index");
				player.sendMessage("    /selli <item_index> <amount> - sell amount of index");
				player.sendMessage("for more type: /cubonomy 2");
        	}
			event.setCancelled(true);
			return;
        } else if (split[0].equals("/quest")) {
        	player.sendMessage("Quest Related Commands:");
			player.sendMessage("    /createparty - create a party");
			player.sendMessage("    /listparty - list users in your party");
			player.sendMessage("    /joinparty <username> - join username's party");
			player.sendMessage("    /startquest <name of quest> - start a quest with party");
			player.sendMessage("    /quit_quest - quit the instance of quest, lose current exp");
			player.sendMessage("    /class_exp - list amount of unassigned exp");
			player.sendMessage("    /assign_exp <class> <amount> - assign amount exp to class");
        }else if (split[0].equals("/new_binder")) {
        	player.sendMessage("Item binding is now restricted to one binding per item.");
        	player.sendMessage("This is because it was too confusing for right click binding");
        	player.sendMessage("when it only works on some items. Now spells are bound to both.");
        	player.sendMessage("either right click or left click will activate.");
        	player.sendMessage("Rebinders have been added to the Server and can be created using");
        	player.sendMessage("/binder Ability_Name item_id_to_bind");
        	player.sendMessage("while holding the item to create it for.");
        	event.setCancelled(true);
        } 
	}
	
	private void processQuest(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/startquest") || split[0].equals("/start_quest")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /start_quest filename");
        	} else {
        		if (MineQuest.getQuester(player).getParty() == null) {
        			MineQuest.getQuester(player).createParty();
        		}
        		String qname = split[1];
        		int i;
        		for (i = 2; i < split.length; i++) qname = qname + " " + split[i];
        		MineQuest.getQuester(player).startQuest(qname);
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
        }  else if (split[0].equals("/create_party")) {
        	MineQuest.getQuester(player).createParty();
        	player.sendMessage("Party Created");
        	event.setCancelled(true);
        } else if (split[0].equals("/join_party")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /join_party player_name");
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
        	if (MineQuest.getQuester(player).getQuest() != null) {
        		MineQuest.getQuester(player).getQuest().removeQuester(MineQuest.getQuester(player));
        	} else {
        		player.sendMessage("You are not in a quest...");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/list_party")) {
        	if (MineQuest.getQuester(player).getParty() != null) {
            	for (Quester quester : MineQuest.getQuester(player).getParty().getQuesters()) {
            		player.sendMessage(quester.getName());
            	}
        	} else {
        		player.sendMessage("You are not in a party");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/list_quest")) {
        	for (QuestProspect qp : MineQuest.getQuester(player).getAvailableQuests()) {
        		player.sendMessage(qp.getName());
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/list_complete_quest")) {
        	for (QuestProspect qp : MineQuest.getQuester(player).getCompletedQuests()) {
        		player.sendMessage(qp.getName());
        	}
        	event.setCancelled(true);
        }
	}
	
	private void processQuester(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/mystash")) {
			MineQuest.getQuester(player).getChestSet().add(player);
			event.setCancelled(true);
		} else if (split[0].equals("/cancel")) {
			MineQuest.getQuester(player).getChestSet().cancelAdd(player);
			event.setCancelled(true);
		} else if (split[0].equals("/dropstash")) {
			Town town = MineQuest.getTown(player);
			MineQuest.getQuester(player).getChestSet().rem(player, town);
			event.setCancelled(true);
		} else if (split[0].equals("/char")) {
			Quester quester = MineQuest.getQuester(player);
			player.sendMessage("You are level " + quester.getLevel() + " with " + quester.getExp() + "/" + (400 * (quester.getLevel() + 1)) + " Exp");

			for (SkillClass skill : quester.getClasses()) {
				skill.display();
			}
			event.setCancelled(true);
		} else if (split[0].equals("/save")) {
			MineQuest.getQuester(player).save();
			event.setCancelled(true);
		} else if (split[0].equals("/load")) {
			MineQuest.getQuester(player).update();
			event.setCancelled(true);
		} else if (split[0].equals("/abillist")) {
			if (split.length < 2) {
				MineQuest.getQuester(player).listAbil();
			} else {
				if (MineQuest.getQuester(player).getClass(split[1]) != null) {
					MineQuest.getQuester(player).getClass(split[1]).listAbil();
				} else {
					player.sendMessage(split[1] + " is not a valid class");
				}
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
			if (split.length < 2) {
				player.sendMessage("Usage: /bind <ability>");
				event.setCancelled(true);
				return;
			}
			String abil = split[1];
			int i;
			for (i = 2; i < split.length; i++) abil = abil + " " + split[i];
			MineQuest.getQuester(player).bind(abil);
			event.setCancelled(true);
		} else if (split[0].equals("/look_bind")) {
			if (split.length < 2) {
				player.sendMessage("Usage: /bind <ability>");
				event.setCancelled(true);
				return;
			}
			String abil = split[1];
			int i;
			for (i = 2; i < split.length; i++) abil = abil + " " + split[i];
			MineQuest.getQuester(player).lookBind(abil);
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
		}  else if (split[0].equals("/binder")) {
        	if (split.length < 3) {
        		player.sendMessage("Usage: /binder Ability_Name item_id");
        	} else {
    			String abil = split[1];
    			int i;
    			for (i = 2; i < split.length - 1; i++) abil = abil + " " + split[i];
            	int item = Integer.parseInt(split[i]);
            	
            	MineQuest.getQuester(player).addBinder(abil, item);
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/item_id")) {
        	ItemStack item = player.getItemInHand();
        	if (item != null) {
        		player.sendMessage(item.getType().name() + " is item id " + item.getTypeId());
        	} else {
        		player.sendMessage("You are not holding anything");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/spawn_npc")) {
        	event.setCancelled(true);
        	if (split.length < 2) {
        		player.sendMessage("Usage: /spawn_npc <npc_name>");
        		return;
        	}
        	if (MineQuest.getQuester(player).canEdit(player.getLocation().getBlock())) {
            	Location location = player.getLocation();
            	MineQuest.addQuester(new NPCQuester(split[1], NPCMode.GENERIC, player.getWorld(), location));
        	} else {
        		player.sendMessage("You don't have permission to edit this area");
        	}
        } else if (split[0].equals("/remove_npc")) {
        	event.setCancelled(true);
        	if (split.length < 2) {
        		player.sendMessage("Usage: /remove_npc <npc_name>");
        		return;
        	}
        	if (MineQuest.getQuester(split[1]) instanceof NPCQuester) {
        		NPCQuester quester = (NPCQuester)MineQuest.getQuester(split[1]);
	        	if (MineQuest.getQuester(player).canEdit(quester.getPlayer().getLocation().getBlock())) {
	        		quester.removeSql();
	        		quester.setHealth(0);
	        		MineQuest.remQuester(quester);
	        	} else {
	        		player.sendMessage("You don't have permission to edit their area");
	        	}
        	} else {
        		player.sendMessage(split[1] + " is not a valid NPC to remove");
        	}
        } else if (split[0].equals("/replace")) {
        	if (split.length < 3) {
        		player.sendMessage("Usage: /replace old_ability_name with new_ability_name");
        	} else {
        		int divider;
        		for (divider = 1; divider < split.length; divider++) {
        			if (split[divider].equals("with")) {
        				break;
        			}
        		}
        		if (divider < split.length) {
            		int i;
            		String first = split[1];
            		for (i = 2; i < divider; i++) {
            			first = first + " " + split[i];
            		}
            		String second = split[divider + 1];
            		for (i = divider + 2; i < split.length; i++) {
            			second = second + " " + split[i];
            		}
	        		SkillClass skill = MineQuest.getQuester(player).getClassFromAbil(first);
	        		if (skill == null) {
	        			player.sendMessage(split[1] + " is not a valid ability");
	        		} else {
	        			if (MineQuest.isTownEnabled() && (MineQuest.getTown(player) == null)) {
	        				player.sendMessage("Must be in a town to modify spellbook");
	        			} else {
	        				skill.replaceAbil(first, second);
	        			}
	        		}
        		} else {
            		player.sendMessage("Usage: /replace old_ability_name with new_ability_name");
        		}
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/addclass")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /addclass <combat_class>");
        	} else {
        		MineQuest.getQuester(player).addClass(split[1]);
        	}
        	event.setCancelled(true);
        }
	}
	
	private void processStore(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/store")) {
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
        	String cubes_string = StoreBlock.convert((long)MineQuest.getQuester(player).getCubes());
	    	
			player.sendMessage("You have " + cubes_string);
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/addblock")) {
        	if (split.length < 4) {
        		player.sendMessage("Usage: /addblock type price item_id");
        		return;
        	}
        	
    		Town town = MineQuest.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);

    			if (store != null) {
    				Block block = player.getWorld().getBlockAt(player.getLocation());
    				if (MineQuest.getQuester(player).canEdit(block)) {
        				store.addBlock(split[1], split[2], split[3]);
        				player.sendMessage(split[1] + " added to store");
    				}
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town");
    		}
			event.setCancelled(true);
        } else if (split[0].equals("/remblock")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /remblock type");
        		return;
        	}
        	
    		Town town = MineQuest.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);

    			if (store != null) {
    				Block block = player.getWorld().getBlockAt(player.getLocation());
    				if (MineQuest.getQuester(player).canEdit(block)) {
        				store.remBlock(split[1]);
        				player.sendMessage(split[1] + " removed from store");
    				}
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town");
    		}
			event.setCancelled(true);
        } else if (split[0].equals("/init_store")) {
        	if (MineQuest.getTown(player) == null) {
        		player.sendMessage("You are not in a town");
    			event.setCancelled(true);
    			return;
        	}
        	if (MineQuest.getTown(player).getStore(player) == null) {
        		player.sendMessage("You are not in a store");
    			event.setCancelled(true);
    			return;
        	}
        	if (!MineQuest.getTown(player).getTownProperty().canEdit(MineQuest.getQuester(player))) {
        		player.sendMessage("You do not have permission to edit town");
    			event.setCancelled(true);
    			return;
        	}
        	
        	NPCSignShop shop = MineQuest.getTown(player).getStore(player);
        	shop.intialize(MineQuest.getQuester(player));
			event.setCancelled(true);
        } else if (split[0].equals("/spawn_store_npc")) {
        	if (MineQuest.getTown(player) == null) {
        		player.sendMessage("You are not in a town");
    			event.setCancelled(true);
    			return;
        	}
        	if (MineQuest.getTown(player).getStore(player) == null) {
        		player.sendMessage("You are not in a store");
    			event.setCancelled(true);
    			return;
        	}
        	if (!MineQuest.getTown(player).getTownProperty().canEdit(MineQuest.getQuester(player))) {
        		player.sendMessage("You do not have permission to edit town");
    			event.setCancelled(true);
    			return;
        	}
        	
        	Location location = player.getLocation();
        	MineQuest.addQuester(new NPCQuester(split[1], NPCMode.STORE, player.getWorld(), location));
        	event.setCancelled(true);
        } else if (split[0].equals("/delete_store")) {
    		Town town = MineQuest.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);

    			if (store != null) {
    				Block block = player.getWorld().getBlockAt(player.getLocation());
    				if (MineQuest.getQuester(player).canEdit(block)) {
	    				store.delete();
	    				town.remove(store);
	    				player.sendMessage("Store deleted");
    				}
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town");
    		}
			event.setCancelled(true);
        }
	}
	
	private void processTown(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/town_spawn")) {
			int index, i;
			double distance;
			List<Town> towns = MineQuest.getTowns();
			
			if (towns.size() == 0) {
				player.sendMessage("There are no spawns - Contact your administrator");
				event.setCancelled(true);
				return;
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
			player.teleport(towns.get(index).getLocation());
			if (MineQuest.healSpawnEnable()) {
				Quester quester = MineQuest.getQuester(player);
				quester.setHealth(quester.getMaxHealth());
			}
			event.setCancelled(true);
		} else if (split[0].equals("/townloc")) {
			player.sendMessage("You are at " + player.getLocation().getX() + " " + player.getLocation().getY() + " " + 
					player.getLocation().getZ() + " P:" + player.getLocation().getPitch() + " Y:" + player.getLocation().getYaw());
			event.setCancelled(true);
		}else if (split[0].equals("/createtown")) {
        	MineQuest.createTown(player);
			event.setCancelled(true);
        } else if (split[0].equals("/finishtown")) {
        	if (split.length <= 1) {
        		player.sendMessage("Usage: /finishtown <name>");
        	} else {
        		MineQuest.finishTown(player, split[1]);
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/setmayor")) {
        	if ((MineQuest.getTown(player) != null) && (MineQuest.getTown(player).getTownProperty().getOwner().equals(MineQuest.getQuester(player)))) {
        		MineQuest.getTown(player).setOwner(split[1]);
        	} else {
        		player.sendMessage("You are not in a town or you are not the mayor");
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
        	if (split.length <= 1) {
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
				player.sendMessage("Spawn location set");
			} else {
				player.sendMessage("You are not in a town");
			}
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
        	if (town == null) {
        		player.sendMessage("You are not in a town");
            	event.setCancelled(true);
            	return;
        	}
        	Property prop = town.getProperty(player);
        	Quester quester = MineQuest.getQuester(player);
        	if (prop != null) {
	        	if (prop.getOwner() == null) {
	        		if (quester.getCubes() > prop.getPrice()) {
	        			town.buy(quester, prop);
	        		} else {
	        			player.sendMessage("You cannot afford this property");
	        		}
	        	} else {
	        		player.sendMessage("This Property is not for sale");
	        	}
        	} else {
        		player.sendMessage("You are not on a property");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/addedit")) {
        	event.setCancelled(true);
	    	if (MineQuest.getQuester(player).canEdit(player.getWorld().getBlockAt(player.getLocation()))) {
	        	Town town = MineQuest.getTown(player);
	        	if (town != null) {
	        		Property prop = town.getProperty(player);
	        		if (prop == null) prop = town.getTownProperty();
	        		
	        		if ((split.length < 2) || (MineQuest.getQuester(split[1]) == null)) {
	        			player.sendMessage("Usage: /addedit <username>");
	        		} else {
	        			prop.addEdit(MineQuest.getQuester(split[1]));
	        			player.sendMessage("Editor " + split[1] + " added");
	        		}
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
	    	}
        } else if (split[0].equals("/remedit")) {
        	event.setCancelled(true);
	    	if (MineQuest.getQuester(player).canEdit(player.getWorld().getBlockAt(player.getLocation()))) {
	        	Town town = MineQuest.getTown(player);
	        	if (town != null) {
	        		Property prop = town.getProperty(player);
	        		if (prop == null) prop = town.getTownProperty();
	        		
	        		if ((split.length < 2) || (MineQuest.getQuester(split[1]) == null)) {
	        			player.sendMessage("Usage: /addedit <username>");
	        		} else {
	        			prop.remEdit(MineQuest.getQuester(split[1]));
	        			player.sendMessage("Editor " + split[1] + " removed");
	        		}
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
	    	}
        } else if (split[0].equals("/delete_town")) {
        	event.setCancelled(true);
	    	if (MineQuest.getQuester(player).canEdit(player.getWorld().getBlockAt(player.getLocation()))) {
	        	Town town = MineQuest.getTown(player);
	        	if (town != null) {
	        		town.delete();
	        		MineQuest.remTown(town);
	        		player.sendMessage("Town deleted");
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
	    	}
        } else if (split[0].equals("/expand_town")) {
        	event.setCancelled(true);
        	if (split.length < 2) {
        		player.sendMessage("Usage: /expand_town <town_name>");
        		return;
        	}
        	Town town = MineQuest.getTown(split[1]);
        	
        	if (town == null) {
        		player.sendMessage(split[1] + " is not a valid town");
        	} else {
        		town.expand(MineQuest.getQuester(player));
        	}
        } else if (split[0].equals("/set_town_y")) {
        	event.setCancelled(true);
        	if (split.length < 3) {
        		player.sendMessage("Usage: /set_town_y <town_name> <y>");
        		return;
        	}
        	Town town = MineQuest.getTown(split[1]);
        	
        	if (town == null) {
        		player.sendMessage(split[1] + " is not a valid town");
        	} else {
        		town.setMinY(MineQuest.getQuester(player), Integer.parseInt(split[2]));
        	}
        } else if (split[0].equals("/set_town_height")) {
        	event.setCancelled(true);
        	if (split.length < 3) {
        		player.sendMessage("Usage: /set_town_height <town_name> <height>");
        		return;
        	}
        	Town town = MineQuest.getTown(split[1]);
        	
        	if (town == null) {
        		player.sendMessage(split[1] + " is not a valid town");
        	} else {
        		town.setHeight(MineQuest.getQuester(player), Integer.parseInt(split[2]));
        	}
        }
	}
	
	private void processMerc(String[] split, Player player, PlayerChatEvent event) {
	    if (split[0].equals("/regroup")) {
	    	MineQuest.getQuester(player).regroup();
	    	event.setCancelled(true);
	    } else if (split[0].equals("/npc_property")) {
	    	if (split.length < 4) {
	    		player.sendMessage("Usage: /npc_property <npc_name> <property_name> <property_value>");
	    		event.setCancelled(true);
	    		return;
	    	}
    		String value = split[3];
    		int i;
    		for (i = 4; i < split.length; i++) value = value + " " + split[i];
    		if (MineQuest.getQuester(split[1]) instanceof NPCQuester) {
    			((NPCQuester)MineQuest.getQuester(split[1])).setProperty(split[2], value);
    		} else {
    			player.sendMessage(split[1] + " is not a valid NPC");
    		}
	    	event.setCancelled(true);
	    } else if (split[0].equals("/list_mercs")) {
	    	if (MineQuest.getTown(player) != null) {
	    		player.sendMessage("Available in " + MineQuest.getTown(player).getName() + ":");
	    		for (NPCQuester quester : MineQuest.getTown(player).getAvailableNPCs()) {
	    			player.sendMessage(quester.getName() + " : " + quester.getCost());
	    		}
	    	} else {
	    		player.sendMessage("You are not in a town");
	    	}
	    	event.setCancelled(true);
	    } else if (split[0].equals("/set_merc_spawn")) {
	    	if (MineQuest.getTown(player) != null) {
	    		if (MineQuest.getTown(player).getTownProperty().canEdit(MineQuest.getQuester(player))) {
	        		MineQuest.getTown(player).setMERCSpawn(player.getLocation());
	        		player.sendMessage("Mercenary Spawn Set");
	    		} else {
	    			player.sendMessage("You do not have permission to edit town");
	    		}
	    	} else {
	    		player.sendMessage("You are not in a town");
	    	}
	    	event.setCancelled(true);
	    } else if (split[0].equals("/spawn_merc")) {
	    	if (split.length > 1) {
	        	if (MineQuest.getTown(player) != null) {
	        		MineQuest.getTown(player).addMerc(split[1], MineQuest.getQuester(player));
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
	    	} else {
	    		player.sendMessage("Usage: /spawn_merc name");
	    	}
	    	event.setCancelled(true);
	    } else if (split[0].equals("/buy_merc")) {
	    	if (split.length < 2) {
	    		player.sendMessage("Usage: /buy_merc <npc_name>");
				event.setCancelled(true);
				return;
			}
			if ((MineQuest.getQuester(split[1]) instanceof NPCQuester)
					&& (NPCMode.FOR_SALE == ((NPCQuester) MineQuest
							.getQuester(split[1])).getMode())) {
				((NPCQuester) MineQuest.getQuester(split[1])).buyNPC(MineQuest
						.getQuester(player));
			} else {
    			player.sendMessage(split[1] + " is not a mercenary for hire");
    		}
	    	event.setCancelled(true);
	    } else if (split[0].equals("/set_merc_item")) {
	    	if (split.length < 2) {
	    		player.sendMessage("Usage: /set_merc_item <npc_name>");
				event.setCancelled(true);
				return;
	    	}
			Quester quester = MineQuest.getQuester(split[1]);
			if (!(quester instanceof NPCQuester) || !(MineQuest.getQuester(player).hasQuester(quester))) {
	    		player.sendMessage(split[1] + " is not one of your mercenaries");
				event.setCancelled(true);
				return;
			}
	    	((NPCQuester)quester).giveItem(MineQuest.getQuester(player));
	    	event.setCancelled(true);
	    } else if (split[0].equals("/npc_char") || split[0].equals("/merc_char")) {
	    	if (split.length < 2) {
	    		player.sendMessage("Usage: /merc_char <merc_name>");
				event.setCancelled(true);
				return;
	    	}
			Quester quester = MineQuest.getQuester(split[1]);
			if (!(quester instanceof NPCQuester) || !(MineQuest.getQuester(player).hasQuester(quester))) {
	    		player.sendMessage(split[1] + " is not one of your mercenaries");
				event.setCancelled(true);
				return;
			}
			player.sendMessage(split[1] + " is a level " + quester.getLevel() + " with " + quester.getExp() + "/" + (400 * (quester.getLevel() + 1)) + " Exp");
	
			quester.getClass("Warrior").display();
			quester.sendMessage(" Health: " + quester.getHealth() + "/" + quester.getMaxHealth());
			event.setCancelled(true);
	    } else if (split[0].equals("/my_mercs")) {
	    	MineQuest.getQuester(player).listMercs();
	    	event.setCancelled(true);
	    }
	}
	
	private void processDebug(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/goto")) {
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
        		player.teleport(world.getSpawnLocation());
        		event.setCancelled(true);
        	}
        } else if (split[0].equals("/setworldtime")) {
        	World world = MineQuest.getSServer().getWorld(split[1]);
        	world.setTime(Long.parseLong(split[2]));
        	event.setCancelled(true);
        } else if (split[0].equals("/debug")) {
        	MineQuest.getQuester(player).debug();
        	event.setCancelled(true);
        } else if (split[0].equals("/nomobs")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /nomobs <world_name>");
            	event.setCancelled(true);
            	return;
        	}
        	World world = MineQuest.getSServer().getWorld(split[1]);
        	
        	if (world != null) {
        		MineQuest.noMobs(world);
        		player.sendMessage("No mobs activated for world: " + world.getName());
        	} else {
        		player.sendMessage(split[1] + " is not a valid world");
        	}
        	
        	event.setCancelled(true);
        } else if (split[0].equals("/mobs")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /mobs <world_name>");
            	event.setCancelled(true);
            	return;
        	}
        	World world = MineQuest.getSServer().getWorld(split[1]);
        	
        	if (world != null) {
        		MineQuest.yesMobs(world);
        		player.sendMessage("Yes mobs activated for world: " + world.getName());
        	} else {
        		player.sendMessage(split[1] + " is not a valid world");
        	}
        	
        	event.setCancelled(true);
        } else if (split[0].equals("/mobss")) {
        	if (player.getWorld().getLivingEntities() == null) {
        		player.sendMessage("No Living Entities List");
        	} else {
        		player.sendMessage("There are " + MineQuest.getMobSize() + " " + player.getWorld().getLivingEntities().size());
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/recalculate_health")) {
        	if (player.isOp()) {
        		player.sendMessage("Recalculating all Health");
        		for (Quester quester : MineQuest.getQuesters()) {
        			player.sendMessage(quester.getName() + " - " + quester.recalculateHealth());
        			quester.save();
        		}
        		player.sendMessage("Recalculated!");
        	} else {
        		player.sendMessage("Only an op can do that");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/how_many")) {
        	player.sendMessage("There are " + MineQuest.getSServer().getOnlinePlayers().length + " online players");
        	event.setCancelled(true);
        }
	}
}
