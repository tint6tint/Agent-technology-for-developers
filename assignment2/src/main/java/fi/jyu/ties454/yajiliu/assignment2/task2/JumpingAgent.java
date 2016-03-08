package fi.jyu.ties454.yajiliu.assignment2.task2;

import java.util.ArrayList;

import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class JumpingAgent extends Agent{
	
	@Override	
	protected void setup(){	
		
		addBehaviour(new TickerBehaviour(this,3000) {
			
			
			
			@Override
			protected void onTick() {
				// TODO Auto-generated method stub

				// TODO Auto-generated method stub
				ArrayList<Location> locations= new ArrayList<>();
				locations= getLocations();
				for(Location location : locations){
					if(location.getName().equals(myAgent.here().getName())){
						continue;
					}else{
						doMove(location);
						System.out.println(location.getName());
						break;
					}
				}				
				
			
				
			}
		});
		
	}
	
	private ArrayList<Location> getLocations() {
	    // adapted from :
	    // http://www.iro.umontreal.ca/~vaucher/Agents/Jade/Mobility/ControllerAgent.java
	    getContentManager().registerLanguage(new SLCodec());
	    getContentManager().registerOntology(MobilityOntology.getInstance());

	    // Get available locations with AMS
	    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
	    request.setLanguage(new SLCodec().getName());
	    request.setOntology(MobilityOntology.getInstance().getName());
	    Action action = new Action(getAMS(), new QueryPlatformLocationsAction());
	    try {
	        getContentManager().fillContent(request, action);
	    } catch (CodecException | OntologyException e) {
	        throw new Error(e);
	    }
	    request.addReceiver(action.getActor());
	    send(request);

	    // Receive response from AMS
	    MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchSender(getAMS()),
	            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
	    ACLMessage resp = blockingReceive(mt);
	    ContentElement ce;
	    try {
	        ce = getContentManager().extractContent(resp);
	    } catch (CodecException | OntologyException e) {
	        throw new Error(e);
	    }
	    Result result = (Result) ce;
	    jade.util.leap.Iterator it = result.getItems().iterator();
	    
	    ArrayList<Location> locations = new ArrayList<Location>();
	    while (it.hasNext()) {
	        Location loc = (Location) it.next();
	        locations.add(loc);
	    }
	    return locations;
	}
}
