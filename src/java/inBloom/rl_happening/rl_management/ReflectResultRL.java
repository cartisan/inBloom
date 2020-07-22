package inBloom.rl_happening.rl_management;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.ERcycle.ReflectResult;
import inBloom.storyworld.Happening;

/**
 * Result of the reflect method. Contains PlotLauncher instance
 * and personalities for the next simulation.
 * Can be extended to allow further parameters.
 */
public class ReflectResultRL extends ReflectResult {
	
	protected SarsaLambda sarsa;
	
	public ReflectResultRL(PlotLauncher<?, ?> runner, PlotModel<?> model, List<LauncherAgent> agents, SarsaLambda sarsa) {
		this(runner, model, agents, sarsa, true);
	}
	
	public ReflectResultRL(PlotLauncher<?, ?> runner, PlotModel<?> model, List<LauncherAgent> agents, SarsaLambda sarsa, boolean shouldContinue) {
		super(runner, model, agents, shouldContinue);
		this.sarsa = sarsa;
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
	
	// TODO DEP
	public String toString() {
//		String result = "Agents: ";
//		for (LauncherAgent ag : this.agents) {
//			result += ag.name + ": " + ag.personality + ", ";
//		}
		
		String result = "";
		
		result += "Happenings:\n";
		List<Happening<?>> allHappenings = this.model.happeningDirector.getAllHappenings();
		for(Happening<?> happening: allHappenings) {
			result += "               " + happening + "\n";
		}
		
		HashMap<String, HashMap<Happening<?>, Double>> weights = this.sarsa.weights;
		result += "Weights:\n";
		result += this.sarsa.printWeights();
		
		result += "\nEligibility Traces:\n";
		
		result += this.sarsa.printEligibilityTraces();
			
		return result;
	}
}
