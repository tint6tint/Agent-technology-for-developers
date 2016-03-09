package fi.jyu.ties454.cleaningAgents.infra;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import fi.jyu.ties454.cleaningAgents.agent.GameAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class ReflectiveRun {
	/**
	 * This main method was more of an experiment to find the agents in a
	 * package automatically. This was later replaced by the Game class and
	 * individual Run classes for each setup.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		int cleaningGroup = 0;
		int soilingGroup = 0;
		InputStream is = Game.class.getResourceAsStream("map3.txt");
		Floor map = Floor.readFromReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		Random r = new Random(467545L);
		String partsShopClass = DefaultDevices.class.getName();
		if (args.length > 0) {
			map = Floor.readFromFile(new File(args[0]));
		}
		if (args.length > 1) {
			cleaningGroup = Integer.parseInt(args[1]);
		}
		if (args.length > 2) {
			soilingGroup = Integer.parseInt(args[2]);
		}
		if (args.length > 3) {
			r = new Random(Long.parseLong(args[3]));
		}
		if (args.length > 4) {
			partsShopClass = args[4];
		}

		List<GameAgent> cleaners = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			String cleaningAgentClass = "fi.jyu.ties454.assignment3.group" + cleaningGroup + ".cleaning.Agent" + i;
			GameAgent agent = (GameAgent) Class.forName(cleaningAgentClass).newInstance();
			cleaners.add(agent);
		}

		List<GameAgent> soilers = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			String soilingAgentClass = "fi.jyu.ties454.assignment3.group" + soilingGroup + ".soiling.Agent" + i;
			GameAgent agent = (GameAgent) Class.forName(soilingAgentClass).newInstance();
			soilers.add(agent);
		}

		PartsShop ps = new PartsShop(Class.forName(partsShopClass));

		Properties pp = new Properties();
		pp.setProperty(Profile.GUI, Boolean.TRUE.toString());
		Profile p = new ProfileImpl(pp);
		AgentContainer ac = jade.core.Runtime.instance().createMainContainer(p);
		Manager manager = new Manager(cleaners, soilers, map, ps, r, 20);
		GUI gui = new GUI();
		manager.addListener(gui);
		try {
			ac.acceptNewAgent(Manager.AID.getLocalName(), manager).start();
		} catch (StaleProxyException e) {
			throw new Error(e);
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				gui.setVisible(true);
			}
		});
		gui.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
}
