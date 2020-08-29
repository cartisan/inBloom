package inBloom.storyworld;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jason.RevisionFailedException;
import jason.asSemantics.AffectiveDimensionChecks;
import jason.asSemantics.Mood;
import jason.asSemantics.Personality;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;

import inBloom.ActionReport;
import inBloom.LauncherAgent;
import inBloom.PlotLauncher;
import inBloom.PlotModel;
import inBloom.helper.PerceptAnnotation;
import inBloom.jason.PlotAwareAg;


/**
 * Responsible for modeling the state of an agent/character in the storyworld. Manages the agent's
 * inventory that consists of {@link Item item subclasses} by offering methods to modify it. <br>
 * Subclasses should implement further, domain-specific methods that execute character behavior and
 * modify a character's state.
 * @author Leonid Berov
 */
public class Character extends Existent {
    static Logger logger = Logger.getLogger(Character.class.getName());

    private PlotModel<?> model;

	@ModelState
    public LinkedList<Item> inventory = new LinkedList<>();
    public String name = null;
    public Location location = null;
    public PlotAwareAg plotAgentPendant;

    public boolean isSick = false;
    
	public Character() {
	}

	public Character(String name) {
		this.initialize(name);
	}

	public void initialize(LauncherAgent lAgent) {
		this.setName(lAgent.name);
		this.setPlotAgentPendant(lAgent.name);

		// important to set location and inventory using the respective methods, so that percepts are generated for ASL agent
		this.model.getLocation(lAgent.location).enter(this);
		for (Item i : lAgent.inventory) {
			try {
				this.plotAgentPendant.addBel(ASSyntax.createLiteral("has", i.literal()));
				this.addToInventory(i);
			} catch (RevisionFailedException e) {
				logger.severe("Couldn't add belief about initial inventory item: " + i.getItemName());
			}
		}
	}

	public void initialize(String name) {
		this.setName(name);
		this.setPlotAgentPendant(name);
	}

	public ActionReport goTo(Location target) {
		return new ActionReport(target.enter(this));
	}

	/**
	 * Creates a list of {@link jason.asSyntax.Literal literal-}perceptions that represent the complete state of an
	 * agent's inventory, which can be used to generate ASL agent perceptions.
	 * @return
	 */
	public LinkedList<Literal> createInventoryPercepts() {
		LinkedList<Literal> invRepr = new LinkedList<>();

		for (Item item : this.inventory) {
			invRepr.add(ASSyntax.createLiteral("has", item.literal()));
		}

		return invRepr;
	}

	public void addToInventory(Item item) {
		this.inventory.add(item);
	}

	public Item removeFromInventory(Item item) {
		if(this.inventory.remove(item)) {
			return item;
		}
		logger.severe("Character " + this.name + " can't remove item " + item.getItemName() + ". Doesn't have one.");
		return null;
	}

	public Item removeFromInventory(String itemName) {
		for (Item item : this.inventory) {
			if (item.getItemName().equals(itemName)) {
				return item;
			}
		}
		logger.severe("Character " + this.name + " can't remove item " + itemName + ". Doesn't have one.");
		return null;
	}

	public boolean has(String itemType) {
		for (Item item : this.inventory) {
			if (item.getItemName().equals(itemType)) {
				return true;
			}
		}

		return false;
	}

	public Item get(String itemType) {
		for (Item item : this.inventory) {
			if (item.getItemName().equals(itemType)) {
				return item;
			}
		}

		return null;
	}

	public ActionReport share(String itemType, Character receiver) {
		ActionReport res = new ActionReport();

		if (this.has(itemType)) {
			res.addPerception(this.name, new PerceptAnnotation().addTargetedEmotion("pride", "self"));
			res.addPerception(receiver.name, new PerceptAnnotation().addTargetedEmotion("gratitude", receiver.name));
			res.success = true;

			// sharing (as opposed to giving) doesn't remove item from giver's inventory... Yay cornucopia!
			Item item = this.get(itemType);
			receiver.addToInventory(item);
			logger.info(this.name + " shared some " + item.literal() + ".");
		}

		return res;
	}

	public ActionReport share(String itemType, List<Character> receivers) {
		ActionReport res = new ActionReport();

		if (this.has(itemType)) {
			Item item = this.get(itemType);

			res.addPerception(this.name, new PerceptAnnotation().addTargetedEmotion("pride", "self"));
			for(Character receiver : receivers) {
				// sharing (as opposed to giving) doesn't remove item from giver's inventory... Yay cornucopia!
				res.addPerception(receiver.name, new PerceptAnnotation().addTargetedEmotion("gratitude", receiver.name));
				receiver.addToInventory(item);
			}

			res.success = true;
			logger.info(this.name + " shared some " + item.literal() + ".");
		}

		return res;
	}

