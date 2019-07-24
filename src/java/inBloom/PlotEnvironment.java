package inBloom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.asSemantics.ActionExec;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import jason.asSemantics.Intention;
import jason.asSemantics.Personality;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;
import jason.environment.TimeSteppedEnvironment;
import jason.infra.centralised.CentralisedEnvironment;
import jason.runtime.MASConsoleGUI;
import jason.runtime.RuntimeServicesInfraTier;

import inBloom.graph.Edge;
import inBloom.graph.PlotGraphController;
import inBloom.graph.Vertex;
import inBloom.graph.Vertex.Type;
import inBloom.helper.EnvironmentListener;
import inBloom.helper.PerceptAnnotation;
import inBloom.helper.PlotpatternAnalyzer;
import inBloom.helper.TermParser;
import inBloom.jason.PlotAwareAg;
import inBloom.jason.PlotAwareCentralisedAgArch;
import inBloom.jason.PlotAwareCentralisedRuntimeServices;
import inBloom.storyworld.Character;
import inBloom.storyworld.Location;

/**
 *  Responsible for relaying action requests from ASL agents to the {@link inBloom.PlotModel Storyworld} and
 *  perceptions from the Storyworld to ASL agents (via {@link jason.asSemantics.AffectiveAgent jason's AffectiveAgent}).
 *  Each action is reported to the {@link inBloom.graph.PlotGraphController PlotGraphController} for visual representation. <br>
 *  Subclasses need to override {@link #executeAction(String, Structure)} to implement their domain-specific relaying
 *  and should make sure to execute {@code super.executeAction(agentName, action);}, which will take care of plotting.
 *
 *  <p> The environment is set up to pause a simulation if all agents repeated the same action for {@link #MAX_REPEATE_NUM}
 *  times.
 *
 * @see inBloom.stories.little_red_hen.FarmEnvironment
 * @author Leonid Berov
 */
public abstract class PlotEnvironment<ModType extends PlotModel<?>> extends TimeSteppedEnvironment {
	static Logger logger = Logger.getLogger(PlotEnvironment.class.getName());

	/** number of times all agents need to repeat an action sequence before system is paused; -1 to switch off */
	public static final Integer MAX_REPEATE_NUM = 5;
	/** number of environment steps, before system automatically pauses; -1 to switch off */
	public static Integer MAX_STEP_NUM = -1;
	/** time in ms that {@link TimeSteppedEnvironment} affords agents to propose an action, before each step times out */
	static final String STEP_TIMEOUT = "100";

    /** Safes the time the plot has started to compute plot time, i.e. temporal duration of the plot so far. */
    private static Long startTime = 0L;
    /** Aggregates the durations of all pauses, so that plot time can disregard time spent paused. */
    private static Long pauseDuration = 0L;

    /**
     * Returns the current plot time in ms, i.e. the time that has passed since simulation was started
     * @return time in ms (Long)
     */
    public static Long getPlotTimeNow() {
    	if (PlotEnvironment.startTime > 0) {
    		return (System.nanoTime() - PlotEnvironment.pauseDuration - PlotEnvironment.startTime) / 1000000; // normalize nano to milli sec
    	}
    	return 0L;
    }

    /**
     * Updates {@link #pauseDuration} with the duration of a new pause.
     * @param duration
     */
    public static void notePause(Long duration) {
    	pauseDuration += duration;
    }

    /**
     * A list of environment listeners which get called on certain events
     * in the environment.
     */
    private List<EnvironmentListener> listeners = new LinkedList<>();

    protected ModType model;

