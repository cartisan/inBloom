package inBloom;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import jason.asSemantics.Mood;
import jason.util.Pair;

import inBloom.helper.MoodMapper;
import inBloom.rl_happening.islandWorld.IslandModel;
import inBloom.stories.little_red_hen.RedHenLauncher;
import inBloom.storyworld.Character;
import inBloom.storyworld.Happening;
import inBloom.storyworld.HappeningDirector;
import inBloom.storyworld.Location;
import inBloom.storyworld.ModelState;
import inBloom.storyworld.ScheduledHappeningDirector;


/**
 * Responsible for modeling the storyworld. Subclasses should implement methods to handle each action
 * that is available to ASL agents. Their action requests are relayed to your model by your
 * {@link inBloom.PlotEnvironment environment subclass}. <br>
 * Your subclass should maintain the current state of all the objects and agents in the story world. This class
 * provides you with domain-independent model functionality. For now this is just a collection of
 * {@link Character agent models}.
 *
 *
 * @see inBloom.stories.little_red_hen.FarmModel
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
	
	/** Stores values of model fields (including locations etc.), so that after each action we can check if storyworld changed. <br>
	 *  <b>mapping:</b>  (field, instance of field) --> old field value */
	private Table<Field, Object, Object> fieldValueStore;

	/** Saves for each model-state, if a character's action / a happening resulted in it's change --> allows causality detection. <br>
	 *  <b>mapping:</b> fieldName --> (charName, action/happening) */
	private Map<String, Pair<String, String>> causalityMap;

	public PlotModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
		this.moodMapper = new MoodMapper();
        this.characters = new HashMap<>();
		this.locations = new HashMap<>();

		//set up a map that tracks the values of all subclass fields, in order to detect change
		this.causalityMap = new HashMap<>();
		this.fieldValueStore = HashBasedTable.create();

		this.setUpFieldTracking(this);

		// setup default location
		this.addLocation(DEFAULT_LOCATION_NAME);

        // add all instantiated agents to world model
        for (LauncherAgent lAgent : agentList) {
        	this.addCharacter(lAgent);
        }

        this.happeningDirector = hapDir;
        hapDir.setModel(this);
	}

	public void setUpFieldTracking(Object obj) {
        try {
        	// TODO hier sehen wir, wie wir Annotation @ModelState prüfen
			for (Field f: obj.getClass().getDeclaredFields()) {
				if (!f.isAnnotationPresent(ModelState.class)) {
	        		continue;
	        	}
	        	f.setAccessible(true);
	        	if (null != f.get(obj)) {
	        		this.fieldValueStore.put(f, obj, f.get(obj));
	        	} else {
	        		// For some reason Tables do not support putting null values, so we have to put our custom null.
	        		// This means that setting the f (from null to a value) will be detectable as state change.
	        		this.fieldValueStore.put(f, obj, ModelState.DEFAULT_STATES.NOVAL);
	        	}
	        }
        } catch (Exception e) {
        	logger.severe("SEVERE: PlotModel is not able to access instance field to set up tracking for story world state changes");
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

    	// setting name outside of constructor relevant, otherwise char initialization will occur, which at this point might be impossible
    	character.setName(lAgent.name);
    	this.addCharcter(character);
	}

	/**
	 * Creates a new character in the model.
	 * To be used only during MAS execution (after setup/initialize), when a new agent is created. E.g. see
	 * {@linkplain PlotEnvironment#createAgent(String, String, jason.asSemantics.Personality)}
	 * @param agentName
	 */
	public void addCharacter(String agentName) {
    	Character character = new Character(agentName);
    	this.addCharcter(character);
	}

	private void addCharcter(Character character) {
    	character.setModel(this);
		this.characters.put(character.name, character);
    	this.setUpFieldTracking(character);
	}

	public void removeCharacter(String agName) {
		Character character = this.characters.remove(agName);
		character.setModel(null);
	}

	public void addLocation(String name) {
		Location loc = new Location(name);
		this.addLocation(loc);
	}

	public void addLocation(Location loc) {
    	this.setUpFieldTracking(loc);
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

	/**
	 * Called by PlotEnvironment when a new time step is started, before it proceeds with agent action execution.
	 * This method is responsible for checking whether any happenings are eligible for execution, and executes them.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void checkHappenings(int step) {
		logger.fine("Executing happenings if present");
		List<Happening<?>> happenings = this.happeningDirector.getTriggeredHappenings(step);

		for (Happening h : happenings) {
			h.identifyCause(this.causalityMap);
			h.execute(this);
			this.environment.addEventPercept(h.getPatient(), h.getEventPercept());

			// update saved storyworld state, so that the happening h is entered as causes for the change
			this.noteStateChanges(h);
		}
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
						   + this.happeningDirector.getClass().getSimpleName());
		}
	}

	public void mapMood(String name, Mood mood) {
		if (X_AXIS_IS_TIME) {
			// time in ms based mood log
			Long plotTime = PlotEnvironment.getPlotTimeNow();
			this.moodMapper.addMood(name, plotTime, mood);
			logger.fine("mapping " + name + "'s pleasure value: " + mood.getP() + " at time: " + plotTime.toString());
		} else {
			// time-step based mood log
			Integer timeStep = PlotLauncher.runner.getUserEnvironment().getStep();
			this.moodMapper.addMood(name, new Long(timeStep), mood);
			logger.fine("mapping " + name + "'s pleasure value: " + mood.getP() + " at time: " + timeStep.toString());
		}
	}

	/**
	 * Checks whether the state of the storyworld, represented by the values of all the fields of the model instance
	 * (and it's components) that are tracked due to an {@linkplain ModelState} annotation, changed due to an action
	 * executed by causer. This needs to be checked after each agent action.
	 *
	 * @param causer Name of agent responsible for action (i.e. in whose plot graph cause will be present)
	 * @param action Term representing the action that should be noted as cause for state change
	 */
	public synchronized void noteStateChanges(String causer, String action) {
		try {
			for (Cell<Field, Object, Object> cell : this.fieldValueStore.cellSet()) {
	        	Object oldV = cell.getValue();
	        	Object currentV = cell.getRowKey().get(cell.getColumnKey());

	        	if(currentV != null && !currentV.equals(oldV)) {
	        		// take note that the value of field f changed because of agentName's action
	        		this.causalityMap.put(cell.getRowKey().getName(), new Pair<>(causer, action));

	        		// update new field value in our dict
	        		// TODO: Does this change the set we iterate over?!
	        		this.fieldValueStore.put(cell.getRowKey(), cell.getColumnKey(), currentV);

	        		logger.fine("Storyworld changed due to " + causer + "'s action: " + action + " (property: " + cell.getRowKey().getName() + ")");
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
	public synchronized void noteStateChanges(Happening<?> hap) {
		try {
			for (Cell<Field, Object, Object> cell : this.fieldValueStore.cellSet()) {
	        	Object oldV = cell.getValue();
	        	Object currentV = cell.getRowKey().get(cell.getColumnKey());
	        	if(currentV != null && !currentV.equals(oldV)) {
	        		// take note that the value of field f changed because of this happening (as perceived by its patient)
	        		this.causalityMap.put(cell.getRowKey().getName(), new Pair<>(hap.patient, hap.percept));

	        		// update new field value in our dict
	        		this.fieldValueStore.put(cell.getRowKey(), cell.getColumnKey(), currentV);
	        		logger.fine("Storyworld changed due to happening (property: " + cell.getRowKey().getName() + ")");
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

	public int getStateValue() {

		/* STRING HASHVALUES
		 * 
		 * The hashValues of the same String, initialised in two different runs, will return
		 * the same hashValues
		 * 
		 * The hashValues of the same Object, initialised in two different runs however, will
		 * not return the same hashValues -> see CurrentModelState.createCharacter
		 */
		int stateValue = 0;

		
		/*
		 * 1. MODEL SPECIFIC FIELDS
		 */
		Field[] fields = this.getClass().getDeclaredFields();
		for(Field field: fields) {

			/*
			 * The HashMap's hashCodes change when the value inside (f.e. hunger) is changed
			 */
			if(field.isAnnotationPresent(ModelState.class)) {
				try {
					int fieldCode = field.getName().toString().hashCode();
					stateValue += fieldCode;
					stateValue += (fieldCode * field.get(this).toString().hashCode());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}

		}
		
		
		/*
		 * 2. CHARACTER SPECIFIC VALUES
		 */
		for(Character character: characters.values()) {
			
			/* MULTIPLY WITH CHARACTER CODE
			 * 
			 * To create different, unique values for the Locations/Moods, when two characters might
			 * be in the same Location / Mood and therefore would have the add the same values here
			 * without multiplication
			 */
			int characterCode = character.toString().hashCode();
			
			stateValue += characterCode; 													// what if multiple characters have the same name?
			stateValue += (characterCode * character.location.toString().hashCode());  		// -""-
			stateValue += (characterCode * character.getMood().getFullName().hashCode());
			
			Object[] items = character.inventory.toArray();
			for(Object item: items) {
				stateValue += (characterCode * item.toString().hashCode());
			}

		}
		
		return stateValue;
	}

	public HashMap<String, Object> getDetailedState() {
				
		HashMap<String, Object> detailedValues = new HashMap<String, Object>();

		/* STRING HASHVALUES
		 * 
		 * The hashValues of the same String, initialised in two different runs, will return
		 * the same hashValues
		 * 
		 * The hashValues of the same Object, initialised in two different runs however, will
		 * not return the same hashValues -> see CurrentModelState.createCharacter*/

		for(Character character: characters.values()) {
			
			//TODO hierüber
			character.getMood().getFullName().hashCode();

			//detailedValues.put("Character", character.toString().hashCode()); // what if multiple characters have the same name?
			//detailedValues.put("Location", character.location.toString().hashCode()); // -""-
			
			Object[] items = character.inventory.toArray();
			for(Object item: items) {
				//detailedValues.put("Inventory item", item.toString().hashCode());
			}
			
			character.getMood().getFullName().hashCode();
			
			/**Map<Long, List<Mood>> myMoods = this.moodMapper.getMoodByAgent(character.name);
			Collection<List<Mood>> moodCollection = myMoods.values();*/
			
			/**Table<String, Long, List<Mood>> allMyMoods = this.moodMapper.getTimedMoodMap();
			Map<Long, List<Mood>> myMoods = allMyMoods.row(character.name);
			Collection<List<Mood>> moodCollection = myMoods.values();
			
			// für jeden Long (TimeSomething?) gibt es eine eigene Mood
			for(List<Mood> currentMood: moodCollection) {
				//System.out.println("List<Mood> currentMood:     " + currentMood);
				//System.out.println("List<Mood> currentMood hash:" + currentMood.toString().hashCode());
				//detailedValues.put("curM Name", currentMood);
				//detailedValues.put("curM hash", currentMood.toString().hashCode());
				for(Mood mood: currentMood) {
					//System.out.println("Mood mood:                   " + mood);
					//System.out.println("Mood mood hash:              " + mood.toString().hashCode());
					detailedValues.put("mood Name", mood.getFullName());
					detailedValues.put("mood hash", mood.getFullName().hashCode());
					
					
					// multiply with character name instead
					detailedValues.put("Mood", (mood.getFullName().hashCode()*character.name.hashCode()));
					
					//detailedValues.put("Mood", (mood.hashCode()*currentMood.hashCode()));
				}
			}*/

		}

		Field[] fields = this.getClass().getDeclaredFields();
		for(Field field: fields) {
			//TODO
			//nur mit annotation @ModelState
			
			// es gibt auch private field in this class fieldValueStore, wo alle Fields drin sind
			// Character, Location und alle mit @ModelState
			
			// yes, the hashMap's hashCodes change when the value inside (f.e. hunger) is changed
			try {
				//detailedValues.put("FName", field.getName().toString().hashCode());
				//detailedValues.put("FValue", field.get(this).toString().hashCode());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}

		}
		
		return detailedValues;
	}

	public void printField(Field field) {
		try {
			System.out.println(field.getName() + ": " + field.get(this));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

	}
}
