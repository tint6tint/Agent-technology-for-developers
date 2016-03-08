package fi.jyu.ties454.yajiliu.assignment2.task1;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Adviser extends Agent {
	private String clothAdvise;
	
	public String getClothAdvise() {
		return clothAdvise;
	}

	public void setClothAdvise(String clothAdvise) {
		this.clothAdvise = clothAdvise;
	}

	@Override
	protected
	void setup(){
		System.out.println("Adviser starts");
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Cloth adviser");
		sd.setName("JadeClothAdviser");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		ACLMessage recMessage = blockingReceive();
		if (recMessage != null) {
			String senderName = recMessage.getSender().getName();
			System.out.println(getName()+" receive message from " + senderName);
			String rec = recMessage.getContent();
			System.out.println("receive message is " + rec);
			double temp= Double.parseDouble(rec);
			System.out.println(advise(temp));
		}else {
			System.out.println("null exception");			
		}
		
	}
	private String advise(double temperature){
		String clothAdviser;
		if(temperature<0){
			clothAdviser="Jacket";
		}else if(temperature==0){
			clothAdviser="coat";
		}else{
			clothAdviser="Feel free to wear something cool";
		}
		return clothAdviser;
	}
}
