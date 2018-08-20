package plotmas;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSemantics.ActionExec;
import jason.asSemantics.Intention;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;
import jason.environment.TimeSteppedEnvironment;
import jason.runtime.MASConsoleGUI;
import jason.util.Pair;
import plotmas.graph.PlotGraphController;
import plotmas.helper.EnvironmentListener;
import plotmas.helper.PerceptAnnotation;
import plotmas.helper.TermParser;

/**
 *  Responsible for relaying action requests from ASL agents to the {@link plotmas.PlotModel Storyworld} and
 *  perceptions from the Storyworld to ASL agents (via {@link jason.asSemantics.AffectiveAgent jason's AffectiveAgent}). 
 *  Each action is reported to the {@link plotmas.graph.PlotGraphController PlotGraphController} for visual representation. <br>
 *  Subclasses need to override {@link #executeAction(String, Structure)} to implement their domain-specific relaying 
 *  and should make sure to execute {@code super.executeAction(agentName, action);}, which will take care of plotting.
 *  
 *  <p> The environment is set up to pause a simulation if all agents repeated the same action for {@link #MAX_REPEATE_NUM}
 *  times.
 * 
 * @see plotmas.stories.little_red_hen.FarmEnvironment
 * @author Leonid Berov
 */
public abstract class PlotEnvironment<ModType extends PlotModel<?>> extends TimeSteppedEnvironment {
	public static final Integer MAX_REPEATE_NUM = 7;
	static final String STEP_TIMEOUT = "100";
	
    static Logger logger = Logger.getLogger(PlotEnvironment.class.getName());
    public static Long startTime = 0L;
    
    /**
     * A list of environment listeners which get called on certain events
     * in the environment.
     */
    private List<EnvironmentListener> listeners = new LinkedList<>();
    
    /**
     * Returns the current plot time in ms, i.e. the time that has passed since simulation was started
     * @return time in ms (Long)
     */
    public static Long getPlotTimeNow() {
    	return (System.nanoTime() - PlotEnvironment.startTime) / 1000000; // normalize nano to milli sec
    }
    
    protected ModType model;
    
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
    
    /**
     * Whenever an action is scheduled, this map stores the related intention.
     * This mapping is then used in <i>executeAction</i> to derive the intention of the agents action,
     * in order to create an actualization edge.
     * This relaying is necessary, because the action needs to be plotted when it is executed,
     * and not when it is scheduled, otherwise the graph would get out of order.
     */
    private ConcurrentHashMap<String, HashMap<Structure, Intention>> actionIntentionMap = new ConcurrentHashMap<>();
    
    /**
     * Jason-internal initialization executed by the framwork during 
     * {@link jason.infra.centralised.CentralisedEnvironment#CentralisedEnvironment(jason.mas2j.ClassParameters,
     *  jason.infra.centralised.BaseCentralisedMAS) CentralisedEnvironment instaniation}. Responsible for setting
     *  up timeouts and over action policy.
     *   
     * @see jason.environment.TimeSteppedEnvironment#init(java.lang.String[])
     */
    @Override
    public void init(String[] args) {
    	if (args.length > 0)
    		logger.warning("Initilization arguments provided but usage unclear, ignoring. Args: " + args.toString());
    	
    	String[] env_args = {STEP_TIMEOUT};
    	super.init(env_args);

    	// Make sure actions are executed even if reasoning cycle comes up with several actions in one environment step
    	this.setOverActionsPolicy(OverActionsPolicy.queue);
    }
    
    /**
     * Plotmas specific initialization executed after {@linkplain #init(String[])}, but shortly before MAS execution
     * ensues. Responsible for setting up plot-related information like story time and agents' action counting.
     * Executed during {@link PlotLauncher#initzializePlotEnvironment(com.google.common.collect.ImmutableList) PlotLauncher
     * initialization}.
     * @param agents
     */
    public void initialize(List<LauncherAgent> agents) {
    	PlotEnvironment.startTime = System.nanoTime();
    	initializeActionCounting(agents);
    }
    
