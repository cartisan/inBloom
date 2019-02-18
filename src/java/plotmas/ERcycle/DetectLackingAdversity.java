package plotmas.ERcycle;

import jason.asSemantics.Emotion;
import plotmas.ERcycle.PlotCycle.EngageResult;
import plotmas.graph.Vertex;
import plotmas.stories.little_red_hen.RedHenHappeningCycle;

public class DetectLackingAdversity extends ProblemDetectionState {

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
			if (v.getEmotions().stream().anyMatch(Emotion.getNegativeEmotions()::contains))
				// there was at least some adversity
				return null;
		}
		
		// no adversity found
		logger.info("Detected lack of adversity");
		return new IntroduceAntagonist(this.controller);
	}

}
