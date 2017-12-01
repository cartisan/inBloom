package plotmas;

import java.util.List;
import java.util.logging.Logger;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;
import jason.asSemantics.Option;
import jason.asSyntax.Pred;
import plotmas.graph.PlotGraph;
import plotmas.graph.Vertex;
import plotmas.helper.MoodMapper;

public class PlotAwareAg extends AffectiveAgent {
	static Logger logger = Logger.getLogger(AffectiveAgent.class.getName());
	public static MoodMapper moodMapper = new MoodMapper();
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
        PlotGraph.getPlotListener().addEvent(this.name, "+" + emotion.getName(), Vertex.Type.EMOTION);
        logger.info(this.name + " - appraised emotion: " + emotion.getName());
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
	
	@Override
    public Option selectOption(List<Option> options) {
		Option opt = super.selectOption(options);
		if(!(opt == null) & (!(null == opt.getPlan().getLabel()))) {
			Pred label = opt.getPlan().getLabel();
			if(!(label.getAnnots("isIntention").isEmpty())) {
				PlotGraph.getPlotListener().addEvent(this.name, label.getFunctor(), Vertex.Type.EVENT);
				logger.info(this.name + " added plan: " + label.getFunctor());
			}
		}
		
		return opt;
	}
	
	public void initializeMoodMapper() {
		this.mapMood(this.getPersonality().defaultMood());
	}
	
	private void mapMood(Mood mood) {
		Long plotTime = (System.nanoTime() - PlotEnvironment.startTime) / 1000000; // normalize nano to milli sec

		moodMapper.addMood(this.name, plotTime, mood.getP());
		logger.fine("mapping " + this.name + "'s pleasure value: " + mood.getP() + " at time: " + plotTime.toString());
	}
}
