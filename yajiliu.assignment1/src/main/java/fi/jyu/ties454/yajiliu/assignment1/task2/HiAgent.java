package fi.jyu.ties454.yajiliu.assignment1.task2;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class HiAgent extends Agent{
	@Override
	protected
	void setup(){		
		ACLMessage recMessage = blockingReceive();
		System.out.println("Hi agent world!"+ getName());
		String senderName= recMessage.getSender().getName();
		System.out.println("receive message from " + senderName);
		if(recMessage!=null){
			String rec =recMessage.getContent();
			System.out.println("receive message is " + rec);
			ACLMessage reply=recMessage.createReply();
			reply.setContent("Hi");
			send(reply);
		}
		
	}
}
