package fi.jyu.ties454.yajiliu.assignment2.task3;

import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Bidder extends Agent {

	private static MessageTemplate price = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	private static MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);

	@Override
	protected void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("bidder");
		sd.setName("bidders-offer");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		int max_amount = randomiser();
		System.out.println("Bidder " + getName() + "price" + max_amount);

		addBehaviour(new CyclicBehaviour() {

			@Override
			public void action() {
				ACLMessage recMessage = receive(price);
				ACLMessage result = receive(mt);

				// TODO Auto-generated method stub
				// Register the book-selling service in the yellow pages
				if (recMessage != null) {
					String senderName = recMessage.getSender().getName();
					System.out.println(getName() + " receive message from " + senderName);
					String rec = recMessage.getContent();
					System.out.println("receive message is " + rec);
					int temp = Integer.parseInt(rec);
					if (temp <= max_amount) {
						ACLMessage sendMessage = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
						sendMessage.setContent(Integer.toString(temp));
						sendMessage.addReceiver(new AID("Auctioneer", AID.ISLOCALNAME));
						send(sendMessage);
					}
				}
				if (result != null) {
					String senderName = result.getSender().getName();
					System.out.println(getName() + " receive message from " + senderName);
					String rec = result.getContent();
					System.out.println("receive message is " + rec);
					doDelete();
				}
				if (result == null && recMessage == null) {
					// maybe nothing?
					block();
				}

			}
		});

	}

	int randomiser() {
		Random r = new Random();
		return r.nextInt(500) + 500;
	}

	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Close the GUI

		// Printout a dismissal message
		System.out.println("Bidder" + getAID().getName() + " terminating");
	}
}
