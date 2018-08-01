package plotmas;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;
import jason.asSemantics.Personality;
import plotmas.helper.MoodMapper;
import plotmas.storyworld.Happening;
import plotmas.storyworld.HappeningDirector;
import plotmas.storyworld.ScheduledHappeningDirector;
import plotmas.storyworld.StoryworldAgent;


/**
 * Responsible for modeling the storyworld. Subclasses should implement methods to handle each action 
 * that is available to ASL agents. Their action requests are relayed to your model by your 
 * {@link plotmas.PlotEnvironment environment subclass}. <br>
 * Your subclass should maintain the current state of all the objects and agents in the story world. This class
 * provides you with domain-independent model functionality. For now this is just a collection of
 * {@link StoryworldAgent agent models}.
 * 
 * 
 * @see plotmas.stories.little_red_hen.FarmModel
 * @author Leonid Berov
 */
public abstract class PlotModel<EnvType extends PlotEnvironment<?>> {
	static protected Logger logger = Logger.getLogger(PlotModel.class.getName());
	
	public static MoodMapper moodMapper = new MoodMapper();
	public static final boolean X_AXIS_IS_TIME = false;		// defines whether moods will be mapped based on plotTim or timeStep
															// in latter case, average mood will be calculated over all cycles in a timeStep
	
	public HashMap<String, StoryworldAgent> agents;
	public HappeningDirector happeningDirector; 
	public EnvType environment = null;

	
	public static String addEmotion(String... ems) {
    	String result = "[";
    	for(String em: ems) {
    		if (Emotion.getAllEmotions().contains(em)) {
    			result += Emotion.ANNOTATION_FUNCTOR + "(" + em + "),";
    		}
    		else{
    			logger.warning("Error: Trying to add an invalid emotion to a percept: " + em);
    			throw new RuntimeException("Trying to add an invalid emotion to a percept: " + em);
    		}
    	}
    	
    	// remove comma after last emotion
    	result = result.substring(0, result.length() - 1);
    	result += "]";
    	
    	return result;
    }
	
	public static String addTargetedEmotion(String em, String target) {
    	String result = "[";
		
    	if (Emotion.getAllEmotions().contains(em)) {
			result += Emotion.ANNOTATION_FUNCTOR + "(" + em + "," + target + ")";
		}
		else {
			logger.warning("Error: Trying to add an invalid emotion to a percept: " + em);
			throw new RuntimeException("Trying to add an invalid emotion to a percept: " + em);
		}

		result += "]";
    	
    	return result;
    }
	
	public PlotModel(List<LauncherAgent> agentList, HappeningDirector hapDir) {
        this.agents = new HashMap<String, StoryworldAgent>();
        
        // add all instantiated agents to world model
        for (LauncherAgent lAgent : agentList) {
        	this.addAgent(lAgent);
        }

        this.happeningDirector = hapDir;
        hapDir.setModel(this);
	}
	
	public StoryworldAgent getAgent(String name) {
		return this.agents.get(name);
	}
	
	public void addAgent(LauncherAgent lAgent) {
		// set up connections between agents, model and environment
    	StoryworldAgent sAgent = new StoryworldAgent(lAgent);
    	this.addAgent(sAgent);		
	}

	public void addAgent(String agentName, Personality pers) {
    	StoryworldAgent sAgent = new StoryworldAgent();
    	sAgent.setPersonality(pers);
    	sAgent.name = agentName;
    
    	this.addAgent(sAgent);		
		
	}

	private void addAgent(StoryworldAgent sAgent) {
		agents.put(sAgent.name, sAgent);
    	sAgent.setModel(this);
	}
	
	public void removeAgent(String agName) {
		this.agents.remove(agName);
	}
	
	/**
	 * Called by PlotEnvironment when a new time step is started, before it proceeds with agent action execution.
	 * This method is responsible for checking whether any happenings are eligible for execution, and executes them. 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void stepStarted(int step) {
		List<Happening<?>> happenings = this.happeningDirector.getTriggeredHappenings(step);
		
		for (Happening h : happenings) {
			h.execute(this);
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

	public Logger getLogger() {
		return logger;
	}

	public EnvType getEnvironment() {
		return this.environment;
	}
	
	public void setEnvironment(EnvType env) {
		this.environment = env;
	}
}
