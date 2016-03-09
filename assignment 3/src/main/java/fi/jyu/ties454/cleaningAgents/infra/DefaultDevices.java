package fi.jyu.ties454.cleaningAgents.infra;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import fi.jyu.ties454.cleaningAgents.actuators.BackwardMover;
import fi.jyu.ties454.cleaningAgents.actuators.Cleaner;
import fi.jyu.ties454.cleaningAgents.actuators.Dirtier;
import fi.jyu.ties454.cleaningAgents.actuators.ForwardMover;
import fi.jyu.ties454.cleaningAgents.actuators.Rotator;
import fi.jyu.ties454.cleaningAgents.agent.GameAgent;
import fi.jyu.ties454.cleaningAgents.sensors.DirtSensor;
import fi.jyu.ties454.cleaningAgents.sensors.WallSensor;
import jade.core.Agent;

/**
 * Class containing the actual implementation of actuators
 *
 * @author michael
 *
 */
public class DefaultDevices {

	/**
	 * This cleaner is a prototype to be used on the next missions to Mars.
	 * Cleans the area under the robot and all dirt within the 5x5 square around
	 * the robot. (even cleans trough walls!)
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = areaCleanerCost)
	public static class AreaCleaner extends Device implements Cleaner {

		AreaCleaner(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public boolean clean() {
			DefaultDevices.sleep(DefaultDevices.areaCleanTime);
			Location agentLocation = this.state.getLocation();
			boolean dirtFound = false;
			for (int x = agentLocation.X - 2; x <= (agentLocation.X + 2); x++) {
				for (int y = agentLocation.Y - 2; y <= (agentLocation.Y + 2); y++) {
					Location cleanLocation = new Location(x, y);
					if (this.map.isValidLocation(cleanLocation)) {
						dirtFound = this.map.isDirty(cleanLocation) || dirtFound;
						this.map.clean(cleanLocation);
					}
				}
			}
			return dirtFound;
		}

	}

	/**
	 * Soils the area under the robot and one cell in each direction (altogether
	 * 9 cells). Can be used 20 times.
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = areaDirtierCost)
	public static class AreaDirtier extends Device implements Dirtier {

		private static final int maxUse = 20;
		private int uses = 0;

		AreaDirtier(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public void makeMess() {
			if (!this.isEmpty()) {
				DefaultDevices.sleep(DefaultDevices.areDirtierTime);
				Location agentLocation = this.state.getLocation();
				for (int x = agentLocation.X - 1; x <= (agentLocation.X + 1); x++) {
					for (int y = agentLocation.Y - 1; y <= (agentLocation.Y + 1); y++) {
						Location locationToBeSoiled = new Location(x, y);
						if (this.map.isValidLocation(locationToBeSoiled)) {
							this.map.soil(locationToBeSoiled);
						}
					}
				}
				this.uses++;
			}
		}

		@Override
		public boolean isEmpty() {
			return this.uses == AreaDirtier.maxUse;
		}

	}

	private static class BackwardMoverImpl extends MoverImpl implements BackwardMover {
		public BackwardMoverImpl(Floor floor, AgentState state, int maxMoveSize) {
			super(floor, state, maxMoveSize, true);
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}
	}

	/**
	 * Move the robot backwards one step.
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = basicBackwardMovercost)
	public static class BasicBackwardMover extends BackwardMoverImpl {
		BasicBackwardMover(Floor floor, AgentState state, List<AgentState> others) {
			super(floor, state, 1);
		}

		@Override
		public int move() {
			DefaultDevices.sleep(DefaultDevices.basicMoveTime);
			return super.move();
		}
	}

	/**
	 * Cleans the area under the robot.
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = basicCleanerCost)
	public static class BasicCleaner extends Device implements Cleaner {

		BasicCleaner(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public boolean clean() {
			DefaultDevices.sleep(DefaultDevices.basicCleanTime);
			boolean wasDirty = this.map.isDirty(this.state.getLocation());
			this.map.clean(this.state.getLocation());
			return wasDirty;
		}

	}

	/**
	 * Soils the area under the robot.
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = basicDirtierCost)
	public static class BasicDirtier extends Device implements Dirtier {

		// private static final int maxUse = 20;
		// private int uses = 0;

		BasicDirtier(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public void makeMess() {
			if (!this.isEmpty()) {
				DefaultDevices.sleep(DefaultDevices.basicDirtierTime);
				this.map.soil(this.state.getLocation());
				// this.uses++;
			}
		}

		@Override
		public boolean isEmpty() {
			// return this.uses == BasicDirtier.maxUse;
			return false;
		}

	}

	/**
	 * Checks for dirt under the robot
	 *
	 * @author michael
	 */
	@Device.AvailableDevice(cost = basicDirtSensorCost)
	public static class BasicDirtSensor extends Device implements DirtSensor {

