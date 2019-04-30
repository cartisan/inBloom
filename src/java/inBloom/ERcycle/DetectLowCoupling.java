package inBloom.ERcycle;

import inBloom.ERcycle.PlotCycle.EngageResult;
import inBloom.graph.Edge;
import inBloom.stories.little_red_hen.RedHenHappeningCycle;

public class DetectLowCoupling extends ProblemDetectionState {

	private static final double COUPLING_THRESHOLD = 0.01;   // so far, only arbitrarily assigned threshold

	protected DetectLowCoupling(RedHenHappeningCycle controller) {
		super(controller);
		this.nextReflectionState = this.getInstanceFor(DetectLackingAdversity.class);
	}

	@Override
	protected ProblemFixCommand performDetect(EngageResult er) {
		if (this.controller.charCount > 1) {
			// count number of inter-character edges as proxy for character coupling
			double speechNum = (double) er.getPlotGraph().getEdges().stream().filter(e -> e.getType() == Edge.Type.COMMUNICATION)
												 			      			 .count();
			double totalSemanticEdges = (double) er.getPlotGraph().getEdges().stream().filter(e -> e.getType() != Edge.Type.ROOT)
																		  			  .filter(e -> e.getType() != Edge.Type.TEMPORAL)
																		  			  .filter(e -> e.getType() != Edge.Type.CAUSALITY)
																		  			  .count();
			// TODO: take into account shared events, when these are implemented
			if (speechNum / totalSemanticEdges > COUPLING_THRESHOLD) {
				// coupling is high enough
				logger.info("Coupling of " + speechNum / totalSemanticEdges + " measured, no need for improvement.");
				return null;
			}
			
			// other chars present & coupling is indeed low, fix it 
			logger.info("Coupling of " + speechNum / totalSemanticEdges + " measured, need to increase this.");
			return new MakeExtraverted(er);
		} else {
			// There is only one agent present, no coupling possible. We need to introduce a new agent, first
			this.nextReflectionState = this.getInstanceFor(DetectLowCoupling.class);
			return new IntroduceAntagonist(this.controller);
		}
		
	}

}
