/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.ERcycle.EngageResult;
import inBloom.ERcycle.PlotCycle;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.helper.MoodMapper;
import inBloom.helper.Tellability;
import jason.asSemantics.Personality;
import jason.runtime.MASConsoleGUI;

// CC = CounterfactualityCycle
// RHCC = RedHenCounterfactualityCycle
// RLC = ReinforcementLearningCycle
// RC = RobinsonCycle

/**
 * Vergleiche CounterfactualityCycle -> ebenfalls die abstrakte Implementation von PlotCycle,
 * dessen konkrete Implementation in RedHenCounterfactualityCycle gegeben ist. In unserem
 * Falle ist eine (die) konkrete Implementation von ReinforcementLearningCycle in
 * RobinsonCycle gegeben
 * 
 * @author Julia Wippermann
 * @version 6.1.20
 *
 */
public abstract class ReinforcementLearningCycle extends PlotCycle {

	private Personality[] agentPersonalities;
	private String[] agentNames;
	
	private static int run = 0;
	
	/**
	 * The personalities of the last cycle.
	 */
	protected Personality[] lastPersonalities;
	/**
	 * The launcher of the last cycle.
	 */
	protected PlotLauncher<?,?> lastRunner;
	protected FeaturePlotModel<?> plotModel;
	
	
	protected SarsaLambda sarsa;
	
	

	// TODO zusätzliche Parameter bei Sven (in CounterfactualityCycle)
	/**
	 * Constructor, invoking the super constructor, using the agentSource, setting the GUI to active
	 * and saving all agent names
	 * 
	 * @param agentSource
	 * 			The name of the asl document where the relevant agent is saved (without the .asl postfix)
	 * @param agentNames
	 * 			A list of all agents' names that should be part of the story
	 * 			in the same order as the list of the agents' personalities
	 * @param agentPersonalities
	 * 			A list of all agents' personalities that should be part of the story
	 * 			in the same order as the list of the agents' names
	 */
	public ReinforcementLearningCycle(String agentSource, String[] agentNames, Personality[] agentPersonalities) {
		
		super(agentSource, true);
		
		this.agentNames = agentNames;
		this.agentPersonalities = agentPersonalities;	
//		this.originalGraph = originalGraph; // TODO why needed? -> for Tellability or GUI or something? Analysing?
//		this.originalMood = originalMood; // TODO why needed?
		
		/* 
		 * AGENTS -> TODO same (copied) as in in this class: createInitialReflectResultRL
		 * 
		 * A set of functioning LauncherAgents is created from a list of names and a seperate
		 * list of matching personalities
		 */
		List<LauncherAgent> agents = this.createAgs(this.agentNames, this.agentPersonalities);
		
		
		/**
		 * SARSA(LAMBDA) - Initialization
		 * - create all Happenings
		 * - initialize Weights
		 * - initialize eligibility Traces
		 */
		log("Initialising Sarsa(Lambda)");
		this.sarsa = new SarsaLambda(this.getPlotModel(agents), this); // this is given for SarsaLambda to have access to the log method

	}

	@Override
	protected ReflectResultRL reflect(EngageResult erOriginal) {
		EngageResult er = (EngageResult) erOriginal;
		this.log("I am reflecting");
		
		
		// TELLABILITY
		
		// 1. Get and compute the Tellability after this run
		Tellability tellability = er.getTellability();
		double currTellability = tellability.compute();
		this.log(" Current Tellability: " + currTellability);
		
		
		if(!shouldContinue()) {
			return new ReflectResultRL(null, null, null, null, false);
		}

		// Start the next cycle
		/*this.lastPersonalities = this.personalityIterator.next();
		this.log("Next Personalities: ");
		for (Personality pers : this.lastPersonalities) {
			this.log("\t" + pers.toString());
		}*/

		this.lastRunner = this.getPlotLauncher();
		this.lastRunner.setShowGui(false);

		List<LauncherAgent> agents = this.createAgs(this.agentNames, this.agentPersonalities);

		PlotModel<?> model = this.getPlotModel(agents);
		return new ReflectResultRL(this.lastRunner, model, agents, sarsa);
		
//		return null;
	}

	// initial = initial for each run = each episode = each plot
	@Override
	protected ReflectResultRL createInitialReflectResult() {
		
		this.log("Creating initial Reflect Results");
		
		/* 
		 * RUNNER
		 * 
		 * We get the story specific runner
		 */
		PlotLauncher<?, ?> runner = this.getPlotLauncher();
		// TODO this was in Sven's Code, but it isn't relevant to me now. Maybe later though.
		runner.setShowGui(false);
		
		
		/* 
		 * AGENTS
		 * 
		 * A set of functioning LauncherAgents is created from a list of names and a seperate
		 * list of matching personalities
		 */
		List<LauncherAgent> agents = this.createAgs(this.agentNames, this.agentPersonalities);

		
		/* 
		 * MODEL
		 * 
		 * We get the story specific model
		 */
		this.plotModel = this.getPlotModel(agents);
		// TODO hier HappeningScheduler übergeben -> s. RedhenHappening
		
		
		/*
		 * We create the ReflectResultRL using the runner, model and LauncherAgents we just got.
		 * 
		 * A ReflectResultRL saves the information we need for the next simulation, e.g.
		 * the PlotLauncher, the Model and the LauncherAgents
		 */
		ReflectResultRL ReflectResultRL = new ReflectResultRL(runner, this.plotModel, agents, sarsa);
		
		this.log("Cycle " + currentCycle);
		
		return ReflectResultRL;
	}
	
	@Override
	protected void finish(EngageResult erOriginal) {
		EngageResult er = (EngageResult) erOriginal;
		// Print results
		// flush and close handled by super implementation
		super.finish(er);
	}

	/**
	 * @Override
	 * 
	 * Runs a single simulation until it is paused (finished by Plotmas or user) or some time has passed.
	 * @param rr ReflectResultRL containing Personality array with length equal to agent count as well as PlotLauncher instance
	 * @return EngageResult containing the graph of this simulation and its tellability score.
	 */
	protected EngageResult engage(ReflectResultRL rr) {
		log("  Engaging...");
		log("    Parameters: " + rr.toString());
		PlotLauncher<?,?> runner = rr.getRunner();

		try {
			
			// create AutomatedHappeningDirector with SarsaLambda
			AutomatedHappeningDirector hapDir = new AutomatedHappeningDirector(this.sarsa);
			// create a Thread that also gets the AutomatedHappeningDirector. RLCycle will then attach the AutomatedHappeningDirector to the given PlotLauncher
			Thread t = new Thread(new RLCycle(runner, rr.getModel(), cycle_args, rr.getAgents(), this.agentSrc, hapDir));
			
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
	 *  These methods should be overriden with the specification of the relevant story
	 */
	
	public abstract PlotLauncher<?, ?> getPlotLauncher();
	
	public abstract FeaturePlotModel<?> getPlotModel(List<LauncherAgent> agents);
	

	
	
	
	private boolean shouldContinue() {
		run++;
		if(run >= 3) {
			return false;
		} else {
			return true;
		}
	}
	
	

	
	// In PlotCycle engage -> Thread erstellt mit Cycle drin -> Cycle weiter unter, hat Run, bekommt
	// auch ein happeningScheduler übergeben
	// letztendlich in jedem EnvironmentStep fragt das Model einmal, soll ich Happening? -> da eingreifen -> in Model implementiert
	// in PlotModel checkHappenings(int step) -> fragt happeningDirector nach getTriggeredHappenings
	// -> das in RTLHappeningDirector überschreiben
	// während engage im Grunde live checken
}