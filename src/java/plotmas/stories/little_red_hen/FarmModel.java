package plotmas.stories.little_red_hen;

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
	
	
	public FarmModel(List<LauncherAgent> agents, HappeningDirector hapDir) {
		super(agents, hapDir);
		
		this.actionCount = 0;
		this.wheat = null;
		this.wheatFound = false;
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
}