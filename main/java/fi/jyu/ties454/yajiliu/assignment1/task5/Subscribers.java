package fi.jyu.ties454.yajiliu.assignment1.task5;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class Subscribers extends Agent {

	Message updatedContentFromPublisher = new Message();

	@Override
	protected void setup() {
		String[] topics = { "DTIME", "P", "EXPTIME", "NTIME", "NP", "NEXPTIME", "DSPACE", "L", "PSPACE", "EXPSPACE",
				"NSPACE", "NL", "NPSPACE", "NEXPSPACE" };

		int topicSeq = (int) (Math.random() * (topics.length - 1));
		String topic = topics[topicSeq];
		updatedContentFromPublisher.setTopic(topic);

		ACLMessage sendMessage = new ACLMessage(ACLMessage.SUBSCRIBE);
		sendMessage.addReceiver(new AID("Broker", AID.ISLOCALNAME));
		sendMessage.setContent(updatedContentFromPublisher.getTopic());
		send(sendMessage);
		// TODO Auto-generated method stub

		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage recMessage = receive();
				if (recMessage != null) {
					int updatedContent = Integer.parseInt(recMessage.getContent());
					updatedContentFromPublisher.setContent(updatedContent);
				}else{
					block();
				}
			}
		});

		addBehaviour(new TickerBehaviour(this,5000) {
			@Override
			protected void onTick() {
				// TODO Auto-generated method stub
				System.out.println(getName());
				System.out.println("topic= " + updatedContentFromPublisher.getTopic());
				System.out.println("content received updated= " + updatedContentFromPublisher.getContent());
			}
		});
	}
}
