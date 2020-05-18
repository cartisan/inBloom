/**
 * 
 */
package inBloom.rl_happening.rl_management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.ERcycle.ReflectResult;
import inBloom.graph.PlotDirectedSparseGraph;
import inBloom.helper.MoodMapper;
import inBloom.rl_happening.islandWorld.IslandLauncher;
import inBloom.rl_happening.islandWorld.IslandModel;
import inBloom.stories.little_red_hen.FarmModel;
import inBloom.stories.little_red_hen.RedHenHappeningCycle;
import inBloom.stories.little_red_hen.RedHenLauncher;
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
	
	@Override
	public PlotLauncher<?, ?> getPlotLauncher() {
		return new IslandLauncher();
	}

	// TODO possible problem: getPlotModel is also called in reflect and getPlotModel calls
	// gethappeningDirector, which CREATES A NEW HappeningDirector and schedules all Happenings
	// NEWLY
	@Override
	public FeaturePlotModel<?> getPlotModel(List<LauncherAgent> agents) {
		// TODO change the ScheduledHappeningDirector into my own RL one :D
		//return new IslandModel(agents, new ScheduledHappeningDirector());
		return new IslandModel(new ArrayList<LauncherAgent>(), this.getHappeningDirector());
	}
	
	public HappeningDirector getHappeningDirector() {
		AutomatedHappeningDirector hapDir = HappeningManager.createHappeningDirector(this.rlApplication);
		//HappeningManager.scheduleHappenings(hapDir);
		return hapDir;
	}

}
