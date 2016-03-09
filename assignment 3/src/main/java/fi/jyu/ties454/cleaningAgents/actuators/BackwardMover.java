package fi.jyu.ties454.cleaningAgents.actuators;

public interface BackwardMover {
	/**
	 * Moves the agent backward according to the actuators capabilities. Returns
	 * the number of cells moved.
	 *
	 * @return The number of cells moved backward
	 */
	int move();
}
