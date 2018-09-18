package plotmas.ERcycle;

import plotmas.ERcycle.PlotCycle.EngageResult;
import plotmas.stories.little_red_hen.RedHenHappeningCycle;

public class DetectUnresolvedHappenings extends ProblemDetectionState {
	
	protected DetectUnresolvedHappenings(RedHenHappeningCycle controller) {
		super(controller);
	}

	@Override
	public ProblemFixCommand detect(EngageResult er) {
		// TODO Auto-generated method stub
		
		// no unresolved happenings identified 
		this.controller.setDetectionState(this.getInstanceFor(DetectInsufficientCoupling.class));
		return null;
	}

}
