package fi.jyu.ties454.cleaningAgents.infra;

import java.io.Serializable;

public class Location implements Serializable{

	private static final long serialVersionUID = 1L;

	public final int X;
	public final int Y;

	public Location(int x, int y) {
		this.X = x;
		this.Y = y;
	}

	public Location nStep(Orientation o, int n){
		switch (o) {
		case N:
			return new Location(this.X, this.Y - n);
		case E:
			return new Location(this.X + n, this.Y);
		case S:
			return new Location(this.X, this.Y + n);
		case W:
			return new Location(this.X - n, this.Y);
		default:
			throw new Error();
		}		
	}
	
	public Location oneStep(Orientation o) {
		return nStep(o, 1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.X;
		result = (prime * result) + this.Y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Location other = (Location) obj;
		if (this.X != other.X) {
			return false;
		}
		if (this.Y != other.Y) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Location [X=" + this.X + ", Y=" + this.Y + "]";
	}

}
