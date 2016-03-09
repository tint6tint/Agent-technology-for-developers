package fi.jyu.ties454.cleaningAgents.example.cleaning;

import java.util.Optional;
import java.util.Random;

import fi.jyu.ties454.cleaningAgents.actuators.Cleaner;
import fi.jyu.ties454.cleaningAgents.actuators.ForwardMover;
import fi.jyu.ties454.cleaningAgents.actuators.Rotator;
import fi.jyu.ties454.cleaningAgents.agent.GameAgent;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.AreaCleaner;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.JumpForwardMover;
import jade.core.behaviours.OneShotBehaviour;

public class RichCleaner1 extends GameAgent {

	private static final long serialVersionUID = 1L;
	private ForwardMover mover;
	private Rotator rotator;
	private Cleaner cleaner;

	@Override
	protected void setup() {

		this.mover = this.getDevice(DefaultDevices.BasicForwardMover.class).get();
		this.rotator = this.getDevice(DefaultDevices.BasicRotator.class).get();
		this.cleaner = this.getDevice(DefaultDevices.BasicCleaner.class).get();

		this.addBehaviour(new OneShotBehaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				Random rand = new Random();
				Optional<AreaCleaner> areaCleaner = RichCleaner1.this.getDevice(DefaultDevices.AreaCleaner.class);
				Optional<JumpForwardMover> jumper = RichCleaner1.this.getDevice(DefaultDevices.JumpForwardMover.class);
				if (areaCleaner.isPresent() && jumper.isPresent()) {
					while (true) {
						jumper.get().move();
						areaCleaner.get().clean();
						if (rand.nextInt(5) == 0) {
							RichCleaner1.this.rotator.rotateCW();
						}
					}
				} else {
					// no money -> use free stuff
					while (true) {
						RichCleaner1.this.mover.move();
						RichCleaner1.this.cleaner.clean();
						if (rand.nextInt(5) == 0) {
							RichCleaner1.this.rotator.rotateCW();
						}
					}
				}
			}
		});
	}

}
