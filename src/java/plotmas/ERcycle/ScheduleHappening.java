package plotmas.ERcycle;

import jason.asSyntax.Trigger;
import plotmas.ERcycle.PlotCycle.EngageResult;
import plotmas.stories.little_red_hen.FarmModel;
import plotmas.stories.little_red_hen.FindCornHappening;
import plotmas.storyworld.Happening;
import plotmas.storyworld.ScheduledHappeningDirector;

/**
 * Detects a fitting happening to resolve a narrative equilibrium state and schedules it for execution right at
 * the beginning of the equilibrium state.
 * @author Leonid Berov
 */
public class ScheduleHappening implements ProblemFixCommand {

	private Happening<?> happening;
	private int startStep;
	
	public ScheduleHappening(int startStep, String character) {
		// TODO: Select fitting happening from a model-based catalog
		this.happening = new FindCornHappening(
								(FarmModel model) -> model.getStep() >= startStep,
								character
							);
		this.startStep = startStep;
	}
	
	@Override
	public void execute(EngageResult er) {
		((ScheduledHappeningDirector) er.getLastModel().happeningDirector).scheduleHappening(this.happening);
	}

	@Override
	public void undo(EngageResult er) {
		((ScheduledHappeningDirector) er.getLastModel().happeningDirector).removeHappening(this.happening);
	}
	
	@Override
	public String message() {
		return "Scheduling happening " + this.happening.getClass().getSimpleName() + " at step " + this.startStep;
	}
	
	/**
	 * Determines the label of the vertex that this happening will cause in a characters subgraph, if it is successfully
	 * perceived. Does not return annotations. Can be used to detect this happening in a plot graph using e.g.
	 * <code> scheduledHappening.getGraphRepresentation().equals(TermParser.removeAnnots(v.getLabel())); </code>
	 * @return Label with belief-added operator and without annotations
	 */
	public String getGraphRepresentation() {
		return Trigger.TEOperator.add.toString() + this.happening.getPercept();
	}

}
