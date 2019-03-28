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
	public static Tree TREE = new Tree();
	public static Farm FARM = new Farm();

	public FarmModel(List<LauncherAgent> agents, HappeningDirector hapDir) {
		super(agents, hapDir);

		// add locations
		this.addLocation(TREE);
		this.addLocation(FARM);
	}

	public boolean farmWork(Character agent) {
		if(agent.location == FARM) {
			FARM.farmingProgress += 1;
			logger.info("Some farming activity was performed");
			return true;
		}
		return false;
	}
	
	public boolean plantWheat(Character agent) {
		if ((agent.location == FARM) & agent.has(Wheat.itemName) ){
			Wheat wheatItem = (Wheat) agent.get(Wheat.itemName);
			if (wheatItem.state == Wheat.STATES.SEED) {
				FARM.produce = wheatItem;
				agent.removeFromInventory(wheatItem);
				
				FARM.produce.state = Wheat.STATES.GROWING;
				this.environment.addEventPerception(agent.name, "plant(wheat)", PerceptAnnotation.fromEmotion("pride"));
				logger.info("Wheat planted");
				
				return true;
			}
		}

		return false;
	}
	
	public boolean tendWheat(Character agent) {
		if ((agent.location == FARM) & (FARM.produce.state == Wheat.STATES.GROWING)){
			FARM.produce.state = Wheat.STATES.RIPE;
			logger.info("Wheat has grown and is ripe now");
			this.environment.addEventPerception(agent.name, "tend(wheat)", PerceptAnnotation.fromEmotion("pride"));
			return true;
		}
		
		return false;
	}
	
	public boolean harvestWheat(Character agent) {
		if ((agent.location == FARM) & (FARM.produce.state == Wheat.STATES.RIPE)){
			FARM.produce.state = Wheat.STATES.HARVESTED;
			agent.addToInventory(FARM.produce);
			FARM.produce = null;
			
			logger.info("Wheat was harvested");
			this.environment.addEventPerception(agent.name, "harvest(wheat)", PerceptAnnotation.fromEmotion("pride"));
			return true;
		}
		
		return false;
	}
	
	public boolean grindWheat(Character agent) {
		if ((agent.location == FARM) & agent.has(Wheat.itemName) & (((Wheat) agent.get(Wheat.itemName)).state == Wheat.STATES.HARVESTED)){
			((Wheat) agent.get(Wheat.itemName)).state = Wheat.STATES.FLOUR;
			logger.info("Wheat was ground to flour");
			this.environment.addEventPerception(agent.name, "grind(wheat)", PerceptAnnotation.fromEmotion("pride"));
			return true;
		}
		return false;
	}

	public boolean bakeBread(Character agent) {
		if ((agent.location == FARM) & agent.has(Wheat.itemName) & (((Wheat) agent.get(Wheat.itemName)).state == Wheat.STATES.FLOUR)){
			Wheat wheatItem = (Wheat) agent.get(Wheat.itemName);
			agent.removeFromInventory(wheatItem);
			agent.addToInventory(new Bread());
			
			logger.info(agent.name + ": baked some bread.");
			this.environment.addEventPerception(agent.name, "bake(bread)", new PerceptAnnotation("pride", "joy"));
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
					this.environment.addEventPerception(observer.getName(),
													   "is_dropped(" + item.getItemName() + ")",
													   PerceptAnnotation.fromCause("sing").addAnnotation("owner", agent.name));
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
	
	public boolean handOver(Character agent, Character receiver, String itemName, Boolean refuse) {
		if (agent.has(itemName) & agent.location.present(receiver)){
			if (refuse) {
				logger.info(agent.name + " does not hand over" + itemName + " to " + receiver);
				return true;				
			} else {
				Item item = agent.removeFromInventory(itemName);
				receiver.addToInventory(item);
				logger.info(agent.name + " hands over " + itemName + " to " +receiver);
				return true;
			}
		}
		return false;
	}
	
	/****** helper classes *******/
	public static class Farm extends Location {

		public Wheat produce;
		public int farmingProgress;
		
		public Farm() {
			super("farm");
			this.farmingProgress = 0;
			this.produce = null;
		}
	}
	
	
	/**
	 * Location that allows agents to enter on two different levels: ground and sky. While agents on different levels
	 * can verbally interact because they are in the same geographical location, they can not interact physically.
	 * Items can only be places below the tree, since they fall down if placed in the treetop.
	 * 
	 * @author Leonid Berov
	 */
	public static class Tree extends Location {
		public List<Character> treetop = null;
		public List<Character> below = null;
		
		public Tree() {
			super("tree");
			this.setCharacters(null);
			this.treetop = new LinkedList<Character>();
			this.below = new LinkedList<Character>();
		}
		
		@Override
		public boolean enter(Character character) {
			boolean result;
			
			if (character.canFly()) {
				result = this.enter(character, this.treetop);
				logger.info(character.name + " flies to the top of an ancient tree");
				
				// everyone sees the inventory of the character on the tree (apart from the character itself)
				for (Character observer : model.getCharacters()) {
					if (!observer.equals(character)) {
						for (Item item : character.inventory) {
							model.environment.addEventPerception(observer.getName(),
																"see(" + item + ")",
																new PerceptAnnotation().addAnnotation("location", "tree").addAnnotation("owner", character.name));
							
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
				loc.addAnnot(ASSyntax.createLiteral("level", ASSyntax.createAtom("sky")));
			} else if (this.below.contains(character)){
				loc.addAnnot(ASSyntax.createLiteral("level", ASSyntax.createAtom("ground")));
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
	
	public static class Wheat extends Item {
		public static enum STATES {SEED, GROWING, RIPE, HARVESTED, FLOUR;}
		public static final String itemName = "wheat";
		
		public STATES state = Wheat.STATES.SEED;
		
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
	
	public static class Bread extends Item {
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