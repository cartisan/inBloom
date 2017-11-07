package plotmas;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;
import jason.util.Pair;
import plotmas.graph.PlotGraph;
import plotmas.graph.Vertex;

public class PlotAwareAg extends AffectiveAgent {
	static Logger logger = Logger.getLogger(AffectiveAgent.class.getName());
	public static HashMap<String, Double> moodMap = new HashMap<>();
	public static HashMap<String, List<Pair<Long,Double>>> timedMoodMap = new HashMap<>();
	
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
		PlotAwareAg.moodMap.put(ts.getUserAgArch().getAgName(), newMood.getP());
		

		if(! PlotAwareAg.timedMoodMap.containsKey(ts.getUserAgArch().getAgName())) {
			PlotAwareAg.timedMoodMap.put(ts.getUserAgArch().getAgName(), new LinkedList<>());
		}
		List<Pair<Long, Double>> lst = PlotAwareAg.timedMoodMap.get(ts.getUserAgArch().getAgName());
		Long plotTime = Instant.now().toEpochMilli() - PlotEnvironment.startTime.toEpochMilli();
		lst.add(new Pair<>(plotTime, newMood.getP()));
	}
}
