package fi.jyu.ties454.yajiliu.assignment2.task3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Auctioneer extends Agent {
	private Set<AID> bidders = new HashSet();
	// choose Dutch auction type
	private int price = 1001;
	private boolean getOffer = false;

	@Override
	protected void setup() {

		System.out.println("Hello! Auctioneer " + getAID().getName() + " is ready.");

		addBehaviour(new UpdateSlavesBehaviour(this, 1000));



		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage recMessage = receive();
				if (recMessage != null) {
					getOffer = true;
					String senderName = recMessage.getSender().getName();
					System.out.println(getName() + " receive message from " + senderName);
					String rec = recMessage.getContent();
					System.out.println("receive message is " + rec);
					int temp = Integer.parseInt(rec);
					ACLMessage sendMessage = new ACLMessage(ACLMessage.SUBSCRIBE);
					sendMessage.setContent(
							"Winner is " + recMessage.getSender().getName() + "with price " + Double.toString(temp));
					for (AID aid : bidders) {
						sendMessage.addReceiver(aid);
						
					}
					send(sendMessage);
					removeBehaviour(this);
				}

			}
		});

	}

	private class UpdateSlavesBehaviour extends TickerBehaviour {

		private static final long serialVersionUID = 1L;

		public UpdateSlavesBehaviour(Agent a, long time) {
			super(a, time);
		}

		@Override
		protected void onTick() {
			// Update the list of bidder agents
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("bidder");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
				bidders = new HashSet<AID>();
				for (int i = 0; i < result.length; ++i) {
					bidders.add(result[i].getName());
					
				}
				if (bidders.size() == 20) {
					this.myAgent.removeBehaviour(this);
					addBehaviour(new PriceAnnouncement(myAgent, 300));
				}
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}

	}

	private class PriceAnnouncement extends TickerBehaviour {

		private static final long serialVersionUID = 1L;

		public PriceAnnouncement(Agent a, long period) {
			super(a, period);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onTick() {
			ACLMessage sendMessage = new ACLMessage(ACLMessage.INFORM);
			sendMessage.setContent(Integer.toString(price));

			for (AID aid : bidders) {
				sendMessage.addReceiver(aid);
			}
			send(sendMessage);
			price--;
			System.out.println(price);
			if (getOffer) {
				this.myAgent.removeBehaviour(this);
			}
		}

	}

}
