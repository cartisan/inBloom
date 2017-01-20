package little_red_hen;

import java.util.logging.Logger;
import jason.asSyntax.*;
import jason.environment.Environment;
import little_red_hen.FarmModel.Agent;
import little_red_hen.FarmModel.Bread;
import little_red_hen.FarmModel.Wheat;

import com.google.common.collect.ImmutableMap;

public class FarmEnvironment extends Environment {
    
    static Logger logger = Logger.getLogger(FarmEnvironment.class.getName());
	
    ImmutableMap<String, Class> term2JavaMap = ImmutableMap.of(
    	    "bread", (Class)Bread.class,
    	    "wheat", (Class)Wheat.class
    	);
    
    private FarmModel model;
	
	public FarmEnvironment() {
		this.model = new FarmModel();
		updatePercepts();
	}
	
	
    @Override
    public boolean executeAction(String ag, Structure action) {
    	boolean result = false;
    	
    	// TODO: this could be done nicer with meta programming!
    	if (action.getFunctor().equals("randomFarming")) {
    		result = model.randomFarming();
    	}
    	
    	if (action.toString().equals("plant(wheat)")) {
    		result = model.plantWheat();
    	}
    	
    	if (action.toString().equals("tend(wheat)")) {
    		result = model.tendWheat();
    	}
    	
    	if (action.toString().equals("harvest(wheat)")) {
    		result = model.harvestWheat();
    	}
    	
    	if (action.toString().equals("grind(wheat)")) {
    		result = model.grindWheat();
    	}
    	
    	if (action.toString().equals("bake(bread)")) {
    		result = model.bakeBread();
    	}
    	
    	if (action.getFunctor().equals("eat")) {
    		Agent agent = model.getAgent(action.getTerm(0).toString());
    		Class itemType = term2JavaMap.get(action.getTerm(1).toString());
    		boolean success = agent.eat(itemType);
    		
    		if (success) {logger.info(action.getTerm(0).toString() + " ate some " + action.getTerm(1).toString());}
    		return success;
    	}

    	if (action.getFunctor().equals("help")) {
    		result = true;
    	}
    	
    	if (action.getFunctor().equals("share")) {
    		String sender = action.getTerm(0).toString();
    		String item = action.getTerm(1).toString();
    		String receiver = action.getTerm(2).toString();
    		
    		Agent agent = model.getAgent(sender);
    		Class<Item> itemType = term2JavaMap.get(item);
    		Agent patient = model.getAgent(receiver);
    		boolean success = agent.share(itemType, patient);
    		
    		result = success;
    	}
    	
    	
    	updatePercepts();
    	return result;
    }
    
    void updatePercepts() {
    	// create inventories
    	removePerceptsByUnif("hen", Literal.parseLiteral("has(X)"));
    	for (String literal : model.hen.createInventoryPercepts()) {
    		addPercept("hen", Literal.parseLiteral(literal));    		
    	}
    	
    	removePerceptsByUnif("dog", Literal.parseLiteral("has(X)"));
    	for (String literal : model.dog.createInventoryPercepts()) {
    		addPercept("dog", Literal.parseLiteral(literal));    		
    	}

    	
    	// update publicly known wheat state
    	if (!(model.wheat == null)) {
    		removePerceptsByUnif(Literal.parseLiteral("wheat(X)"));
    		addPercept(Literal.parseLiteral(model.wheat.literal()));
    	}
    	else {
    		removePerceptsByUnif(Literal.parseLiteral("wheat(X)"));
    	}
    }

	public static void main(String[] args) {
        System.out.println("Hello World!"); 
        new FarmEnvironment();
        System.out.println("It's alife!"); 
	}
}
