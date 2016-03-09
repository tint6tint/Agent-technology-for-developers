package fi.jyu.ties454.assignment3.group2.task3;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import fi.jyu.ties454.cleaningAgents.actuators.ForwardMover;
import fi.jyu.ties454.cleaningAgents.actuators.Rotator;
import fi.jyu.ties454.cleaningAgents.agent.GameAgent;
import fi.jyu.ties454.cleaningAgents.agent.Tracker;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.AreaCleaner;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.BasicDirtSensor;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.BasicForwardMover;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.BasicRotator;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.JackieChanRotator;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.JumpForwardMover;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.LaserDirtSensor;
import fi.jyu.ties454.cleaningAgents.infra.Orientation;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * The agent extends from CleaningAgent, which is actually a normal JADE agent.
 * As an extra it has methods to obtain sensors and actuators.
 */
public class MyCleaner extends GameAgent {

	private static final long serialVersionUID = 1L;

	private Set<AID> slavesAIDs;
	private AID leader;

	private Role role;

	private Table<Integer, Integer, Position> myMap = TreeBasedTable.create();

	private Tracker t;

	private ForwardMover mover;
	private Rotator rotator;
	private LaserDirtSensor lds;
	private BasicDirtSensor bds;
	private AreaCleaner cleaner;
	private int rotateCounter = 1;
	public MyCleaner(Role role) {
		this.role = role;
		if (role == Role.LEADER) {
			slavesAIDs = new HashSet<AID>();
			leader = null;
		}
		t = new Tracker();
	}

	public MyCleaner() {
		this.role = Role.SLAVE;
		this.slavesAIDs = null;
		t = new Tracker();
	}

	@Override
	protected void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();

