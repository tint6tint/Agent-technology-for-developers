package fi.jyu.ties454.cleaningAgents.example.soiling;

import java.util.Optional;

import fi.jyu.ties454.cleaningAgents.actuators.Dirtier;
import fi.jyu.ties454.cleaningAgents.actuators.Rotator;
import fi.jyu.ties454.cleaningAgents.agent.GameAgent;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.DirtExplosion;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.JumpForwardMover;
import jade.core.behaviours.OneShotBehaviour;

public class Soiler1 extends GameAgent {

	private static final long serialVersionUID = 1L;

	private Rotator rotator;
	private Dirtier dirtier;

	@Override
	protected void setup() {

		this.rotator = this.getDevice(DefaultDevices.BasicRotator.class).get();
		this.dirtier = this.getDevice(DefaultDevices.BasicDirtier.class).get();

		this.addBehaviour(new OneShotBehaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				Optional<DirtExplosion> dirtExplosion = Soiler1.this.getDevice(DefaultDevices.DirtExplosion.class);
				if (dirtExplosion.isPresent()) {
					dirtExplosion.get().makeMess();
				}
				Optional<JumpForwardMover> jumper = Soiler1.this.getDevice(DefaultDevices.JumpForwardMover.class);
				if (jumper.isPresent()) {
					jumper.get().move();
					jumper.get().move();
					while (true) {
						Soiler1.this.rotator.rotateCW();
						while (jumper.get().move() == 5) {
							Soiler1.this.dirtier.makeMess();
						}
					}
				}
			}
		});

	}

}
