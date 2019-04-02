package plotmas;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import jason.asSemantics.Mood;
import plotmas.helper.MoodMapper;
import plotmas.stories.little_red_hen.RedHenLauncher;
import plotmas.storyworld.Character;
import plotmas.storyworld.Happening;
import plotmas.storyworld.HappeningDirector;
import plotmas.storyworld.Location;
import plotmas.storyworld.ScheduledHappeningDirector;


/**
 * Responsible for modeling the storyworld. Subclasses should implement methods to handle each action 
 * that is available to ASL agents. Their action requests are relayed to your model by your 
 * {@link plotmas.PlotEnvironment environment subclass}. <br>
 * Your subclass should maintain the current state of all the objects and agents in the story world. This class
 * provides you with domain-independent model functionality. For now this is just a collection of
 * {@link Character agent models}.
 * 
 * 
 * @see plotmas.stories.little_red_hen.FarmModel
 * @author Leonid Berov
 */
/**
 *
 * @author Leonid Berov
 * @param <EnvType>
 */
public abstract class PlotModel<EnvType extends PlotEnvironment<?>> {
	static protected Logger logger = Logger.getLogger(PlotModel.class.getName());
	
	public static final boolean X_AXIS_IS_TIME = true;		// defines whether moods will be mapped based on plotTim or timeStep
															// in latter case, average mood will be calculated over all cycles in a timeStep
	public static final String DEFAULT_LOCATION_NAME = "far far away";
	
	protected HashMap<String, Character> characters = null;
	protected HashMap<String, Location> locations = null;
	public HappeningDirector happeningDirector = null; 
	public EnvType environment = null;
	public MoodMapper moodMapper = null;

	/** Stores values of model-subclass fields, so that after each action we can check if storyworld changed. <br>
	 *  <b>mapping:</b>  fieldName --> old field value */
	private HashMap<String, Object> fieldValueStore;
	
	/** Saves for each character, if one of its actions resulted in a change of the storyworld --> allows causality detection. <br>
	 *  <b>mapping:</b> (characterName, fieldName) --> action*/
	private Table<String, String, String> causalityTable;

	public PlotModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		this.moodMapper = new MoodMapper();
        this.characters = new HashMap<String, Character>();
		this.locations = new HashMap<String, Location>();
		
		// setup default location
		this.addLocation(DEFAULT_LOCATION_NAME);
        
        // add all instantiated agents to world model
        for (LauncherAgent lAgent : agentList) {
        	this.addCharacter(lAgent);
        }

        this.happeningDirector = hapDir;
        hapDir.setModel(this);
        
