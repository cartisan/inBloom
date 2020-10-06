package inBloom.rl_happening.islandWorld;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.helper.EnvironmentListener;
import inBloom.helper.PerceptAnnotation;
import inBloom.rl_happening.rl_management.FeaturePlotModel;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.Item;
import inBloom.storyworld.Location;
import inBloom.storyworld.ModelState;
import jason.asSyntax.Literal;
import inBloom.storyworld.Character;

/**
 * @author Julia Wippermann
 * @version 20.11.19
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
	// Each agent has a fatigue value
	public HashMap<Character, Integer> fatigue;
	// Each agent has a poison value
	public HashMap<Character, Integer> poison;
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
	
	public IslandModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		
		super(agentList, hapDir);		
		
		// numberOfFriends: each Character has 0 friends
		this.friends = new HashMap<Character, Integer>();
		changeAllValues(this.friends, 0);
		
		// hunger: each Character isn't hungry yet
		this.hunger = new HashMap<Character, Integer>();
		changeAllValues(this.hunger, 0);
		
		this.fatigue = new HashMap<Character, Integer>();
		changeAllValues(this.fatigue, 0);
		
		this.poison = new HashMap<Character, Integer>();
		changeAllValues(this.poison, 0);
		
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
		logger.info(agent.name + " went on a cruise.");
				
		agent.goTo(this.ship);
		logger.info(agent.name + " is on ship " + this.ship.name);
		
		result.addPerception(agent.name, new PerceptAnnotation("hope"));
		result.success = true;
		
		// activate Feature
		this.activateFeature(onCruise);
		
		this.getStateValue();
		
		return result;
	}
	
	public ActionReport shipWrecked(Character agent) {

		ActionReport result = new ActionReport();

		if(agent.location == this.ship) {
			
			this.environment.addPercept(agent.name, Literal.parseLiteral("shipWrecked"));	
			result.addPerception(agent.name, new PerceptAnnotation("fear"));	
			
		}
		
		return result;
	}
	
	public ActionReport swimToIsland(Character agent) {
		
		ActionReport result = new ActionReport();
		
		if(agent.location != this.island) {
			
			result = agent.goTo(island);
			deactivateFeature(IslandModel.onCruise);
			activateFeature(IslandModel.onIsland);
			getLogger().info(agent + " stranded on island " + island.name);

		}
		return result;
	}
	
	public ActionReport stayHome(Character agent) {
		
		ActionReport result = new ActionReport();
	
		logger.info(agent.name + " stayed home.");
		
		this.environment.addPercept(agent.name, Literal.parseLiteral("endStory"));
		
		result.success = true;
		
		return result;
	}

	public ActionReport findFriend(Character agent) {
		
		ActionReport result = new ActionReport();

		if(agent.location==this.island && !this.island.isRaining()) {

		logger.info(agent.name + " has found a friend.");

		// number of friends for this agent increases by one
		changeIndividualValue(this.friends, agent, 1);
		
		logger.info(agent.name + " know has " + this.friends.get(agent) + " friends.");
		
		this.environment.addPercept(agent.name, Literal.parseLiteral("has(friend)"));

		result.addPerception(agent.name, new PerceptAnnotation("joy"));
		result.success = true;
		
		this.activateFeature(friend);
		
		}

		return result;
	}

	public ActionReport getFood(Character agent) {
		
		ActionReport result = new ActionReport();
		
		if(agent.location==this.island && !this.island.isRaining()) {
			
			logger.info(agent.name + " looked for food.");
			
			// TODO here a happening could intrude
			// but for now: if he look for food, he finds food
			// and immediately eats it
			
			agent.addToInventory(new Food());
			// new food isn't poisoned yet
			//this.foodIsOkay = true;
	
			this.environment.addPercept(agent.name, Literal.parseLiteral("has(food)"));
			
			result.success = true;
			
			this.activateFeature(hasFood);
		}
		return result;
	}

	public ActionReport eat(Character agent) {
		
		ActionReport result = new ActionReport();

		if(agent.location==this.island) {
			
			// TODO könnte man auch in Character fast alles auslagern -> z.B. Hunger,
			// wobei die percepts eigentlich eher hier gesteucert werden sollten
			
			if(agent.has("food")) {
				
				// save the food for checking it's poisoness later
				Food food = (Food)agent.get("food");
				//agent.removeFromInventory(food);
				
				// In any case, the agent will eat
				result = agent.eat("food");
				logger.info(result.success + "Success");
				logger.info(agent.name + " eats food.");
	
				// only if this was the only food that the agent owned, we can deactivate the feature
				if(!agent.has("food")) {
					this.deactivateFeature(hasFood);
					this.environment.removePercept(agent.name, Literal.parseLiteral("has(food)"));
				}
				
				// he is not hungry anymore
				this.hunger.replace(agent, 0);
	
				this.environment.removePercept(agent.name, Literal.parseLiteral("hungry"));
				logger.info(agent.name + "'s hunger: " + this.hunger.get(agent));
				
				this.deactivateFeature(hungry);
				
				// the Food may have been poisoned though
				if(food.isPoisoned()) {
					// TODO es wäre natürlich schöner, das hier direkt im Agent zu modellieren
					agent.getPoisoned();
					result.addPerception(agent.name, new PerceptAnnotation("distress"));
					this.environment.addPercept(agent.name, Literal.parseLiteral("sick"));
					logger.info(agent.name + " is sick.");
					
					this.deactivateFeature(poisonedFood);
					this.activateFeature(sick);
					
					// hier könnte man result.success = false setzen, aber an sich hat er ja gegessen
				}
			}else {
				
				// Robinson feels distress since his already build hut was destroyed.
				result.addPerception(agent.name, new PerceptAnnotation("disappointment"));
				getFood(agent);
			}
		}
		return result;
	}

	public ActionReport findHealingPlants(Character agent) {
		
		ActionReport result = new ActionReport();
		
		// if agent was sick, then now he isn't anymore
		if(agent.location==this.island && this.island.hasHealingPlants() && !this.island.isRaining()) {
					
			logger.info(agent.name + " has found a healing plants.");
			
			agent.addToInventory(new HealingPlant());
	
			this.environment.addPercept(agent.name, Literal.parseLiteral("has(healingPlant)"));
			
			result.success = true;
			
			//this.activateFeature(hasHealingPlant);
		}
		return result;
	}
	
	public ActionReport useHealingPlants(Character agent) {
		
		ActionReport result = new ActionReport();

		// if agent was sick, then now he isn't anymore
		if(agent.location==this.island && agent.isSick && agent.has("healingPlant")) {
			
			// Remove Item
			agent.removeFromInventory("healingPlant");
			this.environment.removePercept(agent.name, Literal.parseLiteral("has(healingPlant)"));
			
			// Heal agent
			agent.heal();
			this.poison.replace(agent, 0);
			result.addPerception(agent.name, new PerceptAnnotation("relief"));

			this.environment.removePercept(agent.name, Literal.parseLiteral("sick"));
			this.deactivateFeature(sick);
			logger.info(agent.name + " cured himself with a healing plant!");
			
			result.success = true;
		}
		
		return result;
	}
	
	public ActionReport sleep(Character agent) {
		
		ActionReport result = new ActionReport();
		
		if(agent.location==this.island && !this.island.isRaining()) {
			
			// you can only sleep if you have a safe place to sleep in
			if(this.island.hasHut()) {
				
				/*
				 * 1. GO TO HUT
				 */
				
				this.island.enterSublocation(agent, island.hut.name);
				logger.info(agent.name + " is in the hood. Hut I mean. This hut: " + island.hut.name);
				
				
				/*
				 * 2. SLEEP
				 */
				
				logger.info(agent.name + " is asleep.");
				result.addPerception(agent.name, new PerceptAnnotation("gratification"));
				
				
				/*
				 * 3. LEAVE HUT
				 */
				
				this.island.leaveSublocation(agent, island.hut.name);
				logger.info(agent.name + " has left the hut " + island.hut.name);
				
				this.fatigue.replace(agent, 0);
				result.success = true;
				
				
			} else {
				
				// Robinson feels distress since his already build hut was destroyed.
				result.addPerception(agent.name, new PerceptAnnotation("distress"));
				buildHut(agent);
			}
		}	
		
		return result;
	}
	
	public ActionReport buildHut(Character agent) {
		
		ActionReport result = new ActionReport();
		
		if(agent.location==this.island && !this.island.isRaining()) {
		
			logger.info(agent.name + " builds a hut.");
			this.island.buildHut();
			
			result.success = true;
			
			// all agents on the island get the percept
			this.environment.addPercept(this.island, Literal.parseLiteral("exists(hut)"));
			
			this.activateFeature(hut);
			
			result.addPerception(agent.name, new PerceptAnnotation("pride"));
		}
		
		return result;
	}
	
	public ActionReport complain(Character agent) {
		
		ActionReport result = new ActionReport();
		
		if(agent.location==this.island && !this.island.isRaining()) {
			
			// you can only complain if you have a friend
			if(this.friends.get(agent) > 0) {
				
				logger.info(agent.name + " complained.");
				result.success = true;
				
			} else {
				result.success = false;
			}
		}
		return result;
		
	}
	
	public ActionReport extinguishFire(Character agent) {
		
		ActionReport result = new ActionReport();
		
		if(agent.location==this.island && island.isBurning() && !this.island.isRaining()) {
		
			this.island.extinguishFire();
			this.environment.removePercept(this.island, Literal.parseLiteral("fire"));
			result.addPerception(agent.name, new PerceptAnnotation("pride"));
			logger.info(agent.name + " has extinguished the fire.");
			
			this.deactivateFeature(fire);
			
			result.success = true;
		}
		return result;
	}
	
	public ActionReport happyEnd(Character agent) {
		
		ActionReport result = new ActionReport();
		
		logger.info("And Robinson lived happily ever after!");
		
		result.addPerception(agent.name, new PerceptAnnotation("joy"));
		
		getEnvironment().MAX_STEP_NUM =	getEnvironment().getStep();
		
		result.success=true;
		
		return result;
		
	}
		
	public ActionReport goOnShip(Character agent) {
		
		ActionReport result = new ActionReport();
		
		logger.info("Robinson got rescued and lived happily ever after!");
		
		result.addPerception(agent.name, new PerceptAnnotation("relief"));
		
		getEnvironment().MAX_STEP_NUM =	getEnvironment().getStep();
		
		result.success=true;
		
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
		if(this.hunger.get(agent) >= 12) {
			this.getEnvironment().killAgent(agent.name);
			logger.info(agent.name + " has died.");
			
			this.deactivateFeature("isAlive");
			
		// Add hungry percept
		} else if(this.hunger.get(agent) >= 6) {
			if(!this.environment.containsPercept(agent.name, Literal.parseLiteral("hungry"))) {
				this.environment.addPercept(agent.name, Literal.parseLiteral("hungry"));
				this.activateFeature(hungry);
				logger.info(agent.name + " is hungry.");		
			}
		}	
	}

	/**
	 * Increases the sleep value of one agent by 1 and checks if the sleep value surpassed
	 * the thresholds of being actively sleepy (10) or dying of fatigue (20) and reacts to these
	 * changes by either making the agent sleepy or die.
	 * 
	 * @param agent
	 * 			The character whose hunger is to be increased
	 */
	public void increaseFatigue(Character agent) {
		
		// increase hunger by 1
		changeIndividualValue(this.fatigue, agent, 1);

		// check if fatigue has become critical
		if(this.fatigue.get(agent) >= 24) {
			this.getEnvironment().killAgent(agent.name);
			logger.info(agent.name + " has died.");
			
			this.deactivateFeature("isAlive");

		// Add fatigue percept
		} else if(this.fatigue.get(agent) >= 16) {
			if(!this.environment.containsPercept(agent.name, Literal.parseLiteral("fatigue"))) {
				this.environment.addPercept(agent.name, Literal.parseLiteral("fatigue"));
				//this.activateFeature(fatigue);
				logger.info(agent.name + " is sleepy.");		
			}
		}		
	}

	/**
	 * Increases the poison value of one agent by 1 and checks if the hunger value surpassed
	 * the threshold of dying of poison (10) and reacts to these change making the agent die.
	 * 
	 * @param agent
	 * 			The character whose poison is to be increased
	 */
	public void increasePoison(Character agent) {
		
		// increase hunger by 1
		changeIndividualValue(this.poison, agent, 1);

		// check if poison has become critical
		if(this.poison.get(agent) >= 6) {
			this.getEnvironment().killAgent(agent.name);
			logger.info(agent.name + " has died.");
			
			this.deactivateFeature("isAlive");			
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
			logger.info(agent.name + " has lost a friend.");
			logger.info(agent.name + " know has " + this.friends.get(agent) + " friends.");
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
			super("plainBoringWorld");
		}
		
	}
	
	public static class Ship extends Location {

		public Ship() {
			super("magnificentShip");
		}
		
	}
	
	public static class Island extends Location {

		//public boolean hasHut;
		private boolean isBurning;
		private Hut hut;
		private int growTime;
		private static int healingPlantGrowTime = 6;
		private int torrentialRain;
		private static int rainDuration = 6;
		
		
		public Island() {
			super("lonelyIsland");
			this.isBurning = false;
			this.hut = null;
			torrentialRain=0;	
			growTime=0;
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
		
		public void startFire() {
			isBurning = true;
		}
		
		public void extinguishFire() {
			isBurning = false;
		}
		
		public boolean hasHealingPlants() {
			
			if(growTime == 0)
				return true;
						
			return false;
		}
		
		public void removeHealingPlants() {
			
			growTime = healingPlantGrowTime;
		}
		
		public void growPlants() {
			
			if(growTime > 0)
				growTime -= 1;
		}
		
		public boolean isRaining() {
			
			if(torrentialRain > 0)
				return true;
			return false;
		}
		
		public void startRain() {
			torrentialRain = rainDuration;
		}
		
		public void continueRain() {
			if(torrentialRain > 0)
				torrentialRain -= 1;
		}
		
		public boolean isBurning() {
			return this.isBurning;
		}
		
		private class Hut extends Location {

			public Hut() {
				super("cozyHut");
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
	
	public static class HealingPlant extends Item {
		
		static final String itemName = "healingPlant";
		
		public String getItemName() {
			return HealingPlant.itemName;
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