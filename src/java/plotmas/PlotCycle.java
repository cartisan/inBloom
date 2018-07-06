package plotmas;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.google.common.collect.ImmutableList;

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.runtime.MASConsoleGUI;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;

/**
 * Class which facilitates running a cycle of multiple simulations.
 * @author Sven Wilke
 */
public abstract class PlotCycle implements Runnable {
	
	/**
	 * The names of the agents in this simulation.
	 */
	private String[] agentNames;
	/**
	 * The source file of the agent code.
	 */
	private String agentSrc;
	
	/**
	 * Whether the next cycle should start after the current one
	 * is finished.
	 */
	private boolean isPaused;
	
	private JFrame cycleFrame;
	private JTextArea logTextArea = new JTextArea(10, 40);
	
	/**
	 * Creates a new cycle object with specified agents.
	 * @param agentNames an array of the names of all agents
	 * @param agentSrc the name of the source file for the agent code
	 */
	protected PlotCycle(String[] agentNames, String agentSrc) {
		this.agentNames = agentNames;
		this.agentSrc = agentSrc;
		initGui();
	}
	
	private void initGui() {
		cycleFrame = new JFrame("Plot Cycle");
		cycleFrame.setLayout(new BorderLayout());
		JScrollPane scroll = new JScrollPane(logTextArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		cycleFrame.add(scroll, BorderLayout.CENTER);
		
		JButton pauseButton = new JButton("Pause");
		pauseButton.addActionListener(new ActionListener()
			{
			  	public void actionPerformed(ActionEvent e)
			  	{
			  		isPaused = !isPaused;
			  		((JButton)e.getSource()).setText(isPaused ? "Continue" : "Pause");
			  	}
			});
		
		cycleFrame.add(pauseButton, BorderLayout.SOUTH);
		
		cycleFrame.pack();
		cycleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cycleFrame.setVisible(true);
	}
	
	/**
	 * Closes and disposes the log gui.
	 */
	protected void closeGui() {
		cycleFrame.setVisible(false);
		cycleFrame.dispose();
	}
	
	/**
	 * Should be overridden by subclass.
	 * Creates new parameters for the next simulation based on
	 * the results of the previous simulation.
	 * @param er Results of the previous simulation
	 * @return ReflectResult containing parameters (launcher, personalities) for the next simulation
	 */
	protected abstract ReflectResult reflect(EngageResult er);
	/**
	 * Should be overriden by subclass.
	 * Creates parameters for the first simulation.
	 * @return ReflectResult containing parameters (launcher, personalities) for the next simulation
	 */
	protected abstract ReflectResult createInitialReflectResult();
	
	/**
	 * Runs a single simulation until it is paused (finished by Plotmas or user) or some time has passed.
	 * @param rr ReflectResult containing Personality array with length equal to agent count as well as PlotLauncher instance
	 * @return EngageResult containing the graph of this simulation and its tellability score.
	 */
	@SuppressWarnings("deprecation")
	protected EngageResult engage(ReflectResult rr) {
		PlotLauncher runner = rr.getRunner();
		Personality[] personalities = rr.getPersonalities();
		Thread t = new Thread(this.new Cycle(runner, new String[0], createAgs(runner, personalities), this.agentSrc));
		t.start();
		MASConsoleGUI.get().setPause(false);
		long startTime = System.currentTimeMillis();
		long timeout = 1900;
		while(t.isAlive()) {
			try {
				if(MASConsoleGUI.hasConsole()) {
					//MASConsoleGUI.get().getFrame().setVisible(false);
					if(!isPaused) {
						if(MASConsoleGUI.get().isPause() || System.currentTimeMillis() - startTime > timeout) {
							runner.reset();
							t.stop();
						}
					}
				}
				Thread.sleep(150);
			} catch (InterruptedException e) {
				break;
			}
		}
		PlotDirectedSparseGraph graph = new PlotDirectedSparseGraph();
		return new EngageResult(graph, PlotGraphController.getPlotListener().analyze(graph));
	}
	
	private ImmutableList<LauncherAgent> createAgs(PlotLauncher runner, Personality[] personalities) {
		if(personalities.length != this.agentNames.length) {
			throw new IllegalArgumentException("There should be as many personalities as there are agents."
					+ "(Expected: " + agentNames.length + ", Got: " + personalities.length + ")");
		}
		List<LauncherAgent> agents = new LinkedList<LauncherAgent>();
		for(int i = 0; i < agentNames.length; i++) {
			agents.add(runner.new LauncherAgent(agentNames[i], personalities[i]));
		}
		return ImmutableList.copyOf(agents);
	}
	
	/**
	 * Starts the cycle.
	 */
	@Override
	public void run() {
		ReflectResult rr = this.createInitialReflectResult();
		while(rr.shouldContinue) {
			EngageResult er = engage(rr);
			rr = this.reflect(er);
		}
		this.finish();
	}
	
	/**
	 * Can be overridden by subclass.
	 * This is called after the last simulation was run.
	 */
	protected void finish() {
	}
	
	/**
	 * Logs a message to the PlotCycle log window.
	 * '\n' is appended automatically.
	 * @param string Message to log
	 */
	protected void log(String string) {
		logTextArea.append(string + "\n");
		logTextArea.setCaretPosition(logTextArea.getText().length());
		logTextArea.repaint();
	}
	
	/**
	 * Runnable for a single simulation.
	 */
	private class Cycle implements Runnable {
		
		private PlotLauncher runner;
		private String[] args;
		private ImmutableList<LauncherAgent> agents;
		private String agSrc;
		
		Cycle(PlotLauncher runner, String[] args, ImmutableList<LauncherAgent> agents, String agSrc) {
			this.runner = runner;
			this.args = args;
			this.agents = agents;
			this.agSrc = agSrc;
		}

		@Override
		public void run() {
			try {
				runner.run(args, agents, agSrc);
			} catch (JasonException e) {
				log("JasonException!");
			}
		}
	}
	
	/**
	 * Result of the reflect method. Contains PlotLauncher instance
	 * and personalities for the next simulation.
	 * Can be extended to allow further parameters.
	 */
	public class ReflectResult {
		/**
		 * Instance of the PlotLauncher for
		 * the story in question.
		 */
		private PlotLauncher runner;
		/**
		 * Personalities which are used for the agents.
		 * Will be in used in the same order as the
		 * agentNames array passed to the PlotCycle constructor.
		 */
		private Personality[] personalities;
		/**
		 * If this is false, the cycle will not execute another
		 * simulation and call finish().
		 * runner and personalities do not matter in this case.
		 */
		private boolean shouldContinue;
		
		public ReflectResult(PlotLauncher runner, Personality[] personalities) {
			this(runner, personalities, true);
		}
		
		public ReflectResult(PlotLauncher runner, Personality[] personalities, boolean shouldContinue) {
			this.runner = runner;
			this.personalities = personalities;
			this.shouldContinue = shouldContinue;
		}
		
		public PlotLauncher getRunner() {
			return this.runner;
		}
		
		public Personality[] getPersonalities() {
			return this.personalities;
		}
		
		public boolean shouldContinue() {
			return this.shouldContinue;
		}
	}
	
	/**
	 * Result of the engage method. Contains plot graph of
	 * the last simulation and the tellability score.
	 * Can be extended to allow further return values.
	 */
	public class EngageResult {
		private PlotDirectedSparseGraph plotGraph;
		private float tellabilityScore;
		
		public EngageResult(PlotDirectedSparseGraph plotGraph, float tellabilityScore) {
			this.plotGraph = plotGraph;
			this.tellabilityScore = tellabilityScore;
		}
		
		public PlotDirectedSparseGraph getPlotGraph() {
			return this.plotGraph;
		}
		
		public float getTellability() {
			return this.tellabilityScore;
		}
	}
}