	public ActionReport eat(String itemType) {
		ActionReport res = new ActionReport();

		if (this.has(itemType)) {
			Item item = this.get(itemType);

			if (item.isEdible()) {
				this.removeFromInventory(item);
				res.addPerception(this.name, PerceptAnnotation.fromEmotion("joy"));
				res.success = true;
			}
		}

		return res;
	}

	public ActionReport relax() {
		if( AffectiveDimensionChecks.BOUNDARIES.get(AffectiveDimensionChecks.HIG).apply(this.getMood().getA())) {
			logger.info(this.name + " is to aroused to be able to relax.");
			return new ActionReport(false);
		}
		return new ActionReport(true);
	}

	public ActionReport fret() {
		ActionReport res = new ActionReport(true);
		res.addPerception(this.name, PerceptAnnotation.fromEmotion("distress"));
		return res;
	}

	public ActionReport sing() {
		ActionReport res = new ActionReport(true);

		logger.info(this.name + " sings.");

		// if character is in the sky and sings then they loose whatever they held in their (mouth/beak-) inventory
		if(this.location.isSkyLevel(this)) {
			for (Item item : this.inventory) {
				this.removeFromInventory(item);
				this.location.place(item);
				logger.info(this.name + " lost " + item.getItemName() + " which fell to the ground.");

				// everyone present sees things dropping from the sky
				String perceptString = "is_dropped(" + item.getItemName() + ")";
				PerceptAnnotation annots = PerceptAnnotation.fromCause("sing");
				annots.addAnnotation("owner", this.name);
				annots.addCrossCharAnnotation(perceptString, System.nanoTime());

				for (Character observer : this.location.getCharacters()) {
					this.model.environment.addEventPercept(observer.getName(),
													       perceptString,
													       annots);
				}
			}
		}

		return res;
	}

	public ActionReport getPoisoned() {
		
		ActionReport result = new ActionReport();
		
		logger.info(this.name + " was poisoned :o");
		this.isSick = true;
		result.success = true;
		
		return result;
	}

	public ActionReport heal() {
		ActionReport result = new ActionReport();
		
		logger.info(this.name + " is healed.");
		this.isSick = false;
		result.success = true;
		
		return result;
	}
	
	public ActionReport collect(String thing) {
		ActionReport res = new ActionReport();

		if(this.location.contains(thing)) {
			Item item = this.location.remove(thing);
			this.addToInventory(item);
			res.success =  true;
		}
		return res;
	}

	public ActionReport handOver(String itemName, Character receiver) {
		ActionReport res = new ActionReport();

		if (this.has(itemName) & this.location.present(receiver)){
			Item item = this.removeFromInventory(itemName);
			receiver.addToInventory(item);
			logger.info(this.name + " hands over " + itemName + " to " +receiver);

			res.addPerception(this.name, new PerceptAnnotation("fear", "remorse"));
			res.addPerception(receiver.name, new PerceptAnnotation("gloating", "pride"));
			res.success = true;
		}

		return res;
	}

	public ActionReport refuseHandOver(String itemName, Character receiver) {
		ActionReport res = new ActionReport();

		if (this.has(itemName) & this.location.present(receiver)){
			logger.info(this.name + " does not hand over" + itemName + " to " + receiver);

			res.addPerception(this.name, PerceptAnnotation.fromEmotion("pride"));
			res.addPerception(receiver.name, PerceptAnnotation.fromEmotion("anger"));
			res.success = true;
		}

		return res;
	}
	

	public boolean canFly() {
		// TODO: find a flexible implementation
		if (this.name == "crow") {
			return true;
		}
		return false;
	}

	public String toString() {
		return this.name;
	}

	public void setModel(PlotModel<?> model) {
		this.model = model;
	}

	public Personality getPersonality() {
		return this.plotAgentPendant.getPersonality();
	}

	public Mood getMood() {
		return this.plotAgentPendant.getMood();
	}

	public void setName(String agentName) {
		this.name = agentName;
	}

	public String getName() {
		return this.name;
	}

	public void setPlotAgentPendant(PlotAwareAg pAgent) {
		this.plotAgentPendant = pAgent;
	}

	public void setPlotAgentPendant(String pAgentName) {
		this.plotAgentPendant = PlotLauncher.runner.getPlotAgent(pAgentName);
	}
}
