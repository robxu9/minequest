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
package org.monk.MineQuest.Quest;

import java.util.ArrayList;
import java.util.List;

import org.monk.MineQuest.Quester.Quester;

public class Party {
	private List<Quester> questers;
	
	public Party() {
		questers = new ArrayList<Quester>();
	}
	
	public void addQuester(Quester quester) {
		questers.add(quester);
		
		quester.setParty(this);
	}
	
	public void remQuester(Quester quester) {
		questers.remove(quester);
		
		quester.setParty(null);
	}
	
	public List<Quester> getQuesters() {
		return questers;
	}
	
	public Quester[] getQuesterArray() {
		Quester[] ret = new Quester[questers.size()];
		int i = 0;
		
		for (Quester quester : questers) {
			ret[i++] = quester;
		}
		
		return ret;
	}
}
