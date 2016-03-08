package fi.jyu.ties454.yajiliu.assignment2.task1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class SearchAgent extends Agent {
	AID adviser;
	protected void setup() {
		// Printout a welcome message
		System.out.println(" Searching " + getAID().getName() + "starts");
		addBehaviour(new WakerBehaviour(this, 1000) {
			protected void handleElapsedTimeout() {
				// perform operation X
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Cloth adviser");
				template.addServices(sd);
				try {
					DFAgentDescription[] resultOfAdviser = DFService.search(myAgent, template);
					adviser= resultOfAdviser[0].getName();
				} catch (FIPAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				ACLMessage recMessage = blockingReceive();
				if (recMessage != null) {
					String senderName = recMessage.getSender().getName();
					System.out.println(getName()+" receive message from " + senderName);
					String rec = recMessage.getContent();
					System.out.println("receive message is " + rec);
					ACLMessage sendMessage = new ACLMessage(ACLMessage.INFORM);
					sendMessage.setContent(rec);
					sendMessage.addReceiver(adviser);
					send(sendMessage);
				} else {
					System.out.println("null exception");
					block();					
				}
					
				
			}
		});
	}
}
