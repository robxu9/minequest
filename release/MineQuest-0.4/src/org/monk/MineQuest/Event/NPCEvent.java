package org.monk.MineQuest.Event;

import org.monk.MineQuest.Quester.NPCQuester;

public class NPCEvent extends PeriodicEvent {
	private NPCQuester quester;

	public NPCEvent(long delay, NPCQuester quester) {
		super(delay);
		this.quester = quester;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		quester.activate();
		
		super.activate(eventParser);
	}

}
