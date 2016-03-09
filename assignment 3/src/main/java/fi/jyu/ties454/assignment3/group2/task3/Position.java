package fi.jyu.ties454.assignment3.group2.task3;

import fi.jyu.ties454.cleaningAgents.infra.Location;
import jade.util.leap.Serializable;

public class Position extends Location implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int roomID;

	public Position(int x, int y) {
		super(x, y);
		roomID = -1;
	}
	
	public Position(Location loc) {
		super(loc.X, loc.Y);
	}

	public double dist(Position b) {
		return Math.sqrt(Math.pow(this.X - b.X, 2) + Math.pow(this.Y - b.Y, 2));
	}
	
	public boolean equals(Position p) {
		return ((this.Y == p.Y) && (this.X == p.X));
	}
	
	public void setRoom(int id) {
		this.roomID = id;
	}
	
	public int getRoom() {
		return roomID;
	}
}
