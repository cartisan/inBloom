package little_red_hen;

import java.util.HashMap;
import java.util.logging.Logger;

import little_red_hen.jason.FarmEnvironment;

/* Model class for Farm Environment */

public class FarmModel {
	public static enum WHEAT_STATE {SEED, GROWING, RIPE, HARVESTED, FLOUR;}
	static Logger logger = Logger.getLogger(FarmModel.class.getName());

	public Wheat wheat;
	public HashMap<String, Agent> agents;
	private int actionCount;
	private FarmEnvironment environment;
	
	
	public FarmModel(HashMap<String, Agent> agents, FarmEnvironment env) {
		this.actionCount = 0;
		this.agents = agents;
		this.wheat = null;
		this.environment = env;
	}
	
	public Agent getAgent(String name) {
		return this.agents.get(name);
	}
	
	public boolean randomFarming(Agent agent) {
		this.actionCount += 1;
		logger.info("Some farming activity was performed");
		
		if (this.actionCount == 3) {
			this.wheat = new Wheat();
			agent.addToInventory(this.wheat);					//also: [emotion(joy),emotion(gratitude)]
			this.environment.addToListCurrentEvents(agent.name, "found(wheat)[emotion(joy)]");  
			
			logger.info(agent.name + " found wheat grains");
		}
		
		return true;
	}
	
	public boolean plantWheat(Agent agent) {
		Wheat wheatItem = (Wheat) agent.get(Wheat.itemName);
		if (!(wheatItem == null)) {
				if (wheatItem.state == WHEAT_STATE.SEED) {
					this.wheat.state = WHEAT_STATE.GROWING;
					logger.info("Wheat planted");
					return true;
				}
		}
		
		return false;
	}
	
	public boolean tendWheat() {
		if ((this.wheat.state == WHEAT_STATE.GROWING)){
			this.wheat.state = WHEAT_STATE.RIPE;
			logger.info("Wheat has grown and is ripe now");
			return true;
		}
		
		return false;
	}
	
	public boolean harvestWheat() {
		if ((this.wheat.state == WHEAT_STATE.RIPE)){
			this.wheat.state = WHEAT_STATE.HARVESTED;
			logger.info("Wheat was harvested");
			return true;
		}
		
		return false;
	}
	
	public boolean grindWheat() {
		if ((this.wheat.state == WHEAT_STATE.HARVESTED)){
			this.wheat.state = WHEAT_STATE.FLOUR;
			logger.info("Wheat was ground to flour");
			this.wheat = null;
			return true;
		}
		return false;
	}

	public boolean bakeBread(Agent agent) {
		Wheat wheatItem = (Wheat) agent.get(Wheat.itemName);
		if((!(wheatItem == null)) & (wheatItem.state == WHEAT_STATE.FLOUR)) {
			agent.addToInventory(new Bread());
			agent.removeFromInventory(wheatItem);
			
			logger.info(agent.name + ": baked some bread.");
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
			if (state == WHEAT_STATE.SEED) {
				return "wheat(seed)";
			}
			
			if (state == WHEAT_STATE.GROWING) {
				return "wheat(growing)";
			}

			if (state == WHEAT_STATE.RIPE) {
				return "wheat(ripe)";
			}
			
			if (state == WHEAT_STATE.HARVESTED) {
				return "wheat(harvested)";
			}
			
			if (state == WHEAT_STATE.FLOUR) {
				return "wheat(flour)";
			}
		
			return null;
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
