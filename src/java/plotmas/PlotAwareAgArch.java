package plotmas;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.logging.Logger;

import jason.architecture.AgArch;
import jason.asSemantics.Message;
import jason.asSyntax.Literal;
import plotmas.graph.PlotGraph;
import plotmas.graph.Vertex;

/**
 * A type of affective agent architecture that is responsible for maintaining the data that is relevant for plotmas. 
 * It relays the speech acts of the agents to the plot graph to provide inter-character edges. 
 * @author Leonid Berov
 */
public class PlotAwareAgArch extends AgArch {

	static Logger logger = Logger.getLogger(PlotAwareAgArch.class.getName());
	
	@Override
    public void sendMsg(Message m) throws Exception {
        // actually send the message
		super.sendMsg(m);
		
		// plot it in the graph
		PlotGraph.getPlotListener().addRequest(m);
	}
	
	@Override
    public Collection<Literal> perceive() {
        Optional<Collection<Literal>> perceptions = Optional.ofNullable(super.perceive());
        String name = this.getTS().getUserAgArch().getAgName();

        for(Literal p:perceptions.orElse(new LinkedList<Literal>())) {
        	if(!(null == p.getAnnot("emotion"))) {
	    		PlotGraph.getPlotListener().addEvent(name, p.toString(), Vertex.Type.PERCEPT);
	            logger.info(name + " - added perception: " + p.toString());
        	}
        }
        
        return perceptions.orElse(null);
    }
}
