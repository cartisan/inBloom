package inBloom.rl_happening.islandWorld;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.rl_management.FeaturePlotModel;
import inBloom.rl_happening.rl_management.ResultWriter;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.Item;
import inBloom.storyworld.Location;
import inBloom.storyworld.ModelState;
import jason.asSyntax.Literal;
import inBloom.storyworld.Character;

/**
 * @author Julia Wippermann
 * @version 7.8.20
 *
 * The Model defines the effects of Actions on the Environment and Agents.
 */
public class IslandModel extends FeaturePlotModel<IslandEnvironment> {
	
	/**
	 * GLOBAL VARIABLES
	 */
	// One agent can find multiple (anonymous) friends.
	public HashMap<Character, Integer> friends;
	// Each agent has a hunger value
	public HashMap<Character, Integer> hunger;
	@ModelState
	public boolean isOnCruise;
	
	// TODO alles als @ModelState annotieren, was relevant ist für den current State des Models
	
	/**
	 * LOCATIONS
	 */
	public CivilizedWorld civilizedWorld = new CivilizedWorld();
	public Ship ship = new Ship();
	public Island island = new Island();
	
	
	/**
	 * All features as variables -> this way their names can be changed globally without risking errors
	 */
	public static final String alive = "isAlive";
	public static final String onCruise = "onCruise";
	public static final String onIsland = "isOnIsland";
	public static final String hut = "hasHut";
	public static final String friend = "hasAtLeastOneFriend";
	public static final String hungry = "isHungry";
	public static final String hasFood = "ownsFood";
	public static final String sick = "isSick";
	public static final String fire = "isBurning";
	public static final String poisonedFood = "foodIsPoisoned";
	public static final String homesick = "isHomesick";
	
	
	/**
	 * All feature names
	 * Needs to be static in order to create features during initialisation
	 */
	static String[] features = {alive,
								onCruise,
								onIsland,
								hut,
								friend,
								hungry,
								hasFood,
								sick,
								fire,
								poisonedFood,
								homesick};
	
	
	
	/**
	 * CONSTRUCTOR
	 */
	
	public IslandModel(List<LauncherAgent> agentList, HappeningDirector hapDir, ResultWriter writer) {
		
		super(agentList, hapDir, writer);
		
		// numberOfFriends: each Character has 0 friends
		this.friends = new HashMap<Character, Integer>();
		changeAllValues(this.friends, 0);
		
		// hunger: each Character isn't hungry yet
		this.hunger = new HashMap<Character, Integer>();
		changeAllValues(this.hunger, 0);
		
		this.isOnCruise = false;
		
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
		addPlot(agent.name + " went on a cruise.");
				
		agent.goTo(this.ship);
		addPlot(agent.name + " is on a ship with the name " + this.ship.name + ".");
		
		result.addPerception(agent.name, new PerceptAnnotation("hope"));
		result.success = true;
		
		// activate Feature
		this.activateFeature(onCruise);
		
		this.getStateValue();
		
		return result;
	}
	
	public ActionReport stayHome(Character agent) {
		ActionReport result = new ActionReport();
	
		addPlot(agent.name + " stayed home.");
		
		// result.addPerception(agent.name, new PerceptAnnotation("hope"));
		result.success = true;
		
		return result;
	}

	public ActionReport findFriend(Character agent) {
		ActionReport result = new ActionReport();

		addPlot(agent.name + " has found a friend.");

		// number of friends for this agent increases by one
		changeIndividualValue(this.friends, agent, 1);
		
		addPlot(agent.name + " now has " + this.friends.get(agent) + " friends.");
		
		this.environment.addPercept(agent.name, Literal.parseLiteral("has(friend)"));

		result.addPerception(agent.name, new PerceptAnnotation("joy"));
		result.success = true;
		
		this.activateFeature(friend);

		return result;
	}

	public ActionReport getFood(Character agent) {
		ActionReport result = new ActionReport();
		
		addPlot(agent.name + " looked for food.");
		
		// TODO here a happening could intrude
		// but for now: if he look for food, he finds food
		// and immediately eats it
		
		agent.addToInventory(new Food());
		// new food isn't poisoned yet
		//this.foodIsOkay = true;
		
		result.success = true;
		
		this.activateFeature(hasFood);
		
		return result;
	}

