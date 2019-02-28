package plotmas.stories.little_red_hen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import plotmas.LauncherAgent;
import plotmas.PlotModel;
import plotmas.helper.PerceptAnnotation;
import plotmas.storyworld.HappeningDirector;
import plotmas.storyworld.Item;
import plotmas.storyworld.Character;


/**
 * Custom model of the story-world of the "Tale of the Little Red Hen".
 * @author Leonid Berov
 */
public class FarmModel extends PlotModel<FarmEnvironment>{
	
	public static enum WHEAT_STATE {SEED, GROWING, RIPE, HARVESTED, FLOUR;}

	public Wheat wheat;
	public int actionCount;
	public boolean wheatFound;
	
	public Cheese cheese;
	public HashMap<String, List<String>> locations;
	
	public FarmModel(List<LauncherAgent> agents, HappeningDirector hapDir) {
		super(agents, hapDir);
		this.locations = new HashMap<String, List<String>>();
		this.locations.put("other",new LinkedList<String>());
		this.locations.put("onTree",new LinkedList<String>());
		this.locations.put("underTree",new LinkedList<String>());
		for(String agentname :this.characters.keySet()) {
			this.locations.get("other").add(agentname);
		}

		this.actionCount = 0;
		this.wheat = null;
		this.wheatFound = false;
		
		this.cheese = new Cheese();
		if( this.characters.containsKey("crow")) {
			getCharacter("crow").addToInventory(cheese);			
		}

	}

	
	public boolean farmWork(Character agent) {
		if (agent.farmAnimal()) {
			this.actionCount += 1;
			logger.info("Some farming activity was performed");
			return true;
		}
		else if (!agent.farmAnimal()) {  
			logger.info(agent.name + " watches other animals doing some farm work.");
			return false; //hier ggf. return true: Stimmung wird immer schlechter, weil farmWork nicht ausgeführt werden kann
		}
		return false;
	}
	
	public boolean plantWheat(Character agent) {
		Wheat wheatItem = (Wheat) agent.get(Wheat.itemName);
		if (!(wheatItem == null)) {
				if (wheatItem.state == WHEAT_STATE.SEED) {
					this.wheat.state = WHEAT_STATE.GROWING;
					this.environment.addEventPerception(agent.name, "plant(wheat)", PerceptAnnotation.fromEmotion("pride"));
					logger.info("Wheat planted");
					return true;
				}
		}
		
		return false;
	}
	
	public boolean tendWheat(Character agent) {
		if ((this.wheat.state == WHEAT_STATE.GROWING)){
			this.wheat.state = WHEAT_STATE.RIPE;
			logger.info("Wheat has grown and is ripe now");
			this.environment.addEventPerception(agent.name, "tend(wheat)", PerceptAnnotation.fromEmotion("pride"));
			return true;
		}
		
		return false;
	}
	
	public boolean harvestWheat(Character agent) {
		if ((this.wheat.state == WHEAT_STATE.RIPE)){
			this.wheat.state = WHEAT_STATE.HARVESTED;
			logger.info("Wheat was harvested");
			this.environment.addEventPerception(agent.name, "harvest(wheat)", PerceptAnnotation.fromEmotion("pride"));
			return true;
		}
		
		return false;
	}
	
	public boolean grindWheat(Character agent) {
		if ((this.wheat.state == WHEAT_STATE.HARVESTED)){
			this.wheat.state = WHEAT_STATE.FLOUR;
			logger.info("Wheat was ground to flour");
			this.environment.addEventPerception(agent.name, "grind(wheat)", PerceptAnnotation.fromEmotion("pride"));
			return true;
		}
		return false;
	}

	public boolean bakeBread(Character agent) {
		Wheat wheatItem = (Wheat) agent.get(Wheat.itemName);
		if((!(wheatItem == null)) & (wheatItem.state == WHEAT_STATE.FLOUR)) {
			agent.addToInventory(new Bread());
			agent.removeFromInventory(wheatItem);
			
			logger.info(agent.name + ": baked some bread.");
			this.environment.addEventPerception(agent.name, "bake(bread)", new PerceptAnnotation("pride", "joy"));
			return true;
		}
		
		return false;
	}

	
	public boolean strolling(Character agent) {
		if (!agent.flying() & this.locations.get("other").contains(agent.name))  {
			logger.info(agent.name + " strolls around");
			return true;
		}
		return false;
	}

	
	public boolean approach(Character agent, String location) {
		if (location.equals("tree")) {
			if(agent.flying() & this.locations.get("other").contains(agent.name)) { 
				logger.info(agent.name + ": flies to tree");
				if (agent.has("cheese"))
					logger.info(agent.name + " has cheese");
				for (String charName : this.characters.keySet()) {
					if (!charName.equals(agent.name)) {
						Character viewer = this.characters.get(charName);
						for (Item item : agent.inventory) {
							if (atTree(agent.name, viewer.name)) {
								this.environment.addEventPerception(charName, "sees(" + item + ")[location(tree), owner("+agent.name+")]"); 
								logger.info(charName + " sees " + agent.name + " with " + item);
							}
						}
					}
				}
				this.locations.get("onTree").add(agent.name);
				this.locations.get("other").remove(agent.name);	
				return true;
			}
			else if (!agent.flying() & this.locations.get("other").contains(agent.name)) {
				logger.info(agent.name + " walks under the tree and says: Good day, Mistress Crow.");
				this.locations.get("underTree").add(agent.name);
				this.locations.get("other").remove(agent.name);
				return true;
			}
		}
		logger.info(agent.name + " cannot find the right way.");
		return false;
	}

