package fi.jyu.ties454.cleaningAgents.sensors;

import java.util.Optional;

import fi.jyu.ties454.cleaningAgents.infra.FloorState;

public interface DirtSensor {
	/**
	 * Is there dirt at the location of the agent?
	 *
	 * @return
	 */
	FloorState inspect();

	/**
	 * Is there dirt in front of the agent. Not all sensors support this.
	 *
	 * @return
	 */
	Optional<Boolean> dirtInFront();
}
