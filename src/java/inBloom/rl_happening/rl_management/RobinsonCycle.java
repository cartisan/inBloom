/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.ArrayList;
import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.rl_happening.islandWorld.IslandLauncher;
import inBloom.rl_happening.islandWorld.IslandModel;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.ScheduledHappeningDirector;
import jason.asSemantics.Personality;

/**
 * @author Julia Wippermann
 * @version 31.1.20
 *
 */
public class RobinsonCycle extends ReinforcementLearningCycle {
	
	// needs to be static, so that it can be accessed during the invocation of the super constructor
	private static String[] agentNames = {"robinson"};
	private static Personality[] agentPersonalities = {new Personality(1, 0, 0.5, -0.5, 0)};
	
	

	/**
	 * Constructor, invokingthe super constructor using the given source of the asl file for the agents
	 * and the list of agents and personalities as specified at the beginning of the class
	 * 
	 * @param agentSrc
	 * 			The name of the asl document where the relevant agent is saved (without the .asl postfix)
	 */
	public RobinsonCycle() {
		super("islandAgent", agentNames, agentPersonalities);
	}
	
	public static void main(String[] args) {
		RobinsonCycle cycle = new RobinsonCycle();
		cycle.run();
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
			PlotLauncher<?, ?> runner = new IslandLauncher();
			// TODO this was in Sven's Code, but it isn't relevant to me now. Maybe later though.
			runner.setShowGui(false);
			
			
			IslandModel model = new IslandModel(new ArrayList<LauncherAgent>(), new AutomatedHappeningDirector(this.sarsa));
			
			
			/* 
			 * AGENTS
			 * 
			 * A set of functioning LauncherAgents is created from a list of names and a seperate
			 * list of matching personalities
			 */
			List<LauncherAgent> agents = this.createAgs(this.agentNames, this.agentPersonalities);

			for (LauncherAgent ag : agents) {
				ag.location = model.civilizedWorld.name;
			}
			
			/* 
			 * MODEL
			 * 
			 * We get the story specific model
			 */
			// ist bereits
			// this.plotModel = this.getPlotModel(agents);
			// this.plotModel = model;
			// TODO hier HappeningScheduler übergeben -> s. RedhenHappening
			
			
			/*
			 * We create the ReflectResultRL using the runner, model and LauncherAgents we just got.
			 * 
			 * A ReflectResultRL saves the information we need for the next simulation, e.g.
			 * the PlotLauncher, the Model and the LauncherAgents
			 */
			ReflectResultRL ReflectResultRL = new ReflectResultRL(runner, model, agents, sarsa);
			
			this.log("Cycle " + currentCycle);
			
			return ReflectResultRL;
		}
	
	
	
	
	@Override
	public PlotLauncher<?, ?> getPlotLauncher() {
		this.lastRunner = new IslandLauncher();
		
		return this.lastRunner;
	}

	// TODO possible problem: getPlotModel is also called in reflect and getPlotModel calls
	// gethappeningDirector, which CREATES A NEW HappeningDirector and schedules all Happenings
	// NEWLY
	@Override
	public FeaturePlotModel<?> getPlotModel() {
		// TODO change the ScheduledHappeningDirector into my own RL one :D
		return new IslandModel(new ArrayList<LauncherAgent>(), new AutomatedHappeningDirector(sarsa));
		
		//return new IslandModel(new ArrayList<LauncherAgent>(), this.getHappeningDirector());
		//return (FeaturePlotModel<?>)this.lastRunner.getUserModel();
	}
	
	public HappeningDirector getHappeningDirector() {
		AutomatedHappeningDirector hapDir = HappeningManager.createHappeningDirector(this.sarsa);
		//HappeningManager.scheduleHappenings(hapDir);
		return hapDir;
	}

}
