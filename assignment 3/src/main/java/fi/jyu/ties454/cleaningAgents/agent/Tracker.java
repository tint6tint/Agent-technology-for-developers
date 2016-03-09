package fi.jyu.ties454.cleaningAgents.agent;

import fi.jyu.ties454.cleaningAgents.actuators.BackwardMover;
import fi.jyu.ties454.cleaningAgents.actuators.ForwardMover;
import fi.jyu.ties454.cleaningAgents.actuators.Rotator;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices;
import fi.jyu.ties454.cleaningAgents.infra.Location;
import fi.jyu.ties454.cleaningAgents.infra.Orientation;

/**
 * This class has not been extensively tested, please report any strange
 * behavior.
 * 
 * You can use a Tracker to keep track of the location of your agent
 * <b>relative</b> to the place where it started. In order to get correct
 * results, you must register all {@link ForwardMover}s, {@link BackwardMover}s
 * and {@link Rotator}s and only use the ones returned by the register methods
 * otherwise the Tracker will not be able to track actions on the actuator.
 * 
 * Beware, the location reported by the tracker is relative to the starting
 * position, taken to be {@link Location#Location(int, int)} (0,0) and the orientation is relative to the start
 * orientation {@link Orientation#N}.
 * 
 * 
 * @author michael
 *
 */
public class Tracker {

	/**
	 * The location and orientation relative to the place where the agent
	 * started
	 */
	private Location l = new Location(0, 0);
	private Orientation o = Orientation.N;

	public ForwardMover registerForwardMover(ForwardMover m) {
		if (m instanceof DefaultDevices.LeftMover) {
			throw new Error("A leftmover must be registered with Tracker.registerLeftMover()");
		}
		return new ForwardMover() {

			@Override
			public int move() {
				int distance = m.move();
				l = l.nStep(o, distance);
				return distance;
			}
		};
	}

	public BackwardMover registerBackwardMover(BackwardMover m) {
		return new BackwardMover() {

			@Override
			public int move() {
				int distance = m.move();
				l = l.nStep(o, -distance);
				return distance;
			}
		};
	}

	public ForwardMover registerLeftMover(DefaultDevices.LeftMover m) {
		return new ForwardMover() {

			@Override
			public int move() {
				int distance = m.move();
				l = l.nStep(o.ccw(), distance);
				return distance;
			}
		};
	}

	public Rotator registerRotator(Rotator r) {
		return new Rotator() {

			@Override
			public void rotateCW() {
				r.rotateCW();
				o = o.cw();
			}

			@Override
			public void rotateCCW() {
				r.rotateCCW();
				o = o.ccw();
			}
		};
	}

	public Location getLocation() {
		return l;
	}

	public Orientation getOrientation() {
		return o;
	}

}