        //set up a map that tracks the values of all subclass fields, in order to detect change
        this.causalityTable = HashBasedTable.create();
        try {
	        this.fieldValueStore = new HashMap<String, Object>();
	        for (Field f: this.getClass().getDeclaredFields()) {
	        	fieldValueStore.put(f.getName(), f.get(this));
	        }
        } catch (Exception e) {
        	logger.severe("SEVERE: PlotModel is not able to access instance fields to set up tracking for story world state changes");
        	e.printStackTrace();
        }
        	
	}
	
	public void initialize(List<LauncherAgent> agentList) {
		// Important to initialize locations first, so that characters can be initialized in right place
		for (Location loc: this.getLocations()) {
			loc.initialize(this);
		}
        for (LauncherAgent lAgent : agentList) {
        	this.getCharacter(lAgent.name).setModel(this);
        	this.getCharacter(lAgent.name).initialize(lAgent);
        }
        
	}
	
	public Collection<Character> getCharacters() {
		return this.characters.values();
	}
	
	public Character getCharacter(String name) {
		return this.characters.get(name);
	}
	
	/**
	 * Creates a new character in the model.
	 * Used during MAS setup, when the model is created. E.g. see {@linkplain RedHenLauncher#main(String[])}. Characters
	 * created like this are not ready to be used, a call to {@linkplain Character#initialize(LauncherAgent)} is
	 * required, first. This is performed during MAS setuo, too, see: {@linkplain PlotLauncher#initializePlotModel(List)}.
	 * @param lAgent
	 */
	public void addCharacter(LauncherAgent lAgent) {
		// set up connections between agents, model and environment
    	Character character = new Character();
    	character.setModel(this);
		this.characters.put(lAgent.name, character);	
	}

	/**
	 * Creates a new character in the model.
	 * To be used only during MAS execution (after setup/initialize), when a new agent is created. E.g. see 
	 * {@linkplain PlotEnvironment#createAgent(String, String, jason.asSemantics.Personality)}
	 * @param agentName
	 */
	public void addCharacter(String agentName) {
    	Character character = new Character(agentName);
    	character.setModel(this);
		this.characters.put(character.name, character);
		
	}

	public void removeCharacter(String agName) {
		Character character = this.characters.remove(agName);
		character.setModel(null);
	}
	
	public void addLocation(String name) {
		this.locations.put(name, new Location(name));
	}
	
	public void addLocation(Location loc) {
		this.locations.put(loc.name, loc);
	}
	
	public Location getLocation(String name) {
		return this.locations.get(name);
	}
	
	public boolean presentAt(String character,  String location) {
		return this.locations.get(location).present(this.getCharacter(character));
	}
	
	public Collection<Location> getLocations() {
		return this.locations.values();
	}
	
	public boolean goTo(Character agent, Location target) {
		return target.enter(agent);
	}
	
	/**
	 * Called by PlotEnvironment when a new time step is started, before it proceeds with agent action execution.
	 * This method is responsible for checking whether any happenings are eligible for execution, and executes them. 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void checkHappenings(int step) {
		logger.fine("Executing happenings if present");
		List<Happening<?>> happenings = this.happeningDirector.getTriggeredHappenings(step);
		
		for (Happening h : happenings) {
			h.identifyCause(this.causalityTable);
			h.execute(this);
			this.environment.addEventPerception(h.getPatient(), h.getEventPercept());
			
		}
		
    	// update saved storyworld state, but do not enter the happenings as causes for the change,
		// because happenings are not present in agents embedded narrative plot graphs
    	this.noteStateChanges();
	}
	
	/**
	 * Helper method that allows domain-specific subclasses to schedule happenings for execution.
	 * This is only possible if this model was configured to work with a {@linkplain ScheduledHappeningDirector}.
	 * @param  h
	 */
	protected void scheduleHappening(Happening<? extends PlotModel<?>> h) {
		if(this.happeningDirector.getClass().equals(ScheduledHappeningDirector.class)) {
			((ScheduledHappeningDirector) this.happeningDirector).scheduleHappening(h);
		} else {
			logger.warning("Trying to schedule happenings, but wrong happening director enabled: "
						   + happeningDirector.getClass().getSimpleName());
		}
	}
	
	public void mapMood(String name, Mood mood) {
		if (X_AXIS_IS_TIME) {
			// time in ms based mood log
			Long plotTime = PlotEnvironment.getPlotTimeNow();
			moodMapper.addMood(name, plotTime, mood);
			logger.fine("mapping " + name + "'s pleasure value: " + mood.getP() + " at time: " + plotTime.toString());
		} else {
			// time-step based mood log
			Integer timeStep = PlotLauncher.runner.getUserEnvironment().getStep();
			moodMapper.addMood(name, new Long(timeStep), mood);
			logger.fine("mapping " + name + "'s pleasure value: " + mood.getP() + " at time: " + timeStep.toString());
		}
	}
	
	/**
	 * Checks whether the state of the storyworld, represented by the values of all the fields of the model instance, 
	 * changed due to an action executed by causer. This needs to be checked after each agent action.
	 * 
	 * @param causer Name of agent responsible for action (i.e. in whose plot graph cause will be present)
	 * @param action Term representing the action that should be noted as cause for state change
	 */
	public synchronized void noteStateChanges(String causer, String action) {
		try {
	        for (Field f: this.getClass().getDeclaredFields()) {
	        	Object oldV = fieldValueStore.get(f.getName());
	        	Object currentV = f.get(this);
	        	
	        	if((currentV != null) && (!currentV.equals(oldV))) {
	        		// take note that the value of field f changed because of agentName's action
	        		this.causalityTable.put(causer, f.getName(), action);
	        		
	        		// update new field value in our dict
	        		fieldValueStore.put(f.getName(), currentV);
	        		
	        		logger.fine("Storyworld changed due to " + causer + "'s action: " + action + " (property: " + f.getName() + ")");
	        	}
	        }
		} catch (Exception e) {
        	logger.severe("SEVERE: PlotModel is not able to access instance fields to compare story world states");
        	e.printStackTrace();		
		}
	}
	
	/**
	 * Checks whether the state of the storyworld, represented by the values of all the fields of the model instance, 
	 * changed due to a happening. If this is the case it resets any actions that have been noted as causally responsible
	 * for the state.  
	 */
	public synchronized void noteStateChanges() {
		try {
	        for (Field f: this.getClass().getDeclaredFields()) {
	        	Object oldV = fieldValueStore.get(f.getName());
	        	Object currentV = f.get(this);
	        	if((currentV != null) && (!currentV.equals(oldV))) {
	        		
	        		// reset causal connection of this field with any action, cause it was caused by happpening
	        		// TODO: Would it make sense to switch to saving this character-less? Kinda: Objective
	        		for(String agentName : this.characters.keySet()) {
	        			this.causalityTable.remove(agentName, f.getName());
	        		}
	        		
	        		// update new field value in our dict
	        		fieldValueStore.put(f.getName(), currentV);
	        		logger.fine("Storyworld changed due to happening (property: " + f.getName() + ")");
	        	}
	        }
		} catch (Exception e) {
        	logger.severe("SEVERE: PlotModel is not able to access instance fields to compare story world states");
        	e.printStackTrace();		
		}
	}

	public Logger getLogger() {
		return logger;
	}

	public EnvType getEnvironment() {
		return this.environment;
	}
	
	public void setEnvironment(EnvType env) {
		this.environment = env;
	}
	
	public int getStep() {
		return this.environment.getStep();
	}
}
