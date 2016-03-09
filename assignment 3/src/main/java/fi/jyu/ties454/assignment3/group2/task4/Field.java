package fi.jyu.ties454.assignment3.group2.task4;

import fi.jyu.ties454.cleaningAgents.infra.FloorState;
import jade.core.AID;

public class Field {
	
	protected FloorState state;
	protected int visitedCounter;
	protected int cleanedCounter;
	protected AID currentAgent;
	
	protected Position pos;

	public Field(int x, int y) {
		pos = new Position(x, y);
		state = FloorState.CLEAN;
		visitedCounter = 0;
		cleanedCounter = 0;
		currentAgent = null;
	}
	
	public void visited() {
		this.visitedCounter++;
	}
	
	public void cleaned() {
		this.cleanedCounter++;
		this.state = FloorState.CLEAN;
	}

	public int getVisitedCnt() {
		return visitedCounter;
	}
	
	public int getCleanedCnt() {
		return cleanedCounter;
	}
	
	public boolean hasAgent() {
		if (currentAgent != null) {
			return true;
		}
		return false;
	}
	
	public void setState(FloorState s) {
		this.state = s;
	}
	
	public void enter(AID agent) {
		this.currentAgent = agent;
		this.visitedCounter++;
	}
	
	public void leave() {
		this.currentAgent = null;
	}
}
