package plotmas.storyworld;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import plotmas.PlotEnvironment;


/**
 * Responsible for modeling the state of an agent/character in the storyworld. Manages the agent's
 * inventory that consists of {@link Item item subclasses} by offering methods to modify it. <br> 
 * Subclasses should implement further, domain-specific methods that execute character behavior and
 * modify a character's state.
 * @author Leonid Berov
 */
public class StoryworldAgent {
    static Logger logger = Logger.getLogger(StoryworldAgent.class.getName());
	
	public LinkedList<Item> inventory = new LinkedList<Item>();
	public String name;
	private PlotEnvironment<?> environment;

	
	
	public StoryworldAgent() {
		this.name = null;
	}
	
	public StoryworldAgent(String name) {
		this.name = name;
	}
	
	/**
	 * Creates a list of {@link jason.asSyntax.Literal literal-}perceptions that represent the complete state of an 
	 * agent's inventory, which can be used to generate ASL agent perceptions.
	 * @return
	 */
	public LinkedList<String> createInventoryPercepts() {
		LinkedList<String> invRepr = new LinkedList<String>();
		
		for (Item item : inventory) {
			invRepr.add("has(" + item.literal() + ")");
		}

		return invRepr;
	}
	
	public void addToInventory(Item item) {
		inventory.add(item);
	}
	
	public void removeFromInventory(Item item) {
		inventory.remove(item);
	}
	
	public boolean has(String itemType) {
		for (Item item : inventory) {
			if (item.getItemName().equals(itemType)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Item get(String itemType) {
		for (Item item : inventory) {
			if (item.getItemName().equals(itemType)) {
				return item;
			}
		}
		
		return null;
	}
	
	public boolean share(String itemType, StoryworldAgent receiver) {
		if (this.has(itemType)) {
			Item item = this.get(itemType);
			receiver.receive(item, this);
			this.environment.addEventPerception(name,
					String.format("shared(%s,%s)" + PlotModel.addTargetedEmotion("pride", "self"),
								  item.literal(), receiver.name));
			return true;
		}
		
		return false;
	}
	
	public boolean share(String itemType, List<StoryworldAgent> receivers) {
		if (this.has(itemType)) {
			Item item = this.get(itemType);
			
			for(StoryworldAgent receiver : receivers) {
				receiver.receive(item, this);
			}
			
			String recList = receivers.stream().map(rec -> rec.name)
											   .collect(Collectors.joining(",", "[", "]"))
											   .toString();
			
			this.environment.addEventPerception(name,
					String.format("share(%s,%s)" + PlotModel.addTargetedEmotion("pride", "self"),
								  item.literal(), recList));
			
			logger.info(this.name + " shared some " + item.literal() + ".");
			
			return true;
		}
		
		return false;
	}
	
	public boolean receive(Item item, StoryworldAgent from) {
		this.addToInventory(item);
		
		this.environment.addEventPerception(name,
				String.format("receive(%s)" + PlotModel.addTargetedEmotion("gratitude", "self"),
							  item.literal(), this.name));
		
		//logger.info(this.name + " received some " + item.literal() + ".");
		logger.info(this.name + " received some " + item.getItemName() + ".");
	
	
		return true;
	}

	public boolean eat(String itemType) {
		if (this.has(itemType)) {
			Item item = this.get(itemType);
			
			if (item.isEdible()) {
				this.removeFromInventory(item);
				this.environment.addEventPerception(name, 
						String.format("eat(%s)" + PlotModel.addEmotion("satisfaction"), item.literal()));
				
				// in theory: here double dispatch
				// so food can affect agent in specific
				// way
				
				logger.info(this.name + " ate some " + item.literal() + ".");
				return true;
			}
		}
			
		return false;
	}
	
	public boolean relax() {
		this.environment.addEventPerception(name, "relax" + PlotModel.addEmotion("joy"));
		return true;
	}

	public void setEnvironment(PlotEnvironment<?> environment) {
		this.environment = environment;
	}
	
	public String toString() {
		return this.name + "-agent_model";
	}
}