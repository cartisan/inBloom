package little_red_hen.jason;

import java.util.logging.Logger;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;
import little_red_hen.PlotGraph;
import little_red_hen.graph.Vertex;

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
	
}
