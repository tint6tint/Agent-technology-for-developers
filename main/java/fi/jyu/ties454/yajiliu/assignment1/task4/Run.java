package fi.jyu.ties454.yajiliu.assignment1.task4;


import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class Run {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Properties pp = new Properties();
	    pp.setProperty(Profile.GUI, Boolean.TRUE.toString());
	    Profile p = new ProfileImpl(pp);
	    AgentContainer ac = jade.core.Runtime.instance().createMainContainer(p);
	    try {
	    	ac.acceptNewAgent("Hi", new HiAgent()).start();
	    	ac.acceptNewAgent("Hello", new HelloAgent()).start();	        
	    } catch (StaleProxyException e) {
	        throw new Error(e);
	    }
	}

}
