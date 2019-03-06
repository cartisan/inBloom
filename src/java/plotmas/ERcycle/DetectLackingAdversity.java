package plotmas.ERcycle;

import java.util.List;

import org.jfree.data.xy.XYSeries;

import jason.asSemantics.Emotion;
import plotmas.LauncherAgent;
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
		
		boolean negEmotionPresent = this.detectNegativeEmotion(root, er);
		if (!negEmotionPresent) {
			// no adversity found, try introducing an antagonist and insert check for low coupling to ensure it works out
			// make this reversible, so we go back to normal cycle once this was made sure of
			logger.info("Detected lack of adversity");
			if (this.initialNextStage == null) {
				this.initialNextStage = this.nextReflectionState;
			}
			this.nextReflectionState = this.getInstanceFor(DetectLowCoupling.class);
			return new IntroduceAntagonist(this.controller);			
		} else {	
			// there was at least some adversity, check if we can make it stronger
			if (this.detectLowNegativity("protagonist", er)) {
				// TODO: IncreaseAdversityFix -- if adversity created by fix, multiply it. If natural... Try to repeat cause?
			}
			
			// nothing else to do, if we changed next stage to something else, return to initial reasoning cycle cause all is well
			if(this.initialNextStage != null) {
				this.nextReflectionState = this.initialNextStage;
			}
			return null;			
		} 
	}

	private boolean detectLowNegativity(String name, EngageResult er) {
		XYSeries moodData = er.getMoodData().getSeries(name);
		Double minP = moodData.getMinY();
		
		LauncherAgent chara = er.getAgent(name);
		Double defP = chara.personality.defaultMood().getP();
		
		if ((minP < 1.2*defP) & (minP < 0)) {
			return true;
		}
		return false;
	}

	private boolean detectNegativeEmotion(Vertex root, EngageResult er) {
		// Collects all emotions inside root's subgraph into a flat list, checks if any of them are negative
		return er.getPlotGraph().getCharSubgraph(root).stream()
											   .map(v -> v.getEmotions())
											   .flatMap(List::stream)
											   .anyMatch(Emotion.getNegativeEmotions()::contains);
	}
		

}
