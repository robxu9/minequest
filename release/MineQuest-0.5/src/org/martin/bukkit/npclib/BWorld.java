/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.martin.bukkit.npclib;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.EnumSkyBlock;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.WorldProvider;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;

/**
 *
 * @author martin
 */
public class BWorld {

//    private BServer server;
    private World world;
    private CraftWorld cWorld;
    private net.minecraft.server.World mcWorld;
    private WorldServer wServer;
    private WorldProvider wProvider;
    public BWorld(BServer server, String worldName){
//        this.server = server;
        world = server.getServer().getWorld(worldName);
        try {
            cWorld = (CraftWorld) world;
            wServer = cWorld.getHandle();
            wProvider = wServer.worldProvider;
        } catch (Exception ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
        }
    }

    public BWorld(World world){
        this.world = world;
        try {
            cWorld = (CraftWorld) world;
            wServer = cWorld.getHandle();
            wProvider = wServer.worldProvider;
        } catch (Exception ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
        }
    }

    public PlayerManager getPlayerManager(){
        return wServer.manager;
    }

    public CraftWorld getCraftWorld(){
        return cWorld;
    }

    public WorldServer getWorldServer(){
        return wServer;
    }

    public net.minecraft.server.World getMCWorld(){
        return mcWorld;
    }

    public WorldProvider getWorldProvider(){
        return wProvider;
    }

    public boolean createExplosion(double x, double y, double z, float power) {
        return wServer.a(null, x, y, z, power).wasCanceled ? false : true;
    }

    public boolean createExplosion(Location l, float power) {
        return wServer.a(null, l.getX(), l.getY(), l.getZ(), power).wasCanceled ? false : true;
    }

    public void setLightLevel(Block block, final int level){
        wServer.b(EnumSkyBlock.BLOCK, block.getX(), block.getY(), block.getZ(), level);
    }

//    @SuppressWarnings({ "unchecked", "rawtypes" })
//	public void removeEntity(String name,final Player player,JavaPlugin plugin){
//        server.getServer().getScheduler().callSyncMethod(plugin, new Callable() {
//
//            public Object call() throws Exception {
//
//                Location loc = player.getLocation();
//                CraftWorld craftWorld = (CraftWorld) player.getWorld();
//                CraftPlayer craftPlayer = (CraftPlayer) player;
//                double x = loc.getX() + 0.5;
//                double y = loc.getY() + 0.5;
//                double z = loc.getZ() + 0.5;
//                double radius = 10;
//
//                List<Entity> entities = new ArrayList();
//                AxisAlignedBB bb = AxisAlignedBB.a(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
//                entities = craftWorld.getHandle().b(craftPlayer.getHandle(), bb);
//                for (Entity o : entities) {
//                    if (!(o instanceof EntityPlayer)) {
//                        o.getBukkitEntity().remove();
//                    }
//                }
//                return null;
//            }
//        });
//    }
}
