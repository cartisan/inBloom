package little_red_hen.jason;

import jason.JasonException;
import jason.asSemantics.AffectiveAgent;
import jason.asSemantics.Emotion;
import little_red_hen.PlotGraph;
import little_red_hen.graph.Vertex;

public class PlotAwareAg extends AffectiveAgent {

	@Override
    public void addEmotion(Emotion emotion, String type) throws JasonException {
        super.addEmotion(emotion, type);
        
        // add emotion to plot graph
        PlotGraph.getPlotListener().addEvent(ts.getUserAgArch().getAgName(), "+" + emotion.getName(), Vertex.Type.EMOTION);
    }
	
}
