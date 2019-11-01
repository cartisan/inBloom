package inBloom.jason;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;

import inBloom.PlotLauncher;
import inBloom.graph.Edge;
import inBloom.graph.PlotGraphController;
import inBloom.graph.Vertex;
import inBloom.graph.Vertex.Type;
import inBloom.helper.TermParser;

public class PlotCircumstanceListener implements CircumstanceListener {
	static Logger logger = Logger.getLogger(PlotCircumstanceListener.class.getName());

	static Pattern EMO_PER_PATTERN = Pattern.compile("[+|-]emotion\\(\\w+\\)");

	private PlotAwareAg agent;
	private String name;

	public PlotCircumstanceListener(PlotAwareAg agent, String name) {
		this.agent = agent;
		this.name = name;
	}

	@Override
	public void eventAdded(Event e) {
		// no need to keep +/-emotion(joy) events as percepts, we receive them as Type.EMOTION
		Matcher m = EMO_PER_PATTERN.matcher(TermParser.removeAnnots(e.getTrigger().toString()));
		if(m.matches()) {
			return;
		}

		if(!e.getTrigger().isAchvGoal()) {
			String toAdd = "";
			String source = "";
			// If it has source(self) (i.e. is mental note) and has a parent event (stored by jason on the intention stack)
			// then change the source from self to whatever the source is.
			if(e.getTrigger().toString().endsWith("[source(self)]") && e.getIntention() != null) {
				toAdd = e.getTrigger().toString().split("\\[source\\(self\\)\\]")[0];
				source = String.format("[source(%s)]",
						e.getIntention().peek().getTrigger().getOperator().toString() +							// + or - for belief addition or removal
						TermParser.removeAnnots(e.getIntention().peek().getTrigger().getLiteral().toString()));	// causing event without annotation
				toAdd += source;
			} else {
				toAdd = e.getTrigger().toString();
			}

			PlotGraphController.getPlotListener().addEvent(this.name, toAdd, Vertex.Type.PERCEPT, PlotLauncher.getRunner().getUserEnvironment().getStep());
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
			PlotGraphController.getPlotListener().addEvent(this.name, "drop_intention(" + drop + ")[" + Edge.Type.CAUSALITY.toString() +"(" + TermParser.removeAnnots(cause) + ")]", Type.INTENTION, PlotLauncher.getRunner().getUserEnvironment().getStep());
		}
	}

	@Override
	public void intentionSuspended(Intention i, String reason) { }

	@Override
	public void intentionResumed(Intention i) {	}
}
