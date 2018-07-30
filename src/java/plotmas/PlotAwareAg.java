package plotmas;

import java.util.logging.Logger;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;
import plotmas.graph.PlotGraphController;
import plotmas.graph.Vertex;
import plotmas.helper.MoodMapper;

/**
 * A type of affective agent that is responsible for maintaining the data that is relevant for plotmas. It decides which
 * agent events need to be logged in the console, displayed in the plot graph and maintains a table of agent's mood 
 * changes for later analysis, as e.g. by the mood graph.
 * @author Leonid Berov
 */
public class PlotAwareAg extends AffectiveAgent {
	static Logger logger = Logger.getLogger(AffectiveAgent.class.getName());
	public static MoodMapper moodMapper = new MoodMapper();
	public static final boolean X_AXIS_IS_TIME = false;		// defines whether moods will be mapped based on plotTim or timeStep
															// in latter case, average mood will be calculated over all cycles in a timeStep
	private String name;
	
    @Override
    public void initAg() {
        super.initAg();
        this.name = this.getTS().getUserAgArch().getAgName();
    }
        
	@Override
    public void addEmotion(Emotion emotion, String type) throws JasonException {
        super.addEmotion(emotion, type);
        
        // add emotion to plot graph
        int step = PlotLauncher.runner.getUserEnvironment().getStep();
        PlotGraphController.getPlotListener().addEvent(this.name, emotion.toLiteral().toString(), Vertex.Type.EMOTION, step);
        logger.info(this.name + " - appraised emotion: " + emotion.toLiteral().toString());
    }
	
	@Override
	public void updateMoodType(Mood oldMood, Mood newMood) throws JasonException {
		super.updateMoodType(oldMood, newMood);
		logger.info(this.name + "'s new mood: " + newMood.getType());
	}
	
	@Override
	public void updateMoodValue(Mood newMood) {
		this.mapMood(newMood);
	}
	
	public void initializeMoodMapper() {
		this.mapMood(this.getPersonality().defaultMood());
	}
	
	private void mapMood(Mood mood) {
		Long plotTime = PlotEnvironment.getPlotTimeNow();
		Integer timeStep = PlotLauncher.runner.getUserEnvironment().getStep();
		
		if (X_AXIS_IS_TIME) {
			// time in ms based mood log
			moodMapper.addMood(this.name, plotTime, mood);
			logger.fine("mapping " + this.name + "'s pleasure value: " + mood.getP() + " at time: " + plotTime.toString());
		} else {		
			// time-step based mood log
			moodMapper.addMood(this.name, new Long(timeStep), mood);
			logger.fine("mapping " + this.name + "'s pleasure value: " + mood.getP() + " at time: " + timeStep.toString());
		}
	}
}
