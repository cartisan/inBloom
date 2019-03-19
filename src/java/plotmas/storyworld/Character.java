package plotmas.storyworld;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSemantics.Mood;
import jason.asSemantics.Personality;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import plotmas.LauncherAgent;
import plotmas.PlotLauncher;
import plotmas.PlotModel;
import plotmas.helper.PerceptAnnotation;
import plotmas.jason.PlotAwareAg;


/**
 * Responsible for modeling the state of an agent/character in the storyworld. Manages the agent's
 * inventory that consists of {@link Item item subclasses} by offering methods to modify it. <br> 
 * Subclasses should implement further, domain-specific methods that execute character behavior and
 * modify a character's state.
 * @author Leonid Berov
 */
public class Character {
    static Logger logger = Logger.getLogger(Character.class.getName());
	
    private PlotAwareAg plotAgentPendant;
    private PlotModel<?> model;

    public LinkedList<Item> inventory = new LinkedList<Item>();
    public String name = null;
    public Location location = null;

	public Character() {
	}
	
	public Character(String name) {
		this.initialize(name);
	}
	
	public void initialize(LauncherAgent lAgent) {
		this.setName(lAgent.name);
		this.setPlotAgentPendant(lAgent.name);
	}
	
	public void initialize(String name) {
		this.setName(name);
		this.setPlotAgentPendant(name);
	}
	
	/**
	 * Creates a list of {@link jason.asSyntax.Literal literal-}perceptions that represent the complete state of an 
	 * agent's inventory, which can be used to generate ASL agent perceptions.
	 * @return
	 */
	public LinkedList<Literal> createInventoryPercepts() {
		LinkedList<Literal> invRepr = new LinkedList<>();
		
		for (Item item : inventory) {
			invRepr.add(ASSyntax.createLiteral("has", item.literal()));
		}

		return invRepr;
	}
	
	public void addToInventory(Item item) {
		inventory.add(item);
	}
	
	public Item removeFromInventory(Item item) {
		if(inventory.remove(item)) {
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
		for (Item item : inventory) {
			if (item.getItemName().equals(itemType)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Item get(String itemType) {
		for (Item item : inventory) {
			if (item.getItemName().equals(itemType)) {
				return item;
			}
		}
		
		return null;
	}
	
	public boolean share(String itemType, Character receiver) {
		if (this.has(itemType)) {
			Item item = this.get(itemType);
			receiver.receive(item, this);
			this.model.getEnvironment().addEventPerception(name, 
														   String.format("shared(%s,%s)", item.literal(), receiver.name),
														   new PerceptAnnotation().addTargetedEmotion("pride", "self"));
			return true;
		}
		
		return false;
	}
	
	public boolean share(String itemType, List<Character> receivers) {
		if (this.has(itemType)) {
			Item item = this.get(itemType);
			
			for(Character receiver : receivers) {
				receiver.receive(item, this);
			}
			
			String recList = receivers.stream().map(rec -> rec.name)
											   .collect(Collectors.joining(",", "[", "]"))
											   .toString();
			
			this.model.getEnvironment().addEventPerception(name,
														   String.format("share(%s,%s)", item.literal(), recList),
														   new PerceptAnnotation().addTargetedEmotion("pride", "self"));
			
			logger.info(this.name + " shared some " + item.literal() + ".");
			
			return true;
		}
		
		return false;
	}
	
	public boolean receive(Item item, Character from) {
		this.addToInventory(item);
		
		this.model.getEnvironment().addEventPerception(name,
													   String.format("receive(%s,%s)", item.literal(), this.name),
													   new PerceptAnnotation().addTargetedEmotion("gratitude", "self"));
		
		//logger.info(this.name + " received some " + item.literal() + ".");
		logger.info(this.name + " received some " + item.getItemName() + ".");
	
	
		return true;
	}

	public boolean eat(String itemType) {
		if (this.has(itemType)) {
			Item item = this.get(itemType);
			
			if (item.isEdible()) {
				this.removeFromInventory(item);
				this.model.getEnvironment().addEventPerception(name, 
															   String.format("eat(%s)", item.literal()),
															   PerceptAnnotation.fromEmotion("satisfaction"));
				
				// in theory: here double dispatch
				// so food can affect agent in specific
				// way
				
				logger.info(this.name + " ate some " + item.literal() + ".");
				return true;
			}
		}
			
		return false;
	}
	
	public boolean relax() {
//		this.model.getEnvironment().addEventPerception(name, "relax", PerceptAnnotation.fromEmotion("joy"));
		return true;
	}
	
	public boolean flying() {
		// TODO: find a flexible implementation
		if (name == "crow") {
			return true;
		}
		return false;
	}
	
	public boolean farmAnimal() {
		// TODO: find a flexible implementation
		if (name == "crow") {
			return false;
		}
		else if (name == "fox") {
			return false;
		}
		return true;
	}

	public String toString() {
		return this.name + "-agent_model";
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