    /**
     * Stores a mapping from agentName to a (String actionName, Integer count) tuple, which stores how many
     * consecutive times the agent has been executing the action 'actionName'.
     * This is used to pause simulation execution if all agents just repeat their actions for a while.
     */
    protected HashMap<String, List<String>> agentActions;  // agentName -> [action1, action2, ...]
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
     * Counts plot steps, synchronously to {@link TimeSteppedEnvironment#step} but designating the step of the
     * first plotted intention as step 1. Should be preferred as step counter for all inBloom purposes.
     */
    protected int step = 0;

    private boolean initialized = false;

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
    	if (args.length > 0) {
			logger.warning("Initilization arguments provided but usage unclear, ignoring. Args: " + Arrays.toString(args));
		}

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
    	this.initializeActionCounting(agents);
    	this.initialized = true;

    	// create initial state perceptions
		List<Term> locList = this.model.locations.keySet().stream().map(ASSyntax::createAtom).collect(Collectors.toList());
		Literal locListLit = ASSyntax.createLiteral("locations", ASSyntax.createList(locList));

		List<Term> agList = agents.stream().map(ag -> ag.name).map(ASSyntax::createAtom).collect(Collectors.toList());
		Literal agListLit = ASSyntax.createLiteral("agents", ASSyntax.createList(agList));


		for (LauncherAgent agent : agents) {
			// inform agents about available location and other agents
			this.addPercept(agent.name, locListLit);
			this.addPercept(agent.name, agListLit);
		}
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
    	// add attempted action to plot graph
		String motivation = "[" + Edge.Type.MOTIVATION.toString() + "(%s)]";
		Intention intent = this.actionIntentionMap.get(agentName).get(action);
		if(intent != null) {
			motivation = String.format(motivation, TermParser.removeAnnots(intent.peek().getTrigger().getTerm(1).toString()));
		} else {
			motivation = "";
		}
		this.actionIntentionMap.get(agentName).remove(action);

		PlotGraphController.getPlotListener().addEvent(agentName, action.toString() + motivation, Type.ACTION, this.getStep());

    	// let the domain specific subclass handle the actual action execution
    	// ATTENTION: this is were domain-specific action handling code goes
		ActionReport actionReport = this.doExecuteAction(agentName, action);

		// Create percepts of action-results for all present agents
    	List<Character> presentChars = this.getModel().getCharacter(agentName).location.getCharacters();

    	for (Character currentChar : presentChars) {
    		// Add annotation to this action perception that allow plot graph post processing to insert cross-character edges
    		actionReport.getAnnotation(currentChar.name).addCrossCharAnnotation(action.toString(), actionReport.eventTime);

    		if(currentChar.getName().equals(agentName)) {
    			// reporting results for acting agent
    			// TODO: insert "self" into terms on position 0? Or rather do this ASL side?
    			if (!actionReport.success) {
    				// if action failed, acting character feels disappointment
    				actionReport.getAnnotation(currentChar.name).addAnnotation(Emotion.ANNOTATION_FUNCTOR, "disappointment");
    			}
    			this.addEventPercept(currentChar.name, action.toString(), actionReport.getAnnotation(currentChar.name));
    		} else {
    			// reporting results for other present agents --> insert acting agent as first term
    			int arity = action.getArity();
    			Term[] terms = new Term[arity+1];
    			terms[0] = ASSyntax.createAtom(agentName);

    			//shift prior terms from position i to i+1, because agent name is inserted in 0
    			if (arity > 0 ) {
	    			for(int i = 0; i < arity; ++i) {
	    				terms[i+1] = action.getTerm(i);
	    			}
    			}

    			Structure percept = ASSyntax.createStructure(action.getFunctor(), terms);
    			this.addEventPercept(currentChar.name, percept.toString(), actionReport.getAnnotation(currentChar.name));
    		}
    	}

		logger.info(String.format("%s performed %s", agentName, action.toString()));

    	// allow model to see if action resulted in state change
    	this.getModel().noteStateChanges(agentName, action.toString());

    	// make provisions for pause mode
    	this.updateActionCount(agentName, action);

		return actionReport.success;
	}

	/**
	 * You need to override this method in your subclass in order to relay ASL agent's action requests to the appropriate
	 * method in the {@link inBloom PlotModel}, which will decide if it succeeds and how if affects the storyworld.
	 *
	 * This methods gets called by {@linkplain #executeAction(String, Structure)}, which in turn is triggered by Jason.
	 *
	 * @see inBloom.stories.little_red_hen.FarmEnvironment
	 */
	protected ActionReport doExecuteAction(String agentName, Structure action) {
		PlotLauncher.runner.pauseExecution();
		logger.severe("SEVERE: doExecuteAction method is not implemented in PlotEnvironment, it's subclass responsibility to implement it");
		logger.severe("Stopping simulation execution...");
		PlotLauncher.runner.finish();

		return null;
	}

	/**
	 * Saves action intentions for later retrieval in a hashmap.
	 */
	@Override
	public void scheduleAction(String agName, Structure action, Object infraData) {
		Intention intent = ((ActionExec)infraData).getIntention();

		this.actionIntentionMap.putIfAbsent(agName, new HashMap<>());

		this.actionIntentionMap.get(agName).put(action, intent);
		super.scheduleAction(agName, action, infraData);
	}