		BasicDirtSensor(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public Optional<Boolean> dirtInFront() {
			return Optional.empty();
		}

		@Override
		public FloorState inspect() {
			DefaultDevices.sleep(DefaultDevices.basicDirtSenseTime);
			return this.map.state(this.state.getLocation());
		}

	}

	/**
	 * Move the robot forward one step.
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = basicForwardMoverCost)
	public static class BasicForwardMover extends ForwardMoverImpl {
		BasicForwardMover(Floor floor, AgentState state, List<AgentState> others) {
			super(floor, state, 1);
		}

		@Override
		public int move() {
			DefaultDevices.sleep(DefaultDevices.basicMoveTime);
			return super.move();
		}
	}

	/**
	 * Rotates the robot
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = basicRotatorCost)
	public static class BasicRotator extends Device implements Rotator {

		BasicRotator(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public void rotateCCW() {
			DefaultDevices.sleep(DefaultDevices.basicRotateTime);
			this.state.setOrientation(this.state.getOrientation().ccw());
		}

		@Override
		public void rotateCW() {
			DefaultDevices.sleep(DefaultDevices.basicRotateTime);
			this.state.setOrientation(this.state.getOrientation().cw());
		}

	}

	/**
	 * Checks whether there is a wall right in front of the agent.
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = basicWallSensorCost)
	public static class BasicWallSensor extends WallSensorImpl implements WallSensor {

		BasicWallSensor(Floor map, AgentState state, List<AgentState> others) {
			super(map, state, basicWallSensorDepth);
		}

		@Override
		public int canContinueAtLeast() {
			DefaultDevices.sleep(DefaultDevices.basicWallSenseTime);
			return super.canContinueAtLeast();
		}
	}

	/**
	 * Creates the ultimate mess. Spreads dirt all around the robot.
	 * Unfortunately the dirt tank can only be exploded once.
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = dirtExplosionCost)
	public static class DirtExplosion extends Device implements Dirtier {

		boolean used = false;

		DirtExplosion(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public void makeMess() {
			if (!this.used) {
				DefaultDevices.sleep(DefaultDevices.dirtExplosionTime);
				// make dirty: perform 50 random walks of length 20, drop off
				int numberOfWalks = 50;
				int walkLength = 20;
				Location initialLocation = this.state.getLocation();
				Random r = new Random();
				for (int i = 0; i < numberOfWalks; i++) {
					Location currentLocation = initialLocation;
					for (int step = 0; step < walkLength; step++) {
						Location potentialNext = currentLocation.oneStep(Orientation.random(r));
						if (this.map.isValidLocation(potentialNext)) {
							currentLocation = potentialNext;
						}
					}
					this.map.soil(currentLocation);
				}
				this.used = true;
			}
		}

		@Override
		public boolean isEmpty() {
			return this.used;
		}

	}

	private static class ForwardMoverImpl extends MoverImpl implements ForwardMover {
		ForwardMoverImpl(Floor floor, AgentState state, int maxMoveSize) {
			super(floor, state, maxMoveSize, false);
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}
	}

	/**
	 * Move the robot forward up to 1000 steps. First commercialization of a
	 * hybrid device. The genes of a frog were injected into a grasshopper whose
	 * legs are replaced by a kevlar frame.
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = frogForwardMoverCost)
	public static class FrogHopperForwardMover extends ForwardMoverImpl {
		FrogHopperForwardMover(Floor floor, AgentState state, List<AgentState> others) {
			super(floor, state, 1000);
		}

		@Override
		public int move() {
			DefaultDevices.sleep(DefaultDevices.frogMoveTime);
			return super.move();
		}
	}

	/**
	 * Checks whether there is dirt under the robot. The sensor makes one sided
	 * errors 20% of the time. If the sensor tells that the area is clean, it is
	 * clean. If the sensor tells the area is dirty, it might have been clean
	 * with low probability.
	 */
	@Device.AvailableDevice(cost = highProbDirtSensorCost)
	public static class HighProbabilisticDirtSensor extends Device implements DirtSensor {

		private final Random r;

