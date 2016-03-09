package fi.jyu.ties454.yajiliu.assignment1.task2;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;


public class HelloAgent extends Agent {
	@Override
	protected
	void setup(){
		ACLMessage sendMessage = new ACLMessage(ACLMessage.INFORM);
		System.out.println("Hello agent world!"+ getName());
		String hello= "Hello";
		String language= "English";
		sendMessage.setContent(hello);
		sendMessage.setLanguage(language);
		sendMessage.addReceiver(new AID("Hi", AID.ISLOCALNAME));
		send(sendMessage);
		ACLMessage recMessage = blockingReceive();
		String senderName= recMessage.getSender().getName();
		System.out.println("receive message from " + senderName);
		if(recMessage!=null){
			String rec =recMessage.getContent();
			System.out.println("receive message is " + rec);			
		}
	}
}
