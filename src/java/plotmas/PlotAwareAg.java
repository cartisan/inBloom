package plotmas;

import java.util.logging.Logger;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;
import plotmas.graph.PlotGraph;
import plotmas.graph.Vertex;
import plotmas.helper.MoodMapper;

public class PlotAwareAg extends AffectiveAgent {
	static Logger logger = Logger.getLogger(AffectiveAgent.class.getName());
	public static MoodMapper moodMapper = new MoodMapper();
	
	@Override
    public void addEmotion(Emotion emotion, String type) throws JasonException {
        super.addEmotion(emotion, type);
        
        // add emotion to plot graph
        PlotGraph.getPlotListener().addEvent(ts.getUserAgArch().getAgName(), "+" + emotion.getName(), Vertex.Type.EMOTION);
        logger.info(this.getTS().getUserAgArch().getAgName() + " - appraised emotion: " + emotion.getName());
    }
	
	@Override
	public void updateMoodType(Mood oldMood, Mood newMood) throws JasonException {
		super.updateMoodType(oldMood, newMood);
		logger.info(this.getTS().getUserAgArch().getAgName() + "'s new mood: " + newMood.getType());
	}
	
	@Override
	public void updateMoodValue(Mood newMood) {
		this.mapMood(newMood);
	}
	
	public void initializeMoodMapper() {
		this.mapMood(this.getPersonality().defaultMood());
	}
	
	private void mapMood(Mood mood) {
		String agName = ts.getUserAgArch().getAgName();
		Long plotTime = (System.nanoTime() - PlotEnvironment.startTime) / 1000000; // normalize nano to milli sec

		moodMapper.addMood(agName, plotTime, mood.getP());
//		logger.info("mapping " + this.getTS().getUserAgArch().getAgName() + "'s pleasure value: " + mood.getP() + " at time: " + plotTime.toString());
	}
}
