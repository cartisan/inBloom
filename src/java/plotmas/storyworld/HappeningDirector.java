package plotmas.storyworld;

import java.util.List;

import plotmas.PlotEnvironment;
import plotmas.PlotModel;

public interface HappeningDirector {

	/**
	 * Checks whether current state of model activates happenings and returns them.
	 * @param step time step provided by {@linkplain PlotEnvironment}
	 * @return a list containing happenings to be executed (or empty list)
	 */
	public List<Happening<?>> getTriggeredHappenings(int step);
	public void setModel(PlotModel<?> model);
	public HappeningDirector clone();
}