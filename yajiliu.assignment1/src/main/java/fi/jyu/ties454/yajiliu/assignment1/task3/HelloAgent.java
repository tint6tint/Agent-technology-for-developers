package fi.jyu.ties454.yajiliu.assignment1.task3;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;


public class HelloAgent extends Agent {
	private int n=0;
	@Override
	protected void setup(){
		
		ACLMessage sendMessage = new ACLMessage(ACLMessage.INFORM);
		System.out.println("Hello agent world!"+ getName());
		
		addBehaviour(new TickerBehaviour(this,5000){
				protected void onTick(){
					String hello= "Hello"+n;
					String language= "English";
					sendMessage.setContent(hello);
					sendMessage.setLanguage(language);
					send(sendMessage);
					sendMessage.addReceiver(new AID("Hi", AID.ISLOCALNAME));
					System.out.println(sendMessage.getContent());
					n++;
				}
			}				
		);
		addBehaviour(new WakerBehaviour(this, 20000) {
			protected void handleElapsedTimeout() {
				 	takeDown();
			}			
		});
		
		addBehaviour(new CyclicBehaviour() {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage recMessage = receive();				
				if(recMessage!=null){
					String senderName= recMessage.getSender().getName();
					System.out.println("receive message from " + senderName);
					String rec =recMessage.getContent();
					System.out.println("receive message is " + rec);
				}else{
					block();
				}
				
			}
		});
		/*		
		ACLMessage recMessage = blockingReceive();
		String senderName= recMessage.getSender().getName();
		System.out.println("receive message from " + senderName);
		if(recMessage!=null){
			String rec =recMessage.getContent();
			System.out.println("receive message is " + rec);			
		}*/
	}
	protected void takeDown() {
		super.doDelete();
		//takeDown();
		 // Printout a dismissal message
		 System.out.println("agent hello " + getAID().getName()+"terminating.");
	}
}
