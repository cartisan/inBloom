package plotmas.ERcycle;

import plotmas.ERcycle.PlotCycle.EngageResult;
import plotmas.stories.little_red_hen.RedHenHappeningCycle;

public class DetectInsufficientCoupling extends ProblemDetectionState {

	protected DetectInsufficientCoupling(RedHenHappeningCycle controller) {
		super(controller);
	}

	@Override
	public ProblemFixCommand performDetect(EngageResult er) {
		// TODO Auto-generated method stub
		
		// no insufficient coupling identified
		return null;
	}

}
