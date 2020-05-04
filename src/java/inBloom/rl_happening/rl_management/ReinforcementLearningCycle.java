/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.HashSet;
import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotEnvironment;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.ERcycle.CounterfactualityEngageResult;
import inBloom.ERcycle.EngageResult;
import inBloom.ERcycle.PlotCycle;
import inBloom.ERcycle.ReflectResult;
import inBloom.ERcycle.PlotCycle.Cycle;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.graph.PlotGraphController;
import inBloom.helper.Counterfactuality;
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
	
	
	private SarsaLambda rlApplication;
	
	

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
		
		this.rlApplication = new SarsaLambda(this.plotModel);
		//this.rlApplication.initializeParameters();
	}

	@Override
	protected ReflectResult reflect(EngageResult erOriginal) {
		EngageResult er = (EngageResult) erOriginal;
		this.log("I am reflecting");
		
		
		// TELLABILITY
		
		// 1. Get and compute the Tellability after this run
		Tellability tellability = er.getTellability();
		double currTellability = tellability.compute();
		this.log(" Current Tellability: " + currTellability);
		
		// Save tellability, graph and agent's personality if it was better than the best before
		/*if(currTellability > this.bestTellability) {
			this.bestTellability = currTellability;
			this.log("New best Tellability: " + this.bestTellability);
			this.bestPersonalities = this.lastPersonalities;
			this.bestResult = er;

		}
		this.log("Best Tellability So Far: " + this.bestTellability);*/


		// COUNTERFACTUALITY
		/*Counterfactuality counterfactuality = er.getCounterfactuality();
		double currCounterfactuality = counterfactuality.compute();
		this.log(" Current Counterfactuality: " + currCounterfactuality);
		if(currCounterfactuality > this.bestCounterfactuality) {
			this.bestCounterfactuality = currCounterfactuality;
			this.log("New best counterfactuality: " + this.bestCounterfactuality);
			this.bestPersonalities = this.lastPersonalities;
			this.bestResult = er;
		}
		this.log("Best Counterfactuality So Far: " + this.bestCounterfactuality);*/

		// Stop cycle if there are no other personality combinations
		/*if(!this.personalityIterator.hasNext() || currentCycle >= this.endCycle) {
			return new ReflectResult(null, null, null, false);
		}*/
		if(!shouldContinue()) {
			return new ReflectResult(null, null, null, false);
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
		return new ReflectResult(this.lastRunner, model, agents);
		
//		return null;
	}

	@Override
	protected ReflectResult createInitialReflectResult() {
		
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
		 * We create the ReflectResult using the runner, model and LauncherAgents we just got.
		 * 
		 * A ReflectResult saves the information we need for the next simulation, e.g.
		 * the PlotLauncher, the Model and the LauncherAgents
		 */
		ReflectResult reflectResult = new ReflectResult(runner, this.plotModel, agents);
		
		this.log("Cycle " + currentCycle);
		
		return reflectResult;
	}
	
	@Override
	protected void finish(EngageResult erOriginal) {
		EngageResult er = (EngageResult) erOriginal;
		// Print results
		this.log("This is the end.");
		// flush and close handled by super implementation
		super.finish(er);
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
