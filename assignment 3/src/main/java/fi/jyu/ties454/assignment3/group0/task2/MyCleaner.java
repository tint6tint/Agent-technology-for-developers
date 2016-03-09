package fi.jyu.ties454.assignment3.group0.task2;

import java.util.Optional;

import fi.jyu.ties454.cleaningAgents.agent.GameAgent;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.JackieChanRotator;
import jade.core.behaviours.OneShotBehaviour;

/**
 * The agent extends from CleaningAgent, which is actually a normal JADE agent.
 * As an extra it has methods to obtain sensors and actuators.
 */
public class MyCleaner extends GameAgent {

	private static final long serialVersionUID = 1L;

	@Override
	protected void setup() {
		// it is safe to obtain parts in setup(), but using them must be done in
		// behaviors!
		// getting the device is don using the getDevice call.
		// when this call returns true, the update method of the agent has been
		// called
		Optional<JackieChanRotator> fastRotator = this.getDevice(DefaultDevices.JackieChanRotator.class);
		if (fastRotator.isPresent()) {
			System.out.println("Got the moves");
		}
		this.addBehaviour(new OneShotBehaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				// TODO implement using whatever toys you got, like the
				// fastRotator
			}
		});
	}

}
