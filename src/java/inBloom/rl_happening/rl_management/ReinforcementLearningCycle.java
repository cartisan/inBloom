package inBloom.rl_happening.rl_management;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.ERcycle.EngageResult;
import inBloom.ERcycle.PlotCycle;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.helper.MoodMapper;
import inBloom.helper.Tellability;
import inBloom.rl_happening.islandWorld.IslandModel;
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
 * @version 7.8.20
 *
 */
public abstract class ReinforcementLearningCycle extends PlotCycle {

	private static final int numberOfEpisodes = 500;
	
	private Personality[] agentPersonalities;
	private String[] agentNames;
	
	// I changed this to non-static such that it will be initialised with 0 whenever a new instance of the class is created (as needed for RLTrainer)
	private int run = 0;
	
	/**
	 * The personalities of the last cycle.
	 */
	protected Personality[] lastPersonalities;
	/**
	 * The launcher of the last cycle.
	 */
	protected PlotLauncher<?,?> lastRunner;
	//protected FeaturePlotModel<?> plotModel;
	
	
	public ResultWriter resultWriter;
//	protected String fileName;
	protected SarsaLambda sarsa;
	public FeaturePlotModel model;
	
	

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
	// Constructor for RLTraining with RobinsonCycleMultipleTrainings
//	public ReinforcementLearningCycle(String agentSource, String[] agentNames, Personality[] agentPersonalities, String fileName)
	public ReinforcementLearningCycle(String agentSource, String[] agentNames, Personality[] agentPersonalities) {
		
		super(agentSource, true);
		
		this.agentNames = agentNames;
		this.agentPersonalities = agentPersonalities;	
//		this.fileName = fileName;
		
		// Initialize the ResultWriter
//		this.resultWriter = new ResultWriter(fileName);
		this.resultWriter = new ResultWriter();
		
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
		this.sarsa = new SarsaLambda(null, this); // this is given for SarsaLambda to have access to the log method
		
		IslandModel model = new IslandModel(new ArrayList<LauncherAgent>(), new AutomatedHappeningDirector(this.sarsa), this.resultWriter);
		this.model = model;
		
		this.sarsa.initializeSarsa(model);
		
		this.resultWriter.writeTitlesOfPlot();
		this.resultWriter.writeTitlesOfEpisodes(sarsa.weights);

	}

	



	/**
	 * @Override
	 * 
	 * Runs a single simulation until it is paused (finished by Plotmas or user) or some time has passed.
	 * @param rr ReflectResultRL containing Personality array with length equal to agent count as well as PlotLauncher instance
	 * @return EngageResult containing the graph of this simulation and its tellability score.
	 */
	protected EngageResult engage(ReflectResultRL rr) {
		if(currentCycle != 0) {
			log("  Engaging...");
//			log("    Parameters: " + rr.toString());
		}
		
		PlotLauncher<?,?> runner = rr.getRunner();

		try {
			
			Thread t = new Thread(new RLCycle(runner, rr.getModel(), cycle_args, rr.getAgents(), this.agentSrc, rr.getModel().happeningDirector, sarsa));
			
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
				if(TIMEOUT > -1 && (System.currentTimeMillis() - startTime) >= TIMEOUT && RLEnvironment.getPlotTimeNow() >= TIMEOUT) {
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
		
		//Without catching the ConcurrentModificationException
		Tellability tel = PlotGraphController.getPlotListener().analyze(analyzedGraph);
//		Tellability tel = null;
//		try {
//			tel = PlotGraphController.getPlotListener().analyze(analyzedGraph);
//		} catch(ConcurrentModificationException e) {
//		}
		
		analyzedGraph.setName("ER Cycle, engagement step " + currentCycle);
//		if(tel!=null)
			log("Tellability" + Double.toString(tel.compute()));
//		else {
//			log("Tellability" + 0);
//		}
		
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
	
	@Override
	protected ReflectResultRL reflect(EngageResult erOriginal) {
		EngageResult er = (EngageResult) erOriginal;
		this.log("I am reflecting");
		
		
		// TELLABILITY
		
		// 1. Get and compute the Tellability after this run
		Tellability tellability = er.getTellability();
		double currTellability = tellability.compute();
		this.log(" Current Tellability: " + currTellability);
		
		if(sarsa.PRINT_WEIGHTS_AFTER_EPISODE) {
			sarsa.rlCycle.log(sarsa.printFeatureActionValues(sarsa.weights));
		}
		
		
		// 3. New parameters
		// -> give Tellability as a reward to SarsaLambda
		// let SarsaLambda calculate and update weights
		// auch nach jedem Step!
		sarsa.updateWeights(currTellability);

		//resultWriter.writePlotStep();
		resultWriter.writePlotStep(((FeaturePlotModel)er.getLastModel()));
		resultWriter.writeResultOfEpisode(currTellability, tellability, sarsa);
		
		
		// 2. If we reached the end, stop
		if(!shouldContinue()) {
			return new ReflectResultRL(null, null, null, null, false);
		}
		
		
		
		
		// 4. Create and start new run
		this.lastRunner = this.getPlotLauncher();
		this.lastRunner.setShowGui(false);

		List<LauncherAgent> agents = this.createAgs(this.agentNames, this.agentPersonalities);

		// create new model for new run
		FeaturePlotModel<?> model = this.getPlotModel();
		this.model = model;
		return new ReflectResultRL(this.lastRunner, model, agents, sarsa);
		
	}
	
	/**
	 * Starts the cycle.
	 */
	@Override
	public void run() {
		log("Start running");
		ReflectResultRL rr = (ReflectResultRL)this.createInitialReflectResult();
		EngageResult er = null;
		
		while(rr.shouldContinue) {
			++currentCycle;
			log("\nRunning cycle: " + currentCycle);
			er = engage(rr);
			stories.add(er.getPlotGraph());
			if (SHOW_FULL_GRAPH){
				stories.add(er.getAuxiliaryGraph());
			}
			rr = this.reflect(er);
		}
		this.finish(er);
	}
	
	
	
	
	@Override
	protected void finish(EngageResult erOriginal) {
		EngageResult er = (EngageResult) erOriginal;
		// Print results
		// flush and close handled by super implementation
		super.finish(er);
	}
	
	/**
	 *  These methods should be overriden with the specification of the relevant story
	 */
	
	public abstract PlotLauncher<?, ?> getPlotLauncher();
	
	public abstract FeaturePlotModel<?> getPlotModel();
	

	
	
	
	private boolean shouldContinue() {
		run++;
		log("RUN: " + run);
		if(run >= numberOfEpisodes) {
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
