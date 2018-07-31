package plotmas;

import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import plotmas.graph.PlotGraphController;

public class PlotCircumstanceListener implements CircumstanceListener {

	private PlotAwareAg agent;
	private String name;
	
	public PlotCircumstanceListener(PlotAwareAg agent, String name) {
		this.agent = agent;
		this.name = name;
	}
	
	@Override
	public void eventAdded(Event e) { }

	@Override
	public void intentionAdded(Intention i) { }

	@Override
	public void intentionDropped(Intention i) {
		if(i == null || this.agent.getTS().getC().getSelectedOption() == null) {
			return;
		}
		if(!i.isFinished()) {
			String drop = "" + i.peek().getTrigger();
			String cause = "" + this.agent.getTS().getC().getSelectedOption().getPlan().getTrigger();
			PlotGraphController.getPlotListener().addEvent(this.name, "drop_intention(" + drop + ")[cause(" + cause + ")]");
		}
	}	

	@Override
	public void intentionSuspended(Intention i, String reason) { }

	@Override
	public void intentionResumed(Intention i) { }

}
