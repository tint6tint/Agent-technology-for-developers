package fi.jyu.ties454.yajiliu.assignment2.task3;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class Run {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Properties pp = new Properties();
		pp.setProperty(Profile.GUI, Boolean.TRUE.toString());
		Profile p = new ProfileImpl(pp);
		AgentContainer ac = jade.core.Runtime.instance().createMainContainer(p);
		try {
			ac.acceptNewAgent("Auctioneer", new Auctioneer());
			for (int i = 0; i < 20; i++) {
				ac.acceptNewAgent("Bidder" + i, new Bidder()).start();
			}			
			try {
				ac.getAgent("Auctioneer").start();
			} catch (ControllerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (StaleProxyException e) {
			throw new Error(e);
		}
	}

}
