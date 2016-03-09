package fi.jyu.ties454.cleaningAgents.sensors;

public interface WallSensor {
	/**
	 * Is there a wall right in front of the agent?
	 *
	 * @return
	 */
	public default boolean wallInfront() {
		return this.canContinueAtLeast() == 0;
	};

	/**
	 * An underestimation of the number of steps the agent can still move
	 * forward. If smaller than {@link WallSensor#visionDistance()} then the
	 * estimate is exact.
	 *
	 * @return
	 */
	public int canContinueAtLeast();

	/**
	 * How far can this sensor 'see'.
	 *
	 * @return
	 */
	public int visionDistance();
}
