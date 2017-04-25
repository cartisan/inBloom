package little_red_hen.jason;

import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import little_red_hen.Agent;
import little_red_hen.FarmModel;
import little_red_hen.PlotGraph;

public class FarmEnvironment extends Environment {
    
    static Logger logger = Logger.getLogger(FarmEnvironment.class.getName());
    
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
    public boolean executeAction(String agentName, Structure action) {
    	boolean result = false;
    	Agent agent = model.getAgent(agentName);
    	
    	// add attempted action to plot graph
    	PlotGraph.getPlotListener().addEvent(agentName, action.toString());
    	
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
    		String item = action.getTerm(0).toString();
    		result = agent.eat(item);
    	}

    	if (action.getFunctor().equals("help")) {
    		result = true;
    	}
    	
    	if (action.getFunctor().equals("share")) {
    		String item = action.getTerm(1).toString();
    		String receiver = action.getTerm(2).toString();
    		
    		Agent patient = model.getAgent(receiver);
    		result = agent.share(item, patient);
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