	/**
	 * Creates a new agent and registers it in all the necessary places. Starts the agent after registration.
	 * @param name Name of the agent that will be created
	 * @param aslFile ASL file name that contains the agents reasoning code, should be located in src/asl
	 * @param personality an instance of {@linkplain jason.asSemantics.Personality} that will affect the agents behavior
	 */
	public void createAgent(String name, String aslFile, Personality personality) {
		String agName = null;

        try {
        	logger.info("Creating new agent: " + name);

        	// enables plot graph to track new agent's actions
        	PlotGraphController.getPlotListener().addCharacter(name);

        	// create Agent
        	agName = this.getRuntimeServices().createAgent(name, aslFile, PlotAwareAg.class.getName(), null, null, null, null);

        	// set the agents personality
        	AffectiveAgent ag = ((PlotLauncher<?,?>) PlotLauncher.getRunner()).getPlotAgent(agName);
        	ag.initializePersonality(personality);
	    } catch (Exception e) {
	    	e.printStackTrace();
        }

        // enable action counting for new agent, so it is accounted for in auto-pause feature
        this.registerAgentForActionCount(agName);

        // start new agent's reasoning cycle
        this.getRuntimeServices().startAgent(agName);

        // creates a model representation for the new agent
        this.getModel().addCharacter(agName);
	}

	/**
	 * Stops and removes agent agName from the simulation, the model and all accounting facilities.
	 * @param agName The name of the agent to be removed
	 * @param byAgName The name of the agent responsible for removing agName, or null if a happening is responsible
	 */
	public void killAgent(String agName, String byAgName) {
		// stop agent
		this.getRuntimeServices().killAgent(agName, byAgName);

		// make sure action counting for pause does not take removed agent into account
		this.agentActions.remove(agName);

		// indicate removal in plot graph
		PlotGraphController.getPlotListener().addEvent(agName, "died", Vertex.Type.EVENT, this.getStep());

		// remove character from story-world model
		this.model.removeCharacter(agName);
	}

	/**
	 * Stops and removes agent agName from the simulation, the model and all accounting facilities.
	 * @param agName The name of the agent to be removed
	 */
	public void killAgent(String agName) {
		this.killAgent(agName, null);
	}

    /**
     * Adopted getRuntimeServices method; to be used instead of {@linkplain CentralisedEnvironment#getRuntimeServices()}
     * which was available through {@code this.getEnvironmentInfraTier().getRuntimeServices()}.
     * @return Returns a CentralisedRuntimeServices subclass which operates on the inBloom specific
     * {@linkplain PlotAwareCentralisedAgArch} class.
     */
    private RuntimeServicesInfraTier getRuntimeServices() {
        return new PlotAwareCentralisedRuntimeServices(PlotLauncher.getRunner());
    }

	@Override
	protected synchronized void stepStarted(int step) {
		if (this.step > 0) {
			this.step++;

			if(!PlotLauncher.getRunner().isDebug()) {
				logger.info("Step " + this.step + " started for environment");
			}

			if (this.model != null) {
				// Give model opportunity to check for and execute happenings
				this.model.checkHappenings(this.step);
			} else {
				logger.warning("field model was not set, but a step " + this.step + " was started");
			}
		} else {
			// ignore mood data before environment step 1 started
			if (this.model != null) {
				this.getModel().moodMapper.startTimes.add(getPlotTimeNow());
			}
		}

	}

	@Override
	protected void stepFinished(int step, long elapsedTime, boolean byTimeout) {
		// if environment is initialized && agents are done setting up && one of the agents didn't choose an action
		if (this.model != null && byTimeout && step > 5 ) {
			for (Character chara : this.model.getCharacters()) {
				Object action = this.getActionInSchedule(chara.getName());
				if(action == null) {
					this.agentActions.get(chara.getName()).add("--");		// mark inaction by --
				}
			}
		}
		// check if pause mode is enabled, wait with execution while it is
		this.waitWhilePause();
	}


	public void setModel(ModType model) {
		this.model = model;
	}

	public ModType getModel() {
		return this.model;
	}

    @Override
    public int getStep() {
    	return this.step;
    }

    public void setStep(int step) {
    	this.step = step;
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
		this.updateEventPercepts(agName);
		return super.getPercepts(agName);
	}