	public ActionReport eat(Character agent) {
		
		ActionReport result = new ActionReport();
		
		// TODO könnte man auch in Character fast alles auslagern -> z.B. Hunger,
		// wobei die percepts eigentlich eher hier gesteucert werden sollten
		
		if(agent.has("food")) {
			
			// save the food for checking it's poisoness later
			Food food = (Food)agent.get("food");
			agent.removeFromInventory(food);
			
			// only if this was the only food that the agent owned, we can deactivate the feature
			if(!agent.has("food")) {
				this.deactivateFeature(hasFood);
				this.environment.removePercept(agent.name, Literal.parseLiteral("has(food)"));
			}
			
			// In any case, the agent will eat
			result = agent.eat("food");
			addPlot(agent.name + " eats food.");
			
			// he is not hungry anymore
			this.hunger.replace(agent, 0);
			this.environment.removePercept(agent.name, Literal.parseLiteral("hungry"));
			addPlot(agent.name + "'s hunger is " + this.hunger.get(agent) + ".");
			
			this.deactivateFeature(hungry);
			
			// the Food may have been poisoned though
			if(food.isPoisoned()) {
				// TODO es wäre natürlich schöner, das hier direkt im Agent zu modellieren
				agent.getPoisoned();
				this.environment.addPercept(agent.name, Literal.parseLiteral("sick"));
				addPlot(agent.name + " is sick.");
				
				this.deactivateFeature(poisonedFood);
				this.activateFeature(sick);
				
				// hier könnte man result.success = false setzen, aber an sich hat er ja gegessen
			}
		}		

		return result;
	}
	
	public ActionReport sleep(Character agent) {
		
		ActionReport result = new ActionReport();
		
		// you can only sleep if you have a safe place to sleep in
		if(this.island.hasHut()) {
			
			/*
			 * 1. GO TO HUT
			 */
			
			this.island.enterSublocation(agent, island.hut.name);
			addPlot(agent.name + " is in the hut with the name " + island.hut.name + ".");
			
			
			/*
			 * 2. SLEEP
			 */
			addPlot(agent.name + " is asleep.");
			result.addPerception(agent.name, new PerceptAnnotation("relief"));

			
			/* 
			 * 3. HEAL
			 */
			
			// if agent was sick, then now he isn't anymore
			if(agent.isSick) {
				agent.heal();
			}
			
			this.environment.removePercept(agent.name, Literal.parseLiteral("sick"));
			this.deactivateFeature(sick);
			

			
			/*
			 * 4. LEAVE HUT
			 */
			
			this.island.leaveSublocation(agent, island.hut.name);
			addPlot(agent.name + " has left the hut with the name " + island.hut.name + ".");
			
			
			result.success = true;
			
			
		} else {
			result.success = false;
		}
		
		
		return result;
	}
	
	public ActionReport buildHut(Character agent) {
		
		ActionReport result = new ActionReport();
		
		addPlot(agent.name + " builds a hut.");
		this.island.buildHut();
		
		result.success = true;
		
		// all agents on the island get the percept
		this.environment.addPercept(this.island, Literal.parseLiteral("exists(hut)"));
		
		this.activateFeature(hut);
		
		result.addPerception(agent.name, new PerceptAnnotation("pride"));
		
		return result;
	}
	
	public ActionReport complain(Character agent) {
		
		ActionReport result = new ActionReport();
		
		// you can only complain if you have a friend
		if(this.friends.get(agent) > 0) {
			
			addPlot(agent.name + " complained.");
			this.deactivateFeature(friend);
			result.success = true;
			
		} else {
			result.success = false;
		}
		
		return result;
		
	}
	
	public ActionReport extinguishFire(Character agent) {
		
		ActionReport result = new ActionReport();
		
		this.island.isBurning = false;
		this.environment.removePercept(agent.name, Literal.parseLiteral("fire"));
		result.addPerception(agent.name, new PerceptAnnotation("pride"));
		addPlot(agent.name + " has extinguished the fire.");
		
		this.deactivateFeature(fire);
		
		result.success = true;
		return result;
	}

	
	
	/**
	 * PUBLIC HELPER METHODS TO CHANGE MODEL
	 */
	
	/**
	 * Increases the hunger value of one agent by 1 and checks if the hunger value surpassed
	 * the thresholds of being actively hungry (5) or dying of hunger (10) and reacts to these
	 * changes by either making the agent hungry or die.
	 * 
	 * @param agent
	 * 			The character whose hunger is to be increased
	 */
	public void increaseHunger(Character agent) {
		
		// increase hunger by 1
		changeIndividualValue(this.hunger, agent, 1);

		// check if hunger has become critical

		if(this.hunger.get(agent) >= 10) {
			this.getEnvironment().killAgent(agent.name);
			addPlot(agent.name + " has died.");
			
			this.deactivateFeature("isAlive");
			
			// stop story
			PlotLauncher.getRunner().pauseExecution();
			
		} else if(this.hunger.get(agent) >= 5) {
			this.environment.addPercept(agent.name, Literal.parseLiteral("hungry"));
			this.activateFeature(hungry);
			addPlot(agent.name + " is hungry.");
		}
		
	}

