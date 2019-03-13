package plotmas.stories.little_red_hen;

import java.util.LinkedList;
import java.util.List;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import plotmas.LauncherAgent;
import plotmas.PlotModel;
import plotmas.helper.PerceptAnnotation;
import plotmas.storyworld.Character;
import plotmas.storyworld.HappeningDirector;
import plotmas.storyworld.Item;
import plotmas.storyworld.Location;


/**
 * Custom model of the story-world of the "Tale of the Little Red Hen".
 * @author Leonid Berov
 */
public class FarmModel extends PlotModel<FarmEnvironment>{
	
	public static enum WHEAT_STATE {SEED, GROWING, RIPE, HARVESTED, FLOUR;}

	public Wheat wheat;
	public int actionCount;
	public boolean wheatFound;
	
	public FarmModel(List<LauncherAgent> agents, HappeningDirector hapDir) {
		super(agents, hapDir);

		// create locations
		this.createLocation("other");
		this.addLocation(new Tree(this));

		// set all characters' default location
		for(Character chara :this.getCharacters()) {
			this.getLocation("other").enter(chara);
		}

		this.actionCount = 0;
		this.wheat = null;
		this.wheatFound = false;
		
		Item bread = new Bread();
		if( this.characters.containsKey("crow")) {
			getCharacter("crow").addToInventory(bread);			
		}

	}

	public boolean farmWork(Character agent) {
		if (agent.farmAnimal()) {
			this.actionCount += 1;
			logger.info("Some farming activity was performed");
			return true;
		}
		return false;
	}
	
	public boolean plantWheat(Character agent) {
		Wheat wheatItem = (Wheat) agent.get(Wheat.itemName);
		if (!(wheatItem == null)) {
				if (wheatItem.state == WHEAT_STATE.SEED) {
					this.wheat.state = WHEAT_STATE.GROWING;
					this.environment.addEventPerception(agent.name, "plant(wheat)", PerceptAnnotation.fromEmotion("pride"));
					logger.info("Wheat planted");
					return true;
				}
		}
		
		return false;
	}
	
	public boolean tendWheat(Character agent) {
		if ((this.wheat.state == WHEAT_STATE.GROWING)){
			this.wheat.state = WHEAT_STATE.RIPE;
			logger.info("Wheat has grown and is ripe now");
			this.environment.addEventPerception(agent.name, "tend(wheat)", PerceptAnnotation.fromEmotion("pride"));
			return true;
		}
		
		return false;
	}
	
	public boolean harvestWheat(Character agent) {
		if ((this.wheat.state == WHEAT_STATE.RIPE)){
			this.wheat.state = WHEAT_STATE.HARVESTED;
			logger.info("Wheat was harvested");
			this.environment.addEventPerception(agent.name, "harvest(wheat)", PerceptAnnotation.fromEmotion("pride"));
			return true;
		}
		
		return false;
	}
	
	public boolean grindWheat(Character agent) {
		if ((this.wheat.state == WHEAT_STATE.HARVESTED)){
			this.wheat.state = WHEAT_STATE.FLOUR;
			logger.info("Wheat was ground to flour");
			this.environment.addEventPerception(agent.name, "grind(wheat)", PerceptAnnotation.fromEmotion("pride"));
			return true;
		}
		return false;
	}

	public boolean bakeBread(Character agent) {
		Wheat wheatItem = (Wheat) agent.get(Wheat.itemName);
		if((!(wheatItem == null)) & (wheatItem.state == WHEAT_STATE.FLOUR)) {
			agent.addToInventory(new Bread());
			agent.removeFromInventory(wheatItem);
			
			logger.info(agent.name + ": baked some bread.");
			this.environment.addEventPerception(agent.name, "bake(bread)", new PerceptAnnotation("pride", "joy"));
			return true;
		}
		
		return false;
	}

	
	public boolean stroll(Character agent) {
		logger.info(agent.name + " strolls around");
		return true;
	}

	public boolean threaten(Character agent, Character target) {
		// TODO: Rly, asking for all items?!
		if (agent.location.present(target)) {
			for (Item item : target.inventory) {
				logger.info(agent.name + " tells " +target.name + " to hand over " + item + " or " + agent.name + " will take it from " +target.name + " with force.");
				this.environment.addEventPerception(target.name, "threat(" + item + ")owner("+agent.name+")]", new PerceptAnnotation("fear"));
				return true;
			}
		}
		return false;
	}
	
	public boolean ask(Character agent, Character target) {
		// TODO: Rly, asking for all items?!
		if (agent.location.present(target)) {
			for (Item item : agent.inventory) {
				logger.info(agent.name + " asks " +target.name + " to share " + item +" with " + agent.name);
				this.environment.addEventPerception(target.name, "politeQuery(" + item + ")[owner(" + target.name + ")]", new PerceptAnnotation("pity"));
				return true;
			}
		}
		return false;
	}
	
	public boolean flatter(Character agent, Character target) {
		if(agent.location.present(target)) {
			logger.info(agent.name + " flatters "+ target.name);
			this.environment.addEventPerception(target.name, "compliment", new PerceptAnnotation("pride"));
			return true;
		}
		return false;
	}
		
