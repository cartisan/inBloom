package little_red_hen.jason;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.parser.ParseException;
import jason.environment.TimeSteppedEnvironment;
import jason.util.Pair;
import little_red_hen.Agent;
import little_red_hen.FarmModel;
import little_red_hen.Launcher;
import little_red_hen.PlotGraph;

public class FarmEnvironment extends TimeSteppedEnvironment {
    
    static Logger logger = Logger.getLogger(FarmEnvironment.class.getName());
    
    private FarmModel model;
    private HashMap<String, Pair<String, Integer>> agentActionCount;  // agentName -> (action, #subsequent repeats)
    
    private HashMap<String,List<String>> uniqueEventsAdd = new HashMap<>();  // {agentName -> List(events)}
    private HashMap<Pair<Integer,String>, List<Literal>> uniqueEventsRem = new HashMap<>();  // {(stepNum,agentName} -> List(events)

	
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
    
	public void updatePercepts() {
		for(String name: this.model.agents.keySet()) {
			updatePercepts(name);
		}
	}
	
    public void updatePercepts(String agentName) {
		// create inventories
    	removePerceptsByUnif(agentName, Literal.parseLiteral("has(X)"));
    	for (String literal : this.model.agents.get(agentName).createInventoryPercepts()) {
    		addPercept(agentName, Literal.parseLiteral(literal));    		
    	}    		

    	// update publicly known wheat state
    	if (!(model.wheat == null)) {
    		removePerceptsByUnif(agentName, Literal.parseLiteral("wheat(X)"));
    		addPercept(agentName, Literal.parseLiteral(model.wheat.literal()));
    	}
    	else {
    		removePerceptsByUnif(agentName, Literal.parseLiteral("wheat(X)"));
    	}

    	deleteOldUniqueEvents(agentName);
    	addNewUniqueEvents(agentName);
    }

    /*
     * Removes mental notes of events that happened last cycle from agent's BB.
     */
	private void deleteOldUniqueEvents(String agentName) {
    	// 1. get list of unique events for agent agentName, scheduled for deletion this cycle
    	List<Literal> eventList =getListRemEvents(this.getStep(), agentName);
    	
    	// 2. delete respective percepts from agent agentName
    	for(Literal event : eventList) {
			removePercept(agentName, event);
    	}
    	// clean up Map for this step to free up space
    	this.uniqueEventsRem.remove(new Pair<>(this.getStep(), agentName));
	}
	
	/*
	 * Adds events that happened this cycle as mental notes to agent's BB.
	 */
	private void addNewUniqueEvents(String agentName) {
    	for( String event : this.getListCurrentEvents(agentName) ) {
    		try {
    			Literal percept = ASSyntax.parseLiteral(event);
				addPercept(agentName, percept);
				
				//get list of events to be removed next cycle
				List<Literal> remList = getListRemEvents(this.getStep()+1, agentName);
				
				// add percept to this list and put new list back into storing map 
				remList.add(percept);
				this.uniqueEventsRem.put(new Pair<>(this.getStep()+1, agentName), remList);
				
			} catch (ParseException e) {
				logger.severe(e.getMessage());
			}
    	}
    	this.uniqueEventsAdd.remove(agentName);
	}
	
	/*
	 * Returns the list of events that need to be added to agents BB this reasoning step.
	 */	
	private List<String> getListCurrentEvents(String agentName) {
		return this.uniqueEventsAdd.getOrDefault(agentName, new LinkedList<String>());
	}

	/*
	 * Adds the event to the list of events that need to be added to agents BB this reasoning step.
	 */	
	public void addToListCurrentEvents(String agentName, String event) {
		List<String> eventList = this.getListCurrentEvents(agentName);		
		eventList.add(event);
		this.uniqueEventsAdd.put(agentName, eventList);
	}	
	
	/*
	 * Returns the list of events that need to be deleted from agents BB at reasoning step step.
	 */
	private List<Literal> getListRemEvents(int step, String agentName) {
    	Pair<Integer,String> stepAgentTup = new Pair<>(step, agentName);
		return this.uniqueEventsRem.getOrDefault(stepAgentTup, new LinkedList<Literal>());
	}

}
