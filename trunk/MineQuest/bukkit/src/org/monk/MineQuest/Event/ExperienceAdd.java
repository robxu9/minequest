package org.monk.MineQuest.Event;

import org.monk.MineQuest.Quester.Quester;

public class ExperienceAdd extends NormalEvent {
	private Quester[] questers;
	private int exp;
	private int class_exp;

	public ExperienceAdd(long delay, Quester questers[], int exp, int class_exp) {
		super(delay);
		this.questers = questers;
		this.exp = exp;
		this.class_exp = class_exp;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : questers) {
			quester.expGain(exp);
			quester.expClassGain(class_exp);
		}
	}

}
