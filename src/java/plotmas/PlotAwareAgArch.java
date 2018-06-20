package plotmas;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.logging.Logger;

import jason.architecture.AgArch;
import jason.asSyntax.Literal;
import plotmas.graph.PlotGraphController;
import plotmas.graph.Vertex;

/**
 * A type of agent architecture that is responsible for maintaining the data that is relevant for plotmas. 
 * It relays perceptions of agents to the plot graph to provide new beliefs. 
 * @author Leonid Berov
 */
public class PlotAwareAgArch extends AgArch {

	static Logger logger = Logger.getLogger(PlotAwareAgArch.class.getName());

	@Override
    public Collection<Literal> perceive() {
        Optional<Collection<Literal>> perceptions = Optional.ofNullable(super.perceive());
        String name = this.getTS().getUserAgArch().getAgName();

        for(Literal p:perceptions.orElse(new LinkedList<Literal>())) {
        	if(!(null == p.getAnnot("emotion"))) {
	    		PlotGraphController.getPlotListener().addEvent(name, p.toString(), Vertex.Type.PERCEPT);
	            logger.info(name + " - added perception: " + p.toString());
        	}
        }
        
        return perceptions.orElse(null);
    }
}
