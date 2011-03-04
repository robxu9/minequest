package org.monk.MineQuest.Quest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Event.AreaEvent;
import org.monk.MineQuest.Event.BlockEvent;
import org.monk.MineQuest.Event.EntitySpawnerCompleteEvent;
import org.monk.MineQuest.Event.EntitySpawnerCompleteNMEvent;
import org.monk.MineQuest.Event.EntitySpawnerEvent;
import org.monk.MineQuest.Event.EntitySpawnerNoMove;
import org.monk.MineQuest.Event.Event;
import org.monk.MineQuest.Event.ExperienceAdd;
import org.monk.MineQuest.Event.MessageEvent;
import org.monk.MineQuest.Event.QuestEvent;
import org.monk.MineQuest.Quester.Quester;

public class Quest {
	private Quester questers[];
	private List<QuestTask> tasks;
	private List<Event> events;
	
	public Quest(String filename) {
		this.questers = MineQuest.getActiveQuesters();
		tasks = new ArrayList<QuestTask>();
		events = new ArrayList<Event>();

		try {
			BufferedReader bis = new BufferedReader(new FileReader(filename));
			
			String line;
			while ((line = bis.readLine()) != null) {
				String split[] = line.split(":");
				if (split[0].equals("Event")) {
					createEvent(split);
				} else if (split[0].equals("Task")) {
					createTask(split);
				} else if (split[0].equals("World")) {
					if (MineQuest.getSServer().getWorld(split[1]) != null) {
						MineQuest.getSServer().createWorld(split[1], Environment.NORMAL);
					}
					
					World world = MineQuest.getSServer().getWorld(split[1]);
					
					teleport(questers, world);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void teleport(Quester[] questers, World world) {
		for (Quester quester : questers) {
			Location location = quester.getPlayer().getLocation();
			location.setWorld(world);
			quester.getPlayer().teleportTo(location);
		}
	}

	public void createTask(String line[]) {
		int id = Integer.parseInt(line[1]);
		Event[] events = new Event[line[2].split(",").length];
		int i = 0;
		
		for (String event : line[2].split(",")) {
			events[i++] = getEvent(Integer.parseInt(event));
		}
		
		tasks.add(new QuestTask(events, id));
	}
	
	public void createEvent(String line[]) {
		int id = Integer.parseInt(line[1]);
		String type = line[2];
		LivingEntity entities[] = new LivingEntity[questers.length];
		int i = 0;
		for (Quester quester : questers) {
			entities[i++] = quester.getPlayer();
			quester.setQuest(this);
		}
		
		if (type.equals("AreaEvent")) {
			int delay = Integer.parseInt(line[3]);
			int index = Integer.parseInt(line[4]);
			
			if (!line[5].equals("all")) {
				entities = new LivingEntity[line[5].split(",").length];
				int in = 0;
				for (String name : line[5].split(",")) {
					for (Quester quester : questers) {
						if (quester.getName().equals(name)) {
							entities[in++] = quester.getPlayer();
						}
					}
				}
			}
			Location loc = new Location(entities[0].getWorld(), Integer.parseInt(line[6]), Integer.parseInt(line[7]), Integer.parseInt(line[8]));
			int radius = Integer.parseInt(line[9]);
			events.add(new AreaEvent(this, delay, index, entities, loc, radius));
		} else if (type.equals("MessageEvent")) {
			int delay = Integer.parseInt(line[3]);

			if (!line[4].equals("all")) {
				entities = new LivingEntity[line[4].split(",").length];
				int in = 0;
				for (String name : line[4].split(",")) {
					for (Quester quester : questers) {
						if (quester.getName().equals(name)) {
							entities[in++] = quester.getPlayer();
						}
					}
				}
			}
			events.add(new MessageEvent(delay, questers, line[5]));
		} else if (type.equals("BlockEvent")) {
			int delay = Integer.parseInt(line[3]);

			Block block = entities[0].getWorld().getBlockAt(Integer.parseInt(line[4]), Integer.parseInt(line[5]), Integer.parseInt(line[6]));
			
			int mat = Integer.parseInt(line[7]);
			
			events.add(new BlockEvent(delay, block, Material.getMaterial(mat)));
		} else if (type.equals("QuestEvent")) {
			int delay = Integer.parseInt(line[3]);
			int index = Integer.parseInt(line[4]);
			
			events.add(new QuestEvent(this, delay, index));
		} else if (type.equals("EntitySpawnerEvent")) {
			int delay = Integer.parseInt(line[3]);
			String creature = line[7];
			Location location = new Location(entities[0].getWorld(), Integer.parseInt(line[4]), Integer.parseInt(line[5]), Integer.parseInt(line[6]));
			boolean superm;
			if (line[8].equals("f")) {
				superm = false;
			} else {
				superm = true;
			}
			events.add(new EntitySpawnerEvent(delay, location, CreatureType.fromName(creature), superm));
		} else if (type.equals("EntitySpawnerNoMove")) {
			int delay = Integer.parseInt(line[3]);
			String creature = line[7];
			Location location = new Location(entities[0].getWorld(), Integer.parseInt(line[4]), Integer.parseInt(line[5]), Integer.parseInt(line[6]));
			boolean superm;
			if (line[8].equals("f")) {
				superm = false;
			} else {
				superm = true;
			}
			events.add(new EntitySpawnerNoMove(delay, location, CreatureType.fromName(creature), superm));			
		} else if (type.equals("EntitySpawnerCompleteNMEvent")) {
			long delay = Integer.parseInt(line[3]);
			int index = Integer.parseInt(line[4]);
			int event = Integer.parseInt(line[5]);

			events.add(new EntitySpawnerCompleteNMEvent(this, delay, index, (EntitySpawnerEvent)getEvent(event)));
		} else if (type.equals("EntitySpawnerCompleteEvent")) {
			int delay = Integer.parseInt(line[3]);
			int event = Integer.parseInt(line[4]);

			events.add(new EntitySpawnerCompleteEvent(delay, (EntitySpawnerEvent)getEvent(event)));
		} else if (type.equals("ExperienceAdd")) {
			long delay = Integer.parseInt(line[3]);
			int exp = Integer.parseInt(line[5]);
			int class_exp = Integer.parseInt(line[6]);
			if (!line[4].equals("all")) {
				MineQuest.log("Warning: Options other than all are not supported for ExperienceAdd");
			}
			
			events.add(new ExperienceAdd(delay, questers, exp, class_exp));
		}
		events.get(events.size() - 1).setId(id);
	}
	
	private Event getEvent(int id) {
		for (Event event : events) {
			if (event.getId() == id) {
				return event;
			}
		}
		
		return null;
	}

	public Event[] getNextEvents(int index) {
		if (index == -1) {
			for (Quester quester : questers) {
				quester.clearQuest();
			}
			return null;
		}
		
		for (QuestTask task : tasks) {
			if (task.getId() == index) {
				return task.getEvents();
			}
		}
		
		return null;
	}
}
