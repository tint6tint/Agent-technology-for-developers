package fi.jyu.ties454.assignment3.group2.task4;

import fi.jyu.ties454.cleaningAgents.infra.Location;
import jade.util.leap.Serializable;

public class Position extends Location implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected int roomID;
	protected int doorToRoom;

	public Position(int x, int y) {
		super(x, y);
		roomID = -1;
		doorToRoom=0;
	}
	
	public Position(int x, int y, int roomId) {
		super(x, y);
		this.roomID = roomId;
		doorToRoom=0;
	}
	
	public Position(Location loc) {
		super(loc.X, loc.Y);
		this.roomID = -1;
		doorToRoom=0;
	}
	
	public Position(Location loc, int roomId) {
		super(loc.X, loc.Y);
		this.roomID = roomId;
		doorToRoom=0;
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
	
	public void setDoorToRoom(int roomId) {
		this.doorToRoom = roomId;
	}
	
	public int getDoorToRoom() {
		return doorToRoom;
	}
	
	@Override
	public String toString() {
		return "[" + this.X + ", " + this.Y + "] room= " + this.roomID + " doorTo=" + this.doorToRoom;
	}
}