    /**
     * Updates percepts that are related to events that occurred in the mean-time.
     * Events relate to unique percepts that are delivered to the agent only once, and removed in the next reasoning cycle.
     */
	protected void updateEventPercepts(String agentName) {
		this.deleteOldUniqueEvents(agentName);
		this.addNewUniqueEvents(agentName);
	}

	/**
	 * Adds percepts of events that happened during last cycle to agent's perception list.
	 */
	private void addNewUniqueEvents(String agentName) {
    	List<String> eventList = this.getListCurrentEvents(agentName);
		synchronized(eventList) {											// make sure to block writing, while iterating here
	    	for(String event : eventList) {
	    		try {
	    			Literal percept = ASSyntax.parseLiteral(event);
					this.addPercept(agentName, percept);

					//get list of events to be removed next cycle
					List<Literal> remList = this.getListRemEvents(agentName);

					// add percept to this list and put new list back into storing map
					remList.add(percept);
					this.perceivedEventsMap.put(agentName, remList);

				} catch (ParseException e) {
					logger.severe("Couldn't parse event: " + event.toString() + " in PlotEnvironment#addNewUniqueEvents.");
					logger.severe(e.getMessage());
				}
	    	}
		}
    	this.currentEventsMap.remove(agentName);
	}

    /**
     * Removes percepts of events that happened second-last cycle from agent's perception list.
     */
	private void deleteOldUniqueEvents(String agentName) {
    	// 1. get list of unique events for agent agentName, scheduled for deletion this cycle
    	List<Literal> eventList = this.getListRemEvents(agentName);

    	// 2. delete respective percepts from agent agentName
    	for(Literal event : eventList) {
			this.removePercept(agentName, event);
    	}
    	// clean up Map for this step to free up space
    	this.perceivedEventsMap.remove(agentName);
	}

