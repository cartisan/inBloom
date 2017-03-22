package little_red_hen;

import java.util.HashMap;
import java.util.logging.Logger;
import jason.asSyntax.*;
import jason.environment.Environment;
import little_red_hen.FarmModel.Bread;
import little_red_hen.FarmModel.Wheat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class FarmEnvironment extends Environment {
    
    static Logger logger = Logger.getLogger(FarmEnvironment.class.getName());
	
    ImmutableMap<String, Class> term2JavaMap = ImmutableMap.of(
    	    "bread", (Class)Bread.class,
    	    "wheat", (Class)Wheat.class
    	);
    
    private FarmModel model;
	
	public FarmModel getModel() {
		return model;
	}

	public void setModel(FarmModel model) {
		this.model = model;
		updatePercepts();
	}


	public FarmEnvironment() {
		
	}


    @Override
    public boolean executeAction(String ag, Structure action) {
    	boolean result = false;
    	Agent agent = model.getAgent(ag);
    	
    	// TODO: this could be done nicer with meta programming!
    	if (action.getFunctor().equals("randomFarming")) {
    		result = model.randomFarming(agent);
    	}
    	
    	if (action.getFunctor().equals("plant")) {
    		// TODO: type checking on wheat
			result = model.plantWheat(agent);
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
    	
    	if (action.getFunctor().equals("bake")) {
    		// TODO: type checking on bread
    		result = model.bakeBread(agent);
    	}
    	
    	if (action.getFunctor().equals("eat")) {
    		Class itemType = term2JavaMap.get(action.getTerm(0).toString());
    		boolean success = agent.eat(itemType);
    		
    		if (success) {logger.info(ag + " ate some " + action.getTerm(0).toString());}
    		return success;
    	}

    	if (action.getFunctor().equals("help")) {
    		result = true;
    	}
    	
    	if (action.getFunctor().equals("share")) {
    		String item = action.getTerm(1).toString();
    		String receiver = action.getTerm(2).toString();
    		
    		Class<Item> itemType = term2JavaMap.get(item);
    		Agent patient = model.getAgent(receiver);
    		boolean success = agent.share(itemType, patient);
    		
    		result = success;
    	}
    	
    	if (action.getFunctor().equals("relax")) {
			result = agent.relax();
    	}
    	
    	updatePercepts();
    	return result;
    }
    
    void updatePercepts() {
    	// create inventories
    	for(String name: this.model.agents.keySet()) {
        	removePerceptsByUnif(name, Literal.parseLiteral("has(X)"));
        	for (String literal : this.model.agents.get(name).createInventoryPercepts()) {
        		addPercept(name, Literal.parseLiteral(literal));    		
        	}    		
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
}
