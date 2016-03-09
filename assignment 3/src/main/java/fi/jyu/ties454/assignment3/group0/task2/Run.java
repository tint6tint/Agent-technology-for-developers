package fi.jyu.ties454.assignment3.group0.task2;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fi.jyu.ties454.cleaningAgents.agent.GameAgent;
import fi.jyu.ties454.cleaningAgents.infra.Floor;
import fi.jyu.ties454.cleaningAgents.infra.Game;

/**
 * Class to start the simulation
 *
 * @author michael
 *
 */
public class Run {
	public static void main(String[] args) throws Exception {
		// now a clean map is loaded
		InputStream is = Run.class.getResourceAsStream("rectangleRoomClean.txt");
		if (is == null) {
			System.err.println("Did you copy the resource folder as instructed?");
			System.exit(1);
		}
		Floor map = Floor.readFromReader(new InputStreamReader(is, StandardCharsets.US_ASCII));

		List<GameAgent> cleaners = ImmutableList.of(new MyCleaner());
		// more friends to play with
		List<GameAgent> dirtiers = ImmutableList.of(new MyDirtier());

		// Create a game with the map and the cleaners. There are also
		// constructors which take more arguments. They will be used in later
		// exercises.
		Game g = new Game(map, cleaners, dirtiers);
		// Start the game. This will also show the a 'graphical' representation
		// of the state of the rooms.
		// The agent will start on a random location on the map.
		g.start();
	}
}
