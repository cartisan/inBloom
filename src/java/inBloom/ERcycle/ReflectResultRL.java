package inBloom.ERcycle;

import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.rl_happening.rl_management.SarsaLambda;

/**
 * Result of the reflect method. Contains PlotLauncher instance
 * and personalities for the next simulation.
 * Can be extended to allow further parameters.
 */
public class ReflectResultRL {
	/**
	 * Instance of the PlotLauncher for
	 * the story in question.
	 */
	private PlotLauncher<?, ?> runner;
	/**
	 * The current state of SarsaLambda, including
	 * all current weights etc. for this run
	 */
	private SarsaLambda sarsa;
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
	public boolean shouldContinue;
	
	public ReflectResultRL(PlotLauncher<?,?> runner, PlotModel<?> model, SarsaLambda sarsa) {
		this(runner, model, sarsa, true);
	}
	
	public ReflectResultRL(PlotLauncher<?,?> runner, PlotModel<?> model, SarsaLambda sarsa, boolean shouldContinue) {
		this.runner = runner;
		this.model = model;
		this.shouldContinue = shouldContinue;
		this.sarsa = sarsa;
	}
	
	public PlotLauncher<?, ?> getRunner() {
		return this.runner;
	}
	
	public PlotModel<?> getModel() {
		return this.model;
	}
	
	public SarsaLambda getSarsa() {
		return this.sarsa;
	}
	
	public boolean shouldContinue() {
		return this.shouldContinue;
	}
	
	public String toString() {
//		String result = "Agents: ";
//		for (LauncherAgent ag : this.agents) {
//			result += ag.name + ": " + ag.personality + ", ";
//		}
//		
//		result += "Happenings: ";
//		result += this.model.happeningDirector.getAllHappenings().toString();
//			
//		return result;
		
		
		String result = "SARSA LAMBDA";
		
		
		return result;
	}
}
