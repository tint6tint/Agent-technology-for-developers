package fi.jyu.ties454.cleaningAgents.infra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;

import fi.jyu.ties454.cleaningAgents.agent.GameAgent;
import fi.jyu.ties454.cleaningAgents.infra.Floor.FloorUpdateListener;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class Manager extends Agent {

	public interface Listener {
		void processUpdates(int cleanersBudget, int soilersBudget, Map<String, AgentState> cleaners,
				Map<String, AgentState> soilers, Floor map);

		default void agentStateUpdate(int cleanersBudget, int soilersBudget, Map<String, AgentState> cleaners,
				Map<String, AgentState> soilers, Floor map) {
			this.processUpdates(cleanersBudget, soilersBudget, cleaners, soilers, map);
		}

		default void floorUpdate(int cleanersBudget, int soilersBudget, Map<String, AgentState> cleaners,
				Map<String, AgentState> soilers, Floor map) {
			this.processUpdates(cleanersBudget, soilersBudget, cleaners, soilers, map);
		}

		default void budgetUpdate(int cleanersBudget, int soilersBudget, Map<String, AgentState> cleaners,
				Map<String, AgentState> soilers, Floor map) {
			this.processUpdates(cleanersBudget, soilersBudget, cleaners, soilers, map);
		}

		/**
		 * Indicates that the game ended. The final score is the average dirty
		 * area on the map, measures once per second.
		 *
		 * @param score
		 */
		void gameEnded(double score);
	}

	private final LinkedList<Listener> listeners = new LinkedList<>();

	public void addListener(Listener l) {
		this.listeners.add(l);
		l.processUpdates(this.cleanersBudget.get(), this.soilersBudget.get(), this.cleaners, this.soilers, this.map);
	}

	public static final String DEVICE_ACQUISITION_PROTOCOL = "device-acquisition-protocol";
	private static final long serialVersionUID = 1L;
	public static final int initialBudget = 12000;
	public static final AID AID = new AID("Manager", false);
	private final int gameLength;
	private final AtomicInteger cleanersBudget = new AtomicInteger(Manager.initialBudget);
	private final AtomicInteger soilersBudget = new AtomicInteger(Manager.initialBudget);
	private final Map<String, AgentState> cleaners = new HashMap<>();
	private final Map<String, AgentState> soilers = new HashMap<>();
	private final Floor map;
	private final PartsShop partsShop;

	public Manager(List<GameAgent> cleaners, List<GameAgent> soilers, Floor map, PartsShop partsShop, Random rand,
			int gameLength) throws Exception {
		this.map = map;
		this.partsShop = partsShop;
		this.gameLength = gameLength;
		System.out.println("Starting game with the following map:");
		System.out.println(map.toString());

		AgentState.Listener l = new AgentState.Listener() {

			@Override
			public void changed() {
				Manager.this.listeners.forEach(li -> li.agentStateUpdate(Manager.this.cleanersBudget.get(),
						Manager.this.soilersBudget.get(), Manager.this.cleaners, Manager.this.soilers, map));
			}
		};

		map.addListener(new FloorUpdateListener() {

			@Override
			public void update() {
				Manager.this.listeners.forEach(li -> li.floorUpdate(Manager.this.cleanersBudget.get(),
						Manager.this.soilersBudget.get(), Manager.this.cleaners, Manager.this.soilers, map));
			}
		});

		System.out.println("Adding the following cleaners:");
		Location cleanersStartLocation = map.getRandomLocation(rand);
		Orientation cleanersStartOrientation = Orientation.random(rand);
		for (int i = 0; i < cleaners.size(); i++) {
			GameAgent agent = cleaners.get(i);
			AgentState state = new AgentState(agent, cleanersStartLocation, cleanersStartOrientation);
			state.addListener(l);
			String agentName = "Cleaner" + (i + 1);
			this.cleaners.put(agentName, state);
			System.out.println(agentName + " at " + cleanersStartLocation + " oriented " + cleanersStartOrientation
					+ " class " + agent.getClass().getName());
		}
		System.out.println("And the following soilers:");
		for (int i = 0; i < soilers.size(); i++) {
			GameAgent agent = soilers.get(i);
			Location loc = map.getRandomLocation(rand);
			Orientation o = Orientation.random(rand);
			AgentState state = new AgentState(agent, loc, o);
			state.addListener(l);
			String agentName = "Soiler" + (i + 1);
			this.soilers.put(agentName, state);
			System.out.println(agentName + " at " + loc + " oriented " + o + " class " + agent.getClass().getName());
		}
		System.out.println("Following devices are available in the partsShop");
		System.out.println(this.partsShop);
		System.out.println("Let's get started...");
		System.out.println();
	}

	@Override
	protected void setup() {
		AgentContainer ac = this.getContainerController();
		try {
			for (Entry<String, AgentState> agentDescription : Iterables.concat(this.cleaners.entrySet(),
					this.soilers.entrySet())) {
				ac.acceptNewAgent(agentDescription.getKey(), agentDescription.getValue().agent).start();
			}
		} catch (StaleProxyException e) {
			throw new Error(e);
		}

		this.addBehaviour(new TickerBehaviour(this, 1000) {

			private static final long serialVersionUID = 1L;

			{
				super.setFixedPeriod(true);
			}

			List<Double> fractions = new ArrayList<>(Manager.this.gameLength);

			@Override
			protected void onTick() {
				if (this.getTickCount() < Manager.this.gameLength) {
					this.fractions.add(Manager.this.map.dirtyFraction());
				} else {
					this.stop();
				}

			}

			@Override
			public int onEnd() {
				System.out.println("The game is over.");
				Double averageDirtyness = this.fractions.stream().collect(Collectors.averagingDouble(d -> d));
				System.out.println("Average dirtyness was : " + averageDirtyness);
				try {
					ac.getPlatformController().kill();
					Manager.this.listeners.forEach(l -> l.gameEnded(averageDirtyness));
				} catch (ControllerException e) {
					throw new Error(e);
				}
				return super.onEnd();
			}
		});

		this.addBehaviour(new CyclicBehaviour() {

			private static final long serialVersionUID = 1L;

			MessageTemplate t = MessageTemplate.and(

					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchProtocol(Manager.DEVICE_ACQUISITION_PROTOCOL)

			);

			@Override
			public void action() {
				ACLMessage msg = Manager.this.receive(this.t);
				if (msg != null) {
					String deviceName = msg.getContent();
					String content;
					int performative;
					if (Manager.this.partsShop.partExists(deviceName)) {
						int price = Manager.this.partsShop.getPrice(deviceName);
						AtomicInteger budget;
						AgentState state;
						String sender = msg.getSender().getLocalName();
						if ((state = Manager.this.cleaners.get(sender)) != null) {
							budget = Manager.this.cleanersBudget;
						} else if ((state = Manager.this.soilers.get(sender)) != null) {
							budget = Manager.this.soilersBudget;
						} else {
							performative = ACLMessage.FAILURE;
							content = "Agent with name " + msg.getSender() + " not known to manager.";
							ACLMessage reply = msg.createReply();
							reply.setPerformative(performative);
							reply.setContent(content);
							Manager.this.send(reply);
							return;
						}
						if (budget.addAndGet(-price) >= 0) {
							// successfull
							List<AgentState> others = new LinkedList<>();
							Manager.this.cleaners.entrySet().stream().filter(e -> !e.getKey().equals(sender))
									.map(e -> e.getValue()).forEach(others::add);
							Manager.this.soilers.entrySet().stream().filter(e -> !e.getKey().equals(sender))
									.map(e -> e.getValue()).forEach(others::add);
							Manager.this.partsShop.attachPart(deviceName, Manager.this.map, state, others);
							Manager.this.listeners.forEach(li -> li.budgetUpdate(Manager.this.cleanersBudget.get(),
									Manager.this.soilersBudget.get(), Manager.this.cleaners, Manager.this.soilers,
									Manager.this.map));

							performative = ACLMessage.AGREE;
							content = "on it's way";
						} else {
							budget.addAndGet(price);
							performative = ACLMessage.REFUSE;
							content = "Not enough budget";
						}
					} else {
						performative = ACLMessage.FAILURE;
						content = "Part does not exist in the partsShop";
					}
					ACLMessage reply = msg.createReply();
					reply.setPerformative(performative);
					reply.setContent(content);
					Manager.this.send(reply);
				} else {
					this.block();
				}
			}
		});
	}
}