	/**
	 * Decreases the number of friends of the agent by 1, unless the agent has no friends to
	 * start with, in which case nothing happens. If necessary, updates the agents percepts
	 * on having a friend.
	 * 
	 * @param agent
	 * 			The agent whose number of friends is to be decreased
	 */
	public void deleteFriend(Character agent) {
		
		// if the agent had at least 1 friend, delete 1 and show update in the logger
		if(this.friends.get(agent) > 0) {
			this.changeIndividualValue(this.friends, agent, -1);
			this.deactivateFeature(friend);
			addPlot(agent.name + " has lost a friend.");
			addPlot(agent.name + " now has " + this.friends.get(agent) + " friends.");
		}
		
		// if the agent has no friends at all after this, he looses his percept of having a friend
		if(this.friends.get(agent) == 0) {
			this.environment.removePercept(agent.name, Literal.parseLiteral("has(friend)"));
		}
		
		// if the agent has no friends, none will be lost.
		
	}

	/**
	 * Returns the number of friends that an agent has.
	 * 
	 * @param agent
	 * 			The character whose number of friends is asked for
	 * @return
	 * 			The number of friends of the agent
	 */
	public int getNumberOfFriends(Character agent) {
		return this.friends.get(agent);
	}
	
	/**
	 * Returns the number of friends that an agent has.
	 * 
	 * @param agent
	 * 			The character whose number of friends is asked for
	 * @return
	 * 			The number of friends of the agent
	 * @see
	 * 			IslandModel.getNumberOfFriends(Character)
	 */
	public int getNumberOfFriends(String agent) {
		return this.getNumberOfFriends(this.getCharacter(agent));
	}

	/**
	 * Destroys the hut and removes the percept of a hut for all agents on the island
	 */
	public void destroyHut() {
		if(this.island.hasHut()) {
			this.island.destroyHut();
			// this is agent independent -> all agents on the island loose the percept
			this.environment.removePercept(this.island, Literal.parseLiteral("exists(hut)"));
			this.deactivateFeature(hut);
		}
	}
	
	
	
	
	/**
	 * PRIVATE HELPER METHOD FOR HASHMAPS
	 */
	
	/**
	 * Changes all values of a mapping from Character to Integers. For example used to initialize
	 * such a mapping with a useful initial value.
	 * 
	 * @param hashMap
	 * 			The mapping which values are to be changed
	 * @param newValue
	 * 			The value that will be mapped towards all Characters of the mapping
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
	private void changeIndividualValue(HashMap<Character, Integer> hashMap, Character agent, int increment) {
		// number of friends for this agent increases by one
		hashMap.replace(agent, hashMap.get(agent) + increment);
	}

	
	
	
	/**
	 * INNER LOCATION CLASSES
	 */
	
	public static class CivilizedWorld extends Location {

		public CivilizedWorld() {
			super("plain boring world");
		}
		
	}
	
	public static class Ship extends Location {

		public Ship() {
			super("magnificent ship");
		}
		
	}
	
	public static class Island extends Location {

		//public boolean hasHut;
		public boolean isBurning;
		private Hut hut;
		
		public Island() {
			super("lonely island");
			this.isBurning = false;
			this.hut = null;
		}
		
		/**
		 * The sub location hut can be added dynamically during the execution
		 */
		public void buildHut() {
			this.hut = new Hut();
			this.addSublocation(hut);
		}
		
		public void destroyHut() {
			this.destroySublocation(this.hut);
			this.hut = null;
			// TODO das ist ja eigentlich doof mit der lokalen variable noch.
			// Ich brauche sie eigentlich nur, weil ich sonst nicht nach der Hut suchen
			// kann später -> aber wenn ich das über den namen machen klönnte, wäre das
			// natürlich super
			// Dafür die Überlegung, ob man den Hut-namen einigermaßen global speichert
			// z.B. in Island -> oder Hut, die kann ich ja von hier aus aufrufen! Hut.getName oder so!
		}
		
		public boolean hasHut() {
			return hasSublocation(hut);
		}
		
		private class Hut extends Location {

			public Hut() {
				super("cozy hut");
			}
			
		}

	}

	
	
	
	/**
	 * INNER ITEM CLASSES
	 */
	
	public static class Food extends Item {
		static final String itemName = "food";
		private boolean poisoned = false;
		
		public String getItemName() {
			return Food.itemName;
		}
		
		public boolean isEdible() {
			return true;
		}
		
		public boolean isPoisoned() {
			return this.poisoned;
		}
		
		public void poison() {
			this.poisoned = true;
		}
	}




	@Override
	public LinkedList<String> getDomainDependentFeatures() {
		LinkedList<String> allFeatures = new LinkedList<String>();
		for(String i: features) {
			allFeatures.add(i);
		}
		return allFeatures;
	}
	
	public LinkedList<String> getInitiallyActivatedFeatures() {
		
		LinkedList<String> initiallyActivatedFeatures = new LinkedList<String>();
		
		initiallyActivatedFeatures.add(alive);
		
		return initiallyActivatedFeatures;
	}

	
}

// Es gibt eine Möglichkeit zu überprüfen, ob ein Agent einen bestimmten percept hat
// this.environment.containsPercept(agentname, percept)

// TODO consistent use of tense in logger infos about actions