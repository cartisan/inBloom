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
 * 
 * @author Julia Wippermann
 * @version 7.8.20
 * 
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
		
		String result = "";
		
		result += "Happenings:\n";
		List<Happening<?>> allHappenings = this.model.happeningDirector.getAllHappenings();
		for(Happening<?> happening: allHappenings) {
			result += "               " + happening + "\n";
		}
		
		result += "Weights:\n";
		result += sarsa.printFeatureActionValues(sarsa.weights);
		
		result += "\nEligibility Traces:\n";
		
		result += sarsa.printFeatureActionValues(sarsa.eligibilityTraces);
			
		return result;
	}
}
