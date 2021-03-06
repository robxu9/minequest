/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.bukkit.npclib;

import java.util.HashMap;

import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author martin
 */
public class NPCManager {

    private HashMap<String, NPCEntity> npcs = new HashMap<String, NPCEntity>();
    private BServer server;
    @SuppressWarnings("unused")
	private JavaPlugin plugin;

    public NPCManager(JavaPlugin plugin) {
        this.plugin = plugin;
        server = BServer.getInstance(plugin);
    }

    public NPCEntity spawnNPC(String name, Location l) {
        BWorld world = new BWorld(l.getWorld());
        NPCEntity npcEntity = new NPCEntity(server.getMCServer(), world.getWorldServer(), name, new ItemInWorldManager(world.getMCWorld()));
        npcEntity.setPositionRotation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
        npcEntity.setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
        world.getWorldServer().getChunkAt(l.getWorld().getChunkAt(l).getX(), l.getWorld().getChunkAt(l).getZ()).a(npcEntity);
        //world.getWorldServer().manager.addPlayer(npcEntity);
        //server.getEntityTracker().a(npcEntity);
        //server.getEntityTracker().trackPlayer(npcEntity);
        world.getWorldServer().addEntity(npcEntity); //the right way
        npcs.put(name, npcEntity);
        return npcEntity;
    }

    public void despawn(String id) {
        NPCEntity npc = npcs.get(id);
        if (npc != null) {
            npcs.remove(id);
            try {
                npc.world.removeEntity(npc);

                CraftWorld craftWorld = (CraftWorld) npc.getBukkitEntity().getWorld();
                CraftPlayer craftPlayer = (CraftPlayer) npc.getBukkitEntity();
                
				WorldServer world = craftWorld.getHandle();
				world.manager.removePlayer(craftPlayer.getHandle());
				world.players.remove(craftPlayer.getHandle());
				world.tracker.untrackEntity(craftPlayer.getHandle());
				craftWorld.getHandle().kill(craftPlayer.getHandle());
//				npc.clearNetHandler();
				server.getMCServer().serverConfigurationManager.players.remove(craftPlayer.getHandle());
//				craftPlayer.setHandle(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void moveNPC(String npcName, Location l) {
        NPCEntity npc = npcs.get(npcName);
        if (npc != null) {
            npc.move(l.getX(), l.getY(), l.getZ());
        }
    }
	
	
    public NPCEntity getNPC(String name){
        return npcs.get(name);
    }
}
