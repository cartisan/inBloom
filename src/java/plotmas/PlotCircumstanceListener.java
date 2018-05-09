package plotmas;

import java.util.logging.Logger;

import jason.asSemantics.Circumstance;
import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;

public class PlotCircumstanceListener implements CircumstanceListener {

	private String agName;
	private Circumstance C;
	private Logger log;
	
	public PlotCircumstanceListener(String agentName, Circumstance c, Logger log) {
		this.agName = agentName;
		this.C = c;
		this.log = log;
	}

	@Override
	public void eventAdded(Event e) {
		// If we're adding a goal
		if(e.getTrigger().isAddition() && e.getTrigger().isGoal()) {
			if(e.getIntention() == null) {
				log.info(agName + " has null intention goal event: " + e.getTrigger().toString());
			}
		}
	}

	@Override
	public void intentionAdded(Intention i) {
		
	}

	@Override
	public void intentionDropped(Intention i) {
		
	}

	@Override
	public void intentionSuspended(Intention i, String reason) {
		
	}

	@Override
	public void intentionResumed(Intention i) {
		
	}
}
