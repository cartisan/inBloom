package little_red_hen;

import java.util.LinkedList;
import java.util.logging.Logger;

/* Model class for Farm Environment */

public class FarmModel {
	private int actionCount;

	public Agent hen;
	public Agent dog;
	public Wheat wheat;
	public static enum WHEAT_STATE {SEED, GROWING, RIPE, HARVESTED, FLOUR;}
	
    static Logger logger = Logger.getLogger(FarmModel.class.getName());
	
	public FarmModel() {
		this.actionCount = 0;
		
		this.hen = new Agent();
		this.dog = new Agent();
		this.wheat = null;
	}
	
	// TODO: Make this nice!!!
	Agent getAgent(String name) {
		if (name.equals("hen")) {
				return hen;
		}
		if (name.equals("dog")) {
			return dog;
		}
		
		return null; 
	}
	
	boolean randomFarming() {
		this.actionCount += 1;
		logger.info("Some farming activity was performed");
		
		if (this.actionCount == 3) {
			this.wheat = new Wheat();
			
			this.hen.addToInventory(this.wheat);
			
			logger.info("LOOK! There are some wheat grains on the floor.");
		}
		
		return true;
	}
	
	boolean plantWheat() {
		Wheat wheatItem = (Wheat) this.hen.get(Wheat.class);
		if (!(wheatItem == null)) {
				if (wheatItem.state == WHEAT_STATE.SEED) {
					this.wheat.state = WHEAT_STATE.GROWING;
					logger.info("Wheat planted");
					return true;
				}
		}
		
		return false;
	}
	
	boolean tendWheat() {
		if ((this.wheat.state == WHEAT_STATE.GROWING)){
			this.wheat.state = WHEAT_STATE.RIPE;
			logger.info("Wheat has grown and is ripe now");
			return true;
		}
		
		return false;
	}
	
	boolean harvestWheat() {
		if ((this.wheat.state == WHEAT_STATE.RIPE)){
			this.wheat.state = WHEAT_STATE.HARVESTED;
			logger.info("Wheat was harvested");
			return true;
		}
		
		return false;
	}
	
	boolean grindWheat() {
		if ((this.wheat.state == WHEAT_STATE.HARVESTED)){
			this.wheat.state = WHEAT_STATE.FLOUR;
			logger.info("Wheat was ground to flour");
			return true;
		}
		return false;
	}

	boolean bakeBread() {
		// TODO: Generalize for non-hen case
		Wheat wheatIteam = (Wheat) this.hen.get(Wheat.class);
		
		if((!(wheat == null)) & (wheat.state == WHEAT_STATE.FLOUR)) {
			this.hen.addToInventory(new Bread());
			this.hen.removeFromInventory(wheatIteam);
			
			this.wheat = null;
			
			logger.info("Baked some bread.");
			return true;
		}
		
		return false;
	}
	

	
	/****** helper classes *******/
	
	class Agent {
		public LinkedList<Item> inventory = new LinkedList<Item>();
		
		public LinkedList<String> createInventoryPercepts() {
			LinkedList<String> invRepr = new LinkedList<String>();
			
			for (Item item : inventory) {
				invRepr.add("has(" + item.literal() + ")");
			}

			return invRepr;
		}
		
		private void addToInventory(Item item) {
			inventory.add(item);
		}
		
		private void removeFromInventory(Item item) {
			inventory.remove(item);
		}
		
		public boolean has(Class clsNme) {
			for (Item item : inventory) {
				if (item.getClass().equals(clsNme)) {
					return true;
				}
			}
			
			return false;
		}
		
		public Item get(Class clsNme) {
			for (Item item : inventory) {
				if (item.getClass().equals(clsNme)) {
					return item;
				}
			}
			
			return null;
		}
		
		public boolean share(Class<Item> itemType, Agent receiver) {
			if (this.has(itemType)) {
				Item item = this.get(itemType);
				receiver.addToInventory(item);
				return true;
			}
			
			return false;
		}
		
		public boolean eat(Class itemType) {
    		if (this.has(itemType)) {
    			Item item = this.get(itemType);
    			
    			if (item.isEdible()) {
    				this.removeFromInventory(item);
    				
    				// in theory: here double dispatch
    				// so food can affect agent in specific
    				// way
    				
    				return true;
    			}
    		}
    			
    		return false;
		}
	}
	
	class Wheat implements Item {
		public WHEAT_STATE state = WHEAT_STATE.SEED;
		
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
		
		public boolean isEdible() {
			return false;
		}
	}
	
	class Bread implements Item {
		public String literal() {
			return "bread";
		}
		
		public boolean isEdible() {
			return true;
		}
		
	}
	
}