	public boolean sing(Character agent) { 
		logger.info(agent.name + " sings.");
		
		// if agent is in the sky and sings then they loose whatever they held in their (beak-) inventory
		if(agent.location.isSkyLevel(agent)) {
			for (Item item : agent.inventory) {		
				agent.removeFromInventory(item);
				agent.location.place(item);
				logger.info(agent.name + " lost " + item.getItemName() + " which fell to the ground.");
				
				// everyone present see things dropping from the sky
				for (Character observer : agent.location.getCharacters()) {
					this.environment.addEventPerception(observer.getName(), "is_dropped(" + item.getItemName() + ")");
				}
			}
		}
				
		return true;
	}
	
	public boolean collect(Character agent, String thing) {
		if(agent.location.contains(thing)) {
			Item item = agent.location.remove(thing);
			agent.addToInventory(item);
			return true;
		}
		return false;
	}
	
	public boolean handOver(Character agent, Character receiver, String itemName) {
		if (agent.has(itemName) & agent.location.present(receiver)){
			Item item = agent.removeFromInventory(itemName);
			receiver.addToInventory(item);
			logger.info(agent.name + " hands over " + itemName + " to " +receiver);
			return true;
		}
		return false;
	}
	
	public boolean refuseToGive(Character agent, Character enemy, String item) {
		if (agent.has(item) & agent.location.present(enemy)){
			logger.info(agent.name + " does not hand over" + item + " to " +enemy);
			return true;
		}
		return false;
	}
	
	/****** helper classes *******/
	
	/**
	 * Location that allows agents to enter on two different levels: ground and sky. While agents on different levels
	 * can verbally interact because they are in the same geographical location, they can not interact physically.
	 * Items can only be places below the tree, since they fall down if placed in the treetop.
	 * 
	 * @author Leonid Berov
	 */
	public class Tree extends Location {
		private PlotModel<?> model = null;
		
		public List<Character> treetop = null;
		public List<Character> below = null;
		
		public Tree(PlotModel<?> model) {
			super("tree");
			this.setCharacters(null);
			
			this.model = model;
			this.treetop = new LinkedList<Character>();
			this.below = new LinkedList<Character>();
		}
		
		@Override
		public boolean enter(Character character) {
			boolean result;
			
			if (character.flying()) {
				result = this.enter(character, this.treetop);
				logger.info(character.name + " flies to the top of an ancient tree");
				
				// everyone sees the inventory of the character on the tree (apart from the character itself)
				for (Character observer : model.getCharacters()) {
					if (!observer.equals(character)) {
						for (Item item : character.inventory) {
							model.environment.addEventPerception(observer.getName(), "see(" + item + ")[location(tree), owner(" + character.name+")]"); 
							logger.info(observer.name + " sees " + character.name + " with " + item);
						}
					}
				}
			} else {
				result = this.enter(character, this.below);
				logger.info(character.name + " stands below an ancient tree");
			}
			
			return result;
		}
		
		private boolean enter(Character character, List<Character> loc) {
			if(null == character.location) {
				// no prev location, just set this one
				this.characterLocationUpdate(character, loc);
				return true;
			} else if(character.location.leave(character)) {
				// prev location was present, character left it successfully
				this.characterLocationUpdate(character, loc);
				return true;
			} else {
				// prev location was present, character couldn't leave
				return false;
			}
		}
		
		private void characterLocationUpdate(Character character, List<Character> loc) {
			loc.add(character);
			character.location = this;
		}
		
		@Override
		public boolean leave(Character character) {
			if (this.present(character)) {
				this.treetop.remove(character);
				this.below.remove(character);
				character.location = null;
				return true;
			} else {
				logger.severe("Location " + this.name + "can't remove agent " + character.name + ". Not present.");
				return false;
			}
		}
		
		@Override
		public boolean present(Character character) {
			return (this.treetop.contains(character) | this.below.contains(character));
		}
		
		@Override
		public Literal createLocationPercept(Character character) {
			Literal loc = super.createLocationPercept(character);
			
			if(this.treetop.contains(character)) {
				loc.addAnnot(ASSyntax.createLiteral("level", ASSyntax.createAtom("ground")));
			} else if (this.below.contains(character)){
				loc.addAnnot(ASSyntax.createLiteral("level", ASSyntax.createAtom("sky")));
			} else {
				logger.severe("Character "+ character.name +" not present at location " + this.name);
				return null;
			}
			
			return loc;
		}
		
		@Override
		public Boolean isGroundLevel(Character character) {
			if (!this.present(character)) {
				return null;
			}
			if(this.below.contains(character)) {
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public Boolean isSkyLevel(Character character) {
			if (!this.present(character)) {
				return null;
			}
			if(this.treetop.contains(character)) {
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public List<Character> getCharacters() {
			List<Character> chars = new LinkedList<>();
			chars.addAll(this.below);
			chars.addAll(this.treetop);
			return chars;
		}
	}
	
	public class Wheat extends Item {
		static final String itemName = "wheat";
		public WHEAT_STATE state = WHEAT_STATE.SEED;
		
		@Override
		public Literal literal() {
			Literal res = super.literal();
			res.addAnnot(ASSyntax.createLiteral("state", ASSyntax.createAtom(state.toString().toLowerCase())));
			return res;
		}

		@Override
		public String getItemName() {
			return Wheat.itemName;
		}

	}
	
	class Bread extends Item {
		static final String itemName = "bread";
		
		@Override
		public String getItemName() {
			return Bread.itemName;
		}
		
		@Override
		public boolean isEdible() {
			return true;
		}
	}
}