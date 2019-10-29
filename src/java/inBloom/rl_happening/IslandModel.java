/**
 * 
 */
package inBloom.rl_happening;

import java.util.List;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.Character;

/**
 * @author Julia Wippermann
 * @version 29.10.19
 *
 * The Model defines the effects of Actions on the Environment and Agents.
 */
public class IslandModel extends PlotModel<IslandEnvironment> {
	
	/**
	 * GLOBAL VARIABLES
	 */
	
	
	/**
	 * CONSTRUCTOR
	 */
	
	public IslandModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		super(agentList, hapDir);
		// here we could define our variables
	}
	
	
	
	/**
	 * ACTION METHODS
	 */
	
	public ActionReport goOnCruise(Character agent) {
		ActionReport result = new ActionReport();
	
		logger.info(agent.name + " went on a cruise.");
		
		result.addPerception(agent.name, new PerceptAnnotation("hope"));
		result.success = true;
		return result;
	}

}
