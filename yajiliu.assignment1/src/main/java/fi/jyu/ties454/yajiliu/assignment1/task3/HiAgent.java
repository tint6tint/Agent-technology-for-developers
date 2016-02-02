package fi.jyu.ties454.yajiliu.assignment1.task3;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

public class HiAgent extends Agent {
	int reponseCounter=0;
	@Override
	protected void setup() {
		System.out.println("Hi agent world!" + getName());
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage recMessage = blockingReceive();
				if (recMessage != null) {
					String senderName = recMessage.getSender().getName();
					System.out.println("receive message from " + senderName);
					String rec = recMessage.getContent();
					System.out.println("receive message is " + rec);
					ACLMessage reply = recMessage.createReply();
					reply.setContent("Hi"+reponseCounter);
					send(reply);
					reponseCounter++;
				} else {
					block();
				}
			}
		});

	}

}
