package inBloom.storyworld;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;

import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;

public class Location extends Existent {
	static protected Logger logger = Logger.getLogger(Location.class.getName());

	@ModelState
	private List<Character> characters = null;
	@ModelState
	private List<Item> items = null;
	@ModelState
	private LinkedList<Location> sublocations;

	protected PlotModel<?> model = null;
	public String name = null;

	public Location(String name) {
		this.name = name;
		this.characters = new LinkedList<>();
		this.items = new LinkedList<>();
		this.sublocations = new LinkedList<Location>();
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

		// character perceives free items at this location
		for (Item item : this.items) {
			this.model.environment.addPercept(character.name, ASSyntax.createLiteral("at", item.literal(), this.literal()));
			this.model.environment.addEventPercept(character.getName(), "see(" + item.getItemName() + ")");
		}

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
						this.model.environment.addEventPercept(observer.getName(),
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
					this.model.environment.addEventPercept(observer.getName(),
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
	public boolean leaveWithoutSublocation(Character character) {
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
	
	/**
	 * Previously leave is now leaveWithoutSublocation (above).
	 * This leave makes sure that the character leaves all sublocations as well
	 * when leaving the main location
	 * 
	 * @param character
	 * 			The character that is leaving
	 * @return true if the character had been present in the main location beforehand
	 */
	public boolean leave(Character character) {
		
		// first leave all sublocations
		for(Location sublocation: this.sublocations) {
			// if the character hasn't been present in any of the sublocations
			// this call will return false, but throw no errors
			this.leaveSublocation(character, sublocation.name);
		}
		
		// then leave this main location as known
		return this.leaveWithoutSublocation(character);
	}

	public boolean present(Character character) {
		return this.characters.contains(character);
	}

	/**
	 * Adds a sublocation dynamically during runtime
	 * 
	 * @param sublocation
	 * 			The sublocation to be added
	 */
	public void addSublocation(Location sublocation) {
		this.sublocations.add(sublocation);
	}
	
	public boolean destroySublocation(Location sublocation) {
		return this.sublocations.remove(sublocation);
	}

	public boolean hasSublocation(Location sublocation) {
		return this.sublocations.contains(sublocation);
	}
	
	public boolean enterSublocation(Character character, String sublocationName) {
		
		Location sublocation = this.getSublocation(sublocationName);

		if(sublocation != null) {
			sublocation.characters.add(character);
			//TODO delete
			logger.info("Agent " + character.name + " has entered " + sublocationName);
			// TODO Character can only have one location thou, so Character will always have
			// the main location so far -> beware
			return true;
		} else {
			// the sublocation does not exist
			//TODO delete
			logger.info("Agent " + character.name + " has not entered " + sublocationName + " because it does not exist");
			return false;
		}
	}

	public boolean leaveSublocation(Character character, String sublocationName) {

		Location sublocation = this.getSublocation(sublocationName);

		if(sublocation != null && sublocation.present(character)) {
			//TODO delete
			sublocation.characters.remove(character);
			logger.info("Agent " + character.name + " has left " + sublocationName);
			return true;
		} else {
			//TODO delete
			logger.info("Agent " + character.name + " has not left " + sublocationName);
			// the sublocation does not exist or the character is not in the sublocation
			return false;
		}
	}
	
	public Boolean isGroundLevel(Character character) {
		return true;
	}

	public Boolean isSkyLevel(Character character) {
		return false;
	}

	public void place(Item item) {
		this.items.add(item);

		// item perception is created for everyone present
		for (Character character : this.characters) {
			this.model.environment.addPercept(character.name, ASSyntax.createLiteral("at", item.literal(), this.literal()));
			this.model.environment.addEventPercept(character.getName(), "see(" + item.getItemName() + ")");
		}
	}

	public Item remove(Item item) {
		if (this.items.contains(item)) {
			this.items.remove(item);

			// item perception is removed for everyone present
			for (Character character : this.characters) {
				this.model.environment.removePercept(character.name, ASSyntax.createLiteral("at", item.literal(), this.literal()));
			}

			return item;
		} else {
			logger.warning("Location " + this.name + "can't remove item " + item.getItemName() + ". Not present.");
			return null;
		}
	}

	public Item remove(String itemName) {
		for (Item item: this.items) {
			if (item.getItemName().equals(itemName)) {
				this.remove(item);
				return item;
			}
		}

		logger.severe("Location " + this.name + " can't remove item " + itemName + ". Not present.");
		return null;
	}

	public boolean contains(Item item) {
		return this.items.contains(item);
	}


	public boolean contains(String itemName) {
		for (Item item : this.items) {
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
	 * Returns a list of characters present at this location.
	 * @return
	 */
	public List<Character> getCharacters() {
		return this.characters;
	}

	protected void setCharacters(List<Character> characters) {
		this.characters = characters;
	}

	public List<Item> getItems() {
		return this.items;
	}

	protected void setItems(List<Item> items) {
		this.items = items;
	}
	
	/** 
	 * @param sublocation
	 * @return null if the sublocation doesn't exist
	 */
	public Location getSublocation(String sublocation) {
		for(Location existentSublocation: sublocations) {
			if(existentSublocation.name.equals(sublocation)) {
				return existentSublocation;
			}
		}
		return null;
	}
}
