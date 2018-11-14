package plotmas.storyworld;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import plotmas.PlotModel;

public class ScheduledHappeningDirector implements HappeningDirector, Cloneable{

	private List<Happening<?>> stillScheduledHappenings;    // contains happenings that are scheduled but were not yet executed this round
	private List<Happening<?>> happeningsSchedule;			// contains happenings that are scheduled (independent of current cycle's state)
	private PlotModel<?> model;
	
	public ScheduledHappeningDirector() {
		this.happeningsSchedule = new LinkedList<>();
		this.stillScheduledHappenings = new LinkedList<>();
	}

	/**
	 * @see plotmas.storyworld.HappeningDirector#getTriggeredHappenings(int)
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Happening<?>> getTriggeredHappenings(int step) {
		List<Happening<?>> triggeredHapps = new LinkedList<>();
		
		for (Iterator<Happening<?>> iterator = this.stillScheduledHappenings.iterator(); iterator.hasNext();) {
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
		this.happeningsSchedule.add(h);		
		this.stillScheduledHappenings.add(h);		
	}

	@SuppressWarnings("rawtypes")
	public void removeHappening(Happening h) {
		this.happeningsSchedule.remove(h);		
		this.stillScheduledHappenings.remove(h);		
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
		this.stillScheduledHappenings = (LinkedList<Happening<?>>) ((LinkedList<Happening<?>>) this.happeningsSchedule).clone();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ScheduledHappeningDirector clone() {
		ScheduledHappeningDirector clone = new ScheduledHappeningDirector();
		clone.model = this.model;
		clone.happeningsSchedule = (LinkedList<Happening<?>>) ((LinkedList<Happening<?>>) this.happeningsSchedule).clone();
		
		clone.reset();
		return clone;
	}

}