		if (role == Role.LEADER) { // register as LEADER
			sd.setType("LEADER");
			sd.setName("LEADER" + getName());
			aquireLeaderDevices();
		} else if (role == Role.SLAVE) { // register as SLAVE
			sd.setType("SLAVE");
			sd.setName("SLAVE" + getName());
			aquireSlaveDevices();
		} else {
			System.err.println("ERROR: Did you assign the roles right?");
		}

		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		if (role == Role.LEADER) {
			addBehaviour(new UpdateSlavesBehaviour(this, 1000));
		} else if (role == Role.SLAVE) {
			addBehaviour(new GetLeaderBehaviour());
			addBehaviour(new CleaningBehaviour());
		}
	}

	protected void sendCoords(AID receiver) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setLanguage("English");
		msg.addReceiver(receiver);
		try {
			msg.setContentObject(t.getLocation());
		} catch (IOException e) {
			e.printStackTrace();
		}
		send(msg);
	}

	private class ExploringBehaviour extends CyclicBehaviour {
		// collection of Coordinates

		private static final long serialVersionUID = 1L;

		private MessageTemplate respTmp = MessageTemplate.MatchProtocol("Distance");
		
		@Override
		public void onStart() {
			// find wall
			moveAndSend();	
			rotator.rotateCW();
			//find starting coner
			moveAndSend();
			//rotate and start the real cleaning
			rotator.rotateCW();
			mover.move();
			rotator.rotateCW();
		}
		
		@Override
		public void action() {
			explore();
		}

		private void explore() {

			// start exploring
			moveAndSend(); // jump until wall
			rotateCounter++;
			if (rotateCounter % 2 != 0) {
				rotator.rotateCW();
				int wall = mover.move();
				// Avoid stuck in the corner
				if (wall != 0) {
					rotator.rotateCW();
				} else {
					rotator.rotateCW();
					rotator.rotateCW();
				}
			} else {
				rotator.rotateCCW();
				int wall = mover.move();
				if (wall != 0) {
					rotator.rotateCCW();
				} else {
					rotator.rotateCCW();
					rotator.rotateCCW();
				}
			}
		}
		
		// not optimal. messaging and exploring should be placed in
		// 2 separate behaviours to speed up the process
		void moveAndSend() {
			mover.move();
			do {
				if (countDirt(scanArea()) == 0) {
					continue;
				} else {
					// inform slaves
					ACLMessage dirtPos = new ACLMessage(ACLMessage.INFORM);
					dirtPos.setProtocol("Coordinates");
					try {
						dirtPos.setContentObject(new Position(t.getLocation()));
					} catch (IOException e) {
						e.printStackTrace();
					}
					for (AID slave : slavesAIDs) {
						dirtPos.addReceiver(slave);
					}
					myAgent.send(dirtPos);
					System.out.println("DIRT at " + t.getLocation());
					// find nearest slave
					double minDistance = 1e12;
					AID nearestSlave = new AID();
					int messagesReceived = 0;
					while (messagesReceived < slavesAIDs.size()) {
						ACLMessage resp = receive(respTmp);
						if (resp != null) {
							try {
								double temp = 0.0;
								temp = (Double) resp.getContentObject();
								if (temp < minDistance) {
									minDistance = temp;
									nearestSlave = resp.getSender();
									System.out.println(myAgent.getLocalName() + ": found nearest agent: "
											+ nearestSlave.getLocalName());
								}
								messagesReceived++;

								System.out.println(myAgent.getLocalName() + ": received distance ["
										+ String.valueOf(messagesReceived) + "/" + String.valueOf(slavesAIDs.size())
										+ "] from " + resp.getSender().getLocalName());

							} catch (UnreadableException e) {
								System.err.println(myAgent.getLocalName() + "ERROR: could not read distance");
								e.printStackTrace();
							}
						} else {
							block();
						}
					} // for ()

					// send cleaning commands
					ACLMessage ack = new ACLMessage(ACLMessage.INFORM);
					ACLMessage nack = new ACLMessage(ACLMessage.INFORM);
					ack.setProtocol("CONFIRM");
					nack.setProtocol("REFUSE");
					for (AID slave : slavesAIDs) {
						if (slave.getName().equalsIgnoreCase(nearestSlave.getName())) {
							ack.addReceiver(slave);
						} else {
							nack.addReceiver(slave);
						}
					}
					myAgent.send(ack);
					myAgent.send(nack);
				}
			} while (mover.move() != 0); // until agent hits a wall
		}

		int[] scanArea() {
			int[] dirty = new int[4];
			Orientation originalValue = t.getOrientation();
			// find north
			// System.out.println(t.getOrientation());
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
			// System.out.println("suppose to rotate east "+t.getOrientation());
			if (lds.dirtInFront().get()) {
				dirty[1] = 1;
			} else {
				dirty[1] = 0;
			}

			// check south
			rotator.rotateCW();
			// System.out.println("suppose to rotate south
			// "+t.getOrientation());
			if (lds.dirtInFront().get()) {
				dirty[2] = 1;
			} else {
				dirty[2] = 0;
			}

			// check west
			rotator.rotateCW();
			// System.out.println("suppose to rotate west " +
			// t.getOrientation());
			if (lds.dirtInFront().get()) {
				dirty[3] = 1;
			} else {
				dirty[3] = 0;
			}
			while (t.getOrientation() != originalValue)
				rotator.rotateCW();

			return dirty;
		}

		int countDirt(int[] scan) {
			int dirtCount = 0;
			for (int i = 0; i < scan.length; i++) {
				dirtCount += scan[i];
			}
			return dirtCount;
		}

	}

	private class CleaningBehaviour extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		MessageTemplate coord = MessageTemplate.MatchProtocol("Coordinates");
		MessageTemplate ack = MessageTemplate.MatchProtocol("CONFIRM");
		MessageTemplate nack = MessageTemplate.MatchProtocol("REFUSE");

		@Override
		public void action() {
			// 1.
			// receive coordinates to clean
			ACLMessage dirtyPlace = receive(coord);
			Position target = null;
			if (dirtyPlace != null) {
				try {
					target = (Position) dirtyPlace.getContentObject();
					Position myPos = new Position(t.getLocation());
					// calculate distance and respond
					double distance = myPos.dist(target);

					System.out.println(myAgent.getLocalName() + ": target = " + target.toString() + " | dist = "
							+ String.valueOf(distance) + " and I am at " + myPos.toString());

					ACLMessage response = new ACLMessage(ACLMessage.INFORM);
					response.setProtocol("Distance");
					response.addReceiver(dirtyPlace.getSender());
					try {
						response.setContentObject(new Double(distance));
					} catch (IOException e) {
						e.printStackTrace();
					}
					// 2.
					// send distance of myself to the coordinates of the
					// dirty place
					myAgent.send(response);
				} catch (UnreadableException ue) {
					ue.printStackTrace();
				}

			} else {
				// or do something useful?
				block();
				return;
			}

			// 3.
			// receive command to go somewhere to clean
			ACLMessage confirmation = receive(ack);
			ACLMessage refused = receive(nack);
			if (confirmation != null) {
				System.out.println(myAgent.getLocalName() + ": I got a confirmation to clean" + target.toString());
				goAndCleanSimple(target);

			} else if (refused != null) {
				System.out.println(myAgent.getLocalName() + ": I was refused");
			} else {
				System.out.println(myAgent.getLocalName() + ": I don't know what to do");
				// or do something useful?
				block();
			}

		}

		protected void goAndCleanSimple(Position goal) {
			Position myPos = new Position(t.getLocation());
			int dx0 = 0;
			int dy0 = 0;
			int dx1 = 0;
			int dy1 = 0;
			
			int stepCounter = 0;

			// find x coordinate
			while (goal.X != myPos.X) {
				dx0 = goal.X - myPos.X;
				mover.move();
				stepCounter++;
				if (stepCounter % 3 == 0) {
					cleaner.clean();
				}
				myPos = new Position(t.getLocation());
				dx1 = goal.X - myPos.X;

				if (Math.abs(dx0) < Math.abs(dx1)) {
					// wrong direction! turn around
					rotator.rotateCW();
					rotator.rotateCW();
				} else if (Math.abs(dx0) == Math.abs(dx1)) {
					rotator.rotateCW();
				}
			}

			myPos = new Position(t.getLocation());
			// find y coordinate
			while (goal.Y != myPos.Y) {
				dy0 = goal.Y - myPos.Y;
				mover.move();
				stepCounter++;
				if (stepCounter % 3 == 0) {
					cleaner.clean();
				}
				myPos = new Position(t.getLocation());
				dy1 = goal.Y - myPos.Y;

				if (Math.abs(dy0) < Math.abs(dy1)) {
					// wrong direction! turn around
					rotator.rotateCW();
					rotator.rotateCW();
				} else if (Math.abs(dy0) == Math.abs(dy1)) {
					rotator.rotateCW();
				}
			}
			cleaner.clean();
		}

		protected void goAndCleanEfficient(Position goal) {
			Position myPos = new Position(t.getLocation());
			while (!goal.equals(myPos)) {
				// which direction to rotate?
				Orientation currentOrientation = t.getOrientation();
				int diffx = goal.X - myPos.X;
				int diffy = goal.Y - myPos.Y;
				if (Math.abs(diffx) > Math.abs(diffy)) { // go E or W
					if (diffx > 0) { // goal is east of me
						if (currentOrientation == Orientation.E) {
							;
						}
						if (currentOrientation == Orientation.N) {
							rotator.rotateCW();
						}
						if (currentOrientation == Orientation.S) {
							rotator.rotateCCW();
						}
						if (currentOrientation == Orientation.W) {
							rotator.rotateCW();
							rotator.rotateCW();
						}
					} else if (diffx < 0) { // goal is west of me
						if (currentOrientation == Orientation.W) {
							;
						}
						if (currentOrientation == Orientation.N) {
							rotator.rotateCCW();
						}
						if (currentOrientation == Orientation.S) {
							rotator.rotateCW();
						}
						if (currentOrientation == Orientation.E) {
							rotator.rotateCW();
							rotator.rotateCW();
						}
					}
				} else { // go N or S
					if (diffy > 0) { // goal is north
						if (currentOrientation == Orientation.N) {
							;
						}
						if (currentOrientation == Orientation.E) {
							rotator.rotateCW();
						}
						if (currentOrientation == Orientation.W) {
							rotator.rotateCCW();
						}
						if (currentOrientation == Orientation.S) {
							rotator.rotateCW();
							rotator.rotateCW();
						}
					} else if (diffy < 0) { // goal is south
						if (currentOrientation == Orientation.S) {
							;
						}
						if (currentOrientation == Orientation.E) {
							rotator.rotateCW();
						}
						if (currentOrientation == Orientation.W) {
							rotator.rotateCCW();
						}
						if (currentOrientation == Orientation.N) {
							rotator.rotateCW();
							rotator.rotateCW();
						}
					}
				} // rotation decision done

				mover.move();
				// cleaner.clean();
				myPos = new Position(t.getLocation());

				System.out.println(myAgent.getLocalName() + ": me = " + myPos.toString() + " goal = " + goal.toString()
						+ " dist = " + String.valueOf(myPos.dist(goal)));
				// TODO: need to set protocol for sending coordinates
				// sendCoords(leader);
			} // while (!goal.equals(myPos))

			// goal reached, clean now
			cleaner.clean();
		}
	}

	private class UpdateSlavesBehaviour extends TickerBehaviour {
		private static final long serialVersionUID = 1L;

		private boolean exploring = false;

		public UpdateSlavesBehaviour(Agent a, long time) {
			super(a, time);
		}

		@Override
		protected void onTick() {
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("SLAVE");
			dfd.addServices(sd);
			try {
				DFAgentDescription[] res = DFService.search(myAgent, dfd);
				for (int i = 0; i < res.length; i++) {
					slavesAIDs.add(res[i].getName());
				}

				// if at least one slave found -> start exploring
				if (slavesAIDs.size() > 0 && !exploring) {
					myAgent.addBehaviour(new ExploringBehaviour());
					exploring = true;
					System.out.println(myAgent.getLocalName() + ": started exploring behaviour. "
							+ "Number of slaves is " + String.valueOf(slavesAIDs.size()));
				}
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
	}

	private class GetLeaderBehaviour extends Behaviour {
		private static final long serialVersionUID = 1L;

		private boolean foundLeader = false;

		@Override
		public void action() {
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("LEADER");
			dfd.addServices(sd);
			try {
				DFAgentDescription[] res = DFService.search(myAgent, dfd);
				leader = res[0].getName();
				foundLeader = true;
				System.out.println(myAgent.getLocalName() + ": my leader is " + leader.getLocalName());
			} catch (FIPAException fe) {
				fe.printStackTrace();
			} catch (ArrayIndexOutOfBoundsException ae) {
				System.err.println(myAgent.getLocalName() + "WARNING: no leader registered");
				foundLeader = false;
			}
		}

		@Override
		public boolean done() {
			return foundLeader;
		}
	}

	protected void aquireLeaderDevices() {
		Optional<JackieChanRotator> fastRotator = this.getDevice(DefaultDevices.JackieChanRotator.class);
		Optional<JumpForwardMover> jumpFwMover = this.getDevice(DefaultDevices.JumpForwardMover.class);
		Optional<LaserDirtSensor> laserSensor = this.getDevice(DefaultDevices.LaserDirtSensor.class);

		while (!(fastRotator.isPresent() && laserSensor.isPresent() && jumpFwMover.isPresent())) {
			System.err.println(this.getLocalName() + ":: WARNING: wainting for sensors to be present");
		}

		this.mover = t.registerForwardMover(jumpFwMover.get());
		this.rotator = t.registerRotator(fastRotator.get());
		this.lds = laserSensor.get();
	}

	protected void aquireSlaveDevices() {
		Optional<AreaCleaner> aCleaner = this.getDevice(DefaultDevices.AreaCleaner.class);
		Optional<BasicForwardMover> fMover = this.getDevice(DefaultDevices.BasicForwardMover.class);
		Optional<BasicRotator> rotator = this.getDevice(DefaultDevices.BasicRotator.class);

		while (!(aCleaner.isPresent() && fMover.isPresent() && rotator.isPresent())) {
			System.err.println(this.getLocalName() + ":: WARNING: wainting for sensors to be present");
		}

		this.mover = t.registerForwardMover(fMover.get());
		this.rotator = t.registerRotator(rotator.get());
		this.cleaner = aCleaner.get();
	}
}
