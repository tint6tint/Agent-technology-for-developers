package fi.jyu.ties454.yajiliu.assignment1.task5;

import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Publisher extends Agent{
	
	@Override	
	protected void setup(){	
		String[] topics={"DTIME", "P", "EXPTIME", "NTIME", "NP", "NEXPTIME", "DSPACE", "L", "PSPACE", "EXPSPACE", "NSPACE", "NL", "NPSPACE", "NEXPSPACE"};
		Message[] publishers= new Message[25];
		String[] jsonInString = new String[25];
		
		System.out.println("Publisher start"+ getName());
		ObjectMapper mapper = new ObjectMapper();
		
		addBehaviour(new OneShotBehaviour(){

			@Override
			public void action() {
				// TODO Auto-generated method stub
				for(int i=0; i<25;i++){
					Message temp= new Message();
					int topicSeq=(int)(Math.random() * (topics.length-1)); 
					String topic=topics[topicSeq];
					temp.setTopic(topic);
					temp.setContent(i);
					publishers[i]=temp;
				}
			}
			
		});
		addBehaviour(new CyclicBehaviour() {			
			@Override
			public void action() {
				for(int i=0;i<25;i++){
					try {
						jsonInString[i]= mapper.writeValueAsString(publishers[i]);
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println("string in json "+i+"th " + jsonInString[i]);
					ACLMessage sendMessage = new ACLMessage(ACLMessage.INFORM);
					sendMessage.setContent(jsonInString[i]);
					sendMessage.setReplyWith(Integer.toString(i));
					sendMessage.addReceiver(new AID("Broker", AID.ISLOCALNAME));
					send(sendMessage);
					//System.out.println("Publisher sent "+sendMessage.getContent());
					//update content
					int topicSeqUpdate=(int)(Math.random() * 100); 
					publishers[i].setContent(topicSeqUpdate);					
					//System.out.println(publishers[i].getContent());
			}
		}
		});	
				
				
	}		
			
		
		
	
}
