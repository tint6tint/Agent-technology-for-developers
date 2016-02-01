package fi.jyu.ties454.yajiliu.assignment1.task5;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

public class Broker extends Agent {

	@Override
	protected void setup() {
		Map<String, Integer> map = new HashMap<String, Integer>();

			addBehaviour(new CyclicBehaviour() {
				@Override
				public void action() {
					// TODO Auto-generated method stub
					ACLMessage recMessage = blockingReceive();
					if (recMessage.getPerformative() == ACLMessage.INFORM) {
						String receiveJsonMessage = recMessage.getContent();
						Message temp = new Message();
						ObjectMapper mapper = new ObjectMapper();
						try {
							temp = mapper.readValue(receiveJsonMessage, Message.class);
							// System.out.println("topic
							// received"+temp.getTopic());
							// System.out.println("content
							// received"+temp.getContent());
							String topic = temp.getTopic();
							int content = temp.getContent();
							map.put(topic, content);
							/*
							 * if(map.size()>= 26) {
							 * if(!map.containsKey(topic)){ String[]
							 * topics={"DTIME", "P", "EXPTIME", "NTIME", "NP",
							 * "NEXPTIME", "DSPACE", "L", "PSPACE", "EXPSPACE",
							 * "NSPACE", "NL", "NPSPACE", "NEXPSPACE"}; int
							 * topicSeq=(int)(Math.random() *
							 * (topics.length-1)); String key=topics[topicSeq];
							 * map.remove(key); map.put(topic, content); }else{
							 * map.replace(topic, content); } }else {
							 * map.put(topic,content); }
							 */
							/*
							 * for (Map.Entry<String, Integer> entry :
							 * map.entrySet()) { String key = entry.getKey();
							 * int value = entry.getValue(); System.out.println(
							 * "TOPIC: "+key+" CONTENT: "+value); }
							 */
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else if (recMessage.getPerformative() == ACLMessage.CFP) {
						addBehaviour(new WakerBehaviour(myAgent, 5000) {
							protected void handleElapsedTimeout() {
								// perform operation X
								String topicPicker = recMessage.getContent();
								int updatedContent = map.get(topicPicker);
								Message temp = new Message();
								temp.setTopic(topicPicker);
								temp.setContent(updatedContent);
								ACLMessage replyContentToSubscriber = new ACLMessage(ACLMessage.INFORM);
								replyContentToSubscriber.addReceiver(new AID("Subscribers", AID.ISLOCALNAME));

								ObjectMapper mapper = new ObjectMapper();
								String jsonInString;
								try {
									jsonInString = mapper.writeValueAsString(temp);
									replyContentToSubscriber.setContent(jsonInString);
								} catch (JsonProcessingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								send(replyContentToSubscriber);
							}
						});

					} else {
						System.out.println("didnt work");
						block();
					}
				}

			});
		

	}
}
