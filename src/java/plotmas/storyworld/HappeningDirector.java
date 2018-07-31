package plotmas.storyworld;

import java.util.LinkedList;
import java.util.List;

import plotmas.PlotEnvironment;
import plotmas.PlotModel;

public class HappeningDirector{
	private List<Happening<?>> scheduledHappenings;
	private PlotModel<?> model;
	
	public HappeningDirector(PlotModel<?> model) {
		this.model = model;
		this.scheduledHappenings = new LinkedList<>();
	}

	/**
	 * Checks whether current state of model activates happenings and returns them.
	 * @param step time step provided by {@linkplain PlotEnvironment}
	 * @return a list containing happenings to be executed (or empty list)
	 */
	public List<Happening<?>> getTriggeredHappenings(int step) {
		List<Happening<?>> triggeredHapps = new LinkedList<>();
		for (Happening h : this.scheduledHappenings) {
			if (h.triggered(this.model, step)) {
				triggeredHapps.add(h);
			}
		}

		return triggeredHapps;
	}

	public void scheduleHappening(Happening h) {
		this.scheduledHappenings.add(h);		
	}
}
