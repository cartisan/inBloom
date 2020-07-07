package inBloom.ERcycle;

import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.PlotModel;

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
	protected PlotLauncher<?, ?> runner;
	/**
	 * Agents that will be used by the runner 
	 * to generate characters. Personalities
	 * should be set appropriately already.
	 */
	protected List<LauncherAgent> agents;
	/**
	 * Instance of PlotModel for the
	 * next simulation. Will add
	 * agents automatically.
	 */
	protected PlotModel<?> model;
	/**
	 * If this is false, the cycle will not execute another
	 * simulation and call finish().
	 * runner and personalities do not matter in this case.
	 */
	protected boolean shouldContinue;
	
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
	
	public String toString() {
		String result = "Agents: ";
		for (LauncherAgent ag : this.agents) {
			result += ag.name + ": " + ag.personality + ", ";
		}
		
		result += "Happenings: ";
		result += this.model.happeningDirector.getAllHappenings().toString();
			
		return result;
	}
}
