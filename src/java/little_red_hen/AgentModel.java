package little_red_hen;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSemantics.Personality;
import little_red_hen.jason.FarmEnvironment;


public class AgentModel {
    static Logger logger = Logger.getLogger(AgentModel.class.getName());
	
	public LinkedList<Item> inventory = new LinkedList<Item>();
	public String name;
	public String beliefs;
	public String goals;
	private FarmEnvironment environment;

	public Personality personality;
	
	
	public AgentModel() {
		this.name = null;
		this.beliefs = "";
		this.goals = "";
	}
	
	public AgentModel(String name) {
		this.name = name;
		this.beliefs = "";
		this.goals = "";
		this.personality = null;
	}

	public AgentModel(String name, Collection<String> beliefs, Collection<String> goals) {
		this.name = name;
		this.beliefs = createLiteralString(beliefs);
		this.goals = createLiteralString(goals);
		this.personality = null;
	}
	
	public AgentModel(String name, Collection<String> beliefs, Collection<String> goals, Personality personality) {
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
	
	public boolean share(String itemType, AgentModel receiver) {
		if (this.has(itemType)) {
			Item item = this.get(itemType);
			receiver.receive(item);
			this.environment.addToListCurrentEvents(name,
					String.format("shared(%s,%s)[emotion(pride)]", item.literal(), receiver.name));
			return true;
		}
		
		return false;
	}
	
	public boolean share(String itemType, List<AgentModel> receivers) {
		if (this.has(itemType)) {
			Item item = this.get(itemType);
			
			for(AgentModel receiver : receivers) {
				receiver.receive(item);
			}
			
			String recList = receivers.stream().map(rec -> rec.name).collect(Collectors.joining(",", "[", "]")).toString();
			this.environment.addToListCurrentEvents(name,
					String.format("shared(%s,%s)[emotion(pride)]", item.literal(), recList));
			
			return true;
		}
		
		return false;
	}
	
	public boolean receive(Item item) {
		this.addToInventory(item);
		
		// TODO: this emotion might depend on properties of object, in which case its deliberative and should go
		// into ASL side?
		this.environment.addToListCurrentEvents(name,
												String.format("received(%s)[emotion(joy)]", item.literal()));
		return true;
	}

	public boolean eat(String itemType) {
		if (this.has(itemType)) {
			Item item = this.get(itemType);
			
			if (item.isEdible()) {
				this.removeFromInventory(item);
				this.environment.addToListCurrentEvents(name, 
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
		this.environment.addToListCurrentEvents(name, "relaxed[emotion(joy)]");
		return true;
	}

	public void setEnvironment(FarmEnvironment environment) {
		this.environment = environment;
	}
}