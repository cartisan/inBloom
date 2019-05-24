package inBloom.jason;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.logging.Logger;

import inBloom.PlotLauncher;
import inBloom.graph.PlotGraphController;
import inBloom.graph.Vertex;
import jason.architecture.AgArch;
import jason.asSyntax.Literal;

/**
 * A type of agent architecture that is responsible for maintaining the data that is relevant for inBloom. 
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
                int step = PlotLauncher.runner.getUserEnvironment().getStep();
	    		PlotGraphController.getPlotListener().addEvent(name, p.toString(), Vertex.Type.PERCEPT, step);
	            logger.info(name + " - added perception: " + p.toString());
        	}
        }
        
        return perceptions.orElse(null);
    }
}