	/**
	 * Returns the list of events that need to be added to agents perception list this reasoning step.
	 */
	protected List<String> getListCurrentEvents(String agentName) {
		// the lists need to be synchronized, so that agents adding events and environment reading events do not lead to concurrency conditions
		return this.currentEventsMap.getOrDefault(agentName, Collections.synchronizedList(new ArrayList<String>()));
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
	public void addEventPercept(String agentName, String percept) {
		List<String> eventList = this.getListCurrentEvents(agentName);
		eventList.add(percept);
		this.currentEventsMap.put(agentName, eventList);
	}

	/**
	 * Adds 'percept' to the list of events that need to be added to agentName's perception list this reasoning step.
	 */
	public void addEventPercept(String agentName, String percept, PerceptAnnotation annot) {
		List<String> eventList = this.getListCurrentEvents(agentName);

		String event = percept + annot.toString();

		eventList.add(event);
		this.currentEventsMap.put(agentName, eventList);
	}

	/**
	 * Adds a perception for all agents. Avoid using this, as normally perceptions should be contained to the location
	 * at which the state is relevant. Agents at other locations should not perceive a state change.
	 * Use {@link #addPercept(Location, Literal...) } instead.
	 * @see jason.environment.Environment#addPercept(jason.asSyntax.Literal[])
	 */
	@Deprecated
	@Override
    public void addPercept(Literal... perceptions) {
    	super.addPercept(perceptions);
    }

    /**
     * Adds a state perception (i.e. a perception that is permanent and not removed after one reasoning cycle, see
     * {@link #addEventPercept(String, String)}) for all agents present at loc.
     * @param loc location where state change is perceivable
     * @param perceptions state perceptions
     */
    public void addPercept(Location loc, Literal... perceptions) {
    	loc.getCharacters().forEach(chara -> super.addPercept(chara.name, perceptions));
    }

	/**
	 * Removes all percepts from the common perception list that unifies with <i>per</i>.
	 * Avoid using this, as normally perceptions should be contained to the location at which the state is relevant.
	 * Agents at other locations should not perceive a state change.
	 * @see jason.environment.Environment#emovePerceptsByUnif(Literal per)
	 */
	@Deprecated
	@Override
    public int removePerceptsByUnif(Literal per) {
    	return super.removePerceptsByUnif(per);
    }

    /**
     * Removes all percepts that unify with <i>per</i> for the agents present at loc.
     * Example: removePerceptsByUnif(locA, Literal.parseLiteral("position(_)")) will remove
     * all percepts that unify "position(_)".
     * @param loc location where state change is perceivable
     * @param per percept literal to be removed
     * @return the number of removed percepts.
     */
    public int removePerceptsByUnif(Location loc, Literal per) {
    	int sum = 0;
    	for (Character chara : loc.getCharacters()) {
    		sum += super.removePerceptsByUnif(chara.name, per);
		}

    	return sum;
    }

    /**
     * Removes a perception from the common perception list.
     * Avoid using this, as normally perceptions should be contained to the location at which the state is relevant.
	 * Agents at other locations should not perceive a state change.
	 * @see jason.environment.Environment#emovePercept(Literal per)
     */
	@Deprecated
	@Override
    public boolean removePercept(Literal per) {
    	return super.removePercept(per);
    }

    /**
     * Removes a perception from the perception list of all agents present at loc.
     * @param loc location where state change is perceivable
     * @param per percept literal to be removed
     * @return success of removal for all agents
     */
    public boolean removePercept(Location loc, Literal per) {
    	boolean res = true;
    	for (Character chara : loc.getCharacters()) {
    		res = res & super.removePercept(chara.getName(), per);
    	}
    	return res;
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
		this.checkPause();
        try {
            while (MASConsoleGUI.get().isPause()) {
            	logger.info("Execution paused, switching to console output");
                this.wait();
            }
        } catch (Exception e) { }
    }


    /********************** Methods for pausing the execution after nothing happens **************************
    * checks if all agents executed the same action for the last MAX_REPEATE_NUM of times, if yes, pauses the MAS.
    */
    protected void initializeActionCounting(List<LauncherAgent> agents) {
    	this.agentActions = new HashMap<>();
        for (LauncherAgent agent : agents) {
        	this.registerAgentForActionCount(agent.name);
        }
    }

    private void registerAgentForActionCount(String agName) {
    	this.agentActions.put(agName, new LinkedList<String>());
    }

	protected void checkPause() {
		if (this.initialized & !PlotLauncher.getRunner().isDebug()) {
			// same action was repeated Launcher.MAX_REPEATE_NUM number of times by all agents:
	    	if (this.narrativeExquilibrium()) {
	    		// reset counter
	    		logger.info("Auto-paused execution of simulation, because all agents repeated the same action sequence " +
	    				String.valueOf(MAX_REPEATE_NUM) + " # of times.");
	    		this.resetAllAgentActionCounts();
	    		PlotLauncher.runner.pauseExecution();
	    		for(EnvironmentListener l : this.listeners) {
	    			l.onPauseRepeat();
	    		}
	    	}
	    	if (MAX_STEP_NUM > -1 && this.getStep() % MAX_STEP_NUM == 0) {
	    		logger.info("Auto-paused execution of simulation, because system ran for MAX_STEP_NUM steps.");

	    		PlotLauncher.runner.pauseExecution();
	    		for(EnvironmentListener l : this.listeners) {
	    			l.onPauseRepeat();
	    		}
	    	}
		}
	}

	/**
	 * @param agentName
	 * @param action
	 */
	private void updateActionCount(String agentName, Structure action) {
		this.agentActions.get(agentName).add(action.toString());
	}

    /**
     * Determines whether the simulation has reached a narrative equilibrium state, and should be paused.
     * This is the case, when all agents have repeated the same action sequence {@link #MAX_REPEATE_NUM} number of
     * times.
     * @return
     */
    protected boolean narrativeExquilibrium() {
    	if (MAX_REPEATE_NUM < 0) {
    		return false;
    	}

    	HashMap<String,Boolean> agentsRepeating = new HashMap<>();
    	for (String agent : this.agentActions.keySet()) {
    		agentsRepeating.put(agent, false);

    		List<String> actions = this.agentActions.get(agent);
    		HashMap<String,Integer> patRepeats = PlotpatternAnalyzer.countTrailingPatterns(actions);
    		logger.fine(agent + "'s action patterns: " + patRepeats.toString());

    		if (patRepeats.values().stream().mapToInt(x -> x).max().orElse(0) >= MAX_REPEATE_NUM) {
    			agentsRepeating.put(agent, true);
    		}
    	}

    	// test if all agents are set to true
    	if (agentsRepeating.values().stream().allMatch(bool -> bool)) {
    		return true;
    	}
    	return false;
    }

    public void resetAllAgentActionCounts() {
    	for (List<String> actions : this.agentActions.values()) {
			actions.clear();
    	}
    }
}
