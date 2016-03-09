package fi.jyu.ties454.cleaningAgents.actuators;

public interface Dirtier {
	/**
	 * Makes a mess according to the actuators description
	 */
	void makeMess();

	/**
	 * Is the tank of this Dirtier empty?
	 *
	 * @return true in case the tank is empty
	 */
	boolean isEmpty();
}
