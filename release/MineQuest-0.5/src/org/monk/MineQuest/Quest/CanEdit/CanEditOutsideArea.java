package org.monk.MineQuest.Quest.CanEdit;

import org.bukkit.Location;

public class CanEditOutsideArea extends CanEditArea {

	public CanEditOutsideArea(int index, int x, int y, int z, int maxX,
			int maxY, int maxZ) {
		super(index, x, y, z, maxX, maxY, maxZ);
	}
	
	@Override
	public boolean within(Location loc) {
		if ((loc.getX() >= x) && (loc.getX() <= max_x)) {
			return false;
		}
		if ((loc.getY() >= y) && (loc.getY() <= max_y)) {
			return false;
		}
		if ((loc.getZ() >= z) && (loc.getZ() <= max_z)) {
			return false;
		}
		
		return true;
	}

}
