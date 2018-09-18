package plotmas.ERcycle;

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
	public String toString() {
		return "Scheduling happening " + this.happening.getClass().getSimpleName() + " at step " + this.startStep;
	}

}
