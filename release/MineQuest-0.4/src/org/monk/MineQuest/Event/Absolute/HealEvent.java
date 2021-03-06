package org.monk.MineQuest.Event.Absolute;

import org.monk.MineQuest.MineQuest;
import org.monk.MineQuest.Event.EventParser;
import org.monk.MineQuest.Event.PeriodicEvent;
import org.monk.MineQuest.Quester.Quester;

public class HealEvent extends PeriodicEvent {

	private int amount;

	public HealEvent(long delay, int amount) {
		super(delay);
		this.amount = amount;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : MineQuest.getQuesters()) {
			if (!quester.inQuest()) {
				quester.setHealth(quester.getHealth() + amount);
			}
		}
	}

}
