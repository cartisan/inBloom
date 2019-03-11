package plotmas.storyworld;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Location {
	static protected Logger logger = Logger.getLogger(Location.class.getName());
	
	public List<Character> characters = null; 
	public List<Item> existents = null;
	public String name = null;
	
	public Location(String name) {
		this.name = name;
		this.characters = new LinkedList<>();
		this.existents = new LinkedList<>();
	}
	
	public void enter(Character character) {
		this.characters.add(character);
		character.location = this;
	}
	
	public void leave(Character character) {
		if (this.characters.contains(character)) {
			this.characters.remove(character);
		} else {
			logger.warning("Location " + this.name + "can't remove agent " + character.name + ". Not present.");
		}
	}
	
	public boolean present(Character character) {
		return this.characters.contains(character);
	}
	
	public void place(Item item) {
		this.existents.add(item);
	}
	
	public void remove(Item item) {
		if (this.existents.contains(item)) {
			this.existents.remove(item);
		} else {
			logger.warning("Location " + this.name + "can't remove item " + item.getItemName() + ". Not present.");
		}
	}
	
	public void remove(String itemName) {
		for (Item item: this.existents) {
			if (item.getItemName().equals(itemName)) {
				this.remove(item);
				return;
			}
		}
	}
	
	public boolean contains(Item item) {
		return this.existents.contains(item);
	}
	
	
	public boolean contains(String itemName) {
		for (Item item : this.existents) {
			if (item.getItemName().equals(itemName)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
}
