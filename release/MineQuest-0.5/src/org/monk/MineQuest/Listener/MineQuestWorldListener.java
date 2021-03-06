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


import org.bukkit.Chunk;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;
import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Quester.NPCQuester;
import org.monk.MineQuest.Quester.Quester;

public class MineQuestWorldListener extends WorldListener{
	@Override
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		
		for (Quester quester : MineQuest.getQuesters()) {
			if (quester instanceof NPCQuester) {
				NPCQuester nquester = (NPCQuester)quester;
				if (nquester.inChunk(chunk)) {
					nquester.respawn();
				}
			}
		}
	}
	
	@Override
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		
		for (Quester quester : MineQuest.getQuesters()) {
			if (quester instanceof NPCQuester) {
				NPCQuester nquester = (NPCQuester)quester;
				if (nquester.inChunk(chunk)) {
					nquester.despawn();
				}
			}
		}
	}
}
