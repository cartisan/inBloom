package inBloom.ERcycle;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.helper.EnvironmentListener;
import inBloom.helper.MoodMapper;
import inBloom.helper.Tellability;
import jason.JasonException;
import jason.asSemantics.Personality;
import jason.infra.centralised.RunCentralisedMAS;
import jason.runtime.MASConsoleGUI;

/**
 * Class which facilitates running a cycle of multiple simulations.
 * @author Sven Wilke
 */
public abstract class PlotCycle implements Runnable, EnvironmentListener {
	/** Set true to display full plot graphs next to the analyzed one, during ER, for debugging purposes. */
	protected static final boolean SHOW_FULL_GRAPH = false;

	/** Timeout in ms before a single simulation is forcibly stopped. A value of -1 means no timeout.  */
	protected static long TIMEOUT = -1;
	
	/** List of plot graphs generated by ER cycles, for display in UI drop-down. */
	protected List<PlotDirectedSparseGraph> stories;
	
	/** The source file of the agent code. */
	protected String agentSrc;
	
	/** Whether the next cycle should start after the current one is finished. */
	protected boolean isPaused;
	
	/** can be used to provide args to PlotLauncher */
	public String[] cycle_args = new String[0]; 
	
	private JFrame cycleFrame;
	private JTextArea logTextArea = new JTextArea(10, 40);
	
	protected static int currentCycle = 0;
	
	protected boolean isRunning = true;
	
	/**
	 * Creates a new cycle object with specified agents.
	 * @param agentNames an array of the names of all agents
	 * @param agentSrc the name of the source file for the agent code
	 */
	protected PlotCycle(String agentSrc, boolean showGui) {
		this.agentSrc = agentSrc;
		stories = new LinkedList<>();
		if(showGui) {
			initGui();
		}	
	}
	
	protected PlotCycle(String agentSrc) {
		this(agentSrc, true);
	}
	
	private void initGui() {
		cycleFrame = new JFrame("Plot Cycle");
		cycleFrame.setLayout(new BorderLayout());
		
		// setup text field
		JScrollPane scroll = new JScrollPane(logTextArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		cycleFrame.add(scroll, BorderLayout.CENTER);
		
		// setup buttons
		JButton pauseButton = new JButton("Pause");
		pauseButton.addActionListener(new ActionListener()
			{
			  	public void actionPerformed(ActionEvent e)
			  	{
			  		isPaused = !isPaused;
			  		((JButton)e.getSource()).setText(isPaused ? "Continue" : "Pause");
			  	}
			});
		
		JButton btDebug = new JButton("Debug Next Cycle", new ImageIcon(RunCentralisedMAS.class.getResource("/images/debug.gif")));
        btDebug.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	if(cycle_args.length == 0) {
	            	cycle_args = new String[]{"-debug"};
            		btDebug.setText("Stop Debug & Finish Cycle");
            	} else {
            		btDebug.setText("Debug Next Cycle");
            		cycle_args = new String[0];
            		PlotLauncher.getRunner().setDebug(false);
            		isRunning=false;
            	}
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(pauseButton);
		buttonPanel.add(btDebug);
		buttonPanel.revalidate();
		
		cycleFrame.add(buttonPanel, BorderLayout.SOUTH);
		
		// finalize UI
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
		log("  Engaging...");
		log("    Parameters: " + rr.toString());
		PlotLauncher<?,?> runner = rr.getRunner();

		try {
			Thread t = new Thread(new Cycle(runner, rr.getModel(), cycle_args, rr.getAgents(), this.agentSrc));
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
					log("[PlotCycle] SEVERE: timeout for engagement step triggered, analyzing incomplete story and moving on");
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
		log("Tellability" + Double.toString(tel.compute()));
		
		MoodMapper moodData = runner.getUserModel().moodMapper;
		EngageResult er = this.createEngageResult(rr, runner, analyzedGraph, tel, moodData);
		
		if (PlotCycle.SHOW_FULL_GRAPH) {
			PlotDirectedSparseGraph displayGraph = PlotGraphController.getPlotListener().getGraph().clone();
			displayGraph.setName("ER Cycle (full), step " + currentCycle);
			er.setAuxiliaryGraph(displayGraph);
		}
		
		runner.reset();
		isRunning = true;
		return er;
	}
	
	/**
	 * Assembles the RngageResult for the plot cycle, can be override by subclasses to enable
	 * the passing through of subclasses of EngageResult, which carry more data.
	 * @param rr
	 * @param runner
	 * @param analyzedGraph
	 * @param tel
	 * @param moodData
	 * @return
	 */
	protected EngageResult createEngageResult(ReflectResult rr, PlotLauncher<?, ?> runner,
			PlotDirectedSparseGraph analyzedGraph, Tellability tel, MoodMapper moodData) {
		EngageResult er;
		er = new EngageResult(analyzedGraph,
									   tel,
									   rr.getAgents(),
									   runner.getUserModel(),
									   moodData);
		return er;
	}
	
	protected List<LauncherAgent> createAgs(String[] agentNames, Personality[] personalities) {
		if(personalities.length != agentNames.length) {
			throw new IllegalArgumentException("There should be as many personalities as there are agents."
					+ "(Expected: " + agentNames.length + ", Got: " + personalities.length + ")");
		}
		List<LauncherAgent> agents = new LinkedList<LauncherAgent>();
		for(int i = 0; i < agentNames.length; i++) {
			agents.add(new LauncherAgent(agentNames[i], personalities[i]));
		}
		return agents;
	}

	protected List<LauncherAgent> createAg(String agentName, Collection<String> belief, Collection<String> goal, Personality personality) {
		List<LauncherAgent> l = new LinkedList<>();
		l.add(new LauncherAgent(agentName, belief, goal, personality));
		
		return l;		
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
		log("Start running");
		ReflectResult rr = this.createInitialReflectResult();
		EngageResult er = null;
		
		while(rr.shouldContinue) {
			++currentCycle;
			log("Running cycle: " + currentCycle);
			er = engage(rr);
			stories.add(er.getPlotGraph());
			if (SHOW_FULL_GRAPH){
				stories.add(er.getAuxiliaryGraph());
			}
			rr = this.reflect(er);
		}
		this.finish(er);
	}
	
	/**
	 * Can be overridden by subclass.
	 * This is called after the last simulation was run.
	 */
	protected void finish(EngageResult er) {
		log("FINISHING");
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
		
		public Cycle(PlotLauncher<?, ?> runner, PlotModel<?> model, String[] args, List<LauncherAgent> agents, String agSrc) throws Exception {
			this.runner = runner;
			this.args = args;
			this.agents = agents;
			this.agSrc = agSrc;
			this.model = (PlotModel<?>) model.getClass().getConstructors()[0].newInstance(agents, model.happeningDirector);
			
			for(LauncherAgent ag : agents) {
				model.addCharacter(ag);
			}
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
}
