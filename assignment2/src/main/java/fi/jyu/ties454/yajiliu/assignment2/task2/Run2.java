package fi.jyu.ties454.yajiliu.assignment2.task2;
import jade.util.leap.Properties;

import jade.wrapper.AgentContainer;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;

public class Run2 {
	public static void main(String[] args) {
	    Properties pp = new Properties();
	    pp.setProperty(Profile.MAIN, Boolean.FALSE.toString());
	    pp.setProperty(Profile.MAIN_HOST, "127.0.0.1");
	    pp.setProperty(Profile.MAIN_PORT, Integer.toString(1099));
	    Profile p = new ProfileImpl(pp);
	    AgentContainer ac = jade.core.Runtime.instance().createAgentContainer(p);
	    try {
	        ac.acceptNewAgent("JumpingAgent", new JumpingAgent()).start();
	       
	    } catch (StaleProxyException e) {
	        throw new Error(e);
	    }
	}
}
