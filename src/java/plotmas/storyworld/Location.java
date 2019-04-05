package plotmas.storyworld;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import plotmas.PlotModel;
import plotmas.helper.PerceptAnnotation;

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
	
	protected void characterLocationUpdate(Character character) {
		this.characters.add(character);
		character.location = this;
		
		// update agent's position perception for self
		this.model.environment.removePerceptsByUnif(character.name, Literal.parseLiteral("at(X)"));
		this.model.environment.addPercept(character.name, this.createLocationPercept(character, false));
		
		// TODO: character perceives items
		
		for (Character observer : this.characters) {
			List<Character> otherChars = this.characters.stream().filter(c -> !c.equals(observer))
					   											.collect(Collectors.toList());
			// newly entered char's perception of already present agents
			if (observer == character) {
				for (Character otherChara : otherChars) {
					// newly entered char notes location of other chars
					this.model.environment.removePercept(character.name, ASSyntax.createLiteral("at", ASSyntax.createAtom(character.name), ASSyntax.createVar()));
					this.model.environment.addPercept(observer.name, this.createLocationPercept(otherChara, true));
					
					// newly entered char perceives all present agents' inventory
					for (Item item : otherChara.inventory) {
						model.environment.addEventPerception(observer.getName(),
															 "see(" + item.getItemName() + ")",
															 new PerceptAnnotation().addAnnotation("owner", otherChara.name));
					}
				}
			} 
			// present agents perceive newly entered agent
			else {
				// present agents note location of other chars
				this.model.environment.addPercept(observer.name, this.createLocationPercept(character, true));
				
				for (Item item : character.inventory) {
					
					// present agents perceive newly entered agents' inventory
					model.environment.addEventPerception(observer.getName(),
														 "see(" + item.getItemName() + ")",
														 new PerceptAnnotation().addAnnotation("owner", character.name));
				}
			}
		}
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
			
			// update all agents perception of present agents
			for (Character chara : this.characters) {
				// present agents take note of character leaving
				this.model.environment.removePercept(chara.name, this.createLocationPercept(character, true));
			}
			
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
		// TODO: existent perception is added
		this.existents.add(item);
	}
	
	public Item remove(Item item) {
		// TODO: existent perception is removed
		
		if (this.existents.contains(item)) {
			this.existents.remove(item);
			return item;
		} else {
			logger.warning("Location " + this.name + "can't remove item " + item.getItemName() + ". Not present.");
			return null;
		}
	}
	
	public Item remove(String itemName) {
		// TODO: existent perception is removed
		
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
	 * @param character the character for whom the location percept is to be created
	 * @param otherChar true if percept is to be generated for another character, false if for self
	 * @return a 1-ary literal denoting character's location if loc is for self e.g. <code>at(farm)</code>, 2-ary otherwise <code>at(dog,farm)</code>
	 */
	public Literal createLocationPercept(Character character, Boolean otherChar) {
		if (this.present(character)) {
			if (otherChar) {
				return ASSyntax.createLiteral("at", character.literal(), this.literal());
			}
			return ASSyntax.createLiteral("at", this.literal());
		}
		
		logger.severe("Character "+ character.name +" not present at location " + this.name);
		return null;
	}
	
	/**
	 * Returns an AgentSpeak {@link jason.asSyntax.Literal literal} denoting this location and potentially its
	 * current state using annotations.
	 * @return A literal denoting this item and its current state
	 * @see plotmas.stories.little_red_hen.FarmModel.Wheat
	 */
	public Literal literal() {
			return new Pred(this.toString());
	};
	

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