	public boolean threaten(Character agent, Character target) {
		for (Item item : target.inventory) {
			if (atTree(agent.name, target.name)) {
				logger.info(agent.name + " tells " +target.name + " to hand over " + item + " or " + agent.name + " will take it from " +target.name + " with force.");
				this.environment.addEventPerception(target.name, "threat(" + item + ")owner("+agent.name+")]", new PerceptAnnotation("fear"));
				return true;
			}
		}
		return false;
	}
	
	public boolean flatter(Character agent, Character target) {
		if(atTree(agent.name, target.name) & !over(agent.name, target.name)) { //!over() kann ggf. auch weggelassen werden -->  Erklärung: ich dachte, dass es fox nicht viel bringt wenn er auf dem Baum sitzt und crow unter dem Baum singt - dann kommt fox trotzdem nicht an den Käse
			logger.info(agent.name + " flatters "+ target.name);
			this.environment.addEventPerception(target.name, "compliment", new PerceptAnnotation("pride"));
			return true;
		}
		return false;
	}
		
	
	public boolean ask(Character agent, Character target) {
		for (Item item : agent.inventory) {
			if (under(agent.name, target.name)) {
				logger.info(agent.name + " asks " +target.name + " to share " + item +" with " + agent.name);
				this.environment.addEventPerception(target.name, "politeQuery(" + item + ")[owner(" + target.name + ")]", new PerceptAnnotation("pity"));
				return true;
			}
		}
		return false;
	}
	
	public boolean sing(Character agent) { 
		if(agent.flying() & !agent.inventory.isEmpty()) {
			for (Item item : agent.inventory) {		
				agent.removeFromInventory(item);
				for (String listener : this.characters.keySet()) {
					if (!agent.name.equals(listener)) { 	// diese condition sollte eigentlich nicht nötig sein, aber komischerweise bekommt fox sonst keine Perception über "is_dropped("cheese")"
						logger.info(agent.name + ": lifted up her head and began to caw her best, ");
						logger.info(agent.name + ": but the moment she opened her mouth the piece of cheese fell to the ground ");
						this.environment.addEventPerception(listener, "is_dropped(" + item.getItemName() + ")");
						this.locations.get("underTree").add(item.getItemName());
						return true;
					}
				}
			}
		}
		else if (agent.flying()) {
			logger.info(agent.name + " sings a song.");
			return true;
		}
		return false;
	}
	
	public boolean collect(Character agent, String thing) {
		if(this.locations.get("underTree").contains(thing)){
			agent.addToInventory(cheese); //unschön gelöst. Besser: entweder einen String übergeben oder String in Item umwandeln
			this.locations.get("other").remove(thing);
			return true;
		}
		return false;
	}
	
	public boolean handOver(Character agent, String receiver, String item) {
		if (agent.has(item) & atTree(agent.name, receiver)){
			Character other = this.characters.get(receiver);
			other.addToInventory(cheese);		//unschön gelöst.
			agent.removeFromInventory(cheese);		//unschön gelöst.
			logger.info(agent.name + " hands over " + item + " to " +receiver);
			return true;
		}
		return false;
	}
	
	public boolean refuseToGive(Character agent, String enemy, String item) {
		if (agent.has(item) & atTree(agent.name, enemy)){
			logger.info(agent.name + " does not hand over" + item + " to " +enemy);
			return true;
		}
		return false;
	}
	
	/****** helper classes *******/
	
	public class Wheat extends Item {
		static final String itemName = "wheat";
		public WHEAT_STATE state = WHEAT_STATE.SEED;
		
		@Override
		public String literal() {
			String res = "wheat[state(%s)]";
			return String.format(res, state.toString().toLowerCase());
		}

		@Override
		public String getItemName() {
			return itemName;
		}

	}
	
	class Bread extends Item {
		static final String itemName = "bread";
		
		@Override
		public String getItemName() {
			return itemName;
		}
		
		public String literal() {
			return "bread";
		}
		
		public boolean isEdible() {
			return true;
		}
	}
	
	public class Cheese extends Item {
		static final String itemName = "cheese";
		
		@Override
		public String getItemName() {
			return itemName;
		}
		
		public String literal() {
			return "cheese";
		}
		
		public boolean isEdible() {
			return true;
		}

	}
	
	public boolean samePlace(String agent1, String agent2){
		if(locations.get("other").contains(agent1) & locations.get("other").contains(agent2)) {
			return true;
		}
		else if (locations.get("underTree").contains(agent1) & locations.get("underTree").contains(agent2)) {
			return true;
		}
		else if (locations.get("onTree").contains(agent1) & locations.get("onTree").contains(agent2)) {
			return true;
		}
		return false;
	}
	
	public boolean under(String agent1, String agent2) {
		if (locations.get("underTree").contains(agent1) & locations.get("onTree").contains(agent2)) {
			return true;
		}
		return false;
	}
	
	public boolean over(String agent1, String agent2) {
		if (locations.get("onTree").contains(agent1) & locations.get("underTree").contains(agent2)) {
			return true;
		}
		return false;
	}
	
	public boolean atTree(String agent1, String agent2) {
		if(samePlace(agent1, agent2)) {
			return true;
		}
		else if(under(agent1, agent2)) {
			return true;
		}
		else if(over(agent1, agent2)) {
			return true;
		}
		return false;
	}
}