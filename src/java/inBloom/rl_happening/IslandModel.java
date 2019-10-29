/**
 * 
 */
package inBloom.rl_happening;

import java.util.List;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;
import inBloom.juwistest.Island;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.Location;
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
	 * LOCATIONS
	 */
	public Island island = new Island();
	
	
	/**
	 * CONSTRUCTOR
	 */
	
	public IslandModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		super(agentList, hapDir);
		// here we could define our variables
		
		// add locations
		this.addLocation(this.island);
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
	
	
	
	
	/**
	 * INNER LOCATION CLASSES
	 */
	
	public static class Island extends Location {

		public Island() {
			super("island");
			// TODO Auto-generated constructor stub
		}
		
	}

}
