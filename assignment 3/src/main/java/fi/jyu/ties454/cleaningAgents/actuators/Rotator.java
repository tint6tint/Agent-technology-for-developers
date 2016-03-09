package fi.jyu.ties454.cleaningAgents.actuators;

public interface Rotator {
	/**
	 * Rotates the robot clockwise 90 degrees
	 */
	public void rotateCW();

	/**
	 * Rotates the robot counter-clockwise 90 degrees
	 */
	public void rotateCCW();
}