    /**
     * Adds a listener to this plot environment.
     * @param l
     */
    public void addListener(EnvironmentListener l) {
    	this.listeners.add(l);
    }
    
    /**
     * Removes a listener from this plot environment.
     * @param l
     */
    public void removeListener(EnvironmentListener l) {
    	this.listeners.remove(l);
    }
    
	/**
	 * This method is called by the Jason framework in order to determine, which result an agent's action has, and 
	 * to compute potentially effects on the model.<br>
	 * It performs the following managing tasks:
	 * <ul>
	 *  <li>checking whether pause mode is on and execution needs to wait</li>
	 *  <li>relay action to the plot graph</li>
	 *  <li>execute the action</li>
	 *  <li>allow model to check if its state changed due to action execution</li> 
	 *  <li>switch to pause mode if nothing interesting happens</li>
	 * </ul>
	 * Do not override it to implement domain-specific action handling, for that see 
	 * {@linkplain #doExecuteAction(String, Structure)}.
	 */
	@Override
    public boolean executeAction(String agentName, Structure action) {
		// check if pause mode is enabled, wait with execution while it is
		this.waitWhilePause();
		
    	// add attempted action to plot graph
		String motivation = "[motivation(%s)]";
		Intention intent = actionIntentionMap.get(agentName).get(action);
		if(intent != null) {
			motivation = String.format(motivation, TermParser.removeAnnots(intent.peek().getTrigger().getTerm(1).toString()));
		} else {
			motivation = "";
		}
		actionIntentionMap.get(agentName).remove(action);
		
		PlotGraphController.getPlotListener().addEvent(agentName, action.toString() + motivation, getStep());
		
    	// let the domain specific subclass handle the actual action execution
    	// ATTENTION: this is were domain-specific action handling code goes
    	boolean result = this.doExecuteAction(agentName, action);		
    	logger.info(String.format("%s performed %s", agentName, action.toString()));
		
    	// appraise negative emotion if action failed.
    	if(!result) {
    		this.addEventPerception(agentName, action.toString(), PerceptAnnotation.fromEmotion("disappointment"));
    	}
    	
    	// allow model to see if action resulted in state change
    	this.getModel().noteStateChanges(agentName, action.toString());
		
    	// switch on pause mode if nothing happens
    	pauseOnRepeat(agentName, action);
    	
		return result;
	}
	
	/**
	 * You need to override this method in your subclass in order to relay ASL agent's action requests to the appropriate
	 * method in the {@link plotmas PlotModel}, which will decide if it succeeds and how if affects the storyworld.
	 * 
	 * This methods gets called by {@linkplain #executeAction(String, Structure)}, which in turn is triggered by Jason. 
	 * 
	 * @see plotmas.stories.little_red_hen.FarmEnvironment
	 */
	protected boolean doExecuteAction(String agentName, Structure action) {
		PlotLauncher.runner.pauseExecution();
		logger.severe("SEVERE: doExecuteAction method is not implemented in PlotEnvironment, it's subclass responsibility to implement it");
		logger.severe("Stopping simulation execution...");
		PlotLauncher.runner.finish();
		
		return false;
	}
	
	/**
	 * Saves action intentions for later retrieval in a hashmap.
	 */
	@Override
	public void scheduleAction(String agName, Structure action, Object infraData) {
		Intention intent = ((ActionExec)infraData).getIntention();
		if(!actionIntentionMap.containsKey(agName)) {
			actionIntentionMap.put(agName, new HashMap<>());
		}
		actionIntentionMap.get(agName).put(action, intent);
		super.scheduleAction(agName, action, infraData);
	}
	
	@Override
	protected void stepStarted(int step) {
		logger.info("Step started for environment");
		if (this.model != null)
			// Give model opportunity to check for and execute happenings
			this.model.checkHappenings(step);
		else 
			logger.warning("field model was not set, but a step " + step + " was started");
	}
	
