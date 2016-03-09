package fi.jyu.ties454.cleaningAgents.infra;

import java.util.LinkedList;

import fi.jyu.ties454.cleaningAgents.agent.GameAgent;

class AgentState {

	private final LinkedList<Listener> listeners = new LinkedList<>();

	public interface Listener {
		default void moved() {
			this.changed();
		}

		default void turned() {
			this.changed();
		}

		void changed();
	}

	public void addListener(Listener l) {
		this.listeners.add(l);
	}

	final GameAgent agent;
	private Location l;
	private Orientation o;

	public AgentState(GameAgent agent, Location l, Orientation o) {
		super();
		this.agent = agent;
		this.l = l;
		this.o = o;
	}

	public Location getLocation() {
		return this.l;
	}

	public void setLocation(Location l) {
		if (!this.l.equals(l)) {
			this.l = l;
			this.listeners.forEach(li -> li.moved());
		}

	}

	Orientation getOrientation() {
		return this.o;
	}

	void setOrientation(Orientation o) {
		if (!this.o.equals(o)) {
			this.o = o;
			this.listeners.forEach(li -> li.turned());
		}
	}

	@Override
	public String toString() {
		return "AgentState [agent=" + this.agent.getLocalName() + ", l=" + this.l + ", o=" + this.o + "]";
	}

}
