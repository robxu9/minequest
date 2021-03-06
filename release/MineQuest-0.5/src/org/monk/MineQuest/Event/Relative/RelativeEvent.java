package org.monk.MineQuest.Event.Relative;

import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Event.NormalEvent;
import org.monk.MineQuest.Quest.Quest;

public abstract class RelativeEvent extends NormalEvent {

	public RelativeEvent(long delay) {
		super(delay);
	}
	
	public static RelativeEvent newRelative(String[] split, Quest quest) throws Exception {
//		RelativeEvent relativeEvent = null;
		
		if (split[3].equals("")) {
			return null;
		} else {
			MineQuest.log("Error: Unknown Relative Event: " + split[3]);
			throw new Exception();
		}
		
//		relativeEvent.setId(Integer.parseInt(split[1]));
		
//		return relativeEvent;
	}

}
