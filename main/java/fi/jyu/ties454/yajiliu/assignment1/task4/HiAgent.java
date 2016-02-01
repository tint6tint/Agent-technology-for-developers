package fi.jyu.ties454.yajiliu.assignment1.task4;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class HiAgent extends Agent {
	int reponseCounter = 0;

	@Override
	protected void setup() {
		System.out.println("Hi agent world!" + getName());
		addBehaviour(new CyclicBehaviour() {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				addBehaviour(new WakerBehaviour(myAgent, 5000) {
					protected void handleElapsedTimeout() {
						ACLMessage recMessage = blockingReceive();
						if (recMessage != null) {
							String senderName = recMessage.getSender().getName();
							System.out.println("receive message from " + senderName);
							String rec = recMessage.getContent();
							System.out.println("receive message is " + rec);
							System.out.println("receive message reply with " + recMessage.getReplyWith());						
							
							ACLMessage reply = recMessage.createReply();		
							send(reply);							
							System.out.println("hi sent"+ reply.getInReplyTo());
						} else {
							System.out.println("hi null exception");
							block();
							
						}
					}
					
				});
			}
		});
		

	}

}
