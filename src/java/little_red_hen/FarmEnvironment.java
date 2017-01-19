package little_red_hen;

import java.util.logging.Logger;
import jason.asSyntax.*;
import jason.environment.Environment;

public class FarmEnvironment extends Environment {
    
    static Logger logger = Logger.getLogger(FarmEnvironment.class.getName());
	
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
    	
    	// TODO: make this search inventory and if term is found
    	// TODO: execute functor on retrieved item
    	if (action.getFunctor().equals("eat")) {
    		result = false;
    		if (action.getTerm(0).toString().equals("bread")) {
    			result = model.eatBread(action.getTerm(1).toString());
    		}
    	
    	}
    	if (action.getFunctor().equals("help")) {
    		result = true;
    	}
    	
    	
    	updatePercepts();
    	return result;
    }
    
    void updatePercepts() {
    	// create hen's inventory
    	removePerceptsByUnif("hen", Literal.parseLiteral("has(X)"));
    	for (String literal : model.hen.createInventoryPercepts()) {
    		addPercept("hen", Literal.parseLiteral(literal));    		
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
