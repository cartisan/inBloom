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
import inBloom.storyworld.Item;
import inBloom.storyworld.Location;
import jason.asSyntax.Literal;
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
	public HashMap<Character, Integer> friends;
	public HashMap<Character, Integer> hunger;
	public boolean isOnCruise = false;
	
	
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
		this.friends = new HashMap<Character, Integer>();
		changeAllValues(this.friends, 0);
		// 2. hunger: each Character isn't hungry yet
		this.hunger = new HashMap<Character, Integer>();
		changeAllValues(this.hunger, 0);
		
		
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
	
	public ActionReport stayHome(Character agent) {
		ActionReport result = new ActionReport();
	
		logger.info(agent.name + " stayed home.");
		
		// result.addPerception(agent.name, new PerceptAnnotation("hope"));
		result.success = true;
		
		return result;
	}

	public ActionReport findFriend(Character agent) {
		ActionReport result = new ActionReport();

		logger.info(agent.name + " found a friend.");

		// number of friends for this agent increases by one
		increaseIndividualValue(this.friends, agent, 1);

		result.addPerception(agent.name, new PerceptAnnotation("joy"));
		result.success = true;

		return result;
	}

	public ActionReport getFood(Character agent) {
		ActionReport result = new ActionReport();
		
		logger.info(agent.name + " looked for food.");
		
		// TODO here a happening could intrude
		// but for now: if he look for food, he finds food
		// and immediately eats it
		
		agent.addToInventory(new Food());
		
		result.success = true;
		
		return result;
	}

	public ActionReport eat(Character agent) {
		ActionReport result = new ActionReport();

		logger.info(agent.name + " eats food.");

		// agent eats food
		agent.removeFromInventory("food");
		logger.info(agent.name + " ate the food.");
		// he is not hungry anymore
		this.hunger.replace(agent, 0);
		this.environment.removePercept(agent.name, Literal.parseLiteral("hungry"));
		logger.info(agent.name + "'s hunger: " + this.hunger.get(agent));

		result.success = true;

		return result;
	}
	
	public void increaseHunger(Character agent) {
		
		// increase hunger by 1
		increaseIndividualValue(this.hunger, agent, 1);

		logger.info("Hunger has increased. " + agent.name + "'s hunger is " + this.hunger.get(agent));

		// check if hunger has become critical

		if(this.hunger.get(agent) >= 10) {
			this.getEnvironment().killAgent("robinson");
			logger.info(agent.name + " has died.");
		} else if(this.hunger.get(agent) >= 5) {
			// TODO percept hungry
			this.environment.addEventPercept(agent.name, "hungry", new PerceptAnnotation("distress"));
			logger.info(agent.name + " is hungry.");
		}
		
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

	
	
	/**
	 * INNER ITEM CLASSES
	 */
	
	public static class Food extends Item {
		static final String itemName = "food";
		
		public String getItemName() {
			return Food.itemName;
		}
		
		public boolean isEdible() {
			return true;
		}
	}
	
	
	
	
	/**
	 * HELPER METHOD FOR HASHMAPS
	 */
	private void changeAllValues(HashMap<Character, Integer> hashMap, int newValue) {
		for(Character agent: this.characters.values()) {
			// if there already has been a value, it will be replaced (see javadoc of put)
			// if there hasn't been a value, the new one will be added there
			hashMap.put(agent, newValue);
		}
	}

	/**
	 * Increases the value of the given Character in the HashMap by the given increment.
	 * Give a negative increment to decrease the value.
	 * This is used for changing the number of friends or the hunger value.
	 * 
	 * @param hashMap
	 * 			the Mapping in which the Character's value should be changed
	 * @param agent
	 * 			the Character who's value should be changed
	 * @param increment
	 * 			by how much the value should be changed. Can also be negative.
	 */
	private void increaseIndividualValue(HashMap<Character, Integer> hashMap, Character agent, int increment) {
		// number of friends for this agent increases by one
		hashMap.replace(agent, hashMap.get(agent) + increment);
	}

}
