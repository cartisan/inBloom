package plotmas.ERcycle;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jason.util.Pair;
import plotmas.ERcycle.PlotCycle.EngageResult;
import plotmas.graph.Edge;
import plotmas.graph.Vertex;
import plotmas.helper.TermParser;
import plotmas.stories.little_red_hen.RedHenHappeningCycle;

public class DetectUnresolvedHappenings extends ProblemDetectionState {
	protected static Logger logger = Logger.getLogger(ProblemDetectionState.class.getName());
	
	protected DetectUnresolvedHappenings(RedHenHappeningCycle controller) {
		super(controller);
		this.nextReflectionState = this.getInstanceFor(DetectInsufficientCoupling.class);
	}

	@Override
	public ProblemFixCommand performDetect(EngageResult er) {
		// Identify happenings that were scheduled
		List<String> happenningPercepts = controller.getTransformations().stream()
												.filter(x -> x.getClass().equals(ScheduleHappening.class))
												.map(x -> ((ScheduleHappening) x).getGraphRepresentation()) 
												.collect(Collectors.toList());
		
		List<Pair<String, String>> unresolveds = new LinkedList<>();
		// Check if happening appears in plot graph, and if yes motivates a character to intend/do something
		for (Vertex charRoot : er.getPlotGraph().getRoots()) {
			for (Vertex v : er.getPlotGraph().getCharSubgraph(charRoot)) {
				String vPercept = TermParser.removeAnnots(v.getLabel());
				if ( (happenningPercepts.contains(vPercept)) && (!er.getPlotGraph().getOutEdges(v).stream().anyMatch(x -> x.getType().equals(Edge.Type.MOTIVATION)))) {
					// Detected happening with no outgoing motivation edges --> it's unresolved!
					unresolveds.add(new Pair<String, String>(vPercept, charRoot.getLabel()));
				} else if (happenningPercepts.contains(vPercept)){
					// note that this happening is resolved
					logger.info("Detected that happening: " + vPercept + " is resolved");
				}
			}
		}
		
		if (unresolveds.isEmpty()) {
			// No unresolved happenings found at all, this step can be removed from reasoning cycle again
			this.nextReflectionState = this.getInstanceFor(DetectInsufficientCoupling.class);
			return null;
		}
			
		logger.info("Found unresolved happenings: " + unresolveds.toString());

		// First try all possible fixes for one happening, only if that fails try to resolve the following ones
		for (Pair<String, String> hapCharPair : unresolveds) {
			AdaptPersonality fix = AdaptPersonality.getNextFixFor(hapCharPair.getFirst(), hapCharPair.getSecond());
			
			if (fix != null) {
				logger.info("Scheduling " + fix.message() + " to resolve: " + hapCharPair.getFirst());
				this.nextReflectionState = this.getInstanceFor(DetectUnresolvedHappenings.class);
				return fix;
			}
			logger.info("Can't find way to resolve: " + hapCharPair.getFirst());
		}

		// can't resolve any of the happenings :(
		return null;
	}
}
