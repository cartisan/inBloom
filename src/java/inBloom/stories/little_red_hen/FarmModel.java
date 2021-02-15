package inBloom.stories.little_red_hen;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;
import inBloom.storyworld.Character;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.Item;
import inBloom.storyworld.Location;
import inBloom.storyworld.ModelState;


/**
 * Custom model of the story-world of the "Tale of the Little Red Hen".
 * @author Leonid Berov
 */
public class FarmModel extends PlotModel<FarmEnvironment>{
	public Tree tree = new Tree();
	public Farm farm = new Farm();

	public FarmModel(List<LauncherAgent> agents, HappeningDirector hapDir) {
		super(agents, hapDir);

		// add locations
		this.addLocation(this.tree);
		this.addLocation(this.farm);
	}

	public ActionReport farmWork(Character agent) {
		if(agent.location == this.farm) {
			this.farm.farmingProgress += 1;
			logger.info("Some farming activity was performed");
			return new ActionReport(true);
		}
		return new ActionReport(false);
	}

	public ActionReport plantWheat(Character agent) {
		ActionReport res = new ActionReport();

		if (agent.location == this.farm & agent.has(Wheat.itemName) ){
			Wheat wheatItem = (Wheat) agent.get(Wheat.itemName);
			if (wheatItem.state == Wheat.STATES.SEED) {
				agent.removeFromInventory(wheatItem);
				this.farm.produce = wheatItem;
				this.farm.updateProduceState(Wheat.STATES.GROWING);

				logger.info("Wheat planted");
				res.addPerception(agent.name,PerceptAnnotation.fromEmotion("pride"));

				res.success = true;
			}
		}

		return res;
	}

	public ActionReport tendWheat(Character agent) {
		ActionReport res = new ActionReport();

		if (agent.location == this.farm && this.farm.produce != null && this.farm.produce.state == Wheat.STATES.GROWING){
			this.farm.updateProduceState(Wheat.STATES.RIPE);
			logger.info("Wheat has grown and is ripe now");
			res.addPerception(agent.name, PerceptAnnotation.fromEmotion("pride"));
			res.success = true;
		}

		return res;
	}

	public ActionReport harvestWheat(Character agent) {
		ActionReport res = new ActionReport();

		if (agent.location == this.farm && this.farm.produce != null && this.farm.produce.state == Wheat.STATES.RIPE){
			Wheat w =  this.farm.produce;
			w.state = Wheat.STATES.HARVESTED;
			agent.addToInventory(w);
			this.farm.updateProduceState(null);

			logger.info("Wheat was harvested");
			res.addPerception(agent.name, PerceptAnnotation.fromEmotion("pride"));
			res.success = true;
		}

		return res;
	}

	public ActionReport grindWheat(Character agent) {
		ActionReport res = new ActionReport();

		if (agent.location == this.farm & agent.has(Wheat.itemName) & ((Wheat) agent.get(Wheat.itemName)).state == Wheat.STATES.HARVESTED){
			Wheat w =  (Wheat) agent.removeFromInventory(Wheat.itemName);
			w.state = Wheat.STATES.FLOUR;
			agent.addToInventory(w);
			logger.info("Wheat was ground to flour");
			res.addPerception(agent.name, PerceptAnnotation.fromEmotion("pride"));
			res.success =  true;
		}
		return res;
	}

	public ActionReport bakeBread(Character agent) {
		ActionReport res = new ActionReport();

		if (agent.location == this.farm & agent.has(Wheat.itemName) & ((Wheat) agent.get(Wheat.itemName)).state == Wheat.STATES.FLOUR){
			Wheat wheatItem = (Wheat) agent.get(Wheat.itemName);
			agent.removeFromInventory(wheatItem);
			agent.addToInventory(new Bread());

			logger.info(agent.name + ": baked some bread.");
			res.addPerception(agent.name, new PerceptAnnotation("pride"));
			res.success =  true;
		}

		return res;
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
			this.model.environment.removePerceptsByUnif(this, Literal.parseLiteral("at(wheat[X]," + this.literal() + ")[_]"));

			if (state == null) {
				this.produce = null;
			} else{
				this.produce.state = state;
				this.model.environment.addPercept(this, Literal.parseLiteral("at(" + this.produce.literal() + "," + this.literal() + ")"));
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
			this.treetop = new LinkedList<>();
			this.below = new LinkedList<>();
		}

		@Override
		public boolean enter(Character character) {
			boolean result;

			if (character.canFly()) {
				result = this.enter(character, this.treetop);
				logger.info(character.name + " flies to the top of an ancient tree");

				// characters in other places see the inventory of the character on the tree (apart from the character itself)
				List<Character> otherPlacesChars = this.model.getCharacters().stream().filter(chara -> !chara.location.equals(this)).collect(Collectors.toList());
				for (Character observer : otherPlacesChars) {
					for (Item item : character.inventory) {
						this.model.environment.addEventPercept(observer.getName(),
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
			return this.treetop.contains(character) | this.below.contains(character);
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
			res.addAnnot(ASSyntax.createLiteral("state", ASSyntax.createAtom(this.state.toString().toLowerCase())));
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