	public void setModel(ModType model) {
		this.model = model;
		updatePercepts();
	}
	
	public ModType getModel() {
		return this.model;
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
		for(String name: model.characters.keySet()) {
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
     * @see plotmas.stories.little_red_hen.FarmEnvironment#updateStatePercepts(java.lang.String)
     */
    protected void updateStatePercepts(String agentName) {
    	// update list of present agents (excluding self)
    	removePerceptsByUnif(agentName, Literal.parseLiteral("agents(X)"));
    	Set<String> presentAgents = new HashSet<>(this.model.characters.keySet());
    	presentAgents.remove(agentName);
    	List<Term> agentList = presentAgents.stream().map(ASSyntax::createAtom).collect(Collectors.toList());
    	addPercept(agentName, ASSyntax.createLiteral("agents", ASSyntax.createList(agentList)));
    	
    	// update inventory state for each agents
    	removePerceptsByUnif(agentName, Literal.parseLiteral("has(X)"));
    	for (String literal : this.model.characters.get(agentName).createInventoryPercepts()) {
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
	 * Adds 'percept' to the list of events that need to be added to agentName's perception list this reasoning step.
	 */	
	public void addEventPerception(String agentName, String percept) {
		List<String> eventList = this.getListCurrentEvents(agentName);
		eventList.add(percept);
		this.currentEventsMap.put(agentName, eventList);
	}
	
	/**
	 * Adds 'percept' to the list of events that need to be added to agentName's perception list this reasoning step.
	 */	
	public void addEventPerception(String agentName, String percept, PerceptAnnotation annot) {
		List<String> eventList = this.getListCurrentEvents(agentName);		
		
		String event = percept + annot.toString();
		
		eventList.add(event);
		this.currentEventsMap.put(agentName, eventList);
	}
	
    
    /********************** Methods for pausing and continuing the environment *****************************/
	/* necessary, because Jason's pause mode sets the GUI waiting, which means no logging output is possible
	 * However, we want to be logging while processing graphs in pause mode, so we reroute logging output to
	 * the console {@see PlotControlsLauncher#pauseExecution}  
	 */
	
    /**
     * Wakes up the environment when Launcher exits pause mode.
     */
    synchronized void wake() {
    	this.notifyAll();
    	logger.info(" Execution continued, switching to Jason GUI output");
    }
	
	/**
	 * Checks if Launcher is in paused state and defers action execution
	 * until its woken up again.
	 */
	synchronized void waitWhilePause() {
        try {
            while (MASConsoleGUI.get().isPause()) {
            	logger.info("Execution paused, switching to logger output");
                wait();
            }
        } catch (Exception e) { }
    }
	
	
    /********************** Methods for pausing the execution after nothing happens **************************
    * checks if all agents executed the same action for the last MAX_REPEATE_NUM of times, if yes, pauses the MAS.
    */
    protected void initializeActionCounting(List<LauncherAgent> agents) {
    	this.agentActionCount = new HashMap<>();
        
        for (LauncherAgent agent : agents) {
        	this.registerAgentForActionCount(agent.name);
        }
        }
    
    private void registerAgentForActionCount(String agName) {
    	// set up a neutral action count for each agent: no action, executed 1 time
    	agentActionCount.put(agName, new Pair<String, Integer>("", 1));
    }
	
	protected void pauseOnRepeat(String agentName, Structure action) {
		Pair<String, Integer> actionCountPair = agentActionCount.get(agentName);
		
		// same action was repeated Launcher.MAX_REPEATE_NUM number of times by all agents:
    	if (allAgentsRepeating()) {
    		// reset counter
    		logger.severe("Auto-paused execution of simulation because all agents repeated their last action for " +
    				String.valueOf(MAX_REPEATE_NUM) + " of times.");
    		PlotLauncher.runner.pauseExecution();
    		resetAllAgentActionCounts();
    		for(EnvironmentListener l : listeners) {
    			l.onPauseRepeat();
    		}
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
