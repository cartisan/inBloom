/**
 * 
 */
package inBloom.rl_happening;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;
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
	// One agent can find multiple (anonymous) friends.
	HashMap<Character, Integer> numberOfFriends;
	boolean isOnCruise = false;
	
	
	/**
	 * LOCATIONS
	 */
	public CivilizedWorld civilizedWorld = new CivilizedWorld();
	public Ship ship = new Ship();
	public Island island = new Island();
	
	
	/**
	 * CONSTRUCTOR
	 */
	
	public IslandModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		
		/**
		 * SUPER CONSTRUCTOR
		 */
		super(agentList, hapDir);
		
		
		/**
		 * INITIALIZE VARIABLES
		 */
		// 1. numberOfFriends: each Character has 0 friends
		this.numberOfFriends = new HashMap<Character, Integer>();
		for(Character agent: this.characters.values()) {
			this.numberOfFriends.put(agent, 0);
		}
		
		
		/**
		 * ADD LOCATIONS
		 */
		this.addLocation(this.civilizedWorld);
		this.addLocation(this.ship);
		this.addLocation(this.island);
	}
	
	
	
	/**
	 * ACTION METHODS
	 */
	
	public ActionReport goOnCruise(Character agent) {
		
		ActionReport result = new ActionReport();
	
		this.isOnCruise = true;
		logger.info(agent.name + " went on a cruise.");
		
		agent.goTo(this.ship);
		logger.info(agent.name + " is on ship " + this.ship.name);
		
		result.addPerception(agent.name, new PerceptAnnotation("hope"));
		result.success = true;
		
		return result;
	}
	
	public ActionReport doNothing(Character agent) {
		ActionReport result = new ActionReport();
	
		logger.info(agent.name + " did nothing.");
		
		// result.addPerception(agent.name, new PerceptAnnotation("hope"));
		result.success = true;
		
		return result;
	}
	
	public ActionReport findFriend(Character agent) {
		ActionReport result = new ActionReport();
	
		logger.info(agent.name + " found a friend.");
		
		// number of friends for this agent increases by one
		this.numberOfFriends.replace(agent, this.numberOfFriends.get(agent)+1);
		
		result.addPerception(agent.name, new PerceptAnnotation("joy"));
		result.success = true;
		
		return result;
	}
	
	
	
	
	/**
	 * INNER LOCATION CLASSES
	 */
	
	public static class CivilizedWorld extends Location {

		public CivilizedWorld() {
			super("civilized world");
			// TODO Auto-generated constructor stub
		}
		
	}
	
	public static class Ship extends Location {

		public Ship() {
			super("magnificent ship");
			// TODO Auto-generated constructor stub
		}
		
	}
	
	public static class Island extends Location {

		public Island() {
			super("lonely island");
			// TODO Auto-generated constructor stub
		}
		
	}

}
