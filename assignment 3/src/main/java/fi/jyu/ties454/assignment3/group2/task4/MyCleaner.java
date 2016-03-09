package fi.jyu.ties454.assignment3.group2.task4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
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
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.LaserDirtSensor;
import fi.jyu.ties454.cleaningAgents.infra.DefaultDevices.LeftMover;
import fi.jyu.ties454.cleaningAgents.infra.Location;
import fi.jyu.ties454.cleaningAgents.infra.Orientation;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
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
	private SimpleWeightedGraph<Integer, DefaultWeightedEdge> myGraph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(
			DefaultWeightedEdge.class);
	private Table<Integer, Integer, Position> doorLUT = TreeBasedTable.create();

	private Tracker t;

	private ForwardMover mover;
	private Rotator rotator;
	private LaserDirtSensor lds;
	private ForwardMover lmover;
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
			addBehaviour(new MappingBehaviour());
			// addBehaviour(new UpdateSlavesBehaviour(this, 1000));
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

	public void goToWaypoint(Position waypoint) {
		Position myPos = new Position(t.getLocation());
		int dx0 = waypoint.X - myPos.X;
		int dy0 = waypoint.Y - myPos.Y;

		do {
			if (dx0 > 0) {
				while (t.getOrientation() != Orientation.E) {
					rotator.rotateCW();
				}
			} else if (dx0 < 0) {
				while (t.getOrientation() != Orientation.W) {
					rotator.rotateCW();
				}
			}

			while (waypoint.X != t.getLocation().X && mover.move() != 0)
				;

			if (dy0 < 0) {
				while (t.getOrientation() != Orientation.N) {
					rotator.rotateCW();
				}
			} else if (dy0 > 0) {
				while (t.getOrientation() != Orientation.S) {
					rotator.rotateCW();
				}
			}

			while (waypoint.Y != t.getLocation().Y && mover.move() != 0)
				;

			myPos = new Position(t.getLocation());
			dx0 = waypoint.X - myPos.X;
			dy0 = waypoint.Y - myPos.Y;

		} while (!myPos.equals(waypoint));

	}

	private class MappingBehaviour extends Behaviour {

		private boolean mappingDone = false;

		private ArrayList<Integer> queue;
		private int roomCounter;
		private int currentRoom;

		@Override
		public void onStart() {
			queue = new ArrayList<Integer>();
			roomCounter = 1;
			currentRoom = 1;
			// queue.add(roomCounter); // add root to queue
			myGraph.addVertex(roomCounter); // first node of graph
		}

		@Override
		public void action() {
			Position[] corners = findCorners();
			addRoomToMap(corners[0], corners[1]);
			rotator.rotateCW();
			ArrayList<Position> doors = findAllDoors();

			updateGraph(doors);

			// go to next room

			if (queue.isEmpty()) {
				mappingDone = true;
				System.out.println("Mapping Behaviour done!");
				myAgent.addBehaviour(new ExploringBehaviour());
			} else {
				int nextRoom = queue.get(0);

				System.out.println("Next room is " + nextRoom);
				System.out.println("Rooms left: " + queue);
				ArrayList<Position> way = path(nextRoom);
				for (Position waypoint : way) {
					goToWaypoint(waypoint);
					mover.move();
					rotator.rotateCW();
				}
				currentRoom = nextRoom;
				queue.remove(0);
			}
		}

		@Override
		public boolean done() {
			// exploring behaviour, delete mapping behaviour
			return mappingDone;
		}

		public ArrayList<Position> path(int destinationRoom) {
			int y = t.getLocation().Y;
			int x = t.getLocation().X;
			Position loc = myMap.get(y, x);
			int currentRoom = loc.getRoom();
			ArrayList<Position> waypoints = new ArrayList<Position>();

			// get rooms in shortest path
			List<DefaultWeightedEdge> rooms = DijkstraShortestPath.findPathBetween(myGraph, currentRoom,
					destinationRoom);

			System.out.print("shortest path: ");
			System.out.println(rooms);

			// find coordinates
			// add waypoint for every room-change
			for (int i = 0; i < rooms.size(); i++) {
				int from = myGraph.getEdgeSource(rooms.get(i));
				int to = myGraph.getEdgeTarget(rooms.get(i));
				Position waypoint = doorLUT.get(from, to);
				waypoints.add(waypoint);
			}

			return waypoints;
		}

		public void updateGraph(ArrayList<Position> doors) {
			int y = t.getLocation().Y;
			int x = t.getLocation().X;
			Position loc = myMap.get(y, x);
			int currentRoom = loc.getRoom();

			for (Position door : doors) {

				if (!myMap.contains(door.Y, door.X)) {
					// new door discovered
					door.setDoorToRoom(++roomCounter);
					myMap.put(door.Y, door.X, door);
					System.out.println("updateGraph:: new door " + door.toString());

					myGraph.addVertex(roomCounter);
					myGraph.addEdge(currentRoom, roomCounter);
					System.out.println("updateGraph:: added Vertex " + roomCounter);
					System.out.println("updateGraph:: added Edge (" + currentRoom + ", " + roomCounter + ")");
					doorLUT.put(door.getRoom(), door.getDoorToRoom(), new Position(door));
					doorLUT.put(door.getDoorToRoom(), door.getRoom(), new Position(door));
					queue.add(roomCounter);
				} else {
					// if door was already in map?
					// existing door information is wrong! change graph!
					Position existingDoor = myMap.get(door.Y, door.X);

					System.out.println("updateGraph:: door was already in map! existingDoor = " + existingDoor);
					// check, if door information (roomId, doorToRoom) is
					// correct. otherwise change graph

					if (!myGraph.containsEdge(existingDoor.getRoom(), door.getRoom())) {
						System.out.println(
								"updateGraph:: edge not found: " + existingDoor.getRoom() + " to " + door.getRoom());
						// a.) remove wrong edge from graph
						myGraph.removeEdge(existingDoor.getRoom(), existingDoor.getDoorToRoom());
						doorLUT.remove(existingDoor.getRoom(), existingDoor.getDoorToRoom());
						doorLUT.remove(existingDoor.getDoorToRoom(), existingDoor.getRoom());

						// b.) remove wrong vertex
						myGraph.removeVertex(existingDoor.getDoorToRoom());
						// remove also from queue
						if (queue.contains(existingDoor.getDoorToRoom())) {
							queue.remove(queue.indexOf(existingDoor.getDoorToRoom()));
						}

						// c.) add correct edge
						myGraph.addEdge(door.getRoom(), existingDoor.getRoom());

						// d.) update myMap
						existingDoor.setDoorToRoom(door.getRoom());
						myMap.put(door.Y, door.X, new Position(existingDoor));
						doorLUT.put(existingDoor.getDoorToRoom(), existingDoor.getRoom(), new Position(existingDoor));
						doorLUT.put(existingDoor.getRoom(), existingDoor.getDoorToRoom(), new Position(existingDoor));
					}
				}
			}

		}

		public ArrayList<Position> findAllDoors() {
			Position startLocation = new Position(t.getLocation());
			Position currentLocation = new Position(startLocation);
			ArrayList<Position> doors = new ArrayList<Position>();
			int currentRoom = myMap.get(startLocation.Y, startLocation.X).getRoom();

			do {
				int wall = 1;
				do {
					wall = mover.move(); // one step fw
					if (lmover.move() != 0) {
						// we found a door
						Position newDoor = new Position(t.getLocation(), currentRoom);
						doors.add(newDoor);

						// move back to the room
						rotator.rotateCW();
						mover.move();
						rotator.rotateCCW();
					}

				} while (wall != 0);

				rotator.rotateCW();
				currentLocation = new Position(t.getLocation());
			} while (!startLocation.equals(currentLocation));

			return doors;
		}

		public Position[] findCorners() {
			Position[] corners = new Position[2];

			while (mover.move() != 0)
				; // move against wall
			rotator.rotateCW(); // turn
			while (mover.move() != 0)
				;// move to first corner
			corners[0] = new Position(t.getLocation());

			// second corner
			rotator.rotateCW();
			while (mover.move() != 0)
				;

			// third corner
			rotator.rotateCW();
			while (mover.move() != 0)
				;
			corners[1] = new Position(t.getLocation());

			return corners;
		}

		public void addRoomToMap(Position c1, Position c3) {
			if (c1.X < c3.X) {
				for (int i = c1.X; i <= c3.X; i++) {
					if (c1.Y < c3.Y) {
						for (int j = c1.Y; j <= c3.Y; j++) {
							myMap.put(j, i, new Position(j, i, currentRoom));
						}
					} else {
						for (int j = c3.Y; j <= c1.Y; j++) {
							myMap.put(j, i, new Position(j, i, currentRoom));
						}
					}
				}
			} else {
				for (int i = c3.X; i <= c1.X; i++) {
					if (c1.Y < c3.Y) {
						for (int j = c1.Y; j <= c3.Y; j++) {
							myMap.put(j, i, new Position(j, i, currentRoom));
						}
					} else {
						for (int j = c3.Y; j <= c1.Y; j++) {
							myMap.put(j, i, new Position(j, i, currentRoom));
						}
					}
				}
			}
		}
	}

	private class ExploringBehaviour extends CyclicBehaviour {
		// collection of Coordinates

		private static final long serialVersionUID = 1L;

		private MessageTemplate respTmp = MessageTemplate.MatchProtocol("Distance");

		protected ArrayList<Integer> roomIDs;

		@Override
		public void onStart() {
			System.out.println("ExploringBehaviour::onStart");
			roomIDs = new ArrayList<Integer>(myGraph.vertexSet());

		}

		@Override
		public void action() {
			for (int room : roomIDs) {
				//Get corners for the current room
				ArrayList<Position> corners=getCorners(room);
				//Move to corner with xmin and ymin
				goToWaypoint(corners.get(0));
				//explore until we reach xmax and ymax
				explore(corners.get(1));
				// go to next room

			}
		}

		private ArrayList<Position> getCorners(int roomID) {
			ArrayList<Position> corners = new ArrayList<Position>();
			Table<Integer, Integer, Position> roomCells = TreeBasedTable.create();
			for (Cell<Integer, Integer, Position> cell : myMap.cellSet()) {
				if (cell.getValue().getRoom() == roomID && cell.getValue().getDoorToRoom() == 0) {
					roomCells.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
				}
			}
			int xMin = 10000, yMin = 10000;
			int xMax = -10000, yMax = -10000;
			for (Cell<Integer, Integer, Position> cell : roomCells.cellSet()) {
				if (cell.getRowKey() > yMax) {
					yMax = cell.getRowKey();
				}
				if (cell.getRowKey() < yMin) {
					yMin = cell.getRowKey();
				}
				if (cell.getColumnKey() > xMax) {
					xMax = cell.getColumnKey();
				}
				if (cell.getColumnKey() < xMin) {
					xMin = cell.getColumnKey();
				}
			}
			corners.add(new Position(xMin, yMin));
			corners.add(new Position(xMax, yMax));
			return corners;
		}

		private void explore(Position maxCorner) {

			System.out.println("ExploringBehaviour::explore");

			// start exploring
			moveAndSend(maxCorner); // jump until wall
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
		void moveAndSend(Position maxCorner) {
			Position myPos = null;
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
				myPos = new Position(t.getLocation());
			} while (!myPos.equals(maxCorner)); // until agent hits a wall
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
		Optional<BasicForwardMover> fMover = this.getDevice(DefaultDevices.BasicForwardMover.class);
		Optional<LaserDirtSensor> laserSensor = this.getDevice(DefaultDevices.LaserDirtSensor.class);
		Optional<LeftMover> leftMover = this.getDevice(DefaultDevices.LeftMover.class);

		while (!(fastRotator.isPresent() && laserSensor.isPresent() && fMover.isPresent() && leftMover.isPresent())) {
			System.err.println(this.getLocalName() + ":: WARNING: wainting for sensors to be present");
		}

		this.mover = t.registerForwardMover(fMover.get());
		this.rotator = t.registerRotator(fastRotator.get());
		this.lds = laserSensor.get();
		this.lmover = t.registerLeftMover(leftMover.get());
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
