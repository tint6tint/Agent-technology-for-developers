package fi.jyu.ties454.cleaningAgents.infra;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import com.google.common.base.Joiner;

import fi.jyu.ties454.cleaningAgents.infra.Manager.Listener;

public class GUI extends JFrame implements Listener {

	private static final long serialVersionUID = 1L;

	private final JTextArea output;

	private List<String> lines;

	public GUI() {
		this.output = new JTextArea(25, 80);
		this.output.setFont(new Font("monospaced", Font.PLAIN, 30));

		this.output.setEditable(false);
		this.add(new JScrollPane(this.output));
		this.pack();
	}

	private static final Joiner j = Joiner.on('\n');

	@Override
	public void floorUpdate(int cleanersBudget, int soilersBudget, Map<String, AgentState> cleaners,
			Map<String, AgentState> soilers, Floor map) {

		this.lines = map.writeToStringList();
		String text = j.join(this.lines);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GUI.this.output.setText(text);
			}
		});
		// when the text is replaced, highlights seem gone!

		this.forceUpdateHighLights(cleaners, soilers);
	}

	@Override
	public void processUpdates(int cleanersBudget, int soilersBudget, Map<String, AgentState> cleaners,
			Map<String, AgentState> soilers, Floor map) {
		this.floorUpdate(cleanersBudget, soilersBudget, cleaners, soilers, map);
	}

	private final static Color cleanerColor = new Color(Color.GREEN.getRed(), Color.GREEN.getGreen(),
			Color.GREEN.getBlue(), 100);
	private final static Color soilerColor = new Color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(),
			100);

	private final static DefaultHighlightPainter cleanerHighLighter = new DefaultHighlighter.DefaultHighlightPainter(
			cleanerColor);
	private final static DefaultHighlightPainter soilerHighLighter = new DefaultHighlighter.DefaultHighlightPainter(
			soilerColor);

	private final Map<String, Object> agentHighligths = new HashMap<>();

	private final Map<String, Location> previousLocation = new HashMap<>();

	private void highlight(String agentName, Location l, DefaultHighlightPainter h) {
		Location prev = this.previousLocation.get(agentName);
		if ((prev != null) && prev.equals(l)) {
			return;
		}
		this.previousLocation.put(agentName, l);

		int characterNumer = 0;
		for (int i = 0; i < l.Y; i++) {
			// skip line
			characterNumer += this.lines.get(i).length() + 1; // characters + \n
		}
		characterNumer += l.X;

		final int thecharachter = characterNumer;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					Object highLight = GUI.this.agentHighligths.get(agentName);
					if (highLight == null) {
						GUI.this.agentHighligths.put(agentName,
								GUI.this.output.getHighlighter().addHighlight(thecharachter, thecharachter + 1, h));
					} else {
						GUI.this.output.getHighlighter().changeHighlight(highLight, thecharachter, thecharachter + 1);
					}
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

	}

	private void forceUpdateHighLights(Map<String, AgentState> cleaners, Map<String, AgentState> soilers) {
		this.previousLocation.clear();
		this.updateHighLights(cleaners, soilers);
	}

	private void updateHighLights(Map<String, AgentState> cleaners, Map<String, AgentState> soilers) {
		for (Entry<String, AgentState> agent : cleaners.entrySet()) {
			this.highlight(agent.getKey(), agent.getValue().getLocation(), cleanerHighLighter);
		}

		for (Entry<String, AgentState> agent : soilers.entrySet()) {
			this.highlight(agent.getKey(), agent.getValue().getLocation(), soilerHighLighter);
		}
	}

	@Override
	public void agentStateUpdate(int cleanersBudget, int soilersBudget, Map<String, AgentState> cleaners,
			Map<String, AgentState> soilers, Floor map) {

		this.updateHighLights(cleaners, soilers);
	}

	@Override
	public void gameEnded(double score) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				JOptionPane.showMessageDialog(GUI.this,
						String.format("Game Ended. Average percentage of dirt : %.2f", score * 100));
				// this.setVisible(false);
				// this.dispose();

			}
		});

	}
}
