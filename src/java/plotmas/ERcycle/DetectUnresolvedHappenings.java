package plotmas.ERcycle;

import java.util.List;
import java.util.stream.Collectors;

import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import plotmas.PlotLauncher;
import plotmas.ERcycle.PlotCycle.EngageResult;
import plotmas.graph.Edge;
import plotmas.graph.Vertex;
import plotmas.helper.TermParser;
import plotmas.stories.little_red_hen.RedHenHappeningCycle;

public class DetectUnresolvedHappenings extends ProblemDetectionState {
	
	protected DetectUnresolvedHappenings(RedHenHappeningCycle controller) {
		super(controller);
	}

	@Override
	public ProblemFixCommand detect(EngageResult er) {
		// continue with usual reasoning cycle after this
		this.controller.setDetectionState(this.getInstanceFor(DetectInsufficientCoupling.class));
		
		// Identify happenings that were scheduled
		List<String> happenningPercepts = controller.getTransformations().stream()
												.filter(x -> x.getClass().equals(ScheduleHappening.class))
												.map(x -> ((ScheduleHappening) x).getGraphRepresentation()) 
												.collect(Collectors.toList());
		
		// Check if happening appears in plot graph, and if yes motivates a character to intend/do something
		for (Vertex charRoot : er.getPlotGraph().getRoots()) {
			for (Vertex v : er.getPlotGraph().getCharSubgraph(charRoot)) {
				String vPercept = TermParser.removeAnnots(v.getLabel());
				if ( (happenningPercepts.contains(vPercept)) && (!er.getPlotGraph().getOutEdges(v).stream().anyMatch(x -> x.getType().equals(Edge.Type.MOTIVATION)))) {
					// Detected happening with no outgoing motivation edges --> it's unresolved!
					return new AdaptPersonality(vPercept, charRoot.getLabel());
				} else {
					// note that this happening is resolved
					happenningPercepts.remove(vPercept);
				}
			}
		}
		
		if(!happenningPercepts.isEmpty()) {
			logger.warning("Detected unresolved happening: " + happenningPercepts.get(0) + " but found no possible fix");
		}
			
		// all good; no unresolved happenings identified 
		return null;
	}
}
