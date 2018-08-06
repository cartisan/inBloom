package plotmas.jason;

import java.util.logging.Logger;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import jason.asSemantics.Mood;
import plotmas.PlotLauncher;
import plotmas.graph.PlotGraphController;
import plotmas.graph.Vertex;

/**
 * A type of affective agent that is responsible for maintaining the data that is relevant for plotmas. It decides which
 * agent events need to be logged in the console, displayed in the plot graph and maintains a table of agent's mood 
 * changes for later analysis, as e.g. by the mood graph.
 * @author Leonid Berov
 */
public class PlotAwareAg extends AffectiveAgent {
	static Logger logger = Logger.getLogger(AffectiveAgent.class.getName());

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
		PlotLauncher.runner.getUserModel().mapMood(this.name, newMood);
	}
	
	public void initializeMoodMapper() {
		PlotLauncher.runner.getUserModel().mapMood(this.name, this.getPersonality().defaultMood());
	}
}
