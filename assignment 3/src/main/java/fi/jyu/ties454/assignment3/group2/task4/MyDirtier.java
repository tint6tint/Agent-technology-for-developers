package fi.jyu.ties454.assignment3.group2.task4;

import java.util.Optional;
import java.util.Random;

import fi.jyu.ties454.cleaningAgents.actuators.Dirtier;
import fi.jyu.ties454.cleaningAgents.actuators.ForwardMover;
import fi.jyu.ties454.cleaningAgents.agent.GameAgent;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.AreaDirtier;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.BasicRotator;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.DirtExplosion;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.JumpForwardMover;
import jade.core.behaviours.OneShotBehaviour;

/**
 * The agent extends from {@link GameAgent}, which is actually a normal JADE
 * agent. As an extra it has methods to obtain sensors and actuators.
 */
public class MyDirtier extends GameAgent {

	private static final long serialVersionUID = 1L;

	@Override
	protected void setup() {

		BasicRotator rotator = this.getDevice(DefaultDevices.BasicRotator.class).get();
		Dirtier d = this.getDevice(DefaultDevices.BasicDirtier.class).get();
		ForwardMover f = this.getDevice(DefaultDevices.BasicForwardMover.class).get();

		this.addBehaviour(new OneShotBehaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				Random r = new Random();
				Optional<AreaDirtier> areaDirtier = MyDirtier.this.getDevice(DefaultDevices.AreaDirtier.class);
				Optional<JumpForwardMover> jumper = MyDirtier.this.getDevice(DefaultDevices.JumpForwardMover.class);
				if (jumper.isPresent()) {
					jumper.get().move();
					jumper.get().move();
					while (true) {
						rotator.rotateCW();
						while (jumper.get().move() == 5) {
							if (areaDirtier.isPresent() && !areaDirtier.get().isEmpty()) {
								areaDirtier.get().makeMess();
							} else {
								// just normal mess
								d.makeMess();
							}
							if (r.nextInt(5) == 0) {
								rotator.rotateCW();
							}
							if (r.nextInt(25) == 0) {
								// attempt dirt explosion. This only works if
								// there is money left.
								Optional<DirtExplosion> dirtExplosion = MyDirtier.this
										.getDevice(DefaultDevices.DirtExplosion.class);
								if (dirtExplosion.isPresent()) {
									dirtExplosion.get().makeMess();
								}
							}
							if (r.nextInt(10) == 0) {
								f.move();
							}
						}
					}
				}
			}
		});

	}

}
