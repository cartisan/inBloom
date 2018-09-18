package plotmas.storyworld;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import plotmas.PlotModel;

public class ScheduledHappeningDirector implements HappeningDirector{

	private List<Happening<?>> scheduledHappenings;
	private PlotModel<?> model;
	
	public ScheduledHappeningDirector() {
		this.scheduledHappenings = new LinkedList<>();
	}

	/**
	 * @see plotmas.storyworld.HappeningDirector#getTriggeredHappenings(int)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Happening<?>> getTriggeredHappenings(int step) {
		List<Happening<?>> triggeredHapps = new LinkedList<>();
		
		for (Iterator<Happening<?>> iterator = this.scheduledHappenings.iterator(); iterator.hasNext();) {
		    Happening h = iterator.next();
			if (h.triggered(this.model)) {
				triggeredHapps.add(h);
		        iterator.remove();
		    }
		}
		return triggeredHapps;
	}

	@SuppressWarnings({ "rawtypes" })
	public void scheduleHappening(Happening h) {
		this.scheduledHappenings.add(h);		
	}

	@SuppressWarnings("rawtypes")
	public void removeHappening(Happening h) {
		this.scheduledHappenings.remove(h);		
	}
	
	@Override
	public void setModel(PlotModel<?> model) {
		this.model = model;
	}

}
