package little_red_hen;

import java.util.LinkedList;

public class Agent {
	public LinkedList<Item> inventory = new LinkedList<Item>();
	public String name;
	
	public Agent(String name) {
		this.name = name;
	}
	
	public Agent() {
		this.name = null;
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
}