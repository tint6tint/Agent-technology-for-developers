package fi.jyu.ties454.yajiliu.assignment2.task1;

import java.io.IOException;

import org.bitpipeline.lib.owm.OwmClient;
import org.bitpipeline.lib.owm.WeatherData;
import org.bitpipeline.lib.owm.WeatherStatusResponse;
import org.json.JSONException;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class WeatherInfo extends Agent {

	@Override

	protected void setup() {
		//Register Service		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Weather info provider");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("Weather info starts");
		OwmClient owm = new OwmClient();
		WeatherStatusResponse currentWeather = null;
		try {
			currentWeather = owm.currentWeatherAtCity("Jyvaeskylae", "FI");
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		if (currentWeather.hasWeatherStatus()) {
			WeatherData weather = currentWeather.getWeatherStatus().get(0);
			System.out.println(weather.getTemp());
			double temp = (double) weather.getTemp();
			temp = temp - 273.15;	
			
			ACLMessage sendMessage = new ACLMessage(ACLMessage.INFORM);
			sendMessage.setContent(Double.toString(temp));

			sendMessage.addReceiver(new AID("SearchAgent", AID.ISLOCALNAME));
			send(sendMessage);	
			
			sd.setName(Double.toString(temp));	
			
		} else {
			System.out.println("Didnt found");
		}
	}

	

	
}
