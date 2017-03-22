package little_red_hen;

import java.util.Collection;
import java.util.LinkedList;

import com.google.common.collect.ImmutableList;

public class Agent {
	public LinkedList<Item> inventory = new LinkedList<Item>();
	public String name;
	public String beliefs;
	public String goals;
	
	
	public Agent() {
		this.name = null;
		this.beliefs = "";
		this.goals = "";
	}
	
	public Agent(String name) {
		this.name = name;
		this.beliefs = "";
		this.goals = "";
	}

	public Agent(String name, Collection<String> beliefs, Collection<String> goals) {
		this.name = name;
		this.beliefs = createLiteralString(beliefs);
		this.goals = createLiteralString(goals);
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
	
	public boolean relax() {
		//TODO: How to return a positive emotion?
		return true;
	}
}