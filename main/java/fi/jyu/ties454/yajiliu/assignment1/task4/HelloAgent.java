package fi.jyu.ties454.yajiliu.assignment1.task4;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.introspection.AddedBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;

public class HelloAgent extends Agent {
	
	@Override
	
	protected void setup(){	
		System.out.println("Hello agent world!"+ getName());
		addBehaviour(new TickerBehaviour(this, 1000) {	
			int times=0;
			@Override
			protected void onTick() {
				// TODO Auto-generated method stub
				
				ACLMessage sendMessage = new ACLMessage(ACLMessage.INFORM);
				sendMessage.setContent("Hello for the "+ times+" times");
				sendMessage.setReplyWith(Integer.toString(times));
				sendMessage.addReceiver(new AID("Hi", AID.ISLOCALNAME));
				send(sendMessage);	
				System.out.println("hello sent "+sendMessage.getReplyWith());
				times++;
				addBehaviour(new Behaviour() {
					
					@Override
					public boolean done() {
						// TODO Auto-generated method stub
						return true;
					}
					
					@Override
					public void action() {
						// TODO Auto-generated method stub
						int receiveVariable=0;
						MessageTemplate t = MessageTemplate.MatchInReplyTo(Integer.toString(receiveVariable));
						ACLMessage msg = receive(t);
					    if (msg != null) {
					        // Message received. Process it
					    	String senderName= msg.getSender().getName();
							System.out.println("receive message from " + senderName);
							String rec =msg.getInReplyTo();
							System.out.println("receive message is " + rec);
							receiveVariable++;
							done();
					    } else {
					    	  
					          block();
					    }
					}
				});

			}		
		
		});
		
		
		

}
}