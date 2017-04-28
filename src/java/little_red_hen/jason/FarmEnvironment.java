package little_red_hen.jason;

import java.util.HashMap;
import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.runtime.MASConsoleGUI;
import jason.util.Pair;
import little_red_hen.Agent;
import little_red_hen.FarmModel;
import little_red_hen.Launcher;
import little_red_hen.PlotGraph;

public class FarmEnvironment extends Environment {
    
    static Logger logger = Logger.getLogger(FarmEnvironment.class.getName());
    
    private FarmModel model;
    private HashMap<String, Pair<String, Integer>> agentActionCount;

	
	public HashMap<String, Pair<String, Integer>> getAgentActionCount() {
		return agentActionCount;
	}

	public void setAgentActionCount(HashMap<String, Pair<String, Integer>> agentActionCount) {
		this.agentActionCount = agentActionCount;
	}
    
    public void resetAllAgentActionCounts() {
    	for (String agent : agentActionCount.keySet()) {
    		agentActionCount.put(agent, new Pair<String, Integer>("", 1));
    	}
    }

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
    	if (action.getFunctor().equals("random_farming")) {
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
    	
    	pauseOnRepeat(agentName, action);
    	return result;
    }

    private boolean allAgentsRepeating() {
    	for (Pair<String, Integer> actionCountPair : agentActionCount.values()) {
    		if (actionCountPair.getSecond() < Launcher.MAX_REPEATE_NUM) {
    			return false;
    		}
    	}
    	// all agents counts are > 4
    	return true;
    }
    
	private void pauseOnRepeat(String agentName, Structure action) {
		Pair<String, Integer> actionCountPair = agentActionCount.get(agentName);
		
		// same action was repeated Launcher.MAX_REPEATE_NUM number of times by all agents:
    	if (allAgentsRepeating()) {
    		// reset counter
    		Launcher.runner.pauseExecution();
    		resetAllAgentActionCounts();
    	}
    	
    	// new action is same as last action
    	if (actionCountPair.getFirst().equals(action.toString())) {
    		agentActionCount.put(agentName, new Pair<String, Integer>(action.toString(),
    																  actionCountPair.getSecond()+1));
    	} 
    	// new action different from last action
    	else {
    		agentActionCount.put(agentName, new Pair<String, Integer>(action.toString(), 1));
    	}
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
