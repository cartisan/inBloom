package little_red_hen.jason;

import java.util.Collection;
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
    
    /**
     * Stores a mapping from agentNames to a list of new events, that happened in model but agent hasn't perceived yet:<br>
     * 	&nbsp; {agentName -> List(events:String)}<br>
     * These events will be transformed into perceptions (type:Literal) during a subsequent run of 
     * <i>updateEventPercepts/1</i>, and delivered to the agent during <i>getPercepts/1</i>. They will be than marked
     * for deletion from the agent's percepts.
     */
    private HashMap<String,List<String>> currentEventsMap = new HashMap<>();
    
    /**
     * Stores a mapping from agentNames to a list of old events, that have already been perceived by the agent:<br>
     * 	&nbsp; {agentName -> List(events:Literal)}<br>
     * These events will be removed from the agents list of perception during a subsequent run of
     * <i>updateEventPercepts/1</i>. 
     */
    private HashMap<String, List<Literal>> perceivedEventsMap = new HashMap<>();


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
    	
    	pauseOnRepeat(agentName, action);
    	return result;
    }

    
	public void updatePercepts() {
		for(String name: this.model.agents.keySet()) {
			updatePercepts(name);
		}
	}
	
    public void updatePercepts(String agentName) {
		updateStatePercepts(agentName);
    	updateEventPercepts(agentName);
    }
    
    /**
     * Updates percepts that are related to the state of the model and the agent,
     * as opposed updating percepts related to events.
     * States relate to percepts that are maintained over time.
     */
    private void updateStatePercepts(String agentName) {
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
    }

    /**
     * Updates percepts that are related to events that occurred in the mean-time,
     * as opposed updating percepts related to states.
     * Events relates to unique percepts that are delivered to the agent only once.
     */
	private void updateEventPercepts(String agentName) {
		deleteOldUniqueEvents(agentName);
    	addNewUniqueEvents(agentName);
	}

    /**
     * Removes percepts of events that happened second-last cycle from agent's perception list.
     */
	private void deleteOldUniqueEvents(String agentName) {
    	// 1. get list of unique events for agent agentName, scheduled for deletion this cycle
    	List<Literal> eventList =getListRemEvents(agentName);
    	
    	// 2. delete respective percepts from agent agentName
    	for(Literal event : eventList) {
			removePercept(agentName, event);
    	}
    	// clean up Map for this step to free up space
    	this.perceivedEventsMap.remove(agentName);
	}
	
	/**
	 * Adds percepts of events that happened during last cycle to agent's perception list.
	 */
	private void addNewUniqueEvents(String agentName) {
    	for( String event : this.getListCurrentEvents(agentName) ) {
    		try {
    			Literal percept = ASSyntax.parseLiteral(event);
				addPercept(agentName, percept);
				
				//get list of events to be removed next cycle
				List<Literal> remList = getListRemEvents(agentName);
				
				// add percept to this list and put new list back into storing map 
				remList.add(percept);
				this.perceivedEventsMap.put(agentName, remList);
				
			} catch (ParseException e) {
				logger.severe(e.getMessage());
			}
    	}
    	this.currentEventsMap.remove(agentName);
	}
	
	/**
	 * Updates the perception list with the current state of the model, and returns a list of up to date percepts.
	 * @see jason.environment.TimeSteppedEnvironment#getPercepts(java.lang.String)
	 */
	@Override
	public Collection<Literal> getPercepts(String agName) {
		this.updatePercepts(agName);
		return super.getPercepts(agName);
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
	
	/**
	 * Returns the list of events that need to be added to agents perception list this reasoning step.
	 */	
	private List<String> getListCurrentEvents(String agentName) {
		return this.currentEventsMap.getOrDefault(agentName, new LinkedList<String>());
	}

	/**
	 * Adds the event to the list of events that need to be added to agents perception list this reasoning step.
	 */	
	public void addToListCurrentEvents(String agentName, String event) {
		List<String> eventList = this.getListCurrentEvents(agentName);		
		eventList.add(event);
		this.currentEventsMap.put(agentName, eventList);
	}	
	
	/**
	 * Returns the list of events that need to be deleted from agent's perception list because they were
	 * conveyed to it last cycle.
	 */
	private List<Literal> getListRemEvents(String agentName) {
		return this.perceivedEventsMap.getOrDefault(agentName, new LinkedList<Literal>());
	}
	
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
}
