package plotmas;

import java.util.logging.Logger;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;
import plotmas.graph.MoodGraph;
import plotmas.graph.PlotGraph;
import plotmas.graph.Vertex;

public class PlotAwareAg extends AffectiveAgent {
	static Logger logger = Logger.getLogger(AffectiveAgent.class.getName());
	
	@Override
    public void addEmotion(Emotion emotion, String type) throws JasonException {
        super.addEmotion(emotion, type);
        
        // add emotion to plot graph
        PlotGraph.getPlotListener().addEvent(ts.getUserAgArch().getAgName(), "+" + emotion.getName(), Vertex.Type.EMOTION);
    }
	
	@Override
	public void updateMood(Mood oldMood, Mood newMood) throws JasonException {
		super.updateMood(oldMood, newMood);
		logger.info(this.getTS().getUserAgArch().getAgName() + "'s new mood: " + newMood.getType());
	}
	
	@Override
	public void updateMoodValue(Mood newMood, int cycleNumber) {
		MoodGraph.getMoodListener().addMoodPoint(newMood.getP(),
												 cycleNumber,
												 ts.getUserAgArch().getAgName());
	}
}
