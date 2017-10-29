package plotmas.storyworld;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSemantics.Personality;
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
	public String beliefs;
	public String goals;
	private PlotEnvironment environment;

	public Personality personality;
	
	
	public StoryworldAgent() {
		this.name = null;
		this.beliefs = "";
		this.goals = "";
	}
	
	public StoryworldAgent(String name) {
		this.name = name;
		this.beliefs = "";
		this.goals = "";
		this.personality = null;
	}

	public StoryworldAgent(String name, Personality personality) {
		this.name = name;
		this.beliefs = "";
		this.goals = "";
		this.personality = personality;
	}
	
	public StoryworldAgent(String name, Collection<String> beliefs, Collection<String> goals, Personality personality) {
		this.name = name;
		this.beliefs = createLiteralString(beliefs);
		this.goals = createLiteralString(goals);
		this.personality = personality;
	}
	
	private String createLiteralString(Collection<String> literalList) {
		String result = "";
		
		for (String literal : literalList) {
			result += literal + ",";
		}
		
		if (result.length() > 0) {
			// delete last trailing ','
			result = result.substring(0, result.length()-1);
		}
		
		return result;
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
			receiver.receive(item);
			this.environment.addEventPerception(name,
					String.format("shared(%s,%s)[emotion(pride, self)]", item.literal(), receiver.name));
			return true;
		}
		
		return false;
	}
	
	public boolean share(String itemType, List<StoryworldAgent> receivers) {
		if (this.has(itemType)) {
			Item item = this.get(itemType);
			
			for(StoryworldAgent receiver : receivers) {
				receiver.receive(item);
			}
			
			String recList = receivers.stream().map(rec -> rec.name).collect(Collectors.joining(",", "[", "]")).toString();
			this.environment.addEventPerception(name,
					String.format("shared(%s,%s)[emotion(pride, self)]", item.literal(), recList));
			logger.info(this.name + " shared some " + item.literal() + ".");
			
			
			return true;
		}
		
		return false;
	}
	
	public boolean receive(Item item) {
		this.addToInventory(item);
		
		// TODO: this emotion might depend on properties of object, in which case its deliberative and should go
		// into ASL side?
		this.environment.addEventPerception(name,
												String.format("received(%s)[emotion(joy)]", item.literal()));
		logger.info(this.name + " received some " + item.literal() + ".");
		return true;
	}

	public boolean eat(String itemType) {
		if (this.has(itemType)) {
			Item item = this.get(itemType);
			
			if (item.isEdible()) {
				this.removeFromInventory(item);
				this.environment.addEventPerception(name, 
														String.format("ate(%s)[emotion(satisfaction)]", item.literal()));
				
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
		this.environment.addEventPerception(name, "relaxed[emotion(joy)]");
		return true;
	}

	public void setEnvironment(PlotEnvironment environment) {
		this.environment = environment;
	}
	
	public String toString() {
		return this.name + "-agent_model";
	}
}