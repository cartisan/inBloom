package plotmas.storyworld;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import plotmas.PlotModel;

public class ScheduledHappeningDirector implements HappeningDirector, Cloneable{

	private List<Happening<?>> scheduledHappenings;    // contains happenings that are scheduled but were not yet executed this round
	private List<Happening<?>> allHappenings;			// contains happenings that are scheduled (independent of current cycle's state)
	private PlotModel<?> model;
	
	public ScheduledHappeningDirector() {
		this.allHappenings = new LinkedList<>();
		this.scheduledHappenings = new LinkedList<>();
	}

	public List<Happening<?>> getAllHappenings() {
		return this.allHappenings;
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
		this.allHappenings.add(h);		
		this.scheduledHappenings.add(h);		
	}

	@SuppressWarnings("rawtypes")
	public void removeHappening(Happening h) {
		this.allHappenings.remove(h);		
		this.scheduledHappenings.remove(h);		
	}
	
	@Override
	public void setModel(PlotModel<?> model) {
		this.model = model;
	}

	/**
	 * Resets the director after execution of one cycle, so that all happenings that were scheduled for execution
	 * are rescheduled for next run. This is necessary because during cycle execution, happenings that got triggered
	 * are removed from {@link #stillScheduledHappeningsstil}.
	 */
	@SuppressWarnings("unchecked")
	public void reset() {
		this.scheduledHappenings = (LinkedList<Happening<?>>) ((LinkedList<Happening<?>>) this.allHappenings).clone();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ScheduledHappeningDirector clone() {
		ScheduledHappeningDirector clone = new ScheduledHappeningDirector();
		clone.model = this.model;
		clone.allHappenings = (LinkedList<Happening<?>>) ((LinkedList<Happening<?>>) this.allHappenings).clone();
		
		clone.reset();
		return clone;
	}

}
