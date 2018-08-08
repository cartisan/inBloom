package plotmas.storyworld;

import java.util.LinkedList;
import java.util.List;

import plotmas.PlotModel;

public class ScheduledHappeningDirector implements HappeningDirector{

	private List<Happening<?>> scheduledHappenings;
	private PlotModel<?> model;
	
	public ScheduledHappeningDirector() {
		this.scheduledHappenings = new LinkedList<>();
	}

	/* (non-Javadoc)
	 * @see plotmas.storyworld.HappeningDirector#getTriggeredHappenings(int)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	// TODO: happenings are to fire only once!
	public List<Happening<?>> getTriggeredHappenings(int step) {
		List<Happening<?>> triggeredHapps = new LinkedList<>();
		for (Happening h : this.scheduledHappenings) {
			if (h.triggered(this.model)) {
				triggeredHapps.add(h);
			}
		}

		return triggeredHapps;
	}

	@SuppressWarnings({ "rawtypes" })
	public void scheduleHappening(Happening h) {
		this.scheduledHappenings.add(h);		
	}

	@Override
	public void setModel(PlotModel<?> model) {
		this.model = model;
	}

}
