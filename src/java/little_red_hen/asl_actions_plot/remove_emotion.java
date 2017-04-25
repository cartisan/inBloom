package little_red_hen.asl_actions_plot;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import little_red_hen.PlotGraph;
import little_red_hen.graph.Vertex;

@SuppressWarnings("serial")
public class remove_emotion extends DefaultInternalAction {
	
	@Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
		PlotGraph.getPlotListener().addEvent(ts.getUserAgArch().getAgName(), "-" + args[0].toString(), Vertex.Type.EMOTION);
        return true;
    }

}