		HighProbabilisticDirtSensor(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
			this.r = new Random();
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public Optional<Boolean> dirtInFront() {
			return Optional.empty();
		}

		@Override
		public FloorState inspect() {
			DefaultDevices.sleep(DefaultDevices.highProbDirtSenseTime);
			FloorState realState = this.map.state(this.state.getLocation());
			if ((realState == FloorState.CLEAN) && (this.r.nextInt(5) == 0)) {
				return FloorState.DIRTY;
			}
			return realState;
		}

	}

	/**
	 * Rotates the robot in no time (really)
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = jackieChanRotatorCost)
	public static class JackieChanRotator extends Device implements Rotator {

		JackieChanRotator(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public void rotateCCW() {
			DefaultDevices.sleep(DefaultDevices.jackieChanRotateTime);
			this.state.setOrientation(this.state.getOrientation().ccw());
		}

		@Override
		public void rotateCW() {
			DefaultDevices.sleep(DefaultDevices.jackieChanRotateTime);
			this.state.setOrientation(this.state.getOrientation().cw());
		}

	}

	/**
	 * Move the robot forward up to 5 steps.
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = jumpForwardMoverCost)
	public static class JumpForwardMover extends ForwardMoverImpl {
		JumpForwardMover(Floor floor, AgentState state, List<AgentState> others) {
			super(floor, state, 5);
		}

		@Override
		public int move() {
			DefaultDevices.sleep(DefaultDevices.jumpForwardMoveTime);
			return super.move();
		}
	}

	/**
	 * The Ferrari among dirt sensors. Besides vary fast checking of dirt under
	 * the robot, it is able to measure dirt anywhere in front of the robot.
	 * Uses some sort of laser technique which was developed for a classified
	 * NASA project.
	 *
	 * If there is dirt under the robot or anywhere in front of the robot, the
	 * sensor will be able to tell. However, the sensor cannot tell exactly how
	 * far in front the dirt is.
	 *
	 * @author michael
	 */
	@Device.AvailableDevice(cost = laserDirtSensorCost)
	public static class LaserDirtSensor extends Device implements DirtSensor {

		LaserDirtSensor(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public Optional<Boolean> dirtInFront() {
			DefaultDevices.sleep(DefaultDevices.laserDirtInFrontSensorTime);
			Location startLocation = this.state.getLocation();
			Orientation orientation = this.state.getOrientation();

			Location potentialNewLocation = startLocation.oneStep(orientation);
			while (this.map.isValidLocation(potentialNewLocation)) {				
				if (this.map.isDirty(potentialNewLocation)) {
					return Optional.of(true);
				}
				potentialNewLocation = potentialNewLocation.oneStep(orientation);
			}
			return Optional.of(false);
		}

		@Override
		public FloorState inspect() {
			DefaultDevices.sleep(DefaultDevices.laserDirtSenseTime);
			return this.map.state(this.state.getLocation());
		}

	}

	/**
	 * Getting seasick of rotating all the time? Use this sneaky
	 * {@link ForwardMover} which actually moves you sideways to the left.
	 *
	 * @author michael
	 *
	 */
	@Device.AvailableDevice(cost = basicSidewaysMoverCost)
	public static class LeftMover extends Device implements ForwardMover {

		LeftMover(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
		}

		@Override
		public int move() {
			DefaultDevices.sleep(DefaultDevices.basicSideWaysMovetime);
			Location newLocation = this.state.getLocation();
			Orientation orientation = this.state.getOrientation().ccw();

			Location potentialNewLocation = newLocation.oneStep(orientation);
			if (!this.map.isValidLocation(potentialNewLocation)) {
				return 0;
			}
			this.state.setLocation(potentialNewLocation);
			return 1;
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}
	}

	/**
	 * Checks whether there is dirt under the robot. The sensor makes two sided
	 * errors 25% of the time. Measuring multiple times increases the confidence
	 * when using this sensor.
	 */
	@Device.AvailableDevice(cost = lowProbDirtSensorCost)
	public static class LowProbabilisticDirtSensor extends Device implements DirtSensor {

		private final Random r;

		LowProbabilisticDirtSensor(Floor map, AgentState state, List<AgentState> others) {
			super(map, state);
			this.r = new Random();
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public Optional<Boolean> dirtInFront() {
			return Optional.empty();
		}

		@Override
		public FloorState inspect() {
			DefaultDevices.sleep(DefaultDevices.lowProbDirtSenseTime);
			FloorState realState = this.map.state(this.state.getLocation());
			if (this.r.nextInt(4) == 0) {
				return realState.invert();
			}
			return realState;
		}

	}

