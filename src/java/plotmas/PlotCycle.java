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

import jason.JasonException;
import jason.asSemantics.Personality;
import jason.runtime.MASConsoleGUI;
import plotmas.graph.PlotDirectedSparseGraph;
import plotmas.graph.PlotGraphController;
import plotmas.helper.EnvironmentListener;
import plotmas.helper.Tellability;

/**
 * Class which facilitates running a cycle of multiple simulations.
 * @author Sven Wilke
 */
public abstract class PlotCycle implements Runnable, EnvironmentListener {
	/**
	 * Timeout in ms before a single simulation is forcibly stopped
	 * A value of -1 means no timeout.
	 */
	protected static long TIMEOUT = -1;
	
	/**
	 * The names of the agents in this simulation.
	 */
	protected String[] agentNames;
	
	protected List<PlotDirectedSparseGraph> stories;
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
	
	protected static int currentCycle = 0;
	
	private boolean isRunning = true;
	
	/**
	 * Creates a new cycle object with specified agents.
	 * @param agentNames an array of the names of all agents
	 * @param agentSrc the name of the source file for the agent code
	 */
	protected PlotCycle(String[] agentNames, String agentSrc, boolean showGui) {
		this.agentNames = agentNames;
		this.agentSrc = agentSrc;
		stories = new LinkedList<>();
		if(showGui) {
			initGui();
		}
	}
	
