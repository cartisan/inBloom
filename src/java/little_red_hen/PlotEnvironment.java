package little_red_hen;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;
import jason.environment.TimeSteppedEnvironment;
import jason.util.Pair;

public class PlotEnvironment extends TimeSteppedEnvironment {
	public static final Integer MAX_REPEATE_NUM = 10;
    static Logger logger = Logger.getLogger(PlotEnvironment.class.getName());
    
    protected PlotModel model;
    
    /**
     * Stores a mapping from agentName to a (String actionName, Integer count) tuple, which stores how many
     * consecutive times the agent has been executing the action 'actionName'.
     * This is used to pause simulation execution if all agents just repeat their actions for a while.
     */
    protected HashMap<String, Pair<String, Integer>> agentActionCount;  // agentName -> (action, #consecutive repeats)
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

    
    public void initialize(List<AgentModel> agents) {
    	initializeActionCounting(agents);
    }
    
	@Override
    public boolean executeAction(String agentName, Structure action) {
    	// add attempted action to plot graph
    	PlotGraph.getPlotListener().addEvent(agentName, action.toString());
		
		return false;
	}
    
    /********************** Methods for updating agent percepts **************************
    * - distinguishes between:
    *   - perceptions of model states, things that are constantly there but might have different values
    *   - perceptions of occured events, which happen once and hence also need to be perceived only once by everyone 
    *     present
    */
    
	/**
	 * Updates the perception list with the current state of the model, and returns a list of up to date percepts.
	 * @see jason.environment.TimeSteppedEnvironment#getPercepts(java.lang.String)
	 */
	@Override
	public Collection<Literal> getPercepts(String agName) {
		this.updatePercepts(agName);
		return super.getPercepts(agName);
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
     * as opposed to updating percepts related to events. States relate to percepts that are
     * maintained over time: inventories, creatures being present and so on.
     * 
     * Subclass this method to add domain-specific states to the percepts, don't forget to
     * first call `super.updateStatePercepts(agentName);`
     * 
     * @see little_red_hen.jason.FarmEnvironment#updateStatePercepts(java.lang.String)
     */
    protected void updateStatePercepts(String agentName) {
    	// update list of present animals (excluding self)
    	removePerceptsByUnif(Literal.parseLiteral("animals(X)"));
    	Set<String> presentAnimals = new HashSet<>(this.model.agents.keySet());
    	presentAnimals.remove(agentName);
    	List<Term> animList = presentAnimals.stream().map(ASSyntax::createAtom).collect(Collectors.toList());
    	addPercept(agentName, ASSyntax.createLiteral("animals", ASSyntax.createList(animList)));
    	
    	// create inventories
    	removePerceptsByUnif(agentName, Literal.parseLiteral("has(X)"));
    	for (String literal : this.model.agents.get(agentName).createInventoryPercepts()) {
    		addPercept(agentName, Literal.parseLiteral(literal));    		
    	}    		
    }

    /**
     * Updates percepts that are related to events that occurred in the mean-time,
     * as opposed to updating percepts related to states.
     * Events relates to unique percepts that are delivered to the agent only once.
     */
	protected void updateEventPercepts(String agentName) {
		deleteOldUniqueEvents(agentName);
    	addNewUniqueEvents(agentName);
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
     * Removes percepts of events that happened second-last cycle from agent's perception list.
     */
	private void deleteOldUniqueEvents(String agentName) {
    	// 1. get list of unique events for agent agentName, scheduled for deletion this cycle
    	List<Literal> eventList = getListRemEvents(agentName);
    	
    	// 2. delete respective percepts from agent agentName
    	for(Literal event : eventList) {
			removePercept(agentName, event);
    	}
    	// clean up Map for this step to free up space
    	this.perceivedEventsMap.remove(agentName);
	}
	
	/**
	 * Returns the list of events that need to be added to agents perception list this reasoning step.
	 */	
	protected List<String> getListCurrentEvents(String agentName) {
		return this.currentEventsMap.getOrDefault(agentName, new LinkedList<String>());
	}
	
	/**
	 * Returns the list of events that need to be deleted from agent's perception list because they were
	 * conveyed to it last cycle.
	 */
	private List<Literal> getListRemEvents(String agentName) {
		return this.perceivedEventsMap.getOrDefault(agentName, new LinkedList<Literal>());
	}

	/**
	 * Adds 'event' to the list of events that need to be added to agentName's perception list this reasoning step.
	 */	
	public void addEventPerception(String agentName, String event) {
		List<String> eventList = this.getListCurrentEvents(agentName);		
		eventList.add(event);
		this.currentEventsMap.put(agentName, eventList);
	}
	
    
    /********************** Methods for pausing the execution after nothing happens **************************
    * checks if all agents executed the same action for the last MAX_REPEATE_NUM of times, if yes, pauses the simu.
    */
    protected void initializeActionCounting(List<AgentModel> agents) {
        HashMap<String, Pair<String, Integer>> agentActionCount = new HashMap<>();
        
        // set up connections between agents, model and environment
        for (AgentModel agent : agents) {
        	agentActionCount.put(agent.name, new Pair<String, Integer>("", 1));
        }
        this.agentActionCount = agentActionCount;
    }
	
	protected void pauseOnRepeat(String agentName, Structure action) {
		Pair<String, Integer> actionCountPair = agentActionCount.get(agentName);
		
		// same action was repeated Launcher.MAX_REPEATE_NUM number of times by all agents:
    	if (allAgentsRepeating()) {
    		// reset counter
    		RedHenLauncher.runner.pauseExecution();
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
	
    private boolean allAgentsRepeating() {
    	for (Pair<String, Integer> actionCountPair : agentActionCount.values()) {
    		if (actionCountPair.getSecond() < MAX_REPEATE_NUM) {
    			return false;
    		}
    	}
    	// all agents counts are >= MAX_REPEATE_NUM
    	return true;
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
}
