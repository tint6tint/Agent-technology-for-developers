package fi.jyu.ties454.yajiliu.assignment1.task5;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Broker extends Agent {

	@Override
	protected void setup() {
		Map<String, Integer> mapPublisher = new HashMap<String, Integer>();

		Map<String, AID> mapSuberscribers = new HashMap<String, AID>();

		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				// TODO Auto-generated method stub
				MessageTemplate tInForm = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				MessageTemplate tSUBSCRIBE = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
				ACLMessage recMessageFromPublisher = receive(tInForm);
				ACLMessage recMessageFromSubscriber = receive(tSUBSCRIBE);
				/*
				 * updated publisher list
				 */
				if (recMessageFromPublisher != null) {
					String receiveJsonMessage = recMessageFromPublisher.getContent();
					Message temp = new Message();
					ObjectMapper mapper = new ObjectMapper();
					try {
						temp = mapper.readValue(receiveJsonMessage, Message.class);
						String topic = temp.getTopic();
						int content = temp.getContent();
						mapPublisher.put(topic, content);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				/*
				 * updated subscriber list
				 */
				if (recMessageFromSubscriber != null) {
					String topicPicker = recMessageFromSubscriber.getContent();
					AID subscribersID = recMessageFromSubscriber.getSender();
					mapSuberscribers.put(topicPicker, subscribersID);
				}
				/*
				 * in order to keep sending the updated message so it iterated all the subscribers
				 */
				if (recMessageFromSubscriber != null && recMessageFromPublisher != null) {
					for (Map.Entry<String, AID> entry : mapSuberscribers.entrySet()) {						
						if(mapPublisher.containsKey(entry.getKey())){
							ACLMessage replyContentToSubscriber = new ACLMessage(ACLMessage.INFORM);
							replyContentToSubscriber.addReceiver(entry.getValue());
							int content= mapPublisher.get(entry.getKey());
							replyContentToSubscriber.setContent(Integer.toString(content));
							send(replyContentToSubscriber);
						}
					}
				} else {
					block();
				}
			}

		});

	}
}
