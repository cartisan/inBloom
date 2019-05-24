package inBloom.stories.little_red_hen;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;
import inBloom.storyworld.Character;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.Item;
import inBloom.storyworld.Location;
import inBloom.storyworld.ModelState;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;


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
				agent.removeFromInventory(wheatItem);
				FARM.produce = wheatItem;
				FARM.updateProduceState(Wheat.STATES.GROWING);
				
				this.environment.addEventPercept(agent.name, "plant(wheat)", PerceptAnnotation.fromEmotion("pride"));
				logger.info("Wheat planted");
				
				return true;
			}
		}

		return false;
	}
	
	public boolean tendWheat(Character agent) {
		if ((agent.location == FARM) & (FARM.produce.state == Wheat.STATES.GROWING)){
			FARM.updateProduceState(Wheat.STATES.RIPE);
			logger.info("Wheat has grown and is ripe now");
			this.environment.addEventPercept(agent.name, "tend(wheat)", PerceptAnnotation.fromEmotion("pride"));
			return true;
		}
		
		return false;
	}
	
	public boolean harvestWheat(Character agent) {
		if ((agent.location == FARM) & (FARM.produce.state == Wheat.STATES.RIPE)){
			Wheat w =  FARM.produce;
			w.state = Wheat.STATES.HARVESTED;
			agent.addToInventory(w);
			FARM.updateProduceState(null);
			
			logger.info("Wheat was harvested");
			this.environment.addEventPercept(agent.name, "harvest(wheat)", PerceptAnnotation.fromEmotion("pride"));
			return true;
		}
		
		return false;
	}
	
	public boolean grindWheat(Character agent) {
		if ((agent.location == FARM) & agent.has(Wheat.itemName) & (((Wheat) agent.get(Wheat.itemName)).state == Wheat.STATES.HARVESTED)){
			Wheat w =  (Wheat) agent.removeFromInventory(Wheat.itemName);
			w.state = Wheat.STATES.FLOUR;
			agent.addToInventory(w);
			logger.info("Wheat was ground to flour");
			this.environment.addEventPercept(agent.name, "grind(wheat)", PerceptAnnotation.fromEmotion("pride"));
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
			this.environment.addEventPercept(agent.name, "bake(bread)", new PerceptAnnotation("pride", "joy"));
			return true;
		}
		
		return false;
	}
	
	/****** helper classes *******/
	public static class Farm extends Location {
		
		@ModelState 
		public Wheat produce;
		
		@ModelState 
		public int farmingProgress;
		
		public Farm() {
			super("farm");
			this.farmingProgress = 0;
			this.produce = null;
		}
		
		public void updateProduceState(Wheat.STATES state) {
			this.model.environment.removePerceptsByUnif(Literal.parseLiteral("existant(wheat[X])"));
			
			if (state == null) {
				this.produce = null;
			} else{
				this.produce.state = state;
				this.model.environment.addPercept(Literal.parseLiteral("existant(" + FarmModel.FARM.produce.literal() + ")"));
			}
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
			this.treetop = new LinkedList<Character>();
			this.below = new LinkedList<Character>();
		}
		
		@Override
		public boolean enter(Character character) {
			boolean result;
			
			if (character.canFly()) {
				result = this.enter(character, this.treetop);
				logger.info(character.name + " flies to the top of an ancient tree");
				
				// characters in other places see the inventory of the character on the tree (apart from the character itself)
				List<Character> otherPlacesChars = model.getCharacters().stream().filter(chara -> !chara.location.equals(TREE)).collect(Collectors.toList());
				for (Character observer : otherPlacesChars) {
					for (Item item : character.inventory) {
						model.environment.addEventPercept(observer.getName(),
															 "see(" + item.getItemName() + ")",
															 new PerceptAnnotation().addAnnotation("location", this.name).addAnnotation("owner", character.name));
						
						logger.info(observer.name + " sees " + character.name + " with " + item);
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
				loc.add(character);
				this.characterLocationUpdate(character);
				return true;
			} else if(character.location.leave(character)) {
				// prev location was present, character left it successfully
				loc.add(character);
				this.characterLocationUpdate(character);
				return true;
			} else {
				// prev location was present, character couldn't leave
				return false;
			}
		}
		
		
		@Override
		public boolean leave(Character character) {
			if (this.present(character)) {
				super.leave(character);
				this.treetop.remove(character);
				this.below.remove(character);
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
		public Literal createLocationPercept(Character character, Boolean otherChar) {
			Literal loc = super.createLocationPercept(character, otherChar);
			
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