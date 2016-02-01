package fi.jyu.ties454.yajiliu.assignment1.task5;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class Subscribers extends Agent {
	@Override
	protected void setup() {
		String[] topics = { "DTIME", "P", "EXPTIME", "NTIME", "NP", "NEXPTIME", "DSPACE", "L", "PSPACE", "EXPSPACE",
				"NSPACE", "NL", "NPSPACE", "NEXPSPACE" };
		Message[] publishers = new Message[50];
		for (int i = 0; i < 50; i++) {
			Message temp = new Message();
			int topicSeq = (int) (Math.random() * (topics.length - 1));
			String topic = topics[topicSeq];
			temp.setTopic(topic);
			publishers[i] = temp;
		}

		// TODO Auto-generated method stub
		addBehaviour(new TickerBehaviour(this, 20000) {
			@Override
			protected void onTick() {
				addBehaviour(new CyclicBehaviour() {
					@Override
					public void action() {
						for (int i = 0; i < 50; i++) {
							ACLMessage sendMessage = new ACLMessage(ACLMessage.CFP);
							sendMessage.addReceiver(new AID("Broker", AID.ISLOCALNAME));
							sendMessage.setContent(publishers[i].getTopic());
							send(sendMessage);
						}

						// TODO Auto-generated method stub
						ACLMessage recMessage = blockingReceive();
						String updatedContent = recMessage.getContent();
						Message temp = new Message();
						ObjectMapper mapper = new ObjectMapper();

						try {
							temp = mapper.readValue(updatedContent, Message.class);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("topic " + temp.getTopic());
						System.out.println("content received updated" + temp.getContent());

					}
				});
			}
		});
	}
}
