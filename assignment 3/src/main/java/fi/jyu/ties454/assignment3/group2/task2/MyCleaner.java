package fi.jyu.ties454.assignment3.group2.task2;

import java.util.Optional;

import fi.jyu.ties454.cleaningAgents.actuators.ForwardMover;
import fi.jyu.ties454.cleaningAgents.actuators.Rotator;
import fi.jyu.ties454.cleaningAgents.agent.GameAgent;
import fi.jyu.ties454.cleaningAgents.agent.Tracker;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.AreaCleaner;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.BasicCleaner;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.BasicDirtSensor;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.BasicForwardMover;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.BasicWallSensor;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.JackieChanRotator;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.JumpForwardMover;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.LaserDirtSensor;
import fi.jyu.ties454.cleaningAgents.infra.Orientation;
import jade.core.AgentState;
import jade.core.behaviours.CyclicBehaviour;
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
		// getting the device is done using the getDevice call.
		// when this call returns true, the update method of the agent has been
		// called
		Optional<JackieChanRotator> fastRotator = this.getDevice(DefaultDevices.JackieChanRotator.class);
		Optional<JumpForwardMover> fhfMover = this.getDevice(DefaultDevices.JumpForwardMover.class);
		Optional<LaserDirtSensor> laserSensor = this.getDevice(DefaultDevices.LaserDirtSensor.class);
		Optional<AreaCleaner> arCl = this.getDevice(DefaultDevices.AreaCleaner.class);
		Optional<BasicWallSensor> wallSensor = this.getDevice(DefaultDevices.BasicWallSensor.class);
		Optional<BasicDirtSensor> basicSensor = this.getDevice(DefaultDevices.BasicDirtSensor.class);
		Optional<BasicCleaner> basicCleaner = this.getDevice(DefaultDevices.BasicCleaner.class);
		Optional<BasicForwardMover> basicMover = this.getDevice(DefaultDevices.BasicForwardMover.class);
		
		while (!(fastRotator.isPresent() &&
				fhfMover.isPresent() &&
				laserSensor.isPresent() &&
				arCl.isPresent() &&
				wallSensor.isPresent() &&
				basicSensor.isPresent())) {
			System.out.println("wainting for sensors to be present");
		}
		
		Tracker t = new Tracker();
		Rotator rotator = t.registerRotator(fastRotator.get());	
		ForwardMover jumper = t.registerForwardMover(fhfMover.get());
		ForwardMover mover = t.registerForwardMover(basicMover.get());
		
		LaserDirtSensor lds = laserSensor.get();
		AreaCleaner areaCleaner = arCl.get();
		
		BasicWallSensor basicWallSensor = wallSensor.get();
		
		BasicDirtSensor basicDirtSensor= this.getDevice(DefaultDevices.BasicDirtSensor.class).get();
		BasicCleaner bc = basicCleaner.get();
		// 1500+3000+3000+3000+1500
		
		
		addBehaviour(new OneShotBehaviour() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				while(true){
						// TODO Auto-generated method stub
						int[] dirty = scanArea();
						int dirtCount = countDirt(dirty);
						
						if (dirtCount==0){
							if(basicWallSensor.wallInfront()){
								System.out.println("exploring"+t.getLocation());
								explore();
							}else{
								System.out.println("jumping location from"+t.getLocation());
								jumper.move();
								System.out.println("after jumping "+ t.getLocation());
							}
						}else if(dirtCount>1){
							areaCleaner.clean();
							// Because everything is clean then we need to relocate the agent
							explore();
						}else {
							//Case: dirt count == 1
							rotateTowardsDirt(dirty);
							mover.move();
							//After relocated to find the dirty and clean
							bc.clean();
							if(basicWallSensor.wallInfront()){
								explore();
							}
						}
				}
			}
			int countDirt(int[] scan) {
				int dirtCount = 0;
				for (int i = 0; i < scan.length; i++) {
					dirtCount += scan[i];
				}
				return dirtCount;
			}
			
			void explore() {
				System.out.println("explore starts" + t.getOrientation());
				while(!basicWallSensor.wallInfront()){
					jumper.move();
					if (moveAndSense()){
						return;
					}
					System.out.println("should find wall" + t.getLocation());
				}
				rotator.rotateCW();
				while(!basicWallSensor.wallInfront()){
					jumper.move();
					if (moveAndSense()){
						return;
					}
					System.out.println("should find conner" + t.getLocation());
				}
				//found the corner
				rotator.rotateCW();
				System.out.println("corner find and rotate " + t.getLocation());
				
				//start real cleaning
				while(!basicWallSensor.wallInfront()){
					if (moveAndSense()){
						return;
					}
					else{
						// cw, step, cw, walk
						rotator.rotateCW();
						if (moveAndSense())
						return;
						mover.move();
						rotator.rotateCW();
						while (!basicWallSensor.wallInfront()) {
							if (moveAndSense())
								return;
						}
						
						// ccw, step, ccw, walk
						rotator.rotateCCW();
						if (moveAndSense())
							return;
						mover.move();
						rotator.rotateCCW();
						while (!basicWallSensor.wallInfront()) {
							if (moveAndSense())
								return;
						}
						
					
						
					}
				}

			}
			
			boolean moveAndSense() {
				mover.move();
				return (countDirt(scanArea()) > 0);
			}
			
			void rotateTowardsDirt(int[] scan) {
				int i;
				for (i = 0; i < scan.length; i++) {
					if (scan[i] == 1) {
						break;
					}
				}
				
				switch (i) {
				case 0: // rotate northwards
					
					while (t.getOrientation() != Orientation.N) rotator.rotateCW();
					break;
				case 1: // rotate eastwards
					while (t.getOrientation() != Orientation.E) rotator.rotateCW();
					break;
				case 2: // rotate south
					while (t.getOrientation() != Orientation.S) rotator.rotateCW();
					break;
				case 3: // rotate westwards
					while (t.getOrientation() != Orientation.W) rotator.rotateCW();
					break;
				}
			}
			
			int[] scanArea() {
				int[] dirty = new int[4];
				Orientation originalValue=t.getOrientation();
				// find north
				System.out.println(t.getOrientation());
				while (t.getOrientation() != Orientation.N) {
					rotator.rotateCW();
				}
				// check north
				if (lds.dirtInFront().get()) {
					dirty[0] = 1;
				} else {
					dirty[0] = 0;
				}
				
				// check east
				
				rotator.rotateCW();
				System.out.println("suppose to rotate east "+t.getOrientation());
				if (lds.dirtInFront().get()) {
					dirty[1] = 1;
				} else {
					dirty[1] = 0;
				}
				
				// check south
				rotator.rotateCW();
				System.out.println("suppose to rotate south "+t.getOrientation());
				if (lds.dirtInFront().get()) {
					dirty[2] = 1;
				} else {
					dirty[2] = 0;
				}
				
				// check west
				rotator.rotateCW();
				System.out.println("suppose to rotate west " + t.getOrientation());
				if (lds.dirtInFront().get()) {
					dirty[3] = 1;
				} else {
					dirty[3] = 0;
				}
				while (t.getOrientation() != originalValue) rotator.rotateCW();
				
				return dirty;
			}	
		});
		
	}

}
