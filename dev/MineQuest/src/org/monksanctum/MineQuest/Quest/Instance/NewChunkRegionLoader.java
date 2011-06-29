package org.monksanctum.MineQuest.Quest.Instance;

import java.io.DataInput;
import java.io.File;
import java.util.Random;

import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.ChunkLoader;
import net.minecraft.server.ChunkRegionLoader;
import net.minecraft.server.CompressedStreamTools;
import net.minecraft.server.ConvertProgressUpdater;
import net.minecraft.server.Convertable;
import net.minecraft.server.EntityTracker;
import net.minecraft.server.IWorldAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.RegionFileCache;
import net.minecraft.server.World;
import net.minecraft.server.WorldLoaderServer;
import net.minecraft.server.WorldManager;
import net.minecraft.server.WorldServer;

import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.LongHashtable;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.monksanctum.MineQuest.MineQuest;

/**
 * The complete idea for this system came from tehbeard.
 * 
 * @author jmonk
 *
 */
public class NewChunkRegionLoader extends ChunkRegionLoader {
	
	public static CraftWorld createWorld(String name, Environment environment, int instance) {
		long seed = (new Random()).nextLong();
		ChunkGenerator generator = null;
        File folder = new File(name);
        CraftWorld world = (CraftWorld) MineQuest.getSServer().getWorld(name + instance);

        if (world != null) {
        	((NewChunkRegionLoader)world.getHandle().p().a(world.getHandle().worldProvider)).reloadChunks(world.getHandle(), name + instance);
            return world;
        }

        if ((folder.exists()) && (!folder.isDirectory())) {
            throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
        }

        MinecraftServer console = ((CraftServer)MineQuest.getSServer()).getServer();
        Convertable converter = new WorldLoaderServer(folder);
        if (converter.isConvertable(name)) {
        	MineQuest.getSServer().getLogger().info("Converting world '" + name + "'");
            converter.convert(name, new ConvertProgressUpdater(console));
        }

        int dimension = 200 + console.worlds.size();
        WorldServer internal = new WorldServer(console, new NewServerNBTManager(new File("."), instance, name, true), name + instance, dimension, seed, environment, generator);
        internal.z = console.worlds.get(0).z;

        internal.tracker = new EntityTracker(console, dimension);
        internal.addIWorldAccess((IWorldAccess) new WorldManager(console, internal));
        internal.spawnMonsters = 1;
        internal.setSpawnFlags(true, true);
        console.worlds.add(internal);

        if (generator != null) {
            internal.getWorld().getPopulators().addAll(generator.getDefaultPopulators(internal.getWorld()));
        }

        MineQuest.getSServer().getPluginManager().callEvent(new WorldInitEvent(internal.getWorld()));

        short short1 = 196;
        long i = System.currentTimeMillis();
        for (int j = -short1; j <= short1; j += 16) {
            for (int k = -short1; k <= short1; k += 16) {
                long l = System.currentTimeMillis();

                if (l < i) {
                    i = l;
                }

                if (l > i + 1000L) {
                    int i1 = (short1 * 2 + 1) * (short1 * 2 + 1);
                    int j1 = (j + short1) * (short1 * 2 + 1) + k + 1;

                    System.out.println("Preparing spawn area for " + name + ", " + (j1 * 100 / i1) + "%");
                    i = l;
                }

                ChunkCoordinates chunkcoordinates = internal.getSpawn();
                internal.chunkProviderServer.getChunkAt(chunkcoordinates.x + j >> 4, chunkcoordinates.z + k >> 4);

                while (internal.doLighting()) {
                    ;
                }
            }
        }
        
        MineQuest.getSServer().getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));
        
        return internal.getWorld();
	}

	private File file2;

	public NewChunkRegionLoader(File file, File file2) {
		super(file2);
		this.file2 = file2;
	}

	@Override
    public Chunk a(World world, int i, int j)
    {
        java.io.DataInputStream datainputstream = RegionFileCache.c(file2, i, j);
        NBTTagCompound nbttagcompound;
        if(datainputstream != null)
        {
            nbttagcompound = CompressedStreamTools.a((DataInput)datainputstream);
        } else
        {
            return null;
        }
        if(!nbttagcompound.hasKey("Level"))
        {
            System.out.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is missing level data, skipping").toString());
            return null;
        }
        if(!nbttagcompound.k("Level").hasKey("Blocks"))
        {
            System.out.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is missing block data, skipping").toString());
            return null;
        }
        Chunk chunk = ChunkLoader.a(world, nbttagcompound.k("Level"));
        if(!chunk.a(i, j))
        {
            System.out.println((new StringBuilder()).append("Chunk file at ").append(i).append(",").append(j).append(" is in the wrong location; relocating. (Expected ").append(i).append(", ").append(j).append(", got ").append(chunk.x).append(", ").append(chunk.z).append(")").toString());
            nbttagcompound.a("xPos", i);
            nbttagcompound.a("zPos", j);
            chunk = ChunkLoader.a(world, nbttagcompound.k("Level"));
        }
        chunk.h();
        return chunk;
    }

	@Override
	public void a(World arg0, Chunk arg1) {
		
	}

	private void reloadChunks(WorldServer world, String name) {
		world.chunkProviderServer.chunks = new LongHashtable<Chunk>();

        short short1 = 196;
        long i = System.currentTimeMillis();
        for (int j = -short1; j <= short1; j += 16) {
            for (int k = -short1; k <= short1; k += 16) {
                long l = System.currentTimeMillis();

                if (l < i) {
                    i = l;
                }

                if (l > i + 1000L) {
                    int i1 = (short1 * 2 + 1) * (short1 * 2 + 1);
                    int j1 = (j + short1) * (short1 * 2 + 1) + k + 1;

                    System.out.println("Preparing spawn area for " + name + ", " + (j1 * 100 / i1) + "%");
                    i = l;
                }

                ChunkCoordinates chunkcoordinates = world.getSpawn();
                world.chunkProviderServer.getChunkAt(chunkcoordinates.x + j >> 4, chunkcoordinates.z + k >> 4);

                while (world.doLighting()) {
                    ;
                }
            }
        }
	}

}