	private static abstract class MoverImpl extends Device {

		private final int maxMove;
		private final boolean reverse;

		MoverImpl(Floor map, AgentState state, int maxMoveSize, boolean reverse) {
			super(map, state);
			this.maxMove = maxMoveSize;
			this.reverse = reverse;
		}

		public int move() {
			Location newLocation = this.state.getLocation();
			Orientation orientation = this.reverse ? this.state.getOrientation().oposite()
					: this.state.getOrientation();

			int steps = 0;
			for (; steps < this.maxMove; steps++) {
				Location potentialNewLocation = newLocation.oneStep(orientation);
				if (!this.map.isValidLocation(potentialNewLocation)) {
					break;
				}
				newLocation = potentialNewLocation;
			}
			this.state.setLocation(newLocation);
			return steps;
		}

	}

	private static class WallSensorImpl extends Device implements WallSensor {

		private final int depth;

		WallSensorImpl(Floor map, AgentState state, int visionDist) {
			super(map, state);
			this.depth = visionDist;
		}

		@Override
		public void attach(GameAgent agent) {
			agent.update(this);
		}

		@Override
		public int canContinueAtLeast() {
			Location newLocation = this.state.getLocation();
			Orientation orientation = this.state.getOrientation();

			int steps = 0;
			for (; steps < this.depth; steps++) {
				Location potentialNewLocation = newLocation.oneStep(orientation);
				if (!this.map.isValidLocation(potentialNewLocation)) {
					break;
				}
				newLocation = potentialNewLocation;
			}
			return steps;
		}

		@Override
		public int visionDistance() {
			return this.depth;
		}
	}

	private static final int timeFactor = 25; // default = 50
	private static final int costFactor = Manager.initialBudget / 120;

	private static final int free = 0;
	private static final int cheap = costFactor * 5;
	private static final int middle = costFactor * 15;
	private static final int expensive = costFactor * 30;

	private static final int slooow = timeFactor * 50;
	private static final int slow = timeFactor * 25;
	private static final int normalSpeed = timeFactor * 10;
	private static final int fast = timeFactor * 5;
	private static final int faster = timeFactor * 1;
	private static final int instant = 0;

	private static final int areaCleanerCost = expensive;
	private static final int areaCleanTime = normalSpeed;

	private static final int areaDirtierCost = middle;
	public static final long areDirtierTime = normalSpeed;

	private static final int basicBackwardMovercost = cheap;

	private static final int basicCleanerCost = free;
	private static final int basicCleanTime = normalSpeed;

	private static final int basicDirtierCost = free;
	private static final int basicDirtierTime = slooow;

	private static final int basicDirtSenseTime = fast;
	private static final int basicDirtSensorCost = free;

	private static final int basicForwardMoverCost = free;
	private static final int basicMoveTime = fast;

	private static final int basicRotateTime = normalSpeed;
	private static final int basicRotatorCost = free;

	private static final int basicSidewaysMoverCost = middle;
	private static final int basicSideWaysMovetime = fast;

	private static final int basicWallSenseTime = fast;
	private static final int basicWallSensorCost = middle;
	private static final int basicWallSensorDepth = 1;

	private static final int dirtExplosionCost = middle;
	private static final int dirtExplosionTime = instant;

	private static final int frogForwardMoverCost = expensive;
	private static final int frogMoveTime = instant;

	private static final int highProbDirtSenseTime = faster;
	private static final int highProbDirtSensorCost = middle;

	private static final int jackieChanRotateTime = instant;
	private static final int jackieChanRotatorCost = middle;

	private static final int jumpForwardMoverCost = middle;
	private static final long jumpForwardMoveTime = fast;

	private static final int laserDirtInFrontSensorTime = fast;
	private static final int laserDirtSenseTime = instant;
	private static final int laserDirtSensorCost = expensive;

	private static final int lowProbDirtSenseTime = faster;
	private static final int lowProbDirtSensorCost = cheap;

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// Handling this exception is hard.
			// There are two reasons why it can be thrown
			// 1. the platform is shutting down.
			// 2. cheating - another thread is started which interrupts the
			// agent thread.
			// Cheating is ignored since starting a new thread is not allowed
			// anyway.
			// * If ignored, the agent will not stop.
			// * If an Error or other RuntimeException is thrown, it is reported
			// as an error.
			// We hijack the Agent.Interrupted exception as a middleway.
			// The agent stops, not reported as an error, semantically pretty
			// close.
			throw new Agent.Interrupted();
		}
	}
}
