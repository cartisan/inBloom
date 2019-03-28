package plotmas.storyworld;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import plotmas.PlotModel;

public class Location {
	static protected Logger logger = Logger.getLogger(Location.class.getName());
	
	private List<Character> characters = null; 
	private List<Item> existents = null;
	protected PlotModel<?> model = null;
	public String name = null;
	
	public Location(String name) {
		this.name = name;
		this.characters = new LinkedList<>();
		this.existents = new LinkedList<>();
	}
	
	public void initialize(PlotModel<?> model) {
		this.model = model;
	}
	
	/**
	 * Adds character to this location, and makes sure it leaves its old one to ensure consistency. Returns true if
	 * transportation was successful.
	 * @param character
	 * @return
	 */
	public boolean enter(Character character) {
		if(null == character.location) {
			// no prev location, just set this one
			this.characterLocationUpdate(character);
			return true;
		} else if(character.location.leave(character)) {
			// prev location was present, character left it successfully
			this.characterLocationUpdate(character);
			return true;
		} else {
			// prev location was present, character couldn't leave
			return false;
		}
	}
	
	private void characterLocationUpdate(Character character) {
		this.characters.add(character);
		character.location = this;
	}
	
	/**
	 * Removes character from this location, and updates character's representation of its own location. Returns true
	 * if removal was successful.
	 * @param character
	 * @return
	 */
	public boolean leave(Character character) {
		if (this.characters.contains(character)) {
			this.characters.remove(character);
			character.location = null;
			return true;
		} else {
			logger.severe("Location " + this.name + "can't remove agent " + character.name + ". Not present.");
			return false;
		}
	}
	
	public boolean present(Character character) {
		return this.characters.contains(character);
	}
	
	public Boolean isGroundLevel(Character character) {
		return true;
	}
	
	public Boolean isSkyLevel(Character character) {
		return false;
	}
	
	public void place(Item item) {
		this.existents.add(item);
	}
	
	public Item remove(Item item) {
		if (this.existents.contains(item)) {
			this.existents.remove(item);
			return item;
		} else {
			logger.warning("Location " + this.name + "can't remove item " + item.getItemName() + ". Not present.");
			return null;
		}
	}
	
	public Item remove(String itemName) {
		for (Item item: this.existents) {
			if (item.getItemName().equals(itemName)) {
				this.remove(item);
				return item;
			}
		}
		
		logger.severe("Location " + this.name + " can't remove item " + itemName + ". Not present.");
		return null;
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
	
	/**
	 * Returns an AgentSpeak literal denoting the character's position at the present location.
	 * @param character
	 * @return a 1-ary literal denoting character's location
	 */
	public Literal createLocationPercept(Character character) {
		if (this.present(character)) {
			return ASSyntax.createLiteral("at", ASSyntax.createAtom(this.name));
		}
		
		logger.severe("Character "+ character.name +" not present at location " + this.name);
		return null;
	}

	public List<Character> getCharacters() {
		return characters;
	}

	protected void setCharacters(List<Character> characters) {
		this.characters = characters;
	}

	public List<Item> getExistents() {
		return existents;
	}

	protected void setExistents(List<Item> existents) {
		this.existents = existents;
	}
}
