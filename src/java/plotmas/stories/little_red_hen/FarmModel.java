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
	public HashMap<String, List<Character>> locations;
	
	public FarmModel(List<LauncherAgent> agents, HappeningDirector hapDir) {
		super(agents, hapDir);
		this.locations = new HashMap<String, List<Character>>();
		this.locations.put("other",new LinkedList<Character>());
		this.locations.put("onTree",new LinkedList<Character>());
		this.locations.put("underTree",new LinkedList<Character>());
		for(String agentname :this.characters.keySet()) {
			this.locations.get("other").add(this.characters.get(agentname));
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
		this.actionCount += 1;
		logger.info("Some farming activity was performed");
		return true;
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
	
	public boolean enterScene(Character agent) {
		if(!this.locations.get("onTree").contains(agent) & agent.getName() == "crow" ) { 
			logger.info(agent.name + ": flew to tree");
			if (agent.has("cheese"))
				logger.info(agent.name + " has cheese");
			this.locations.get("onTree").add(agent);
			this.locations.get("other").remove(agent);
			for (String charName : this.characters.keySet()) {
				if(!agent.getName().contentEquals(charName)) {
					this.environment.addEventPerception(charName, "want(cheese)");
				}
			};
		
			return true;
		}
		else if (!(agent.getName() == "crow")){
			logger.info(agent.name + " strolls around");
			return true;
		}
		return false;
	}
	
	public boolean goToTree(Character agent, Character target) {
		if (!agent.has("cheese") & !this.locations.get("underTree").contains(agent)) {
			logger.info(agent.name + ": Good day, Mistress Crow.");
			this.locations.get("underTree").add(agent);
			this.locations.get("other").remove(agent);
			return true;
		}
		
		return false;
	}
	
	public boolean flatter(Character agent, Character target) {
		if(!agent.has("cheese") & this.locations.get("underTree").contains(agent)) {
			logger.info(agent.name + ": How well you are looking today: how glossy your feathers; how bright your eye. "
					+ "I feel sure your voice must surpass that of other birds, just as your figure does; "
					+ "let me hear but one song from you that I may greet you as the Queen of Birds.");
			this.environment.addEventPerception(target.name, "compliment", new PerceptAnnotation("joy"));
			return true;
		}
		
		return false;
	}
	
	public boolean sing(Character agent, Character listener) { 
		if (agent.has("cheese") & this.locations.get("onTree").contains(agent)) {
			listener.addToInventory(cheese);
			agent.removeFromInventory(cheese);
			logger.info(agent.name + ": lifted up her head and began to caw her best, "
					+ "but the moment she opened her mouth the piece of cheese fell to the ground, "
					+ "only to be snapped up by: " + listener.name);
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
	
}