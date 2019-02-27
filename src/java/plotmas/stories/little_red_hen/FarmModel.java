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
			this.locations.get("other").add(this.characters.get(agentname).name);
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
	
	public boolean approachTree(Character agent) {
		if(agent.flying() & this.locations.get("other").contains(agent.name)) { 
			logger.info(agent.name + ": flies to tree");
			if (agent.has("cheese"))
				logger.info(agent.name + " has cheese");
			for (String charName : this.characters.keySet()) {
				if (!charName.equals(agent.name)) {
					Character viewer = this.characters.get(charName);
					for (Item item : agent.inventory) {
						if (samePlace(agent.name, viewer.name)) {
							this.environment.addEventPerception(charName, "sees(" + item + ")[location(tree)]"); 
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
		return false;
	}
	
	public boolean flatter(Character agent) {
		for (String charKey : this.characters.keySet()) {
			if (!charKey.equals(agent.name)) {
				Character target = this.characters.get(charKey);
				if(under(agent.name, target.name)) {
					logger.info(agent.name + "flatters "+ target.name);
					this.environment.addEventPerception(target.name, "compliment", new PerceptAnnotation("joy"));
					return true;
				}
			}
		}
		return false;
	}
		
	public boolean threaten(Character agent) {
		for (String charKey : this.characters.keySet()) {
			if (!charKey.equals(agent.name)) {
				Character target = this.characters.get(charKey);
				if (samePlace(agent.name, target.name)) {
					logger.info(agent.name + " tells " +target.name + " to hand over cheese or " + agent.name + " will take it from " +target.name + " with force.");
					this.environment.addEventPerception(target.name, "threat", new PerceptAnnotation("fear"));
					return true;
				}
			}	
		}
		return false;
	}
	
	public boolean ask(Character agent) {
		for (String charKey : this.characters.keySet()) {
			if (!charKey.equals(agent.name)) {
				Character target = this.characters.get(charKey);
				if (under(agent.name, target.name)) {
					logger.info(agent.name + " asks " +target.name + " to hand share cheese with " + agent.name);
					this.environment.addEventPerception(target.name, "politeQuery", new PerceptAnnotation(""));
					return true;
				}
			}	
		}
		return false;
	}
	
	public boolean sing(Character agent) { 
		for (String charKey : this.characters.keySet()) {
			if (!charKey.equals(agent.name)) {
				Character listener = this.characters.get(charKey);
				if (over(agent.name, listener.name) & agent.has("cheese")) {
					agent.removeFromInventory(cheese);
					logger.info(agent.name + ": lifted up her head and began to caw her best, "
							+ "but the moment she opened her mouth the piece of cheese fell to the ground ");
					this.locations.get("underTree").add(cheese.getItemName());
					return true;
				}
				else if (this.locations.get("onTree").contains(agent.name) & !agent.has("cheese") ) {
					logger.info(agent.name + " sings a song to " + listener.name);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean collect(Character agent, String thing) {
		if(this.locations.get("underTree").contains(thing)){
			agent.addToInventory(cheese); //unschön gelöst. Ich würde lieber entweder einen String übergeben oder String in Item umwandeln
			this.locations.get("other").remove(thing);
			return true;
		}
		return false;
	}
	
	public boolean handOver(Character agent) {
		for (String charKey : this.characters.keySet()) {
			if (!charKey.equals(agent.name)) {
				Character receiver = this.characters.get(charKey);
				if (agent.has("cheese") & samePlace(agent.name, receiver.name)){
					receiver.addToInventory(cheese);
					agent.removeFromInventory(cheese);
					logger.info(agent.name + " hands over Cheese to " +receiver.name);
					return true;
				}
				else if (agent.has("cheese") & over(agent.name, receiver.name)){
					receiver.addToInventory(cheese);
					agent.removeFromInventory(cheese);
					logger.info(agent.name + " hands over Cheese to " +receiver.name);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean refuse(Character agent) {
		for (String charKey : this.characters.keySet()) {
			if (!charKey.equals(agent.name)) {
				Character enemy = this.characters.get(charKey);
				if (agent.has("cheese") & samePlace(agent.name, enemy.name)){
					logger.info(agent.name + " does not hand over Cheese to " +enemy.name);
					return true;
				}
				else if (agent.has("cheese") & over(agent.name, enemy.name)){
					logger.info(agent.name + " does not hand over Cheese to " +enemy.name);
					return true;
				}
			}
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
	
}