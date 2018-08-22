package plotmas;

import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import plotmas.graph.PlotGraphController;
import plotmas.graph.Vertex;
import plotmas.jason.PlotAwareAg;

public class PlotCircumstanceListener implements CircumstanceListener {

	private PlotAwareAg agent;
	private String name;
	
	public PlotCircumstanceListener(PlotAwareAg agent, String name) {
		this.agent = agent;
		this.name = name;
	}
	
	@Override
	public void eventAdded(Event e) {
		if(!e.getTrigger().isAchvGoal()) {
			String toAdd = "";
			String source = "";
			// If it has source(self) (i.e. is mental note) and has an intention (i.e. a source within the agent's reasoning)
			// then change the source from self to whatever the source is.
			if(e.getTrigger().toString().endsWith("[source(self)]") && e.getIntention() != null) {
				toAdd = e.getTrigger().toString().split("\\[source\\(self\\)\\]")[0];
				source = String.format("[source(%s)]",
						e.getIntention().peek().getTrigger().getLiteral().toString());
				toAdd += source;
			} else {
				toAdd = e.getTrigger().toString();
			}
			PlotGraphController.getPlotListener().addEvent(name, toAdd, Vertex.Type.PERCEPT, PlotLauncher.getRunner().getUserEnvironment().getStep());
		}
	}

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
			PlotGraphController.getPlotListener().addEvent(this.name, "drop_intention(" + drop + ")[cause(" + cause + ")]", PlotLauncher.getRunner().getUserEnvironment().getStep());
		}
	}	

	@Override
	public void intentionSuspended(Intention i, String reason) { }

	@Override
	public void intentionResumed(Intention i) { }

}
