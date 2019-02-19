package plotmas.ERcycle;

import jason.asSemantics.Emotion;
import plotmas.ERcycle.PlotCycle.EngageResult;
import plotmas.graph.Vertex;
import plotmas.stories.little_red_hen.RedHenHappeningCycle;

public class DetectLackingAdversity extends ProblemDetectionState {

	private ProblemDetectionState initialNextStage;

	protected DetectLackingAdversity(RedHenHappeningCycle controller) {
		super(controller);
	}

	private Vertex getProtagonistRoot(EngageResult er) {
		Vertex root = null;
		for (Vertex v : er.getPlotGraph().getRoots()) {
			if (v.getLabel().equals("protagonist")) {
				root = v;
				break;
			}
			throw new RuntimeException("Agent: 'protegonist' not found in plot graph");
		}
		return root;
	}
	
	@Override
	public ProblemFixCommand performDetect(EngageResult er) {
		Vertex root = getProtagonistRoot(er);
		for (Vertex v : er.getPlotGraph().getCharSubgraph(root)) {
			// check if protagonist ever experienced a negative emotion
			if (v.getEmotions().stream().anyMatch(Emotion.getNegativeEmotions()::contains)) {
				// there was at least some adversity
				// if we changed next stage to something else, return to initial reasoning cycle cause all is well
				if(this.initialNextStage != null) {
					this.nextReflectionState = this.initialNextStage;
				}
				return null;
			}
		}
		
		// no adversity found, try introducing an antagonist and insert check for low coupling to ensure it works out
		// make this reversible, so we go back to normal cycle once this was made sure of
		logger.info("Detected lack of adversity");
		if (this.initialNextStage == null) {
			this.initialNextStage = this.nextReflectionState;
		}
		this.nextReflectionState = this.getInstanceFor(DetectLowCoupling.class);
		return new IntroduceAntagonist(this.controller);
	}

}
