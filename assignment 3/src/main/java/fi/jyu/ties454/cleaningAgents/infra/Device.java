package fi.jyu.ties454.cleaningAgents.infra;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fi.jyu.ties454.cleaningAgents.agent.GameAgent;

public abstract class Device {

	/**
	 * This annotation indicates that the given device should be made available
	 * to the agent for the given price.
	 *
	 * @author michael
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface AvailableDevice {
		int cost();
	}

	protected final Floor map;
	protected final AgentState state;

	Device(Floor map, AgentState state) {
		this.map = map;
		this.state = state;
	}

	public abstract void attach(GameAgent agent);
}