	protected PlotCycle(String[] agentNames, String agentSrc) {
		this(agentNames, agentSrc, true);
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
		if(cycleFrame != null) {
			cycleFrame.setVisible(false);
			cycleFrame.dispose();
		}
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
	protected EngageResult engage(ReflectResult rr) {
		log("Engaging...");
		PlotLauncher<?,?> runner = rr.getRunner();

		Thread t = new Thread(new Cycle(runner, rr.getModel(), new String[0], rr.getAgents(), this.agentSrc));
		t.start();
		
		MASConsoleGUI.get().setPause(false);
		boolean hasAddedListener = false;
		long startTime = System.currentTimeMillis();
		while(isRunning) {
			try {
				// This is needed in the loop, because the plot environment is null before starting
				if(!hasAddedListener) {
					if(runner.getEnvironmentInfraTier() != null) {
						if(runner.getEnvironmentInfraTier().getUserEnvironment() != null) {
							runner.getUserEnvironment().addListener(this);
							hasAddedListener = true;
						}
					}
				}
				// Handle the timeout if it was set
				if(TIMEOUT > -1 && (System.currentTimeMillis() - startTime) >= TIMEOUT && PlotEnvironment.getPlotTimeNow() >= TIMEOUT) {
					isRunning = false;
				}
				Thread.sleep(150);
			} catch (InterruptedException e) {
			}
		}
		while(isPaused) {
			try {
				Thread.sleep(150);
			} catch(InterruptedException e) {
			}
		}
		
		PlotDirectedSparseGraph analyzedGraph = new PlotDirectedSparseGraph();			// analysis results will be cloned into this graph
		Tellability tel = PlotGraphController.getPlotListener().analyze(analyzedGraph);
		analyzedGraph.setName("ER Cycle, engagement step " + currentCycle);
		
		EngageResult er = new EngageResult(analyzedGraph,
										   tel,
										   rr.getAgents(),
										   rr.getModel());
		
		runner.reset();
		isRunning = true;
		return er;
	}
	
	protected List<LauncherAgent> createAgs(Personality[] personalities) {
		if(personalities.length != this.agentNames.length) {
			throw new IllegalArgumentException("There should be as many personalities as there are agents."
					+ "(Expected: " + agentNames.length + ", Got: " + personalities.length + ")");
		}
		List<LauncherAgent> agents = new LinkedList<LauncherAgent>();
		for(int i = 0; i < agentNames.length; i++) {
			agents.add(new LauncherAgent(agentNames[i], personalities[i]));
		}
		return agents;
	}
	
	@Override
	public void onPauseRepeat() {
		this.isRunning = false;
	}
	
	/**
	 * Starts the cycle.
	 */
	@Override
	public void run() {
		ReflectResult rr = this.createInitialReflectResult();
		EngageResult er = null;
		
		while(rr.shouldContinue) {
			++currentCycle;
			log("Running cycle: " + currentCycle);
			er = engage(rr);
			stories.add(er.getPlotGraph());
			rr = this.reflect(er);
		}
		this.finish(er);
	}
	
	/**
	 * Can be overridden by subclass.
	 * This is called after the last simulation was run.
	 */
	protected void finish(EngageResult er) {
	}
	
	/**
	 * Logs a message to the PlotCycle log window.
	 * '\n' is appended automatically.
	 * @param string Message to log
	 */
	protected void log(String string) {
		if(logTextArea != null) {
			logTextArea.append(string + "\n");
			logTextArea.setCaretPosition(logTextArea.getText().length());
			logTextArea.repaint();
		}
	}
	
	/**
	 * Runnable for a single simulation.
	 */
	public static class Cycle implements Runnable {
		
		private PlotLauncher<?, ?> runner;
		private PlotModel<?> model;
		private String[] args;
		private List<LauncherAgent> agents;
		private String agSrc;
		
		public Cycle(PlotLauncher<?, ?> runner, PlotModel<?> model, String[] args, List<LauncherAgent> agents, String agSrc) {
			this.runner = runner;
			this.model = model;
			
			for(LauncherAgent ag : agents) {
				model.addCharacter(ag);
			}
			
			this.args = args;
			this.agents = agents;
			this.agSrc = agSrc;
		}

		@Override
		public void run() {
			try {
				runner.initialize(args, model, agents, agSrc);
				runner.run();
			} catch (JasonException e) {
				e.printStackTrace();
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
		private PlotLauncher<?, ?> runner;
		/**
		 * Agents that will be used by the runner 
		 * to generate characters. Personalities
		 * should be set appropriately already.
		 */
		private List<LauncherAgent> agents;
		/**
		 * Instance of PlotModel for the
		 * next simulation. Will add
		 * agents automatically.
		 */
		private PlotModel<?> model;
		/**
		 * If this is false, the cycle will not execute another
		 * simulation and call finish().
		 * runner and personalities do not matter in this case.
		 */
		private boolean shouldContinue;
		
		public ReflectResult(PlotLauncher<?, ?> runner, PlotModel<?> model, List<LauncherAgent> agents) {
			this(runner, model, agents, true);
		}
		
		public ReflectResult(PlotLauncher<?, ?> runner, PlotModel<?> model, List<LauncherAgent> agents, boolean shouldContinue) {
			this.runner = runner;
			this.model = model;
			this.shouldContinue = shouldContinue;
			this.agents = agents;
		}
		
		public PlotLauncher<?, ?> getRunner() {
			return this.runner;
		}
		
		public PlotModel<?> getModel() {
			return this.model;
		}
		
		public List<LauncherAgent> getAgents() {
			return this.agents;
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
		private Tellability tellability;
		private PlotModel<?> lastModel;
		private List<LauncherAgent> lastAgents;
		
		public EngageResult(PlotDirectedSparseGraph plotGraph, Tellability tellability, List<LauncherAgent> lastAgents, PlotModel<?> lastModel) {
			this.plotGraph = plotGraph;
			this.tellability = tellability;
			this.lastAgents = lastAgents;
			this.lastModel = lastModel; 
		}
		
		public PlotModel<?> getLastModel() {
			return lastModel;
		}

		public List<LauncherAgent> getLastAgents() {
			return lastAgents;
		}

		public PlotDirectedSparseGraph getPlotGraph() {
			return this.plotGraph;
		}
		
		public Tellability getTellability() {
			return this.tellability;
		}
	}
}
